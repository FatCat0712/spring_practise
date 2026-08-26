package vn.tayjava.service;

import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.util.UserStatus;

import java.util.List;

public interface UserService {
    long saveUser(UserRequestDto requestDto);
    void updateUser(long userId, UserRequestDto request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    PageResponse<List<UserDetailResponse>> getAllUsers(int pageNo, int pageSize, String search, String... sortBy);
    PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    PageResponse<?> advanceSearchWithSpecification(Pageable pageable, String[] user, String... address);

    void confirmUser(@Min(1) int userId, String secretCode);
}
