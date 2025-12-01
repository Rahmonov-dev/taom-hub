package org.architect.taomhub.media.entity;

import java.time.LocalDateTime;

public class Media {
    private Long id;
    private MediaOwnerType owner;
    private Long ownerId;
    private String url;
    private MediaType type;
    private Long size;
    private LocalDateTime createdAt;
}
