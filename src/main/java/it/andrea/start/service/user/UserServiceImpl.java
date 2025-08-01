package it.andrea.start.service.user;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.andrea.start.constants.RoleType;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.error.exception.BusinessException;
import it.andrea.start.error.exception.ErrorCode;
import it.andrea.start.error.exception.user.UserNotFoundException;
import it.andrea.start.mappers.user.UserMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.repository.user.UserRepository;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.searchcriteria.user.UserSearchSpecification;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.utils.HelperAuthorization;
import it.andrea.start.validator.user.UserValidator;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username) //
                .orElseThrow(() -> new UserNotFoundException(username));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
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
    public Page<UserDTO> list(UserSearchCriteria criteria, Pageable pageable, JWTokenUserDetails userDetails) {
        final Page<User> userPage = userRepository.findAll(new UserSearchSpecification(criteria), pageable);
        return userPage.map(userMapper::toDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO create(UserDTO userDTO, JWTokenUserDetails userDetails) {
        userValidator.validateUser(userDTO, HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN));

        User user = new User();
        userMapper.toEntity(userDTO, user);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO update(UserDTO userDTO, JWTokenUserDetails userDetails) {
        String username = userDTO.getUsername();
        User user = userRepository.findByUsername(username) //
                .orElseThrow(() -> new UserNotFoundException(username));

        boolean isMyProfile = Objects.equals(user.getUsername(), userDetails.getUsername());
        boolean isAdmin = HelperAuthorization.hasRole(userDetails.getAuthorities(), RoleType.ROLE_ADMIN);
        userValidator.validateUserUpdate(userDTO, user, isAdmin, isMyProfile);

        userMapper.toEntity(userDTO, user);
        userRepository.save(user);

        return this.userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long id, JWTokenUserDetails userDetails) {
        User user = userRepository.findById(id) //
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isAdmin = HelperAuthorization.hasRole(user.getRoles(), RoleType.ROLE_ADMIN);
        if (isAdmin) {
            throw new BusinessException(ErrorCode.USER_ROLE_ADMIN_NOT_DELETE);
        }

        boolean isManager = HelperAuthorization.hasRole(user.getRoles(), RoleType.ROLE_MANAGER);
        if (isManager) {
            throw new BusinessException(ErrorCode.USER_ROLE_MANAGER_NOT_DELETE);
        }

        user.setUserStatus(UserStatus.DEACTIVATE);

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String newPassword, String repeatPassword, JWTokenUserDetails userDetails) {
        User user = userRepository.findById(userId) //
                .orElseThrow(() -> new UserNotFoundException(userId));

        userValidator.checkPassword(newPassword, repeatPassword);
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeMyPassword(String newPassword, String repeatPassword, JWTokenUserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()) //
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        userValidator.checkPassword(newPassword, repeatPassword);

        String passwordCrypt = passwordEncoder.encode(newPassword);
        user.setPassword(passwordCrypt);

        userRepository.save(user);
    }

}
