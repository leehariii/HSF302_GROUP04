package com.fptu.forum.enums;

/**
 * Cac hanh dong duoc ghi vao audit_logs.
 * Dung cho Moderator va Admin.
 */
public enum AuditAction {
    // Post actions
    HIDE_POST,
    DELETE_POST,
    PIN_POST,
    UNPIN_POST,

    // Comment actions
    DELETE_COMMENT,

    // Report actions
    RESOLVE_REPORT,
    REJECT_REPORT,

    // User restriction actions
    MUTE_POST,
    MUTE_COMMENT,
    UNMUTE_USER,

    // Admin user management
    BAN_USER,
    UNBAN_USER,
    PROMOTE_MODERATOR,
    DEMOTE_MODERATOR,

    // Topic management
    CREATE_TOPIC,
    UPDATE_TOPIC,
    DELETE_TOPIC
}
