package vn.tayjava.service;

import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.util.UserStatus;

import java.util.List;

public interface UserService {
    long saveUser(UserRequestDto requestDto);
    void updateUser(long userId, UserRequestDto request);
    void changeStatus(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    List<UserDetailResponse> getAllUsers(int pageNo, int pageSize);
}
