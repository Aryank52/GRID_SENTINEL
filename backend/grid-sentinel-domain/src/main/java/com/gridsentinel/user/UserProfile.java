package com.gridsentinel.user;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Extended user profile with preferences, qualifications, and assignments.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId id; // Same as User ID

    @Setter(AccessLevel.PRIVATE)
    private String phoneNumber;

    @Setter(AccessLevel.PRIVATE)
    private String radioCallSign;

    @Setter(AccessLevel.PRIVATE)
    private String employeeId;

    @Setter(AccessLevel.PRIVATE)
    private String department;

    @Setter(AccessLevel.PRIVATE)
    private String title;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId supervisorId;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> certifications = new HashSet<>();

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> qualifications = new HashSet<>(); // e.g., "HV_SWITCHING", "INCIDENT_COMMAND"

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> preferences = new HashMap<>(); // UI prefs, notification settings

    @Setter(AccessLevel.PRIVATE)
    private String timezone = "UTC";

    @Setter(AccessLevel.PRIVATE)
    private String locale = "en";

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private UserProfile(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "User ID cannot be null");
        this.phoneNumber = builder.phoneNumber;
        this.radioCallSign = builder.radioCallSign;
        this.employeeId = builder.employeeId;
        this.department = builder.department;
        this.title = builder.title;
        this.supervisorId = builder.supervisorId;
        this.certifications = builder.certifications != null ? Set.copyOf(builder.certifications) : Set.of();
        this.qualifications = builder.qualifications != null ? Set.copyOf(builder.qualifications) : Set.of();
        this.preferences = builder.preferences != null ? Map.copyOf(builder.preferences) : Map.of();
        this.timezone = builder.timezone != null ? builder.timezone : "UTC";
        this.locale = builder.locale != null ? builder.locale : "en";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    public void addCertification(String certification) {
        certifications.add(Objects.requireNonNull(certification));
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void addQualification(String qualification) {
        qualifications.add(Objects.requireNonNull(qualification));
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void setPreference(String key, String value) {
        preferences.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
        this.updatedAt = Instant.now();
        this.version++;
    }

    public String getPreference(String key) {
        return preferences.get(key);
    }

    public boolean hasQualification(String qualification) {
        return qualifications.contains(qualification);
    }

    public Set<String> getCertifications() {
        return Collections.unmodifiableSet(certifications);
    }

    public Set<String> getQualifications() {
        return Collections.unmodifiableSet(qualifications);
    }

    public Map<String, String> getPreferences() {
        return Collections.unmodifiableMap(preferences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.UserId id;
        private String phoneNumber;
        private String radioCallSign;
        private String employeeId;
        private String department;
        private String title;
        private CommonKernel.UserId supervisorId;
        private Set<String> certifications = new HashSet<>();
        private Set<String> qualifications = new HashSet<>();
        private Map<String, String> preferences = new HashMap<>();
        private String timezone;
        private String locale;

        private Builder() {}

        public Builder id(CommonKernel.UserId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder radioCallSign(String radioCallSign) {
            this.radioCallSign = radioCallSign;
            return this;
        }

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder supervisorId(CommonKernel.UserId supervisorId) {
            this.supervisorId = supervisorId;
            return this;
        }

        public Builder addCertification(String certification) {
            this.certifications.add(Objects.requireNonNull(certification));
            return this;
        }

        public Builder addQualification(String qualification) {
            this.qualifications.add(Objects.requireNonNull(qualification));
            return this;
        }

        public Builder setPreference(String key, String value) {
            this.preferences.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(this);
        }
    }
}