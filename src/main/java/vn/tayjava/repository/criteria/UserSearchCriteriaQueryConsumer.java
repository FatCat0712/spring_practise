package vn.tayjava.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tayjava.model.User;

import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {
    private CriteriaBuilder criteriaBuilder;
    private Predicate predicate;
    private Root<User> root;


    @Override
    public void accept(SearchCriteria searchCriteria) {
        String key = searchCriteria.getKey();
        String operation = searchCriteria.getOperation();
        Object value = searchCriteria.getValue();
        String valueStr = value.toString();

        if(">".equals(operation)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get(key), valueStr));
        }
        else if("<".equals(operation)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get(key), valueStr));
        }
        else {
            if(root.get(key).getJavaType() == String.class) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(key), "%" + valueStr + "%"));
            } else {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(key), valueStr));
            }
        }
    }
}
