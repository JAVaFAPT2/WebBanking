package test.projectcv.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import test.projectcv.dto.UpdateUserDTo;
import test.projectcv.dto.UserDto;
import test.projectcv.exception.AppException;
import test.projectcv.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDTO) throws AppException;

    String login(String username, String password, Long roleId) throws AppException;

    User getUserDetailsFromToken(String token) throws AppException;

    User getUserDetailsFromRefreshToken(String token) throws AppException;

    User updateUser(Long userId, UpdateUserDTo updatedUserDTO) throws AppException;

    Page<User> findAll(String keyword, Pageable pageable) throws AppException;

    void resetPassword(Long userId, String newPassword) throws AppException;

    void blockOrEnable(Long userId, Boolean active) throws AppException;
}
