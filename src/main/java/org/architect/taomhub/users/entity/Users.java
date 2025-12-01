package org.architect.taomhub.users.entity;

import java.time.LocalDateTime;

public class Users {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
