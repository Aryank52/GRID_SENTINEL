package com.gridsentinel.auth;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * System user with authentication and authorization.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId id;

    @NotNull
    @Size(max = 100)
    @Setter(AccessLevel.PRIVATE)
    private String username;

    @NotNull
    @Email
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String email;

    @NotNull
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String passwordHash; // BCrypt or Argon2

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String fullName;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private Set<Role> roles = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    private Boolean enabled = true;

    @Setter(AccessLevel.PRIVATE)
    private Boolean mfaEnabled = false;

    @Setter(AccessLevel.PRIVATE)
    private String mfaSecret;

    @Setter(AccessLevel.PRIVATE)
    private Instant lastLoginAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant passwordChangedAt;

    @Setter(AccessLevel.PRIVATE)
    private Integer failedLoginAttempts = 0;

    @Setter(AccessLevel.PRIVATE)
    private Instant lockedUntil;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private User(Builder builder) {
        this.id = builder.id;
        this.username = Objects.requireNonNull(builder.username, "Username cannot be null").toLowerCase();
        this.email = Objects.requireNonNull(builder.email, "Email cannot be null").toLowerCase();
        this.passwordHash = Objects.requireNonNull(builder.passwordHash, "Password hash cannot be null");
        this.fullName = Objects.requireNonNull(builder.fullName, "Full name cannot be null");
        this.roles = builder.roles != null ? Set.copyOf(builder.roles) : Set.of(Role.OPERATOR);
        this.enabled = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    public void addRole(Role role) {
        roles.add(Objects.requireNonNull(role));
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void removeRole(Role role) {
        roles.remove(role);
        this.updatedAt = Instant.now();
        this.version++;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(Set<Role> roles) {
        return this.roles.stream().anyMatch(roles::contains);
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = Instant.now().plusSeconds(900); // 15 min lockout
        }
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
        this.passwordChangedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void enableMfa(String secret) {
        this.mfaEnabled = true;
        this.mfaSecret = Objects.requireNonNull(secret);
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void disableMfa() {
        this.mfaEnabled = false;
        this.mfaSecret = null;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public boolean isEnabled() {
        return enabled && !isAccountLocked();
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Role {
        // Grid operations
        OPERATOR,           // Field operator
        DISPATCHER,         // Grid dispatcher
        INCIDENT_COMMANDER, // Incident command
        RECOVERY_MANAGER,   // Recovery coordination

        // Planning & Analysis
        RISK_ANALYST,       // Risk assessment
        PLANNER,            // Protection/response planning
        SIMULATION_ENGINEER,// Simulation execution

        // Management
        SUPERVISOR,         // Shift supervisor
        MANAGER,            // Department manager
        DIRECTOR,           // Director level

        // Security & Admin
        SECURITY_ANALYST,   // Physical/cyber security
        AUDITOR,            // Compliance audit
        ADMINISTRATOR,      // System administration

        // Read-only
        VIEWER,             // Read-only access
        EXECUTIVE_VIEWER    // Executive dashboard
    }

    public static final class Builder {
        private CommonKernel.UserId id = CommonKernel.UserId.generate();
        private String username;
        private String email;
        private String passwordHash;
        private String fullName;
        private Set<Role> roles = new HashSet<>();

        private Builder() {}

        public Builder id(CommonKernel.UserId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder addRole(Role role) {
            this.roles.add(Objects.requireNonNull(role));
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}