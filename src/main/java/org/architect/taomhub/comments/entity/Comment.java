package org.architect.taomhub.comments.entity;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private CommentEntityType entityType;
    private Long entityId;
    private Long userId;
    private String text;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
