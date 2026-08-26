package vn.tayjava.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import vn.tayjava.model.User;
import vn.tayjava.util.Gender;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserSpecificationBuilder {
    public final List<SpecSearchCriteria> params;

    public UserSpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    public UserSpecificationBuilder with(String key, String operation, Object value, String prefix, String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public UserSpecificationBuilder with(String orPredicate, String key, String operation, Object value, String prefix, String suffix) {
        SearchOperation oper = SearchOperation.getSimpleOperation(operation.charAt(0));
        if(oper == SearchOperation.EQUALITY) {
            boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
            boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
            if(startWithAsterisk && endWithAsterisk) {
                oper = SearchOperation.CONTAINS;
            } else if(startWithAsterisk) {
                oper = SearchOperation.ENDS_WITH;
            } else if(endWithAsterisk) {
                oper = SearchOperation.STARTS_WITH;
            }
        }

        params.add(new SpecSearchCriteria(orPredicate, key, oper, value));
        return this;
    }

    public Specification<User> build() {
        if(params.isEmpty()) {
            return null;
        }

        Specification<User> result = new UserSpecification(params.get(0));
        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).getOrPredicate()
                    ? Specification.where(result).or(new UserSpecification(params.get(i)))
                    : Specification.where(result).and(new UserSpecification(params.get(i)));
        }
        return result;
    }
}
