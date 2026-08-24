package vn.tayjava.repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.tayjava.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Repository
public class SearchRepository {
    private static final Set<String> NON_SEARCHABLE_FIELDS = Set.of("password");

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Long> findUserIds(int pageNo, int pageSize, String keyword, Sort sort) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> idQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> idRoot = idQuery.from(User.class);
        idQuery.select(idRoot.get("id"));
        applyKeywordFilter(criteriaBuilder, idQuery, idRoot, keyword);
        applySort(criteriaBuilder, idQuery, idRoot, sort);

        TypedQuery<Long> selectQuery = entityManager.createQuery(idQuery);
        selectQuery.setFirstResult(pageNo * pageSize);
        selectQuery.setMaxResults(pageSize);
        List<Long> userIds = selectQuery.getResultList();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(criteriaBuilder.count(countRoot));
        applyKeywordFilter(criteriaBuilder, countQuery, countRoot, keyword);
        Long totalItems = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(userIds, PageRequest.of(pageNo, pageSize, sort), totalItems);
    }

    private void applyKeywordFilter(CriteriaBuilder criteriaBuilder,
                                    CriteriaQuery<?> query,
                                    Root<User> root,
                                    String keyword) {
        if(StringUtils.hasText(keyword)) {
            String keywordPattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            List<String> searchableFields = resolveSearchableStringFields();
            if(searchableFields.isEmpty()) {
                return;
            }

            Predicate[] keywordPredicates = searchableFields.stream()
                    .map(field -> criteriaBuilder.like(criteriaBuilder.lower(root.get(field)), keywordPattern))
                    .toArray(Predicate[]::new);
            Predicate keywordPredicate = criteriaBuilder.or(keywordPredicates);
            query.where(keywordPredicate);
        }
    }

    private void applySort(CriteriaBuilder criteriaBuilder,
                           CriteriaQuery<Long> query,
                           Root<User> root,
                           Sort sort) {
        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for(Sort.Order order : sort) {
            orders.add(order.isAscending() ? criteriaBuilder.asc(root.get(order.getProperty()))
                    : criteriaBuilder.desc(root.get(order.getProperty())));
        }

        if(!orders.isEmpty()) {
            query.orderBy(orders);
        }
    }

    private List<String> resolveSearchableStringFields() {
        return entityManager.getMetamodel()
                .entity(User.class)
                .getSingularAttributes()
                .stream()
                .filter(attribute -> String.class.equals(attribute.getJavaType()))
                .map(SingularAttribute::getName)
                .filter(field -> !NON_SEARCHABLE_FIELDS.contains(field))
                .toList();
    }

}
