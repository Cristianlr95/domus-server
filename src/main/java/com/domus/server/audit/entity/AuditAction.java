package com.domus.server.audit.entity;

public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    STATUS_CHANGE,
    LOGIN,
    LOGOUT,
    DELIVERY,
    VISIT_CHECKIN,
    VISIT_CHECKOUT,
    MESSAGE_SENT
}
