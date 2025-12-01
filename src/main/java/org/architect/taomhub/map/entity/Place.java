package org.architect.taomhub.map.entity;

import java.time.LocalDateTime;

public class Place {
    private Long id;
    private Long placeId;
    private String name;
    private Double latitude;
    private Double longitude;
    private PlaceType type;
    private LocalDateTime cachedAt;
}
