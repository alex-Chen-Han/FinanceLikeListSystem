package com.finance.likelist.service;

import com.finance.likelist.dto.UserDto;
import com.finance.likelist.model.User;
import com.finance.likelist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getActiveUsers() {
        return userRepository.getActiveUsers();
    }

    public List<User> getDeletedUsers() {
        return userRepository.getDeletedUsers();
    }

    public User getUserById(String userId) {
        return userRepository.getUserById(userId);
    }

    @Transactional
    public void createUser(UserDto userDto) {
        if (userRepository.checkUserExists(userDto.getUserId(), userDto.getAccount())) {
            throw new IllegalArgumentException("此使用者 ID 或扣款帳號已存在。");
        }
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setUserName(userDto.getUserName());
        user.setEmail(userDto.getEmail());
        user.setAccount(userDto.getAccount());
        userRepository.insertUser(user);
    }

    @Transactional
    public void updateUser(String userId, UserDto userDto) {
        User existingUser = userRepository.getUserById(userId);
        if (existingUser == null || Boolean.TRUE.equals(existingUser.getIsDeleted())) {
            throw new IllegalArgumentException("找不到使用者或該使用者已被刪除。");
        }
        
        // Ensure that if account is changed, it won't conflict with another user
        if (!existingUser.getAccount().equals(userDto.getAccount()) && userRepository.checkUserExists("DUMMY_ID", userDto.getAccount())) {
            throw new IllegalArgumentException("此扣款帳號已被其他使用者綁定。");
        }

        existingUser.setUserName(userDto.getUserName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setAccount(userDto.getAccount());
        userRepository.updateUser(existingUser);
    }

    @Transactional
    public void deleteUserSoft(String userId) {
        User existingUser = userRepository.getUserById(userId);
        if (existingUser == null || Boolean.TRUE.equals(existingUser.getIsDeleted())) {
            throw new IllegalArgumentException("找不到使用者或該使用者已被刪除。");
        }
        if (userRepository.checkUserHasLikeLists(userId) > 0) {
            throw new IllegalArgumentException("請先刪除喜好清單再移除帳戶");
        }
        userRepository.deleteUserSoft(userId);
    }

    @Transactional
    public void deleteUserHard(String userId) {
        if (userRepository.checkUserHasLikeLists(userId) > 0) {
            throw new IllegalArgumentException("請先刪除喜好清單再移除帳戶");
        }
        userRepository.deleteUserHard(userId);
    }

    @Transactional
    public void restoreUser(String userId) {
        User existingUser = userRepository.getUserById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("找不到使用者。");
        }
        userRepository.restoreUser(userId);
    }
}
