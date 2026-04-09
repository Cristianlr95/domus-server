package com.domus.server.user.entity;

import java.util.Arrays;

public enum PermissionCode {
    VISITS_READ("visits.read"),
    VISITS_CREATE("visits.create"),
    VISITS_UPDATE("visits.update"),
    PACKAGES_READ("packages.read"),
    PACKAGES_CREATE("packages.create"),
    PACKAGES_UPDATE("packages.update"),
    RESIDENTS_READ("residents.read"),
    RESIDENTS_MANAGE("residents.manage"),
    UNITS_READ("units.read"),
    UNITS_MANAGE("units.manage"),
    PARKING_READ("parking.read"),
    PARKING_MANAGE("parking.manage"),
    STORAGES_READ("storages.read"),
    STORAGES_MANAGE("storages.manage"),
    MESSAGING_READ("messaging.read"),
    MESSAGING_CREATE("messaging.create"),
    NOTIFICATIONS_READ("notifications.read"),
    CONCIERGE_DASHBOARD_READ("concierge.dashboard.read"),
    USERS_READ("users.read"),
    USERS_MANAGE("users.manage"),
    ROLES_READ("roles.read"),
    PERMISSIONS_READ("permissions.read"),
    ADMIN_DASHBOARD_READ("admin.dashboard.read"),
    AUDIT_READ("audit.read");

    private final String code;

    PermissionCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static PermissionCode fromCode(String code) {
        return Arrays.stream(values())
            .filter(value -> value.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown permission code: " + code));
    }
}
