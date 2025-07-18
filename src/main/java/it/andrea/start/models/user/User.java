package it.andrea.start.models.user;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import it.andrea.start.constants.Language;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Setter
@Entity
// @formatter:off
@Table(
        name = "users", 
        indexes = { 
                @Index(name = "IDX_USER_USERNAME", columnList = "username"), 
                @Index(name = "IDX_USER_EMAIL", columnList = "email"), 
                @Index(name = "IDX_USER_STATUS", columnList = "username, userStatus") 
                }
        )
// @formatter:
public class User extends BaseEntity implements UserDetails {

    @Serial
    private static final long serialVersionUID = 8219540355116099903L;

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ")
    @SequenceGenerator(name = "USER_SEQ", sequenceName = "USER_SEQUENCE", allocationSize = 1)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String username;

    @Getter
    @Column(nullable = false)
    private String password;

    @Getter
    @Column(nullable = false)
    private String name;

    @Getter
    @Email
    @Column(nullable = false)
    private String email;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @Getter
    @Column(nullable = false)
    private Language languageDefault;

    @Getter
    @ManyToMany(fetch = FetchType.EAGER)
    // @formatter:off
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    // @formatter:on
    private Set<UserRole> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // @formatter:off
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole().name()))
                .toList();
        // @formatter:on
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return userStatus != UserStatus.EXPIRED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userStatus != UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return userStatus == UserStatus.ACTIVE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(email, id, username);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        return Objects.equals(email, other.email) && Objects.equals(id, other.id) && Objects.equals(username, other.username);
    }

}
