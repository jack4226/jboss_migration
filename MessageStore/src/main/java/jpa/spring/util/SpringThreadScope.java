package jpa.spring.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * A simple thread-bound scope implementation using ThreadLocal for spring
 * framework.
 */
public class SpringThreadScope implements Scope {

	private final ThreadLocal<?> threadScopeMap = new ThreadLocal<Object>() {
		protected Map<Object,Object> initialValue() {
			return new HashMap<Object,Object>();
		}
	};

	@Override
	public Object get(String beanName, ObjectFactory<?> objectFactory) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> scope = (Map<Object, Object>) threadScopeMap.get();
		Object beanObject = scope.get(beanName);
		if (beanObject == null) {
			// create a new bean object
			beanObject = objectFactory.getObject();
			// save it to the map
			scope.put(beanName, beanObject);
		}
		return beanObject;
	}

	public Object remove(String beanName) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> scope = (Map<Object, Object>) threadScopeMap.get();
		// remove the bean from the map
		return scope.remove(beanName);
	}

	public void registerDestructionCallback(String beanName, Runnable callback) {
	}

	public String getConversationId() {
		return null;
	}

	public Object resolveContextualObject(String arg0) {
		return null;
	}
}
