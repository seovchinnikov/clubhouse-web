package edu.clubhouseapi.roles;

public enum UserRole {

    ROLE_USER("ROLE_USER"), ROLE_ACTIVE("ROLE_ACTIVE"), ROLE_NOWAIT("ROLE_NOWAIT"), ROLE_BOARDED("ROLE_BOARDED");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static UserRole fromName(String name) {
        for (final UserRole value : values()) {
            if (value.roleName.equals(name)) {
                return value;
            }
        }

        throw new RuntimeException("Unsupported role");
    }
}