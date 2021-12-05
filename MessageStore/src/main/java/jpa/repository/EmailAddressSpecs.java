package jpa.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import jpa.model.EmailAddress;
import jpa.msgui.vo.PagingVo;

public class EmailAddressSpecs {

	public static Specification<EmailAddress> buildSpecForFirstRow() {
		return new Specification<EmailAddress>() {
			private static final long serialVersionUID = 2882172268965394418L;

			public Predicate toPredicate(Root<EmailAddress> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				
				LocalDateTime dateTime = LocalDateTime.now().minusYears(1);
				java.sql.Timestamp ts = java.sql.Timestamp.valueOf(dateTime);
				return builder.greaterThan(root.get("updtTime"), ts);
			}
		};
	}
	

	public static Specification<EmailAddress> buildSpecByPagingVo(PagingVo vo) {
		return new Specification<EmailAddress>() {
			private static final long serialVersionUID = -8346054049294860717L;

			public Predicate toPredicate(Root<EmailAddress> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

				List<Predicate> predicates = new ArrayList<>();
				List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(PagingVo.Type.emailaddr);
				for (PagingVo.Column col : colList) {
					if (vo.getSearchValue(col) == null) {
						continue;
					}
					PagingVo.Criteria criteria  = vo.getSearchCriteria(col);
					predicates.addAll(RepositoryUtils.buildPredicateList(col, criteria, builder, root));
				}
				
				List<Order> orderList = RepositoryUtils.buildOrderByList(vo, builder, root);
				if (!orderList.isEmpty()) {
					//if (!JpaUtil.isDerbyDatabase()) {
						query.orderBy(orderList);
					//}
				}
				
				return builder.and(predicates.toArray(new Predicate[] {}));
			}
		};
	}

}
