package com.ssccgl.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_user_email",  columnList = "email"),
        @Index(name = "idx_user_mobile", columnList = "mobile_number")
    })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "is_email_verified")
    private boolean emailVerified = false;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────
    public User() {}

    // ── Getters ───────────────────────────────────────────
    public Long getId()                        { return id; }
    public String getFirstName()               { return firstName; }
    public String getLastName()                { return lastName; }
    public String getEmail()                   { return email; }
    public String getMobileNumber()            { return mobileNumber; }
    public String getPassword()                { return password; }
    public boolean isActive()                  { return active; }
    public boolean isEmailVerified()           { return emailVerified; }
    public String getPasswordResetToken()      { return passwordResetToken; }
    public LocalDateTime getPasswordResetExpiry() { return passwordResetExpiry; }
    public Set<Role> getRoles()                { return roles; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }
    public String getFullName()                { return firstName + " " + lastName; }

    // ── Setters ───────────────────────────────────────────
    public void setId(Long id)                              { this.id = id; }
    public void setFirstName(String firstName)              { this.firstName = firstName; }
    public void setLastName(String lastName)                { this.lastName = lastName; }
    public void setEmail(String email)                      { this.email = email; }
    public void setMobileNumber(String mobileNumber)        { this.mobileNumber = mobileNumber; }
    public void setPassword(String password)                { this.password = password; }
    public void setActive(boolean active)                   { this.active = active; }
    public void setEmailVerified(boolean emailVerified)     { this.emailVerified = emailVerified; }
    public void setPasswordResetToken(String token)         { this.passwordResetToken = token; }
    public void setPasswordResetExpiry(LocalDateTime expiry){ this.passwordResetExpiry = expiry; }
    public void setRoles(Set<Role> roles)                   { this.roles = roles; }

    // ── Builder ───────────────────────────────────────────
    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private final User u = new User();
        public UserBuilder firstName(String v)      { u.firstName = v;      return this; }
        public UserBuilder lastName(String v)       { u.lastName = v;       return this; }
        public UserBuilder email(String v)          { u.email = v;          return this; }
        public UserBuilder mobileNumber(String v)   { u.mobileNumber = v;   return this; }
        public UserBuilder password(String v)       { u.password = v;       return this; }
        public UserBuilder active(boolean v)        { u.active = v;         return this; }
        public UserBuilder emailVerified(boolean v) { u.emailVerified = v;  return this; }
        public UserBuilder roles(Set<Role> v)       { u.roles = v;          return this; }
        public User build()                         { return u; }
    }
}
