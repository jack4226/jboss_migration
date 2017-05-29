package jpa.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import jpa.constant.RuleCriteria;
import jpa.msgui.vo.PagingVo;
import jpa.util.JpaUtil;

public class RepositoryUtils {
	
	static class PathTuple<T> {
		Path<T> path;
		Object value;
	}
	
	public static <T> List<Predicate> buildPredicatesList(PagingVo vo, PagingVo.Type type, CriteriaBuilder builder, From<?, ?> root) {
		List<Predicate> predicates = new ArrayList<>();
		List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(type);
		for (PagingVo.Column col : colList) {
			if (vo.getSearchValue(col) == null) {
				continue;
			}
			PagingVo.Criteria criteria  = vo.getSearchCriteria(col);
			predicates.addAll(buildPredicateList(col, criteria, builder, root));
		}
		
		return predicates;
	}
	
	
	/**
	 * XXX Should only be called when running with MySQL or PostgreSQL under Hibernate
	 */
	static <T> void buildMySQLRegexp(CriteriaBuilder builder, From<?, ?> root, List<Predicate> predicates, String colName, String colValue, PagingVo.MatchBy matchBy) {
		if (PagingVo.MatchBy.AllWords.equals(matchBy)) {
			List<Predicate> pList = new ArrayList<Predicate>();
			String[] items = (colValue + "").split(" ");
			for (String item : items) {
				if (StringUtils.isNotBlank(item)) {
					if (JpaUtil.isMySQLDatabase()) {
						Expression<String> regexp = builder.function("regexp", String.class, builder.lower(root.<String>get(colName)), builder.literal(item.toLowerCase()));
						pList.add(builder.equal(regexp, "1"));
					}
					else if (JpaUtil.isPgSQLDatabase()) {
						Expression<String> regexp = builder.function("regexp", String.class, root.<String>get(colName), builder.literal(item));
						pList.add(builder.equal(regexp, Boolean.TRUE));
					}
				}
			}
			predicates.add(builder.and(pList.toArray(new Predicate[] {})));
		}
		else {
			String regex = (colValue + "").replaceAll("[ ]+", ".+");
			if (PagingVo.MatchBy.AnyWords.equals(matchBy)) {
				regex = (colValue + "").replaceAll("[ ]+", "|");
			}
			else if (PagingVo.MatchBy.BeginWith.equals(matchBy)) {
				regex = "^" + regex;
			}
			else if (PagingVo.MatchBy.EndWith.equals(matchBy)) {
				regex = regex + "$";
			}
			if (JpaUtil.isMySQLDatabase()) {
				Expression<String> regexp = builder.function("regexp", String.class, builder.lower(root.<String>get(colName)), builder.literal(regex.toLowerCase()));
				predicates.add(builder.equal(regexp, "1"));
			}
			else if (JpaUtil.isPgSQLDatabase()) {
				Expression<String> regexp = builder.function("regexp", String.class, root.<String>get(colName), builder.literal(regex));
				predicates.add(builder.equal(regexp, Boolean.TRUE));
			}
		}
	}

	static <T> void buildSQLLike(CriteriaBuilder builder, From<?, ?> root, List<Predicate> predicates, String colName, String colValue, PagingVo.MatchBy matchBy) {
		if (PagingVo.MatchBy.AllWords.equals(matchBy) || PagingVo.MatchBy.AnyWords.equals(matchBy)) {
			List<Predicate> pList = new ArrayList<Predicate>();
			String[] items = (colValue + "").split(" ");
			for (String item : items) {
				if (StringUtils.isNotBlank(item)) {
					pList.add(builder.like(builder.lower(root.get(colName)), "%" + StringUtils.lowerCase(item) + "%"));
				}
			}
			if (PagingVo.MatchBy.AllWords.equals(matchBy)) {
				predicates.add(builder.and(pList.toArray(new Predicate[] {})));
			}
			else if (PagingVo.MatchBy.AnyWords.equals(matchBy)) {
				predicates.add(builder.or(pList.toArray(new Predicate[] {})));
			}
		}
		else if (PagingVo.MatchBy.BeginWith.equals(matchBy)) {
			predicates.add(builder.like(builder.lower(root.get(colName)), StringUtils.lowerCase(colValue) + "%"));
		}
		else if (PagingVo.MatchBy.EndWith.equals(matchBy)) {
			predicates.add(builder.like(builder.lower(root.get(colName)), "%" + StringUtils.lowerCase(colValue)));
		}
		else {
			predicates.add(builder.like(builder.lower(root.get(colName)), "%" + StringUtils.lowerCase(colValue) + "%"));
		}
	}

	public static <T> List<Predicate> buildPredicateList(PagingVo.Column column, PagingVo.Criteria criteria, CriteriaBuilder builder, From<?, ?> root) {
		List<Predicate> predicates = new ArrayList<>();
		PathTuple<T> tuple = buildPath(column, criteria, root);
		
		if (RuleCriteria.EQUALS.equals(criteria.getOperation())) {
			predicates.add(builder.equal(tuple.path, tuple.value));
		}
		else if (RuleCriteria.NOT_EQUALS.equals(criteria.getOperation())) {
			predicates.add(builder.notEqual(tuple.path, tuple.value));
		}
		else if (RuleCriteria.IS_BLANK.equals(criteria.getOperation())) {
			predicates.add(builder.isNull(tuple.path));
		}
		else if (RuleCriteria.IS_NOT_BLANK.equals(criteria.getOperation())) {
			predicates.add(builder.isNotNull(tuple.path));
		}
		else if (RuleCriteria.GREATER_THAN.equals(criteria.getOperation())) {
			String value = criteria.getValue() == null ? null : criteria.getValue().toString();
			predicates.add(builder.greaterThan(root.<String>get(column.name()), value));
		}
		else if (RuleCriteria.GE.equals(criteria.getOperation())) {
			String value = criteria.getValue() == null ? null : criteria.getValue().toString();
			predicates.add(builder.greaterThanOrEqualTo(root.<String>get(column.name()), value));
		}
		else if (RuleCriteria.LE.equals(criteria.getOperation())) {
			String value = criteria.getValue() == null ? null : criteria.getValue().toString();
			predicates.add(builder.lessThanOrEqualTo(root.<String>get(column.name()), value));
		}
		else if (criteria.getValue() != null && StringUtils.isNotBlank(criteria.getValue().toString())) {
			String value = StringUtils.trim(criteria.getValue().toString());
			if (RuleCriteria.CONTAINS.equals(criteria.getOperation()) && String.class.isAssignableFrom(criteria.getDataType())) {
				predicates.add(builder.like(builder.lower(root.<String>get(column.name())), "%" + StringUtils.lowerCase(value) + "%"));
			}
			else if (RuleCriteria.STARTS_WITH.equals(criteria.getOperation()) && String.class.isAssignableFrom(criteria.getDataType())) {
				predicates.add(builder.like(builder.lower(root.<String>get(column.name())), StringUtils.lowerCase(value) + "%"));
			}
			else if (RuleCriteria.ENDS_WITH.equals(criteria.getOperation()) && String.class.isAssignableFrom(criteria.getDataType())) {
				predicates.add(builder.like(builder.lower(root.<String>get(column.name())), "%" + StringUtils.lowerCase(value)));
			}
			else if (RuleCriteria.REG_EX.equals(criteria.getOperation()) && String.class.isAssignableFrom(criteria.getDataType())) {
				if (value.indexOf(" ") > 0 && JpaUtil.isHibernate() && (JpaUtil.isMySQLDatabase() || JpaUtil.isPgSQLDatabase())) { // use REGEXP
					buildMySQLRegexp(builder, root, predicates, column.name(), value, criteria.getMatchBy());
				}
				else { // use LIKE, match all words
					buildSQLLike(builder, root, predicates, column.name(), value, criteria.getMatchBy());
				}
			}
		}
		return predicates;
	}

	public static <T> List<Order> buildOrderByList(PagingVo vo, CriteriaBuilder builder, Root<T> root) {
		
		List<Order> orderByList = new ArrayList<Order>();
		Set<PagingVo.Column> colSet = vo.getOrderBy().getKeySet();
		
		//boolean hasOrderByUpdtTime = false;
		boolean hasOrderBy = false;
		
		for (Iterator<PagingVo.Column> it=colSet.iterator(); it.hasNext();) {
			PagingVo.Column col = it.next();
			if (vo.getOrderBy().getIsAscending(col) != null) {
				Path<String> orderBy = root.get(col.name());
				if (vo.getOrderBy().getIsAscending(col)) {
					orderByList.add(builder.asc(orderBy));
				}
				else {
					orderByList.add(builder.desc(orderBy));
				}
				//if (PagingVo.Column.updtTime.equals(col)) {
				//	hasOrderByUpdtTime = true;
				//}
				hasOrderBy = true;
 			}
		}
		if (hasOrderBy == false) {
			orderByList.add(builder.desc(root.get(PagingVo.Column.updtTime.name())));
		}
		
		return orderByList;
	}
	
	@SuppressWarnings("unchecked")
	static <T> PathTuple<T> buildPath(PagingVo.Column column, PagingVo.Criteria criteria, From<?, ?> root) {
		Class<?> dataType = criteria.getDataType();
		PathTuple<T> tuple = new PathTuple<>();
		if (java.sql.Timestamp.class.isAssignableFrom(dataType)) {
			tuple.path = (Path<T>) root.<java.sql.Timestamp>get(column.name());
			tuple.value = criteria.getValue();
		}
		else if (java.sql.Date.class.isAssignableFrom(dataType)) {
			tuple.path = (Path<T>) root.<java.sql.Date>get(column.name());
			tuple.value = criteria.getValue();
		}
		else if (Number.class.isAssignableFrom(dataType)) {
			tuple.path = (Path<T>) root.<Number>get(column.name());
			tuple.value = criteria.getValue();
		}
		else if (Boolean.class.isAssignableFrom(dataType)) {
			tuple.path = (Path<T>) root.<Boolean>get(column.name());
			tuple.value = criteria.getValue();
		}
		else {
			tuple.path = (Path<T>) root.<String>get(column.name());
			if (criteria.getValue() != null) {
				tuple.value = criteria.getValue().toString();
			}
			else {
				tuple.value = null;
			}
		}
		return tuple;
	}

}
