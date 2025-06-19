package it.andrea.start.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.andrea.start.constants.RoleType;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.error.exception.BusinessException;
import it.andrea.start.error.exception.ErrorCode;
import it.andrea.start.error.exception.user.UserNotFoundException;
import it.andrea.start.mappers.user.UserMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.searchcriteria.user.UserSearchSpecification;
import it.andrea.start.security.EncrypterManager;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.utils.HelperAuthorization;
import it.andrea.start.validator.user.UserValidator;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final EncrypterManager encrypterManager;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(String username) {
        User user = userRepository.findByUsername(username) //
                .orElseThrow(() -> new UserNotFoundException(username));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id) //
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO whoami(JWTokenUserDetails jWTokenUserDetails) {
        String username = jWTokenUserDetails.getUsername();
        User user = userRepository.findByUsername(username) //
                .orElseThrow(() -> new UserNotFoundException(username));
        
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> listUser(UserSearchCriteria criteria, Pageable pageable, JWTokenUserDetails userDetails) {
        final Page<User> userPage = userRepository.findAll(new UserSearchSpecification(criteria), pageable);
        return userPage.map(userMapper::toDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO userDTO, JWTokenUserDetails userDetails) {
        userValidator.validateUser(userDTO, HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN), true);

        User user = new User();
        userMapper.toEntity(userDTO, user);
        user.setPassword(encrypterManager.encode(userDTO.getPassword()));

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateUser(UserDTO userDTO, JWTokenUserDetails userDetails) {
        String username = userDTO.getUsername();
        User user = userRepository.findByUsername(username) //
                .orElseThrow(() -> new UserNotFoundException(username));

        boolean isMyProfile = user.getUsername().compareTo(userDetails.getUsername()) == 0;

        boolean isAdmin = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);
        userValidator.validateUserUpdate(userDTO, user, isAdmin, isMyProfile);

        userMapper.toEntity(userDTO, user);
        userRepository.save(user);

        return this.userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, JWTokenUserDetails userDetails) {
        User user = userRepository.findById(id) //
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isAdmin = HelperAuthorization.hasRole(user.getRoles(), RoleType.ROLE_ADMIN);
        boolean isManager = HelperAuthorization.hasRole(user.getRoles(), RoleType.ROLE_MANAGER);
        if (isAdmin) {
            throw new BusinessException(ErrorCode.USER_ROLE_ADMIN_NOT_DELETE);
        }
        if (isManager) {
            throw new BusinessException(ErrorCode.USER_ROLE_MANAGER_NOT_DELETE);
        }

        user.setUserStatus(UserStatus.DEACTIVATE);

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePasswordForAdmin(Long userId, ChangePassword changePassword, JWTokenUserDetails userDetails) {
        User user = userRepository.findById(userId) //
                .orElseThrow(() -> new UserNotFoundException(userId));

        userValidator.checkPassword(changePassword);
        user.setPassword(encrypterManager.encode(changePassword.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeMyPassword(ChangePassword changePassword, JWTokenUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()) //
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        userValidator.checkPassword(changePassword);

        String passwordCrypt = encrypterManager.encode(changePassword.getNewPassword());
        user.setPassword(passwordCrypt);

        userRepository.save(user);
    }

}
