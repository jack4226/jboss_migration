package jpa.repository.msg;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import jpa.model.msg.MessageInbox;
import jpa.msgui.vo.PagingVo;
import jpa.repository.RepositoryUtils;

public class MessageInboxSpecs {

	public static Specification<MessageInbox> buildSpecForRowCount(PagingVo vo) {
		return new Specification<MessageInbox>() {
			public Predicate toPredicate(Root<MessageInbox> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

				root.join("fromAddress", JoinType.INNER);
				root.join("ruleLogic", JoinType.LEFT);
				
				List<Predicate> predicates = RepositoryUtils.buildPredicatesList(vo, PagingVo.Type.msginbox, builder, root);
				
				return builder.and(predicates.toArray(new Predicate[] {}));
			}
		};
	}

}
