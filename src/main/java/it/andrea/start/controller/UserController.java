package it.andrea.start.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.andrea.start.annotation.Audit;
import it.andrea.start.constants.AuditActivity;
import it.andrea.start.constants.AuditTypeOperation;
import it.andrea.start.controller.types.ChangePassword;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.searchcriteria.user.UserSearchCriteria;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.service.user.UserService;
import it.andrea.start.validator.OnCreate;

@Tag(name = "User API", description = "API for user CRUD operations")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        super();
        this.userService = userService;
    }

    // @formatter:off
    @Operation(
        description = "Aggiungi un utente da ADMIN o MANAGER",
        summary = "Aggiungi un utente da ADMIN o MANAGER"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.CREATE)
    public ResponseEntity<UserDTO> createUser(
            @RequestBody @Validated(OnCreate.class) UserDTO userDTO, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {

        return ResponseEntity.ok(userService.createUser(userDTO, userDetails));
    }

    // @formatter:off
    @Operation(
        method = "PUT",
        description = "Aggiorna un utente da ADMIN o MANAGER",
        summary = "Aggiorna un utente da ADMIN o MANAGER"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.UPDATE)
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id, //
            @RequestBody UserDTO userDTO, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {

        userDTO.setId(id);
        return ResponseEntity.ok(userService.updateUser(userDTO, userDetails));
    }

    // @formatter:off
    @Operation(
        description = "Elimina un utente da ADMIN o MANAGER",
        summary = "Elimina un utente da ADMIN o MANAGER"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.DELETE)
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {

        userService.deleteUser(id, userDetails);

        return ResponseEntity.ok().build();
    }

    // @formatter:off
    @Operation(
        description = "Informazioni di un utente",
        summary = "Informazioni di un utente"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    @Audit(activity = AuditActivity.USER_OPERATION, type = AuditTypeOperation.GET_INFO)
    public ResponseEntity<UserDTO> infoUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    // @formatter:off
    @Operation(
        description = "Lista degli utenti",
        summary = "Lista degli utenti"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    @GetMapping("/list")
    public ResponseEntity<Page<UserDTO>> listUser(
            UserSearchCriteria searchCriteria, //
            Pageable pageable, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {

        Page<UserDTO> users = userService.listUser(searchCriteria, pageable, userDetails);

        return ResponseEntity.ok(users);
    }

    // @formatter:off
    @Operation(
        description = "Cambio password da ADMIN",
        summary = "Cambio password da ADMIN"
    )
    // @formatter:on
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-password/{userId}")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId, //
            @RequestBody ChangePassword changePassword, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {

        userService.changePasswordForAdmin(userId, changePassword, userDetails);

        return ResponseEntity.ok().build();
    }

    // @formatter:off
    @Operation(
        description = "User self change password",
        summary = "User self change password"
    )
    // @formatter:on
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    @PostMapping("/change-my-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePassword changePassword, //
            @AuthenticationPrincipal JWTokenUserDetails userDetails) {
        
        userService.changeMyPassword(changePassword, userDetails);

        return ResponseEntity.ok().build();
    }

}
