package vn.tayjava.repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.criteria.SearchCriteria;
import vn.tayjava.repository.criteria.UserSearchCriteriaQueryConsumer;
import vn.tayjava.repository.specification.SpecSearchCriteria;
import vn.tayjava.util.Gender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Repository
@Slf4j
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

    public PageResponse<?> advanceSearchUser(int pageNo, int pageSize, String sortBy, String address, String... search) {
        // firstName: T, lastName: T
        List<SearchCriteria> criteriaList = new ArrayList<>();

        // lay ra danh sach user
        if(search != null) {
            for(String s : search) {
                // firstName:value
                Pattern pattern = Pattern.compile("(\\w+?)(:|<|>|<=|>=|!=)(.*)");
                Matcher matcher = pattern.matcher(s);

                if(matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        // lay ra so luong ban ghi
        List<User> users = getUsers(pageNo, pageSize, criteriaList, address, sortBy);

        List<UserDetailResponse> items = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();

        Long totalElements = getTotalElements(criteriaList, address);

        // lay ra so luong ban ghi
        return PageResponse.builder()
                .pageNo(pageNo) // offset = vi tri cua ban ghi trong danh sach
                .pageSize(pageSize)
                .totalPage((int)(Math.ceil((double)totalElements/pageSize)))
                .totalElements(totalElements)
                .items(items)
                .build();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList, String address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = countQuery.from(User.class);
        countQuery.select(criteriaBuilder.count(root));

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer consumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        criteriaList.forEach(consumer);
        predicate = consumer.getPredicate();

        if(StringUtils.hasLength(address)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("addresses").get("city"), "%" + address + "%"));
        }

        countQuery.where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<User> getUsers(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String address, String sortBy) {
        if(pageNo > 0) {
            pageNo = pageNo - 1;
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        root.fetch("addresses", JoinType.LEFT);
        query.distinct(true);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaQueryConsumer consumer = new UserSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        criteriaList.forEach(consumer);
        predicate = consumer.getPredicate();

        if(StringUtils.hasLength(address)) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("addresses").get("city"), "%" + address + "%"));
        }

        query.where(predicate);


        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if ("desc".equalsIgnoreCase(direction)) {
                    query.orderBy(criteriaBuilder.desc(root.get(field)));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get(field)));
                }
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public PageResponse<?> getUserJoinedAddress(Pageable pageable, String[] user, String[] address) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<Address, User> addressRoot = root.join("addresses");
        query.distinct(true);

        // Build query
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addressPre = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
        for(String u : user) {
            Matcher matcher = pattern.matcher(u);
            if(matcher.find()) {
                String key = matcher.group(1);
                String operation = matcher.group(2);
                String value = matcher.group(3);
                String prefix = matcher.group(4);
                String suffix = matcher.group(5);
                SpecSearchCriteria criteria = new SpecSearchCriteria(key, operation, value, prefix, suffix);
                Predicate predicate = toPredicate(root, criteriaBuilder, criteria);
                userPre.add(predicate);
            }
        }

        for(String a : address) {
            Matcher matcher = pattern.matcher(a);
            if(matcher.find()) {
                String key = matcher.group(1);
                String operation = matcher.group(2);
                String value = matcher.group(3);
                String prefix = matcher.group(4);
                String suffix = matcher.group(5);
                SpecSearchCriteria criteria = new SpecSearchCriteria(key, operation, value, prefix, suffix);
                Predicate predicate = toPredicate(addressRoot, criteriaBuilder, criteria);
                addressPre.add(predicate);
            }
        }

        Predicate userPredicate = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
        Predicate addressPredicate = criteriaBuilder.or(addressPre.toArray(new Predicate[0]));
        query.where(criteriaBuilder.and(userPredicate, addressPredicate));

        List<User> users = entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long totalElements = count(user, address);

        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .items(users)
                .build();
    }

    private Long count(String[] user, String[] address){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);
        Join<Address, User> addressRoot = root.join("addresses");
        query.distinct(true);

        // Build query
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addressPre = new ArrayList<>();

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
        for(String u : user) {
            Matcher matcher = pattern.matcher(u);
            if(matcher.find()) {
                String key = matcher.group(1);
                String operation = matcher.group(2);
                String value = matcher.group(3);
                String prefix = matcher.group(4);
                String suffix = matcher.group(5);
                SpecSearchCriteria criteria = new SpecSearchCriteria(key, operation, value, prefix, suffix);
                Predicate predicate = toPredicate(root, criteriaBuilder, criteria);
                userPre.add(predicate);
            }
        }

        for(String a : address) {
            Matcher matcher = pattern.matcher(a);
            if(matcher.find()) {
                String key = matcher.group(1);
                String operation = matcher.group(2);
                String value = matcher.group(3);
                String prefix = matcher.group(4);
                String suffix = matcher.group(5);
                SpecSearchCriteria criteria = new SpecSearchCriteria(key, operation, value, prefix, suffix);
                Predicate predicate = toPredicate(addressRoot, criteriaBuilder, criteria);
                addressPre.add(predicate);
            }
        }

        Predicate userPredicate = criteriaBuilder.or(userPre.toArray(new Predicate[0]));
        Predicate addressPredicate = criteriaBuilder.or(addressPre.toArray(new Predicate[0]));

        query.select(criteriaBuilder.count(root));
        query.where(criteriaBuilder.and(userPredicate, addressPredicate));


        return entityManager.createQuery(query).getSingleResult();
    }

    public @Nullable Predicate toPredicate(Root<User> root, CriteriaBuilder criteriaBuilder, SpecSearchCriteria criteria) {
        log.info("Building predicate for key: {}, operation: {}, value: {}", criteria.getKey(), criteria.getOperation(), criteria.getValue());

        Object value = criteria.getValue();

        List<String> gender = Stream.of(Gender.values()).map(Enum::name).toList();

        return switch (criteria.getOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), gender.contains(value.toString()) ? Gender.valueOf(value.toString()) : value.toString());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()), value.toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), value.toString());
            case LIKE, CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + value + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), value + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + value);
        };
    }

    public @Nullable Predicate toPredicate(Join<Address, User> root, CriteriaBuilder criteriaBuilder, SpecSearchCriteria criteria) {
        log.info("Building predicate for key: {}, operation: {}, value: {}", criteria.getKey(), criteria.getOperation(), criteria.getValue());

        Object value = criteria.getValue();

        List<String> gender = Stream.of(Gender.values()).map(Enum::name).toList();

        return switch (criteria.getOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), gender.contains(value.toString()) ? Gender.valueOf(value.toString()) : value.toString());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()), value.toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), value.toString());
            case LIKE, CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + value + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), value + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + value);
        };
    }

}
