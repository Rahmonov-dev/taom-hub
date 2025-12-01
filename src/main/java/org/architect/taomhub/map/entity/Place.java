package org.architect.taomhub.map.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "place_id", unique = true)
    private Long placeId; // Yandex Maps place ID
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceType type;
    
    @Column(name = "restaurant_id", unique = true)
    private Long restaurantId; // Agar bu joyda restaurant ro'yxatdan o'tgan bo'lsa
    
    @Column(name = "cached_at")
    private LocalDateTime cachedAt;
    
    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
    }
}
