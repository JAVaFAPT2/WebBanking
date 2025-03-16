package org.user.service.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.user.service.controller.UserController;
import org.user.service.model.dto.CreateUser;
import org.user.service.model.dto.UserDto;
import org.user.service.model.dto.UserUpdateStatus;
import org.user.service.model.dto.response.Response;
import org.user.service.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    public UserControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUserSuccess() {
        CreateUser createUser = new CreateUser("John", "Doe", "1234567890", "john.doe@example.com", "password");
        Response mockResponse = new Response("201", "User created successfully");
        when(userService.createUser(any(CreateUser.class))).thenReturn(mockResponse);

        ResponseEntity<Response> responseEntity = userController.registerUser(createUser);

        assertEquals(HttpStatus.CREATED.value(), responseEntity.getStatusCodeValue());
        assertEquals("User created successfully", responseEntity.getBody().getResponseMessage());
    }

    @Test
    public void testReadAllUsersSuccess() {
        UserDto userDto = new UserDto();
        List<UserDto> userList = Collections.singletonList(userDto);
        when(userService.readAllUsers()).thenReturn(userList);

        ResponseEntity<List<UserDto>> responseEntity = userController.readAllUsers();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().size());
    }

    @Test
    public void testUpdateUserStatusSuccess() {
        UserUpdateStatus userUpdateStatus = new UserUpdateStatus();
        Response mockResponse = new Response("200", "User status updated successfully");
        when(userService.updateUserStatus(any(Long.class), any(UserUpdateStatus.class))).thenReturn(mockResponse);

        ResponseEntity<Response> responseEntity = userController.updateUserStatus(1L, userUpdateStatus);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User status updated successfully", responseEntity.getBody().getResponseMessage());
    }

    @Test
    public void testRegisterUserMissingFields() {
        CreateUser createUser = new CreateUser(null, "Doe", "1234567890", "john.doe@example.com", "password");
        Response mockResponse = new Response("400", "Missing required fields");
        when(userService.createUser(any(CreateUser.class))).thenReturn(mockResponse);

        ResponseEntity<Response> responseEntity = userController.registerUser(createUser);

        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
        assertEquals("Missing required fields", responseEntity.getBody().getResponseMessage());
    }


    @Test
    public void testReadUserByInvalidAuthId() {
        when(userService.readUser(any(String.class))).thenReturn(null);

        ResponseEntity<UserDto> responseEntity = userController.readUserByAuthId("invalidAuthId");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
    }
}