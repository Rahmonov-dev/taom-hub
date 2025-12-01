# TaomHub Platform - Texnik Zadaniya (MVP)

## Umumiy ma'lumot

**Platforma:** Map + Restaurant/Cafe/Bar birlashmasi  
**Arxitektura:** Modular Monolith (Spring Boot)  
**Maqsad:** MVP versiyasini ko'tarish

---

## 1. USERS MODULI

### 1.1 Entity
- **Users** - foydalanuvchilar
  - id, name, email, phoneNumber, password, role, createdAt, updatedAt
  - Role: ADMIN, USER, OWNER

### 1.2 Repository
- `UserRepository` - CRUD operations
- `findByEmail`, `findByPhoneNumber`

### 1.3 Service
- `UserService`
  - `register(UserRegistrationDto)` - ro'yxatdan o'tish
  - `login(LoginDto)` - kirish
  - `getUserById(Long id)` - foydalanuvchi ma'lumotlari
  - `updateProfile(Long userId, UserUpdateDto)` - profil yangilash
  - `changePassword(Long userId, String oldPassword, String newPassword)` - parol o'zgartirish

### 1.4 Controller
- `POST /api/users/register` - ro'yxatdan o'tish
- `POST /api/users/login` - kirish
- `GET /api/users/{id}` - foydalanuvchi ma'lumotlari
- `PUT /api/users/{id}` - profil yangilash
- `PUT /api/users/{id}/password` - parol o'zgartirish

### 1.5 DTO
- `UserRegistrationDto` - name, email, phoneNumber, password, role
- `LoginDto` - email, password
- `UserResponseDto` - id, name, email, phoneNumber, role (password yo'q)
- `UserUpdateDto` - name, phoneNumber

---

## 2. MAP MODULI

### 2.1 Entity
- **Place** - Yandex Maps dan kelgan joylar
  - id, placeId (Yandex ID), name, latitude, longitude, type, restaurantId, cachedAt
  - Type: RESTAURANT, CAFE, BAR, TEAHOUSE, COFFEE_SHOP

### 2.2 Repository
- `PlaceRepository`
  - `findByPlaceId` - Yandex ID bo'yicha
  - `findByRestaurantId` - Restaurant ID bo'yicha
  - `findNearbyPlaces(lat, lng, radius)` - atrofdagi joylar (Haversine formula)
  - `searchByName(query)` - nom bo'yicha qidirish

### 2.3 Service
- `PlaceService`
  - `getNearbyPlaces(Double lat, Double lng, Double radius)` - atrofdagi joylar
    - **Cache strategiya:**
      1. Redis dan qidirish: `nearby_places:{lat}:{lng}:{radius}` (TTL: 1 soat)
      2. Agar topilmasa → DB dan qidirish
      3. Agar DB da ham topilmasa → Yandex Maps API ga so'rov
      4. Natijani Redis ga saqlash
  - `searchPlaces(String query, Double lat, Double lng)` - qidirish
    - Birinchi DB dan qidirish
    - Agar topilmasa → Yandex Geocoder API
  - `getPlaceById(Long id)` - joy ma'lumotlari
  - `getPlaceByRestaurantId(Long restaurantId)` - restaurant bo'yicha joy
  - `syncPlacesFromYandex(Double lat, Double lng, Double radius)` - Yandex dan yangilash

### 2.4 Controller
- `GET /api/map/nearby?lat={lat}&lng={lng}&radius={radius}` - atrofdagi joylar
- `GET /api/map/search?query={query}&lat={lat}&lng={lng}` - qidirish
- `GET /api/map/places/{id}` - joy ma'lumotlari
- `GET /api/map/places/restaurant/{restaurantId}` - restaurant bo'yicha joy
- `POST /api/map/sync?lat={lat}&lng={lng}&radius={radius}` - Yandex dan yangilash (admin)

### 2.5 DTO
- `PlaceResponseDto` - id, placeId, name, latitude, longitude, type, restaurantId
- `NearbyPlacesRequestDto` - latitude, longitude, radius (km)
- `SearchPlacesRequestDto` - query, latitude, longitude

### 2.6 Yandex Maps Integration
- **YandexMapsService** (alohida service)
  - `searchNearby(Double lat, Double lng, Double radius, PlaceType type)` - atrofdagi joylar
  - `geocode(String address)` - address bo'yicha koordinatalar
  - `searchByQuery(String query, Double lat, Double lng)` - qidirish
  - Rate limiting: 25,000 so'rov/kun
  - Error handling: API xatosi bo'lsa, DB dan qaytarish

---

## 3. RESTAURANTS MODULI

### 3.1 Entity
- **Restaurant** - platformada ro'yxatdan o'tgan restoranlar
  - id, name, description, address, phoneNumber, category, ownerId, latitude, longitude, isActive, createdAt, updatedAt
  - Category: CAFE, RESTAURANT, BAR, TEAHOUSE, COFFEE_SHOP

### 3.2 Repository
- `RestaurantRepository`
  - `findByOwnerId` - owner bo'yicha
  - `findByCategory` - kategoriya bo'yicha
  - `findByIsActiveTrue` - faol restoranlar
  - `findNearbyRestaurants(lat, lng, radius)` - atrofdagi restoranlar
  - `search(query)` - qidirish (name, description, address)
  - `findByIdAndOwnerId` - owner tekshirish

### 3.3 Service
- `RestaurantService`
  - `createRestaurant(Long ownerId, RestaurantCreateDto, Long placeId)` - restoran yaratish
    - Owner tekshirish (role = OWNER)
    - Place.restaurantId ni yangilash
  - `updateRestaurant(Long id, Long ownerId, RestaurantUpdateDto)` - yangilash
    - Faqat owner o'z restoranini yangilay oladi
  - `getRestaurantById(Long id)` - restoran ma'lumotlari
  - `getRestaurantProfile(Long id)` - to'liq profil (menu, media, comments bilan)
  - `getNearbyRestaurants(Double lat, Double lng, Double radius)` - atrofdagi restoranlar
  - `searchRestaurants(String query)` - qidirish
  - `deactivateRestaurant(Long id, Long ownerId)` - o'chirish (isActive = false)
  - `getOwnerRestaurants(Long ownerId)` - owner restoranlari

### 3.4 Controller
- `POST /api/restaurants` - restoran yaratish (owner)
- `PUT /api/restaurants/{id}` - yangilash (owner)
- `GET /api/restaurants/{id}` - restoran ma'lumotlari
- `GET /api/restaurants/{id}/profile` - to'liq profil
- `GET /api/restaurants/nearby?lat={lat}&lng={lng}&radius={radius}` - atrofdagi restoranlar
- `GET /api/restaurants/search?query={query}` - qidirish
- `DELETE /api/restaurants/{id}` - o'chirish (owner)
- `GET /api/restaurants/owner/{ownerId}` - owner restoranlari

### 3.5 DTO
- `RestaurantCreateDto` - name, description, address, phoneNumber, category, latitude, longitude, placeId
- `RestaurantUpdateDto` - name, description, address, phoneNumber, category
- `RestaurantResponseDto` - id, name, description, address, phoneNumber, category, latitude, longitude, ownerId, isActive
- `RestaurantProfileDto` - RestaurantResponseDto + menuItems, media, comments

---

## 4. MENU MODULI

### 4.1 Entity
- **MenuItem** - restoran menyusi
  - id, restaurantId, name, description, price, category, isAvailable, createdAt, updatedAt
  - Category: DRINK, FOOD, DESSERT, SALAD, SNACK

### 4.2 Repository
- `MenuItemRepository`
  - `findByRestaurantId` - restoran bo'yicha
  - `findByRestaurantIdAndCategory` - restoran + kategoriya
  - `findByRestaurantIdAndIsAvailableTrue` - mavjud ovqatlar

### 4.3 Service
- `MenuItemService`
  - `createMenuItem(Long restaurantId, Long ownerId, MenuItemCreateDto)` - ovqat qo'shish
    - Owner tekshirish (restaurant owner bo'lishi kerak)
  - `updateMenuItem(Long id, Long ownerId, MenuItemUpdateDto)` - yangilash
  - `deleteMenuItem(Long id, Long ownerId)` - o'chirish
  - `getMenuItemById(Long id)` - ovqat ma'lumotlari (media bilan)
  - `getRestaurantMenu(Long restaurantId)` - restoran menyusi
  - `getRestaurantMenuByCategory(Long restaurantId, MenuCategory category)` - kategoriya bo'yicha
  - `toggleAvailability(Long id, Long ownerId)` - mavjud/yo'q holatini o'zgartirish

### 4.4 Controller
- `POST /api/menu` - ovqat qo'shish (owner)
- `PUT /api/menu/{id}` - yangilash (owner)
- `DELETE /api/menu/{id}` - o'chirish (owner)
- `GET /api/menu/{id}` - ovqat ma'lumotlari
- `GET /api/menu/restaurant/{restaurantId}` - restoran menyusi
- `GET /api/menu/restaurant/{restaurantId}/category/{category}` - kategoriya bo'yicha
- `PUT /api/menu/{id}/availability` - mavjud/yo'q (owner)

### 4.5 DTO
- `MenuItemCreateDto` - name, description, price, category
- `MenuItemUpdateDto` - name, description, price, category
- `MenuItemResponseDto` - id, restaurantId, name, description, price, category, isAvailable, mediaIds

---

## 5. MEDIA MODULI

### 5.1 Entity
- **Media** - rasm va videolar
  - id, ownerType, ownerId, url, type, size, createdAt
  - OwnerType: RESTAURANT, MENU_ITEM, COMMENT
  - Type: IMAGE, VIDEO

### 5.2 Repository
- `MediaRepository`
  - `findByOwnerTypeAndOwnerId` - owner bo'yicha
  - `findByOwnerTypeAndOwnerIdAndType` - owner + type

### 5.3 Service
- `MediaService`
  - `uploadMedia(MultipartFile file, MediaOwnerType ownerType, Long ownerId)` - media yuklash
    - File validation (format, size)
    - Cloud storage ga yuklash (yoki local)
    - DB ga saqlash
  - `deleteMedia(Long id, Long ownerId)` - o'chirish
    - Owner tekshirish
  - `getMediaByOwner(MediaOwnerType ownerType, Long ownerId)` - owner media
  - `getMediaById(Long id)` - media ma'lumotlari

### 5.4 Controller
- `POST /api/media/upload` - media yuklash
- `DELETE /api/media/{id}` - o'chirish
- `GET /api/media/{id}` - media ma'lumotlari
- `GET /api/media/owner?ownerType={type}&ownerId={id}` - owner media

### 5.5 DTO
- `MediaUploadDto` - file, ownerType, ownerId
- `MediaResponseDto` - id, ownerType, ownerId, url, type, size, createdAt

---

## 6. COMMENTS MODULI

### 6.1 Entity
- **Comment** - izohlar
  - id, entityType, entityId, userId, text, rating, createdAt, updatedAt
  - EntityType: RESTAURANT, MENU_ITEM, MEDIA

### 6.2 Repository
- `CommentRepository`
  - `findByEntityTypeAndEntityId` - entity bo'yicha
  - `findByUserId` - foydalanuvchi bo'yicha
  - `findByEntityTypeAndEntityIdOrderByCreatedAtDesc` - yangi izohlar birinchi

### 6.3 Service
- `CommentService`
  - `createComment(Long userId, CommentCreateDto)` - izoh yozish
  - `updateComment(Long id, Long userId, CommentUpdateDto)` - yangilash
    - Faqat o'z izohini yangilay oladi
  - `deleteComment(Long id, Long userId)` - o'chirish
  - `getCommentsByEntity(CommentEntityType entityType, Long entityId)` - entity izohlari
  - `getCommentById(Long id)` - izoh ma'lumotlari
  - `getAverageRating(CommentEntityType entityType, Long entityId)` - o'rtacha reyting

### 6.4 Controller
- `POST /api/comments` - izoh yozish
- `PUT /api/comments/{id}` - yangilash
- `DELETE /api/comments/{id}` - o'chirish
- `GET /api/comments/entity?entityType={type}&entityId={id}` - entity izohlari
- `GET /api/comments/{id}` - izoh ma'lumotlari
- `GET /api/comments/rating?entityType={type}&entityId={id}` - o'rtacha reyting

### 6.5 DTO
- `CommentCreateDto` - entityType, entityId, text, rating
- `CommentUpdateDto` - text, rating
- `CommentResponseDto` - id, entityType, entityId, userId, userName, text, rating, createdAt, updatedAt

---

## 7. CACHING STRATEGIYA (Redis)

### 7.1 Cache Keys
- `nearby_places:{lat}:{lng}:{radius}` - atrofdagi joylar (TTL: 1 soat)
- `restaurant:{id}` - restoran ma'lumotlari (TTL: 30 daqiqa)
- `restaurant_profile:{id}` - to'liq profil (TTL: 15 daqiqa)
- `menu:{restaurantId}` - restoran menyusi (TTL: 30 daqiqa)
- `search:{query}:{lat}:{lng}` - qidirish natijalari (TTL: 30 daqiqa)

### 7.2 Cache Invalidation
- Restaurant yangilanganda → `restaurant:{id}` va `restaurant_profile:{id}` invalidate
- MenuItem qo'shilganda/yangilanganda → `menu:{restaurantId}` invalidate
- Media yuklanganda → `restaurant_profile:{id}` invalidate
- Comment qo'shilganda → `restaurant_profile:{id}` invalidate

---

## 8. YANDEX MAPS API

### 8.1 Endpoints
- **Places API** - atrofdagi joylar
- **Geocoder API** - address → koordinatalar
- **Search API** - qidirish

### 8.2 Rate Limiting
- Limit: 25,000 so'rov/kun
- Fallback: Agar limit bo'lsa, faqat DB dan qidirish

### 8.3 Error Handling
- API xatosi bo'lsa → DB dan qaytarish
- Timeout bo'lsa → DB dan qaytarish
- Retry mechanism (3 marta)

---

## 9. DATA REFRESH STRATEGIYA

### 9.1 Scheduler
- **Har kuni kechasi (2:00 AM)** - yopilgan restoranlarni tekshirish
  - Yandex API dan place status
  - Agar yopilgan bo'lsa → `isActive = false`

### 9.2 Manual Refresh
- Admin tomonidan refresh
- Owner tomonidan o'z profilini yangilash

---

## 10. SECURITY

### 10.1 Authentication
- JWT token (keyinroq implement qilinadi MVP da)
- Hozircha: userId header orqali (development)

### 10.2 Authorization
- Owner faqat o'z restoranini boshqaradi
- Admin barcha restoranlarni ko'ra oladi
- User faqat ko'ra oladi

---

## 11. VALIDATION

### 11.1 User Input
- Email format
- Phone number format
- Password strength (min 8 character)
- Price > 0
- Rating 1-5
- File size limit (image: 5MB, video: 50MB)

---

## 12. ERROR HANDLING

### 12.1 Custom Exceptions
- `RestaurantNotFoundException`
- `PlaceNotFoundException`
- `MenuItemNotFoundException`
- `MediaNotFoundException`
- `CommentNotFoundException`
- `UserNotFoundException`
- `UnauthorizedException`
- `YandexApiException`

### 12.2 Global Exception Handler
- `@ControllerAdvice` - barcha exceptionlarni handle qilish
- User-friendly error messages

---

## 13. DATABASE

### 13.1 Tables
- users
- places
- restaurants
- menu_items
- media
- comments

### 13.2 Indexes
- `places(latitude, longitude)` - spatial search
- `places(restaurant_id)` - unique
- `restaurants(owner_id)`
- `restaurants(latitude, longitude)` - spatial search
- `menu_items(restaurant_id)`
- `media(owner_type, owner_id)`
- `comments(entity_type, entity_id)`

---

## 14. API DOCUMENTATION

### 14.1 Swagger/OpenAPI
- Barcha endpointlar uchun dokumentatsiya
- Request/Response examples

---

## 15. TESTING

### 15.1 Unit Tests
- Service layer
- Repository layer (test containers)

### 15.2 Integration Tests
- API endpoints
- Redis integration
- Yandex API mocking

---

## 16. DEPLOYMENT

### 16.1 Docker
- PostgreSQL container
- Redis container
- Application container

### 16.2 Environment Variables
- Database URL
- Redis URL
- Yandex Maps API Key
- File storage path

---

## 17. MVP PRIORITIES

### Must Have (MVP):
1. ✅ User registration/login
2. ✅ Map + location detection
3. ✅ Nearby places (Redis + DB + Yandex)
4. ✅ Restaurant profile view
5. ✅ Menu display
6. ✅ Media (image/video) display
7. ✅ Comments (restaurant, menu, media)
8. ✅ Search (DB + Yandex)

### Keyinroq (v2):
- Table booking
- Ordering
- Payment
- Notifications
- Advanced reviews/ratings
- User favorites
- Restaurant analytics

---

## 18. MODUL O'RTASIDA BOG'LANISHLAR

### 18.1 Map ↔ Restaurant
- `Place.restaurantId` orqali bog'lanish
- Restaurant yaratilganda → Place.restaurantId yangilanadi

### 18.2 Restaurant → Menu
- `MenuItem.restaurantId` orqali

### 18.3 Restaurant/Menu → Media
- `Media.ownerType` va `Media.ownerId` orqali

### 18.4 Restaurant/Menu/Media → Comments
- `Comment.entityType` va `Comment.entityId` orqali

### 18.5 Modullar mustaqil:
- Har bir modul o'z repository, service, controller bilan
- Faqat service layer da bog'lanish
- Entitylar o'rtasida JPA relationship yo'q (modullar mustaqil bo'lishi uchun)

---

## 19. DEVELOPMENT WORKFLOW

### 19.1 Branch Strategy
- `master` - production
- `develop` - development
- `feature/module-name` - har bir modul uchun

### 19.2 Code Review
- Har bir PR code review
- 2 kishi bir-biriga xalaq bermasligi uchun modullar ajratilgan

---

## 20. PERFORMANCE

### 20.1 Optimization
- Pagination (barcha listlar uchun)
- Lazy loading
- Database indexes
- Redis caching
- Connection pooling

### 20.2 Monitoring
- API response time
- Cache hit rate
- Database query performance
- Yandex API usage

---

**Yakunlanish sanasi:** MVP - 2-3 hafta  
**Keyingi versiya:** v2.0 (booking, ordering)

