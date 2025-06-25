package it.andrea.start.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.security.service.JWTokenUserDetails;

public interface UserService {

    UserDTO getByUsername(String username);

    UserDTO getById(Long id);

    UserDTO whoami(JWTokenUserDetails jWTokenUserDetails);

    UserDTO create(UserDTO userDTO, JWTokenUserDetails userDetails);

    UserDTO update(UserDTO userDTO, JWTokenUserDetails userDetails);

    void deactivate(Long id, JWTokenUserDetails userDetails);

    Page<UserDTO> list(UserSearchCriteria criteria, Pageable pageable, JWTokenUserDetails userDetails);

    void changeMyPassword(ChangePassword changePassword, JWTokenUserDetails userDetails);

    void changePassword(Long userId, ChangePassword changePassword, JWTokenUserDetails userDetails);

}
