package org.architect.taomhub.media.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private MediaOwnerType ownerType;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String url;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;
    
    @Column(name = "file_size")
    private Long size;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
