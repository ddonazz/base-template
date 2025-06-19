package it.andrea.start.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.andrea.start.annotation.Audit;
import it.andrea.start.constants.AuditActivity;
import it.andrea.start.constants.AuditTypeOperation;
import it.andrea.start.controller.response.TokenResponse;
import it.andrea.start.controller.types.LoginRequest;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.security.jwt.JwtUtils;
import it.andrea.start.security.service.JWTokenUserDetails;
import it.andrea.start.service.user.UserService;

@Tag(name = "Authorize API")
@RestController
@RequestMapping("/api/authorize")
public class AuthorizeController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthorizeController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super();
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    // @formatter:off
    @Operation(
        method = "POST",
        description = "Login user",
        summary = "Login user"
    )
    // @formatter:on
    @Audit(activity = AuditActivity.USER_OPERATION,type = AuditTypeOperation.LOGIN)
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authorize(@RequestBody @Validated LoginRequest userAndPassword) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userAndPassword.getUsername(), userAndPassword.getPassword());
        authentication = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateToken(authentication);

        return ResponseEntity.ok(new TokenResponse(jwt));
    }
    
    // @formatter:off
    @Operation(
        method = "GET",
        description = "Information current user",
        summary = "Information current user"
    )
    // @formatter:on
    @GetMapping("/who-am-i")
    public ResponseEntity<UserDTO> whoami(@AuthenticationPrincipal JWTokenUserDetails userDetails) {
        return ResponseEntity.ok(userService.whoami(userDetails));
    }

}
