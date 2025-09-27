package com.github.fjbaldon.attendex.backend.model;

public enum Permission {
    // Organization-level permissions
    MANAGE_ORGANIZATION, // Ability to edit organization name, settings, etc.
    MANAGE_ROLES, // Ability to create, edit, and delete roles and their permissions.
    MANAGE_USERS, // Ability to invite, assign roles to, and remove organizers.
    MANAGE_SCANNERS, // Ability to create and delete scanner accounts.

    // Feature-level permissions
    MANAGE_EVENTS,       // CRUD operations on events.
    MANAGE_ATTENDEES,    // CRUD and import operations for attendees.
    VIEW_REPORTS,        // Access to event analytics and reports.
    SCAN_ATTENDANCE      // Permission for the mobile app to sync data.
}
