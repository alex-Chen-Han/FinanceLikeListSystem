package com.finance.likelist.controller;

import com.finance.likelist.common.ApiResponse;
import com.finance.likelist.dto.UserDto;
import com.finance.likelist.model.User;
import com.finance.likelist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/active")
    public ApiResponse<List<User>> getActiveUsers() {
        return ApiResponse.success(userService.getActiveUsers());
    }

    @GetMapping("/deleted")
    public ApiResponse<List<User>> getDeletedUsers() {
        return ApiResponse.success(userService.getDeletedUsers());
    }

    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable String userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @PostMapping
    public ApiResponse<Void> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            userService.createUser(userDto);
            return ApiResponse.success("用戶建立成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ApiResponse<Void> updateUser(@PathVariable String userId, @Valid @RequestBody UserDto userDto) {
        try {
            userService.updateUser(userId, userDto);
            return ApiResponse.success("用戶更新成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUserSoft(@PathVariable String userId) {
        try {
            userService.deleteUserSoft(userId);
            return ApiResponse.success("用戶刪除成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/hard")
    public ApiResponse<Void> deleteUserHard(@PathVariable String userId) {
        try {
            userService.deleteUserHard(userId);
            return ApiResponse.success("用戶永久刪除成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PatchMapping("/{userId}/restore")
    public ApiResponse<Void> restoreUser(@PathVariable String userId) {
        try {
            userService.restoreUser(userId);
            return ApiResponse.success("用戶復原成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
