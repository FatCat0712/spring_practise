package vn.tayjava.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tayjava.dto.request.AddressDTO;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.model.Address;
import vn.tayjava.model.User;
import vn.tayjava.repository.UserRepository;
import vn.tayjava.service.UserService;
import vn.tayjava.util.UserStatus;
import vn.tayjava.util.UserType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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

        log.info("User saved successfully");
        return user.getId();
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public List<UserDetailResponse> getAllUsers(int pageNo, int pageSize) {
        return List.of();
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
}
