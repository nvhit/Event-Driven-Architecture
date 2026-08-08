# 📘 Ngày 1: Kiến Trúc Hướng Sự Kiện
## Xây dựng hệ thống Event Taxonomy — StreamSocial

---

## 📋 Mục tiêu bài học

Sau bài học này, bạn sẽ:

- Hiểu vấn đề của mô hình Request-Response truyền thống
- Nắm nguyên tắc hoạt động của Event-Driven Architecture (EDA)
- Thiết kế Event Taxonomy cho nền tảng mạng xã hội
- Triển khai hệ thống Producer/Consumer với Kafka
- Xây dựng RESTful API với Spring Boot
- Tạo Real-time Dashboard hiển thị sự kiện

---

## �️ Nội dung chính

| # | Chủ đề | Thời lượng |
|---|--------|-----------|
| 1 | 🔴 Vấn đề của Request-Response | 5 phút |
| 2 | 🟢 Giải pháp Event-Driven Architecture | 5 phút |
| 3 | 🟡 Thiết kế Event Taxonomy | 5 phút |
| 4 | 🔵 Kiến trúc Core Components | 5 phút |
| 5 | 🛠️ Demo & Thực hành triển khai | 15 phút |
| 6 | 🎯 Bài tập & Tổng kết | 5 phút |

---

## 🔴 PHẦN 1: VẤN ĐỀ CỦA MÔ HÌNH REQUEST-RESPONSE

---

## Mô hình truyền thống hoạt động thế nào?

```
[User] --request--> [Server] --response--> [User]
         ⏳ Chờ...         ⏳ Chờ...
```

**Giống như một cuộc gọi điện thoại:**
- Mọi hành động đều phải **chờ phản hồi**
- Một thao tác chậm → **tắc nghẽn toàn bộ hệ thống**

---

## Ví dụ: A thả tym một bài đăng trên Instagram

```
A nhấn "thả tim"
    → Cập nhật số lượt like          ⏳ 200ms
    → Thông báo cho người theo dõi   ⏳ 500ms
    → Kích hoạt đề xuất              ⏳ 300ms
    → Ghi nhật ký phân tích          ⏳ 100ms
    
Tổng thời gian = 1100ms (tuần tự) ❌
```

**Nút thắt cổ chai:** Một service chậm → Block toàn bộ luồng xử lý

---

## Khó khăn thực tế tại Big Tech

| Nền tảng | Vấn đề |
|----------|--------|
| **📸 Instagram** | Nút "Like" bị đơ khi traffic cao điểm |
| **🐦 Twitter/X** | Timeline chậm hiển thị khi nội dung viral |
| **💼 LinkedIn** | Yêu cầu kết bạn bị timeout |

> ❓ Câu hỏi: Làm sao để hệ thống vẫn phản hồi nhanh khi có hàng triệu thao tác đồng thời?

---

## 🟢 PHẦN 2: GIẢI PHÁP — KIẾN TRÚC HƯỚNG SỰ KIỆN (EDA)

---

## Ý tưởng cốt lõi

> Thay vì **chờ đợi từng hoạt động**, ta **phát hành sự kiện** và chúng tự động lưu chuyển trong hệ thống.

| Đồng bộ | Bất đồng bộ (EDA) |
|----------|-------------------|
| User → A → B → C | User → Publish Event |
| ⏳ chờ A | Service A ← consume ⚡ |
| ⏳ chờ B | Service B ← consume ⚡ |
| ⏳ chờ C | Service C ← consume ⚡ |

---

## Kiến trúc Thành phần — Hệ thống Hướng Sự kiện

![Kiến trúc Thành phần EDA](images/9b-svg.html)

**Các thành phần chính:**
- 🖥️ Giao diện Dashboard (React + Biểu đồ)
- ☕ Spring Boot API (REST Endpoints)
- 📤 Event Producer (Hành động người dùng, Tương tác nội dung, Sự kiện hệ thống)
- 📨 Kafka Cluster (streamsocial-events — 3 partitions)
- 📥 Event Consumer (Xử lý sự kiện, Cập nhật phân tích, Hiển thị thời gian thực)
- 📊 Analytics Service / 🔔 Notification Service / 💡 Recommendation Service

**Phân loại Sự kiện:**
- 🟢 Hành động Người dùng (6): đăng ký, đăng nhập, cập nhật, theo dõi, đăng bài, xóa bài
- 🔵 Tương tác Nội dung (3): thích, bình luận, chia sẻ
- 🟠 Sự kiện Hệ thống (1): thông báo

**Lợi ích Kiến trúc:**
- ✓ Tách biệt: Các service hoạt động độc lập
- ✓ Mở rộng: Mỗi service tự scale riêng
- ✓ Phục hồi: Service lỗi không chặn event
- ✓ Linh hoạt: Service mới dùng stream có sẵn

---

## So sánh: Synchronous vs Event-Driven

| Tiêu chí | Request-Response | Event-Driven |
|-----------|-----------------|--------------|
| Coupling | Tight (chặt) | Loose (lỏng) |
| Xử lý | Tuần tự | Song song |
| Lỗi lan truyền | Có ❌ | Không ✅ |
| Khả năng mở rộng | Hạn chế | Cao |
| Phức tạp triển khai | Thấp | Trung bình |
| Real-time | Khó | Native ✅ |

---

## Cách hoạt động — Ví dụ "Like" bài đăng

```
A nhấn "Like"
    → Produce event: "content_like"
    
    ┌─ Analytics Service       ← track engagement ⚡
    ├─ Notification Service    ← thông báo tác giả ⚡
    └─ Recommendation Service  ← cập nhật feed ⚡
    
    Tất cả xử lý ĐỒNG THỜI — Tổng thời gian = max(200, 500, 300) = 500ms ✅
```

**Kết quả:** Phản hồi ngay cho A, các service xử lý ngầm phía sau.

---

## Lợi ích chính của EDA

| Đặc tính | Giải thích |
|----------|-----------|
| **🔓 Tách biệt (Decoupling)** | Các service hoạt động độc lập, không gây cản trở lẫn nhau |
| **📈 Khả năng mở rộng (Scalability)** | Mỗi service tự scale dựa trên mô hình tải riêng |
| **🛡️ Khả năng phục hồi (Resilience)** | Service lỗi không ảnh hưởng toàn hệ thống |
| **🔄 Tính linh hoạt (Flexibility)** | Thêm tính năng mới chỉ cần subscribe event có sẵn |

---

## Ứng dụng thực tế tại Big Tech

| Công ty | Sự kiện | Consumers |
|---------|---------|-----------|
| **🎬 Netflix** | Phát video | Cập nhật đề xuất, Tính toán hóa đơn, Theo dõi độ phổ biến |
| **🚗 Uber** | Đặt xe | Ghép tài xế, Định giá, Analytics |
| **💬 Slack** | Gửi tin nhắn | Multi-client update, Notification, Search index |

---

## 🟡 PHẦN 3: THIẾT KẾ EVENT TAXONOMY

---

## Event Taxonomy là gì?

> 📖 **Phân loại có hệ thống** tất cả các loại sự kiện trong hệ thống

**Tại sao cần phân loại?**
- Tổ chức logic rõ ràng, nhất quán
- Dễ mở rộng trong tương lai
- Các team hiểu chung một ngôn ngữ (Ubiquitous Language)
- Routing & filtering event hiệu quả

---

## Luồng Sự kiện — StreamSocial

![Luồng Sự kiện StreamSocial](images/16-svg.html)

**Luồng xử lý:**
1. Người dùng thực hiện hành động
2. API xác thực & định tuyến
3. Xác định loại sự kiện (Đăng ký Người dùng / Tương tác Nội dung / Sự kiện Hệ thống)
4. Producer tạo sự kiện có cấu trúc (UUID + Timestamp)
5. Gửi lên Kafka topic (streamsocial-events)
6. Nhiều consumer xử lý đồng thời (Phân tích, Thông báo, Dashboard, Đề xuất)

---

## Nguyên tắc thiết kế Event

Mỗi event cần đảm bảo:

1. **⚛️ Nguyên tử (Atomic)** — Mỗi event đại diện cho MỘT hành động cụ thể, không gộp nhiều hành động
2. **📦 Đủ ngữ cảnh (Self-contained)** — Chứa đủ thông tin để consumer xử lý độc lập, không cần query thêm
3. **📐 Có cấu trúc (Structured)** — Schema rõ ràng, nhất quán giữa tất cả event types

---

## Cấu trúc một Event

```json
{
    "event_id": "uuid-unique",
    "event_type": "content_like",
    "timestamp": "2025-07-31T10:30:00Z",
    "user_id": "user_12345",
    "session_id": "session_abc123",
    "data": {
        "action": "like",
        "post_id": "post_67890"
    }
}
```

| Field | Vai trò |
|-------|---------|
| `event_id` | Định danh duy nhất |
| `event_type` | Phân loại sự kiện |
| `timestamp` | Thời điểm xảy ra |
| `user_id` | Ai thực hiện |
| `session_id` | Phiên làm việc |
| `data` | Payload chi tiết |

---

## 🔵 PHẦN 4: KIẾN TRÚC CORE COMPONENTS

---

## 3 Thành phần cốt lõi

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   PRODUCER   │────▶│  EVENT STORE │────▶│   CONSUMER   │
│              │     │   (Kafka)    │     │              │
│ Ghi nhận     │     │ Lưu trữ &   │     │ Đăng ký &    │
│ hành động    │     │ phân phối    │     │ xử lý event  │
└──────────────┘     └──────────────┘     └──────────────┘
```

> Kafka đóng vai trò trung tâm — lưu trữ event theo thứ tự, hỗ trợ replay dữ liệu lịch sử.

---

## Event Producer — Code (Spring Kafka)

**Nhiệm vụ:** Ghi lại hành động user → Publish event lên Kafka

```java
@Service
public class EventProducerService {

    private static final String TOPIC = "streamsocial-events";

    @Autowired
    private KafkaTemplate<String, StreamSocialEvent> kafkaTemplate;

    public String publishEvent(EventType type, String userId,
            Map<String, Object> data) {
        StreamSocialEvent event = StreamSocialEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(type)
            .timestamp(LocalDateTime.now())
            .userId(userId)
            .sessionId(UUID.randomUUID().toString())
            .data(data)
            .build();

        kafkaTemplate.send(TOPIC, userId, event);
        return event.getEventId();
    }
}
```

🔑 Dùng `userId` làm partition key → đảm bảo thứ tự event cùng user

---

## Event Consumer — Code (Spring Kafka Listener)

**Nhiệm vụ:** Subscribe topic → Nhận & xử lý event theo loại

```java
@Service
public class EventConsumerService {

    private final List<StreamSocialEvent> processedEvents
        = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "streamsocial-events",
        groupId = "streamsocial-consumers")
    public void consume(StreamSocialEvent event) {
        processedEvents.add(event);
        handleEvent(event);
    }

    private void handleEvent(StreamSocialEvent event) {
        switch (event.getEventType()) {
            case CONTENT_LIKE -> notifyAuthor(event);
            case USER_REGISTRATION -> sendWelcome(event);
            default -> log.info("Event: {}", event);
        }
    }

    public EventListResponse getRecentEvents(int limit) {
        int size = processedEvents.size();
        var recent = processedEvents.subList(
            Math.max(0, size - limit), size);
        return new EventListResponse(true, recent);
    }
}
```

**Mỗi service chỉ consume event mà nó quan tâm.**

---

## 🛠️ PHẦN 5: DEMO & THỰC HÀNH TRIỂN KHAI

---

## Máy Trạng Thái Sự Kiện — StreamSocial

![Máy Trạng Thái Sự Kiện](images/3-svg.html)

**Luồng trạng thái:**
- Bắt đầu → Hành động Người dùng (kích hoạt)
- Hành động Người dùng → Sự kiện Đã tạo (xác thực)
- Sự kiện Đã tạo → Sự kiện Đã gửi (phát hành lên Kafka)
- Sự kiện Đã gửi → Đang xử lý (tiêu thụ)
- Đang xử lý → Hàng đợi Thử lại (lỗi) → Đang xử lý (thử lại)
- Đang xử lý → Thành công Đã xử lý (thành công) → Kết thúc (hoàn tất)

---

## Tech Stack

| Layer | Công nghệ | Vai trò |
|-------|-----------|---------|
| Message Broker | Apache Kafka 7.4.0 | Event Store & Streaming |
| Backend | Spring Boot 3.2 | RESTful API layer |
| Language | Java 17 | Backend runtime |
| Kafka Client | Spring Kafka | Producer/Consumer |
| Frontend | React 18.3.1 | Real-time Dashboard |
| Build Tool | Maven / Gradle | Dependency management |

---

## Cấu trúc Project (Spring Boot)

```
streamsocial/
└── src/main/java/com/streamsocial/
    ├── config/       // Kafka config
    ├── model/        // Event entities
    ├── producer/     // Kafka producers
    ├── consumer/     // Kafka consumers
    ├── controller/   // REST controllers
    ├── dto/          // Request/Response
    └── Application.java
```

**Khởi tạo project từ Spring Initializr (https://start.spring.io):**
- Spring Web
- Spring for Apache Kafka
- Lombok
- Spring Boot DevTools

---

## Bước 1: Định nghĩa Event Model (Java)

```java
public enum EventType {
    USER_REGISTRATION, USER_LOGIN, USER_PROFILE_UPDATE,
    USER_FOLLOW, USER_POST_CREATE, USER_POST_DELETE,
    CONTENT_LIKE, CONTENT_COMMENT, CONTENT_SHARE,
    SYSTEM_NOTIFICATION
}

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class StreamSocialEvent {
    private String eventId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private String userId;
    private String sessionId;
    private Map<String, Object> data;
}
```

---

## Bước 2: Kafka + Spring Boot Config

**docker-compose.yml:**

```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    ports: ["2181:2181"]

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_ADVERTISED_LISTENERS:
        PLAINTEXT://localhost:9092
```

**application.yml:**

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
    consumer:
      group-id: streamsocial-consumers
      auto-offset-reset: latest
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
```

---

## Bước 3: Spring Boot — RESTful API

```java
@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventProducerService producerService;
    @Autowired
    private EventConsumerService consumerService;

    @PostMapping("/user/register")
    public ResponseEntity<ApiResponse> registerUser(
            @RequestBody UserRegistrationDTO dto) {
        String userId = UUID.randomUUID().toString();
        String eventId = producerService.publishEvent(
            EventType.USER_REGISTRATION, userId,
            Map.of("username", dto.getUsername()));
        return ResponseEntity.ok(new ApiResponse(true, userId, eventId));
    }

    @GetMapping("/recent")
    public ResponseEntity<EventListResponse> getRecentEvents() {
        return ResponseEntity.ok(consumerService.getRecentEvents(20));
    }
}
```

📌 Swagger UI tại: `http://localhost:8080/swagger-ui.html` (SpringDoc OpenAPI)

---

## Bước 4: React Dashboard (Real-time)

```jsx
function App() {
  const [events, setEvents] = useState([]);

  useEffect(() => {
    const interval = setInterval(async () => {
      const res = await axios.get(
        'http://localhost:8080/api/v1/events/recent');
      setEvents(res.data.events);
    }, 2000); // Polling mỗi 2 giây
    return () => clearInterval(interval);
  }, []);

  return (
    <StatsGrid events={events} />
    <EventTimeline events={events} />
  );
}
```

---

## Bước 5: Testing (JUnit 5 + MockMvc)

**Unit Test:**

```java
@Test
void testEventCreation() {
    var event = StreamSocialEvent.builder()
        .eventId("test-123")
        .eventType(EventType.USER_REGISTRATION)
        .userId("user-456")
        .data(Map.of("username", "test"))
        .build();

    assertEquals(EventType.USER_REGISTRATION,
        event.getEventType());
}
```

**Integration Test:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void testRegisterUser() throws Exception {
        mockMvc.perform(post(
            "/api/v1/events/user/register")
            .contentType(APPLICATION_JSON)
            .content("""
                {"username":"test",
                 "email":"t@ex.com"}"""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success")
            .value(true));
    }
}
```

```bash
# Chạy tests
./mvnw test    # Maven
./gradlew test # Gradle
```

---

## 🚀 Quick Start — Build & Run

```bash
# 1. Khởi động Kafka
docker-compose up -d

# 2. Build & Run Spring Boot
./mvnw spring-boot:run

# 3. Mở Swagger UI
open http://localhost:8080/swagger-ui.html

# 4. Mở React Dashboard
cd frontend && npm start
open http://localhost:3000
```

✅ Verification: Dashboard cập nhật real-time, Swagger UI tại `:8080/swagger-ui.html`

---

## 🎯 PHẦN 6: BÀI TẬP & TỔNG KẾT

---

## Tiêu chí thành công

- ✅ Event Producer hoạt động và publish thành công lên Kafka topic
- ✅ Event Consumer xử lý tương tác người dùng từ event stream
- ✅ Real-time Dashboard hiển thị luồng sự kiện cập nhật mỗi 2 giây
- ✅ Event Taxonomy hoàn chỉnh (10 loại sự kiện cho mạng xã hội)

---

## 🏋️ Bài tập: Mở rộng Event Taxonomy

**Yêu cầu:** Thêm 5 loại sự kiện mới

| Domain | Gợi ý Event |
|--------|-------------|
| 💬 Nhắn tin trực tiếp | `message_sent`, `message_read` |
| 🛡️ Kiểm duyệt nội dung | `content_reported`, `content_moderated` |
| 📊 Phân tích người dùng | `session_started`, `session_ended` |

**Suy nghĩ thêm:**
- Event nào trải dài nhiều phiên? (chuỗi hội thoại)
- Event nào kích hoạt workflow phức tạp? (report → review → action)

---

## Tổng kết bài học

| Request-Response | → | Event-Driven |
|-----------------|---|--------------|
| Đồng bộ, block | | Bất đồng bộ |
| Tight coupling | | Loose coupling |
| Scale khó | | Scale dễ dàng |
| Lỗi lan truyền | | Fault-tolerant |

**3 thành phần cốt lõi:** Producer → Event Store (Kafka) → Consumer

---

## 📅 Ngày tiếp theo — Day 2

> Triển khai **Kafka Cluster 3 broker** bằng Docker Compose
>
> Single-node → **Distributed system** sẵn sàng production
>
> Xử lý **hàng triệu sự kiện/giây** 🚀

---

## 📚 Tài liệu tham khảo

- Bài viết gốc: [hungnguyens.substack.com](https://hungnguyens.substack.com/p/ngay-1-cac-nguyen-tac-co-ban-cua)
- Source code: [github.com/nvhit/Event-Driven-Architecture](https://github.com/nvhit/Event-Driven-Architecture.git)
- Apache Kafka: [kafka.apache.org](https://kafka.apache.org)
- Spring Boot: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- Spring Kafka: [spring.io/projects/spring-kafka](https://spring.io/projects/spring-kafka)

---

*Nội dung được tổng hợp và tái cấu trúc cho mục đích giảng dạy.*
*Content was rephrased for compliance with licensing restrictions.*
