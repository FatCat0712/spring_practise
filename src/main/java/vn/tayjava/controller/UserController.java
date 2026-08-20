package vn.tayjava.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tayjava.configuration.Translator;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.ResponseData;
import vn.tayjava.service.UserService;


import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
@Tag(name = "User Controller")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Add a new user", description = "This endpoint allows you to add a new user to the system.")
    @PostMapping(value = "/")
    public ResponseData<Integer> addUser(@RequestBody @Valid UserRequestDto user) {
        log.info("Request add user = {} {}", user.getFirstName(), user.getLastName());
        return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), 1);
    }

    @Operation(summary = "Update an existing user", description = "This endpoint allows you to update an existing user's information.")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@PathVariable @Min(1) int userId, @Valid @RequestBody UserRequestDto userDto) {
        log.info("Request update userId={}", userId);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.update.success"));
    }

    @Operation(summary = "Change the status of an existing user", description = "This endpoint allows you to change the status of an existing user.")
    @PatchMapping("/{userId}")
    public ResponseData<?> changeStatus(@PathVariable @Min(1) int userId, @RequestParam boolean status) {
        log.info("Request change status, userId={}", userId);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User status changed");
    }

    @Operation(summary = "Delete an existing user", description = "This endpoint allows you to delete an existing user.")
    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable int userId) {
        log.info("Request delete userId={}", userId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "User deleted");
    }

    @Operation(summary = "Get an existing user", description = "This endpoint allows you to get an existing user's information.")
    @GetMapping("/{userId}")
    public ResponseData<UserRequestDto> getUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") int userId) {
        log.info("Request get user detail, userId={}", userId);
        return new ResponseData<>(HttpStatus.OK.value(), "User retrieved", new UserRequestDto("John", "Doe", "1234567890", ""));
    }

    @Operation(summary = "Get all users", description = "This endpoint allows you to get all users' information.")
    @GetMapping("/list")
    public ResponseData<List<UserRequestDto>> getAllUsers(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize
    ) {
        log.info("Request get all users");
        return new ResponseData<>(HttpStatus.OK.value(), "Users retrieved", List.of(new UserRequestDto("John", "Doe", "1234567890", ""), new UserRequestDto("Jane", "Smith", "0987654321", "")));
    }


}
