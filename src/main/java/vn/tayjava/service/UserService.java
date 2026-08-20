package vn.tayjava.service;

import vn.tayjava.dto.request.UserRequestDto;

public interface UserService {
    int addUser(UserRequestDto requestDto);
}
