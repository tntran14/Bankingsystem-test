# Banking System - Hướng Dẫn Chạy Ứng Dụng

## Cách 1: Chạy nhanh (Windows)
```
run.bat
```
Double-click file `run.bat` — tự động compile, test, và khởi chạy app.

---

## Cách 2: Chạy thủ công

### Build
```bash
mvn clean compile
```

### Khởi chạy ứng dụng
```bash
mvn spring-boot:run

## Truy cập

| Trang | URL |
|---|---|
| **Giao diện chính** | http://localhost:8080 |
| **H2 Database Console** | http://localhost:8080/h2-console |
| **Actuator Health** | http://localhost:8080/actuator/health |
| **Actuator Metrics** | http://localhost:8080/actuator/metrics |

### Đăng nhập
- **Admin**: `admin` / `admin123` (tạo sẵn khi khởi động)
- Đăng ký tài khoản mới qua: `POST /api/auth/register`

### H2 Console
- JDBC URL: `jdbc:h2:mem:banking_db`
- Username: `sa`
- Password: *(để trống)*

## API Endpoints

### Auth (Public)
```
POST /api/auth/login          - Đăng nhập, nhận JWT token
POST /api/auth/register       - Đăng ký tài khoản mới
```

### Customers
```
GET    /api/customers              - Danh sách (phân trang)
GET    /api/customers/{id}         - Chi tiết
GET    /api/customers/search?name= - Tìm kiếm theo tên
POST   /api/customers              - Thêm mới (ADMIN)
PUT    /api/customers/{id}         - Cập nhật (ADMIN)
DELETE /api/customers/{id}         - Xóa (ADMIN)
```

### Accounts
```
GET    /api/accounts                      - Danh sách (ADMIN, phân trang)
GET    /api/accounts/{id}                 - Chi tiết
GET    /api/accounts/customer/{customerId} - Theo khách hàng
POST   /api/accounts                      - Tạo tài khoản
PUT    /api/accounts/{id}                 - Cập nhật
DELETE /api/accounts/{id}                 - Xóa (ADMIN)
PATCH  /api/accounts/{id}/status?status=  - Đổi trạng thái (ADMIN)
GET    /api/accounts/{id}/status-history  - Lịch sử trạng thái
```

### Transactions
```
POST   /api/transactions                - Thực hiện giao dịch
GET    /api/transactions/{id}           - Chi tiết
GET    /api/transactions/search?...     - Tìm kiếm nâng cao (phân trang + sắp xếp)
GET    /api/transactions/account/{id}   - Lịch sử theo tài khoản
DELETE /api/transactions/{id}           - Xóa (ADMIN)
POST   /api/transactions/scheduled      - Tạo giao dịch định kỳ
GET    /api/transactions/scheduled      - Danh sách GD định kỳ
DELETE /api/transactions/scheduled/{id} - Hủy GD định kỳ
```

### Statistics (ADMIN)
```
GET /api/statistics/accounts              - Thống kê TK (số dư cao/trung bình/thấp)
GET /api/statistics/customers/location    - KH theo địa điểm
GET /api/statistics/transactions/weekly   - Báo cáo tuần
GET /api/statistics/transactions/quarterly - Báo cáo quý
GET /api/statistics/transactions/yearly   - Báo cáo năm
```

### Reports (ADMIN)
```
GET /api/reports/transactions/excel?from=&to= - Xuất Excel
GET /api/reports/transactions/pdf?from=&to=   - Xuất PDF
```

### Alerts (ADMIN)
```
GET   /api/alerts                    - Tất cả cảnh báo
