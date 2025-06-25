package it.andrea.start.mappers.user;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.andrea.start.constants.Language;
import it.andrea.start.constants.RoleType;
import it.andrea.start.dto.user.UserDTO;
import it.andrea.start.error.exception.mapping.MappingToDtoException;
import it.andrea.start.error.exception.user.UserRoleNotFoundException;
import it.andrea.start.mappers.AbstractMapper;
import it.andrea.start.models.user.User;
import it.andrea.start.models.user.UserRole;
import it.andrea.start.repository.user.UserRoleRepository;
import jakarta.persistence.EntityManager;

@Component
public class UserMapper extends AbstractMapper<UserDTO, User> {

    private final UserRoleRepository userRoleRepository;

    public UserMapper(final EntityManager entityManager, final UserRoleRepository userRoleRepository) {
        super(entityManager);
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDTO toDto(User entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setUserStatus(entity.getUserStatus());
        dto.setLanguage(entity.getLanguageDefault().getBcp47Tag());
        
        Set<UserRole> rolesEntity = entity.getRoles();
        if (CollectionUtils.isEmpty(rolesEntity)) {
            dto.setRoles(Collections.emptySet());
        } else {
            try {
                // @formatter:off
                Set<RoleType> roleTypes = rolesEntity.stream()
                        .filter(Objects::nonNull)
                        .map(userRole -> Objects.requireNonNull(userRole.getRole(), "User role contains null role type enum"))
                        .collect(Collectors.toUnmodifiableSet());
                // @formatter:on
                
                dto.setRoles(roleTypes);
            } catch (NullPointerException e) {
                throw new MappingToDtoException("Error mapping roles for user: " + entity.getId() + ". Null value encountered in roles collection.", e);
            }
        }

        return dto;
    }

    @Override
    public void toEntity(UserDTO dto, User entity) {
        Objects.requireNonNull(dto, "Input DTO cannot be null for mapping to Entity");
        Objects.requireNonNull(entity, "Input entity cannot be null for mapping to Entity");

        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername() != null ? dto.getUsername().toUpperCase() : null);
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setUserStatus(dto.getUserStatus());
        entity.setLanguageDefault(Language.fromTag(dto.getLanguage()).orElse(Language.getDefault()));

        // @formatter:off
        Set<UserRole> userRoles = dto.getRoles()
                .stream()
                .map(roleType -> userRoleRepository.findByRole(roleType)
                        .orElseThrow(() -> new UserRoleNotFoundException(roleType)))
                .collect(Collectors.toSet());
        // @formatter:on
        
        entity.setRoles(userRoles);
    }

}
