package org.architect.taomhub.menu.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MenuItem {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategory category;
    private List<Long> mediaIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
