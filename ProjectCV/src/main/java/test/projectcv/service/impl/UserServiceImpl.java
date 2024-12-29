package test.projectcv.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import test.projectcv.dto.UpdateUserDTo;
import test.projectcv.dto.UserDto;
import test.projectcv.exception.AppException;
import test.projectcv.model.User;
import test.projectcv.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User createUser(UserDto userDTO) throws AppException {
        return null;
    }

    @Override
    public String login(String username, String password, Long roleId) throws AppException {
        return "";
    }

    @Override
    public User getUserDetailsFromToken(String token) throws AppException {
        return null;
    }

    @Override
    public User getUserDetailsFromRefreshToken(String token) throws AppException {
        return null;
    }

    @Override
    public User updateUser(Long userId, UpdateUserDTo updatedUserDTO) throws AppException {
        return null;
    }

    @Override
    public Page<User> findAll(String keyword, Pageable pageable) throws AppException {
        return null;
    }

    @Override
    public void resetPassword(Long userId, String newPassword) throws AppException {

    }

    @Override
    public void blockOrEnable(Long userId, Boolean active) throws AppException {

    }
}
