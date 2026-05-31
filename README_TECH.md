# Báo cáo Tổng hợp Công nghệ Dự án Banking System

Dự án **Banking System** là một hệ thống quản lý ngân hàng và giao dịch trực tuyến toàn diện (Full-Stack), được phát triển theo mô hình kiến trúc Single Page Application (SPA) ở Frontend kết hợp với RESTful APIs bảo mật cao ở Backend.

Dưới đây là bảng tổng hợp chi tiết toàn bộ các công nghệ, thư viện và giải pháp kiến trúc đã được áp dụng trong dự án:

---

## 🖥️ 1. Backend Architecture (Java & Spring Boot)
Backend của dự án được xây dựng dựa trên hệ sinh thái **Java & Spring Boot Enterprise**, đảm bảo tính mô-đun, dễ mở rộng và hiệu năng cao.

| Công nghệ / Thư viện | Vai trò trong Dự án | Chi tiết Ứng dụng |
| :--- | :--- | :--- |
| **Spring Boot** | Khung phát triển cốt lõi | Quản lý vòng đời ứng dụng, Auto-Configuration, Dependency Injection (IoC). |
| **Spring Security** | Bảo mật toàn hệ thống | Phân quyền truy cập tài nguyên dựa trên vai trò khách hàng (RBAC) với các bộ lọc bảo mật tùy chỉnh. |
| **JSON Web Token (JWT)** | Xác thực không trạng thái | Tạo và xác thực Token ký số mật mã để duy trì phiên đăng nhập bảo mật giữa Frontend và Backend. |
| **Spring Data JPA & Hibernate** | Tương tác Cơ sở dữ liệu | Quản lý các thực thể (Entities), thiết lập mối quan hệ (One-to-One, One-to-Many), điều phối giao dịch tự động thông qua `@Transactional`. |
| **JPA Specification (Criteria API)**| Tìm kiếm động nâng cao | Xây dựng các câu truy vấn SQL phức tạp, linh hoạt tại runtime phục vụ cho chức năng lọc giao dịch nâng cao. |
| **Spring Cache** | Tối ưu hóa hiệu năng | Cache dữ liệu tài khoản, thống kê và báo cáo (`@Cacheable`, `@CacheEvict`) giúp tăng tốc độ phản hồi API, giảm tải cho Database. |
| **Spring Async & Task Executor** | Xử lý bất đồng bộ | Thực thi các giao dịch nặng một cách phi tuần tự (Non-blocking) qua môi trường luồng (Thread pool) riêng biệt. |
| **Spring Task Scheduling** | Tự động hóa tác vụ | Lập lịch chạy ngầm (`@Scheduled`) để quét và tự động thực thi các giao dịch định kỳ (Scheduled Transactions). |
| **Spring Boot Actuator** | Giám sát & Đo lường | Cung cấp các endpoints đo lường sức khỏe hệ thống (`/actuator/health`) và các chỉ số tài nguyên máy chủ. |
| **Lombok** | Tối ưu hóa mã nguồn | Sử dụng các annotations `@Getter`, `@Setter`, `@Builder`, `@Slf4j` giúp mã nguồn sạch đẹp, dễ bảo trì. |

---

## 🗄️ 2. Database & Data Persistence
Hệ thống lưu trữ dữ liệu được thiết kế tối ưu cho cả môi trường phát triển (Development) lẫn kiểm thử (Testing).

| Công nghệ | Cơ chế Hoạt động | Mục tiêu |
| :--- | :--- | :--- |
| **H2 Database Engine** | **Physical File-based Persistence** | Cơ sở dữ liệu nhúng dung lượng nhẹ, cấu hình ghi dữ liệu vật lý xuống ổ đĩa (`data/banking_db`) giúp giữ nguyên toàn bộ dữ liệu test sau mỗi lần khởi động lại server. |
| **Spring DDL Auto** | Cấu hình `ddl-auto: update` | Tự động đồng bộ hóa cấu trúc thực thể Java (Entities) với cấu trúc bảng cơ sở dữ liệu mà không làm mất dữ liệu hiện có. |

---

## 📊 3. Báo cáo & Tài liệu (Report Generation)
Các thư viện chuyên dụng được tích hợp để tự động kết xuất dữ liệu tài chính sang các định dạng văn phòng chuẩn hóa.

| Thư viện | Định dạng Xuất bản | Đặc điểm Tính năng |
| :--- | :--- | :--- |
| **Apache POI** | **Microsoft Excel (`.xlsx`)** | Tạo bảng tính Excel báo cáo doanh thu giao dịch, thống kê dòng tiền, thiết kế định dạng ô, phông chữ và công thức tính toán tài chính tự động. |
| **OpenPDF** | **Adobe PDF (`.pdf`)** | Kết xuất sao kê giao dịch ngân hàng dưới dạng văn bản PDF chuyên nghiệp, hỗ trợ định dạng bảng dữ liệu bảo mật cao và không thể chỉnh sửa. |

---

## 🎨 4. Frontend Architecture (Modern Single Page Application)
Giao diện ứng dụng được phát triển theo xu hướng thiết kế cao cấp, hiện đại, tối ưu hóa tốc độ tải trang bằng mã nguồn thuần (Vanilla).

| Công nghệ | Vai trò & Phong cách Thiết kế | Đặc điểm Nổi bật |
| :--- | :--- | :--- |
| **HTML5 Semantic** | Cấu trúc tài liệu chuẩn SEO | Sử dụng các thẻ ngữ nghĩa (`<nav>`, `<header>`, `<main>`, `<section>`) tối ưu hóa SEO và cấu trúc cây DOM. |
| **CSS3 (Vanilla)** | Thiết kế Premium Dark Theme | - Tone màu tối sang trọng kết hợp hiệu ứng kính mờ (Glassmorphism).<br>- Tích hợp Flexbox & CSS Grid cho thiết kế Responsive co giãn hoàn hảo trên mọi thiết bị.<br>- Bo góc mượt mà, chuyển động (Transitions/Animations) trực quan khi rê chuột. |
| **JavaScript (ES6+)** | Kiến trúc Single Page Application | - **Custom Router:** Chuyển đổi qua lại giữa các màn hình mà không cần tải lại trang (Zero-refresh).<br>- **State Management:** Quản lý tập trung trạng thái bộ lọc, phân trang và dữ liệu phiên người dùng.<br>- **Fetch API Abstraction:** Đóng gói Client gọi API bất đồng bộ tích hợp xử lý lỗi 401/403 tập trung. |
| **Material Icons** | Thư viện biểu tượng | Sử dụng bộ biểu tượng Material của Google giúp giao diện trực quan và chuyên nghiệp. |

---

## 🔒 5. Cơ chế Bảo mật & Kiểm thử bảo mật
Hệ thống tuân thủ chặt chẽ các chuẩn bảo mật tài chính quốc tế phổ biến.

* **Mã hóa mật khẩu:** Mật khẩu người dùng được băm một chiều bằng thuật toán mạnh **BCrypt** trước khi lưu vào Database.
* **Xác thực JWT:** Token được lưu an toàn tại LocalStorage, tự động đính kèm vào header `Authorization: Bearer <token>` ở mỗi request.
* **Cơ chế CORS & Clickjacking Protection:** Chống tấn công giả mạo yêu cầu chéo trang và chống chèn frame độc hại (`X-Frame-Options: SAMEORIGIN`).
* **Phân quyền giao diện động:** Ẩn hoàn toàn các chức năng quản trị đối với khách hàng thường, chỉ cho phép cán bộ quản trị (Admin) truy cập các APIs nhạy cảm.

---

## ⚙️ 6. Yêu cầu Môi trường & Phiên bản (Versions)

Các thông số phiên bản chi tiết được cấu hình trong dự án bao gồm:

| Môi trường / Thư viện | Phiên bản | Ghi chú |
| :--- | :--- | :--- |
| **Java JDK** | `1.8` (hoặc cao hơn) | Tương thích hoàn toàn với JDK 8, JDK 11, JDK 17 và JDK 21. |
| **Maven** | `3.6.x` trở lên | Trình quản lý dependency và build tool chính. |
| **Spring Boot** | `2.7.18` | Phiên bản Spring Boot Enterprise ổn định và hiệu năng cao. |
| **H2 Database** | `1.4.200` (hoặc mới hơn) | Hệ quản trị cơ sở dữ liệu lưu trữ vật lý. |
| **JJWT (JSON Web Token)** | `0.11.5` | Thư viện tạo, ký và parse JWT token bảo mật. |
| **Apache POI** | `5.2.5` | Thư viện xuất báo cáo Excel (.xlsx). |
| **OpenPDF** | `1.3.30` | Thư viện sinh báo cáo sao kê PDF (.pdf). |
| **Lombok** | `1.18.30` (hoặc theo Spring parent) | Thư viện rút gọn mã nguồn Java. |

