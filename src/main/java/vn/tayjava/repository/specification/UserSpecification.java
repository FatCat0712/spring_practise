package vn.tayjava.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import vn.tayjava.model.User;
import vn.tayjava.util.Gender;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class UserSpecification implements Specification<User> {
    private final SpecSearchCriteria criteria;

    public UserSpecification(SpecSearchCriteria specSearchCriteria) {
        this.criteria = specSearchCriteria;
    }

    @Override
    public @Nullable Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        log.info("Building predicate for key: {}, operation: {}, value: {}", criteria.getKey(), criteria.getOperation(), criteria.getValue());

        Object value = criteria.getValue();
        String strValue = value != null ? value.toString() : null;

        List<String> gender = Stream.of(Gender.values()).map(Enum::name).toList();

        return switch (criteria.getOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), gender.contains(strValue) ? Gender.valueOf(strValue) : strValue);
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()), strValue);
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), strValue);
            case LIKE, CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + strValue + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), strValue + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + strValue);
        };
    }
}
