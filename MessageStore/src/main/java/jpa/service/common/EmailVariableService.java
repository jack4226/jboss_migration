package jpa.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.EmailVariable;
import jpa.repository.EmailVariableRepository;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailVariableService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class EmailVariableService implements java.io.Serializable {
	private static final long serialVersionUID = 2189513886746930320L;

	static Logger logger = Logger.getLogger(EmailVariableService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	EmailVariableRepository repository;

	public EmailVariable getByVariableName(String variableName) {
		return repository.findOneByVariableName(variableName);
	}
	
	public Optional<EmailVariable> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<EmailVariable> getAll() {
		return repository.findAllByOrderByRowId();
	}
	
	public List<EmailVariable> getAllBuiltinVariables() {
		return getAllVariablesBy(true);
	}
	
	public List<EmailVariable> getAllCustomVariables() {
		return getAllVariablesBy(false);
	}
	
	private List<EmailVariable> getAllVariablesBy(boolean isBuiltin) {
		return repository.findAllByIsBuiltinOrderByRowId(isBuiltin);
	}
	
	public String getByQuery(String queryStr, int addrId) {
		if (JpaUtil.isDerbyDatabase()) {
			// Derby, replace CONCAT/CONCAT_WS functions with concatenate operators
			Pattern p = Pattern.compile("^(\\w{1,20} )((CONCAT|CONCAT_WS)\\(.*\\))(.*)$",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			Matcher m = p.matcher(queryStr);
			if (m.find() && m.groupCount() >= 4) {
				String concat = "";
				for (int i = 0; i <= m.groupCount(); i++) {
					if (i == 2) {
						concat = StringUtils.removeStartIgnoreCase(m.group(i), "CONCAT");
						if (StringUtils.startsWith(concat, "_WS")) {
							concat = convertToCONCAT(concat);
						}
					}
				}
				String separator = findSeparator(concat);
				
				String[] items = concat.split("[(),\\|\\']");
				List<String> coalesces = new ArrayList<String>();
				for (String item : items) {
					if (StringUtils.length(StringUtils.trim(item)) > 2) {
						coalesces.add("coalesce(" + item +", '')");
					}
				}
				concat = "(";
				for (int i=0; i<coalesces.size(); i++) {
					concat += coalesces.get(i);
					if (i<(coalesces.size()-1)) {
						concat += " || " + "'" + separator + "'" + " || ";
					}
				}
				concat += ")";

				queryStr = m.group(1) + concat + m.group(4);
			}
		}
		Query query = em.createNativeQuery(queryStr);
		query.setParameter(1, addrId);
		try {
			String result = (String) query.getSingleResult();
			return result;
		}
		catch (NoResultException e) {
			return null;
		}
		finally {
		}
	}
	
	private String convertToCONCAT(String concat_ws) {
		String concat_tmp = StringUtils.removeStartIgnoreCase(concat_ws, "_WS");
		String separator = findSeparator(concat_tmp);
		String[] items = concat_tmp.split("[(),\\'\\|]");
		List<String> names = new ArrayList<String>();
		for (int i=0; i<items.length; i++) {
			String item = items[i];
			//System.out.println("item: " + item);
			if (StringUtils.isNotBlank(item) && !StringUtils.equals(item.trim(), separator)) {
				names.add(item);
			}
		}
		String concat = "(";
		for (int i=0; i<names.size(); i++) {
			concat += names.get(i);
			if (i<(names.size()-1)) {
				concat += ", '" + separator + "', ";
			}
		}
		concat += ")";
		return concat;
	}
	
	private String findSeparator(String str) {
		Pattern p = Pattern.compile(".*\\'([\\p{Punct}])\\'.*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		System.out.println("String to match: " + str);
		Matcher m = p.matcher(str);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
			}
			return m.group(1);
		}
		return " ";
	}

	
	public void delete(EmailVariable variable) {
		if (variable==null) return;
		repository.delete(variable);
	}

	public int deleteByVariableName(String variableName) {
		return repository.deleteByVariableName(variableName);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(EmailVariable variable) {
		if (variable==null) return;
		repository.saveAndFlush(variable);
	}
	
	public void update(EmailVariable variable) {
		if (variable==null) return;
		repository.saveAndFlush(variable);
	}
}
