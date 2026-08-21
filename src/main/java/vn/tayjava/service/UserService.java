package vn.tayjava.service;

import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.util.UserStatus;

public interface UserService {
    long saveUser(UserRequestDto requestDto);
    void updateUser(long userId, UserRequestDto request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    PageResponse<?> getAllUsers(int pageNo, int pageSize, String... sortBy);
}
