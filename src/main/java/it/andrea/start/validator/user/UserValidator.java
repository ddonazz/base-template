package it.andrea.start.validator.user;

import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Component;

import it.andrea.start.constants.RoleType;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.error.exception.BusinessException;
import it.andrea.start.error.exception.ErrorCode;
import it.andrea.start.error.exception.user.UserAlreadyExistsException;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    public void checkPassword(ChangePassword changePassword) throws BusinessException {
        if (!Objects.equals(changePassword.getNewPassword(), changePassword.getRepeatPassword())) {
            throw new BusinessException(ErrorCode.USER_REPEAT_PASSWORD_NOT_EQUAL);
        }
    }

    public void validateUser(UserDTO dto, boolean haveAdminRole) {
        String username = dto.getUsername();
        userRepository.findByUsername(username.toUpperCase())
        .ifPresent(user -> {
            throw new UserAlreadyExistsException(username);
        });
        
        String email = dto.getEmail();
        userRepository.findByEmail(email)
        .ifPresent(user -> {
            throw new UserAlreadyExistsException(email);
        });

        Collection<RoleType> roles = dto.getRoles();
        roles.stream()
        .filter(role -> Objects.equals(role, RoleType.ROLE_ADMIN))
        .findFirst()
        .ifPresent(adminRole -> {
            throw new BusinessException(ErrorCode.USER_ROLE_ADMIN_NOT_USABLE);
        });
        
        roles.stream()
        .filter(role -> Objects.equals(role, RoleType.ROLE_MANAGER))
        .findFirst()
        .filter(managerRole -> !haveAdminRole) 
        .ifPresent(filteredManagerRole -> { 
            throw new BusinessException(ErrorCode.USER_ROLE_MANAGER_NOT_USABLE);
        });
    }

    public void validateUserUpdate(UserDTO dto, User entity, boolean haveAdminRole, boolean isMyProfile) {
        String email = dto.getEmail();

        userRepository.findByEmail(email)
        .filter(otherUser -> !otherUser.getId().equals(entity.getId()))
        .ifPresent(conflictingUser -> {
            throw new UserAlreadyExistsException(email);
        });

        Collection<RoleType> roles = dto.getRoles();
        roles.stream()
        .filter(role -> Objects.equals(role, RoleType.ROLE_ADMIN))
        .findFirst()
        .ifPresent(adminRole -> {
            throw new BusinessException(ErrorCode.USER_ROLE_ADMIN_NOT_USABLE);
        });

        if (!isMyProfile) {
            roles.stream()
            .filter(role -> Objects.equals(role, RoleType.ROLE_MANAGER))
            .findFirst()
            .filter(managerRole -> !haveAdminRole) 
            .ifPresent(filteredManagerRole -> { 
                throw new BusinessException(ErrorCode.USER_ROLE_MANAGER_NOT_USABLE);
            });
        }
    }

}
