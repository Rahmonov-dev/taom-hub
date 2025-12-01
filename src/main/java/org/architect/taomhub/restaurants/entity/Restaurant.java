package org.architect.taomhub.restaurants.entity;

import java.time.LocalDateTime;

public class Restaurant {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private RestaurantCategory category;
    private Long ownerId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
