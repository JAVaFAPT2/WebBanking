package org.user.service.service.implementation;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.user.service.exception.EmptyFields;
import org.user.service.exception.ResourceConflictException;
import org.user.service.exception.ResourceNotFound;
import org.user.service.external.AccountService;
import org.user.service.model.Status;
import org.user.service.model.dto.CreateUser;
import org.user.service.model.dto.UserDto;
import org.user.service.model.dto.UserUpdate;
import org.user.service.model.dto.UserUpdateStatus;
import org.user.service.model.dto.response.Response;
import org.user.service.model.entity.User;
import org.user.service.model.entity.UserProfile;
import org.user.service.model.external.Account;
import org.user.service.model.mapper.UserMapper;
import org.user.service.repository.UserRepository;
import org.user.service.service.KeycloakService;
import org.user.service.service.UserService;
import org.user.service.utils.FieldChecker;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final AccountService accountService;

    private UserMapper userMapper = new UserMapper();

    @Value("${spring.application.success}")
    private String responseCodeSuccess;

    @Value("${spring.application.not_found}")
    private String responseCodeNotFound;

    /**
     * Creates a new user.
     *
     * @param userDto The user data transfer object containing user information.
     * @return A response indicating the result of the user creation.
     * @throws ResourceConflictException If the emailId is already registered as a user.
     * @throws RuntimeException If the user with identification number is not found.
     */
    @Override
    public Response createUser(CreateUser userDto) {
        // Check if email already exists in Keycloak
        List<UserRepresentation> existingUsers = keycloakService.readUserByEmail(userDto.getEmailId());
        if (!existingUsers.isEmpty()) {
            log.error("Email already registered: {}", userDto.getEmailId());
            throw new ResourceConflictException("Email already registered");
        }

        // Create Keycloak user representation
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(userDto.getEmailId());
        userRep.setFirstName(userDto.getFirstName());
        userRep.setLastName(userDto.getLastName());
        userRep.setEmail(userDto.getEmailId());
        userRep.setEnabled(true);

        // Set password
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setValue(userDto.getPassword());
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        userRep.setCredentials(Collections.singletonList(cred));

        // Create user in Keycloak
        Response keycloakResponse = keycloakService.createUser(userRep);

        if ("201".equals(keycloakResponse.getResponseCode())) {
            // User created successfully in Keycloak
            User user = new User();
            user.setEmailId(userDto.getEmailId());
            user.setContactNo(userDto.getContactNumber());
            user.setStatus(Status.PENDING);
            String hashedPassword = BCrypt.hashpw(userDto.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);//need to change adding bCrypt password//done
            UserProfile userProfile = new UserProfile();
            userProfile.setFirstName(userDto.getFirstName());
            userProfile.setLastName(userDto.getLastName());
            user.setUserProfile(userProfile);

            userRepository.save(user);

            return Response.builder()
                    .responseMessage("User created successfully")
                    .responseCode("201")
                    .build();
        } else {
            log.error("Failed to create user in Keycloak: {}", keycloakResponse.getResponseMessage());
            throw new RuntimeException("Failed to create user: " + keycloakResponse.getResponseMessage());
        }
    }
    /**
     * Retrieves all users and their corresponding details.
     *
     * @return a list of UserDto objects containing the user information
     */
    @Override
    public List<UserDto> readAllUsers() {
        List<User> users = userRepository.findAll();

        // Get user representations from Keycloak
        List<UserRepresentation> userRepresentations = keycloakService.readUsers(users.stream()
                .map(User::getAuthId)
                .collect(Collectors.toList()));

        // Create a map of user representations for quick lookup
        Map<String, UserRepresentation> userRepMap = userRepresentations.stream()
                .collect(Collectors.toMap(UserRepresentation::getId, Function.identity()));

        return users.stream()
                .map(user -> {
                    UserDto userDto = userMapper.convertToDto(user);
                    UserRepresentation userRep = userRepMap.get(user.getAuthId());

                    // Check if userRep is not null before accessing its properties
                    if (userRep != null) {
                        userDto.setEmailId(userRep.getEmail());
                        // Set other properties from userRep if needed
                    }

                    return userDto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Reads a user from the database using the provided authId.
     *
     * @param authId the authentication id of the user
     * @return the UserDto object representing the user
     * @throws ResourceNotFound if the user is not found on the server
     */
    @Override
    public UserDto readUser(String authId) {

        User user = userRepository.findUserByAuthId(authId).
                orElseThrow(() -> new ResourceNotFound("User not found on the server"));

        UserRepresentation userRepresentation = keycloakService.readUser(authId);
        UserDto userDto = userMapper.convertToDto(user);
        userDto.setEmailId(userRepresentation.getEmail());
        return userDto;
    }

    /**
     * Updates the status of a user.
     *
     * @param id The ID of the user.
     * @param userUpdate The updated user status.
     * @return The response indicating the success of the update.
     * @throws ResourceNotFound If the user is not found.
     * @throws EmptyFields If the user has empty fields.
     */
    @Override
    public Response updateUserStatus(Long id, UserUpdateStatus userUpdate) {

        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFound("User not found on the server"));

        if (FieldChecker.hasEmptyFields(user)) {
            log.error("User is not updated completely");
            throw new EmptyFields("please updated the user", responseCodeNotFound);
        }

        if(userUpdate.getStatus().equals(Status.APPROVED)){
            UserRepresentation userRepresentation = keycloakService.readUser(user.getAuthId());
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(true);
            keycloakService.updateUser(userRepresentation);
        }

        user.setStatus(userUpdate.getStatus());
        userRepository.save(user);

        return Response.builder()
                .responseMessage("User updated successfully")
                .responseCode(responseCodeSuccess).build();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the UserDto object representing the user
     * @throws ResourceNotFound if the user is not found
     */
    @Override
    public UserDto readUserById(Long userId) {

        return userRepository.findById(userId)
                .map(user -> userMapper.convertToDto(user))
                .orElseThrow(() -> new ResourceNotFound("User not found on the server"));
    }

    /**
     * Updates a user with the given ID.
     *
     * @param id The ID of the user to update.
     * @param userUpdate The updated information for the user.
     * @return The response indicating the success or failure of the update operation.
     * @throws ResourceNotFound if the user with the given ID is not found.
     */
    @Override
    public Response updateUser(Long id, UserUpdate userUpdate) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User not found on the server"));

        user.setContactNo(userUpdate.getContactNo());
        BeanUtils.copyProperties(userUpdate, user.getUserProfile());
        userRepository.save(user);

        return Response.builder()
                .responseCode(responseCodeSuccess)
                .responseMessage("user updated successfully").build();
    }

    /**
     * Retrieves a UserDto by the given accountId.
     *
     * @param accountId The account ID of the user.
     * @return The UserDto object corresponding to the given accountId.
     * @throws ResourceNotFound If the account or user is not found on the server.
     */
    @Override
    public UserDto readUserByAccountId(String accountId) {

        ResponseEntity<Account> response = accountService.readByAccountNumber(accountId);
        if(Objects.isNull(response.getBody())){
            throw new ResourceNotFound("account not found on the server");
        }
        Long userId = response.getBody().getUserId();
        return userRepository.findById(userId)
                .map(user -> userMapper.convertToDto(user))
                .orElseThrow(() -> new ResourceNotFound("User not found on the server"));
    }
}
