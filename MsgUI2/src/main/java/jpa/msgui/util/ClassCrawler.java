package jpa.msgui.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class ClassCrawler {
	static final Logger logger = Logger.getLogger(ClassCrawler.class);
	
    public static boolean crawlRecursively(Field field, Set<Class<?>> alreadyCrawled, Map<Field, Set<String>> badFields) {
        if (alreadyCrawled.contains(field.getType())) {
            return !badFields.keySet().contains(field);
        }

        alreadyCrawled.add(field.getType());

        if (Modifier.isStatic(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())
                || field.getType().isPrimitive()) {
            return true;
        } 
        else if (Serializable.class.isAssignableFrom(field.getType())) {
            boolean allGood = true;

            for (Field f : field.getType().getDeclaredFields()) {
                boolean isGood = crawlRecursively(f, alreadyCrawled, badFields);
                if (!isGood) {
                    if (!badFields.containsKey(field)) {
                        badFields.put(field, new HashSet<>());
                    }
                    badFields.get(field).add(f.getType().getSimpleName() + " " + f.getName());
                    allGood = false;
                }
            }

            return allGood;
        } else {
            if (!badFields.containsKey(field)) {
                badFields.put(field, new HashSet<>());
            }

            return false;
        }
    }

    public static Map<Field, Set<String>> initiateCrawling(Collection<Class<?>> roots) {
        Map<Field, Set<String>> badFields = new HashMap<>();

        for (Class<?> root : roots) {
            for (Field f : root.getDeclaredFields()) {
                crawlRecursively(f, new HashSet<>(), badFields);
            }
        }

        return badFields;
    }

    public static Map<Field, Set<String>> initiateCrawling(Class<?> root) {
    	LinkedList<Class<?>> roots = new LinkedList<>();
    	roots.add(root);
    	return initiateCrawling(roots);
    }
    
    
    public static void main(String[] args) {
        LinkedList<Class<?>> roots = new LinkedList<>();
        //roots.add(javax.swing.JComponent.class); // ADD YOUR CLASSES HERE.
        roots.add(jpa.msgui.bean.EmailAddressBean.class);
        Map<Field, Set<String>> badFields = initiateCrawling(roots);

        if (badFields.keySet().size() == 0) {
            logger.info("All fields are serializable (not having checked the given class(es) themselves).");
        } else {
            logger.info("The following fields are not serializable in the class tree(s) given by " + roots + ":");
        }

        for (Field field : badFields.keySet()) {
            logger.info("<UnSer> "
                    + field.getType().getSimpleName() + " " 
                    + field.getName() + " (" 
                    + field.getDeclaringClass().getName() + ") => " + badFields.get(field));
        }
    }
}
