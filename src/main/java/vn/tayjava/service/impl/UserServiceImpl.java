package vn.tayjava.service.impl;

import org.springframework.stereotype.Service;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public int addUser(UserRequestDto requestDto) {
        System.out.println("Adding user: " + requestDto.getFirstName());
        if(requestDto.getFirstName().equals("Tay")) {
            throw new ResourceNotFoundException("User with first name 'Tay' not found");
        }
        return 1;
    }
}
