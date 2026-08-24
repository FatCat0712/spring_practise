package vn.tayjava.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tayjava.configuration.Translator;
import vn.tayjava.dto.request.UserRequestDto;
import vn.tayjava.dto.response.PageResponse;
import vn.tayjava.dto.response.ResponseData;
import vn.tayjava.dto.response.ResponseError;
import vn.tayjava.dto.response.UserDetailResponse;
import vn.tayjava.exception.ResourceNotFoundException;
import vn.tayjava.service.UserService;
import vn.tayjava.util.UserStatus;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Add a new user", description = "This endpoint allows you to add a new user to the system.")
    @PostMapping(value = "/")
    public ResponseData<Long> addUser(@RequestBody @Valid UserRequestDto user) {
        log.info("Request add user, {} {}", user.getFirstName(), user.getLastName());
        try {
            long userId = userService.saveUser(user);
            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);
        }catch (Exception e){
            log.error("errorMessage: {}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), Translator.toLocale("user.add.failure"));
        }
    }

    @Operation(summary = "Update an existing user", description = "This endpoint allows you to update an existing user's information.")
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@PathVariable @Min(1) long userId, @Valid @RequestBody UserRequestDto userDto) {
        log.info("Request update userId={}", userId);
        try {
            userService.updateUser(userId, userDto);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), Translator.toLocale("user.update.success"));
        }catch (ResourceNotFoundException e){
            log.error("errorMessage: {}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Update user fail");
        }
    }

    @Operation(summary = "Change the status of an existing user", description = "This endpoint allows you to change the status of an existing user.")
    @PatchMapping("/{userId}")
    public ResponseData<?> changeStatus(@PathVariable @Min(1) long userId, @RequestParam UserStatus status) {
        log.info("Request change status, userId={}", userId);
        try {
            userService.changeStatus(userId, status);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User status changed");
        }catch (ResourceNotFoundException e){
            log.error("errorMessage: {}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Change user status fail");
        }
    }

    @Operation(summary = "Delete an existing user", description = "This endpoint allows you to delete an existing user.")
    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@Min(value = 1, message = "userId must be greater than 0") @PathVariable long userId) {
        log.info("Request delete userId={}", userId);
        try {
            userService.deleteUser(userId);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User deleted");
        }catch (ResourceNotFoundException e){
            log.error("errorMessage: {}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Delete user fail");
        }
    }

    @Operation(summary = "Get an existing user", description = "This endpoint allows you to get an existing user's information.")
    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") long userId) {
        log.info("Request get user detail, userId={}", userId);
        try {
            UserDetailResponse response = userService.getUser(userId);
            return new ResponseData<>(HttpStatus.OK.value(), "User retrieved", response);
        }catch (ResourceNotFoundException e){
            log.error("errorMessage: {}", e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Get all users", description = "This endpoint allows you to get all users' information.")
    @GetMapping("/list")
    public ResponseData<PageResponse<List<UserDetailResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ...sortBy
    ) {
        log.info("Request get all users");
        return new ResponseData<>(HttpStatus.OK.value(), "Users retrieved", userService.getAllUsers(pageNo, pageSize, search, sortBy));
    }

    @Operation(summary = "Get list of users and search with paging and sorting by criteria", description = "Send a request via this API to get user list by pageNo, pageSize and sort by criteria")
    @GetMapping("/advance-search-by-criteria")
    public ResponseData<?> advancedSearchByCriteria(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String ...search
    ) {
        log.info("Request advance search with criteria and paging and sorting");
        return new ResponseData<>(HttpStatus.OK.value(), "Users retrieved", userService.advanceSearchByCriteria(pageNo, pageSize, sortBy, address, search));
    }


}
