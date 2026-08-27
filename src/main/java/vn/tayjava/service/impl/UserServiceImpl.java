package vn.tayjava.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.tayjava.dto.request.AddressDTO;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.SearchRepository;
import vn.tayjava.repository.UserRepository;
import vn.tayjava.repository.specification.UserSpec;
import vn.tayjava.repository.specification.UserSpecificationBuilder;
import vn.tayjava.service.MailService;
import vn.tayjava.service.UserService;
import vn.tayjava.util.Gender;
import vn.tayjava.util.UserStatus;
import vn.tayjava.util.UserType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Pattern SORT_PATTERN = Pattern.compile("^(\\w+?):(asc|desc)$", Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "email", "phone"
    );

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
//    private final MailService mailService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public long saveUser(UserRequestDto request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .build();

        Set<Address> addresses = convertToAddress(user, request.getAddresses());
        user.setAddresses(addresses);

        user = userRepository.save(user);

//        if(user.getId() != null) {
//            // send email confirm here
//            try {
//                mailService.sendConfirmLink(user.getEmail(), user.getId(), "secretCode");
//            } catch (MessagingException | UnsupportedEncodingException e) {
//                log.error("Failed to send confirmation email: {}", e.getMessage());
//            }
//        }

        if(user.getId() != null) {
            String message = String.format("email=%s,userId=%d,secretCode=%s", user.getEmail(), user.getId(), "secretCode");
            kafkaTemplate.send("confirm-account-topic", message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send message to Kafka: {}", ex.getMessage());
                        } else {
                            log.info("Message sent to Kafka topic={}, partition={}, offset={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        }

        log.info("User saved successfully");
        return user.getId();
    }

    @Override
    public void confirmUser(int userId, String secretCode) {
        log.info("Account confirmed {}", userId);
    }

    @Override
    public void updateUser(long userId, UserRequestDto request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if(!request.getEmail().equals(user.getEmail())){
            // check email from database if not exists then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(user, request.getAddresses()));
        userRepository.save(user);

        log.info("User updated successfully");

    }

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("User status changed");
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
        log.info("User deleted successfully");
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public PageResponse<List<UserDetailResponse>> getAllUsers(int pageNo, int pageSize, String search, String... sortBy) {
        if(pageNo > 0) {
            pageNo = pageNo - 1;
        }

        Sort sort = buildSort(sortBy);
        Page<Long> userIds = searchRepository.findUserIds(
                pageNo,
                pageSize,
                normalizeKeyword(search),
                sort
        );
        List<User> users = getUsersForPage(userIds.getContent());
        List<UserDetailResponse> items = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();

        return PageResponse.<List<UserDetailResponse>>builder()
                .pageNo(userIds.getNumber() + 1)
                .pageSize(userIds.getSize())
                .totalPage(userIds.getTotalPages())
                .items(items)
                .build();
    }

    private Sort buildSort(String... sortBy) {
        List<Sort.Order> sortOrders = new ArrayList<>();
        if(sortBy != null) {
            for(String sort : sortBy) {
                if(!StringUtils.hasText(sort)) {
                    continue;
                }

                Matcher matcher = SORT_PATTERN.matcher(sort.trim());
                if(!matcher.matches()) {
                    continue;
                }

                String field = matcher.group(1);
                if(!ALLOWED_SORT_FIELDS.contains(field)) {
                    continue;
                }

                String direction = matcher.group(2);
                sortOrders.add("asc".equalsIgnoreCase(direction)
                        ? Sort.Order.asc(field)
                        : Sort.Order.desc(field));
            }
        }

        boolean hasIdSort = sortOrders.stream()
                .anyMatch(order -> "id".equalsIgnoreCase(order.getProperty()));
        if(!hasIdSort) {
            sortOrders.add(Sort.Order.asc("id"));
        }

        return Sort.by(sortOrders);
    }

    private String normalizeKeyword(String search) {
        return StringUtils.hasText(search) ? search.trim() : null;
    }

    private List<User> getUsersForPage(List<Long> userIds) {
        if(userIds.isEmpty()) {
            return List.of();
        }

        Map<Long, User> usersById = userRepository.findAllWithAddressesByIdIn(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return userIds.stream()
                .map(usersById::get)
                .toList();
    }

    private Set<Address> convertToAddress(User user, Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(addressDTO -> {
            Address address = Address.builder()
                    .apartmentNumber(addressDTO.getApartmentNumber())
                    .floor(addressDTO.getFloor())
                    .building(addressDTO.getBuilding())
                    .streetNumber(addressDTO.getStreetNumber())
                    .street(addressDTO.getStreet())
                    .city(addressDTO.getCity())
                    .country(addressDTO.getCountry())
                    .build();
            user.saveAddress(address);
            result.add(address);
        });
        return result;
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String address, String sortBy, String... search) {
        return searchRepository.advanceSearchUser(pageNo, pageSize, address, sortBy, search);
    }

    @Override
    public PageResponse<?> advanceSearchWithSpecification(Pageable pageable, String[] user, String... address) {
        Page<User> users = null;
        List<User> list = new ArrayList<>();
        if(user != null && address != null) {
            // tim kiem tren user va address -> join table
            return searchRepository.getUserJoinedAddress(pageable, user, address);
        }else if(user != null) {
            // tim kiem tren user -> khong can join bang address
            //  Specification<User> spec = UserSpec.hasFirstName("T");
            //  Specification<User> genderSpec = UserSpec.notEqualGender(Gender.MALE);
            //  spec = spec.and(genderSpec);

            UserSpecificationBuilder builder = new UserSpecificationBuilder();
            for(String s : user) {
                Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()) {
                    String key = matcher.group(1);
                    String operation = matcher.group(2);
                    String value = matcher.group(3);
                    String prefix = matcher.group(4);
                    String suffix = matcher.group(5);
                    builder.with(key, operation, value, prefix, suffix);
                }
            }

            list = userRepository.findAll(builder.build());

            return PageResponse.builder()
                    .pageNo(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalPage(users != null ? users.getTotalPages() : 0)
                    .totalElements(users != null ? users.getTotalElements() : 0)
                    .items(list)
                    .build();
        }
        else {
            users = userRepository.findAll(pageable);
        }

        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage(users != null ? users.getTotalPages() : 0)
                .totalElements(users != null ? users.getTotalElements() : 0)
                .items(users != null ? users.getContent() : list)
                .build();
    }
}
