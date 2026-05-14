# Restaurant Management System (RMS)

Dự án quản lý nhà hàng với kiến trúc Microservices, tích hợp AI và hỗ trợ đa nền tảng (Web & Mobile).

## 📂 Cấu trúc dự án

- **BE**: Chứa các dịch vụ backend (Spring Boot, Kafka, Oracle Database).
- **FE**: Chứa các ứng dụng frontend (Web React/Vite, Mobile Android).
- **ai**: Chứa các module liên quan đến trí tuệ nhân tạo.

---

## 🛠️ Yêu cầu hệ thống

- **Java 17+** (Dành cho Backend)
- **Node.js 18+** (Dành cho Frontend Web)
- **Docker & Docker Compose** (Chạy Kafka, Zookeeper)
- **Oracle Database 21c/23c** (Mặc định chạy trên cổng 1522)
- **Android Studio** (Dành cho Frontend Mobile)

---

## 🚀 1. Hướng dẫn chạy Backend (BE)

### Bước 1: Khởi động hạ tầng (Kafka)
Di chuyển vào thư mục `BE` và sử dụng Docker Compose:
```bash
cd BE
docker-compose up -d
```
*Lưu ý: Đảm bảo Docker Desktop đã được khởi động.*

### Bước 2: Cài đặt Cơ sở dữ liệu (Oracle)
Đảm bảo Oracle Database đang chạy trên cổng `1522` với các thông tin:
- **Service Name**: `XEPDB1`
- **User**: `Food_Order` / `Ab@123456`

### Bước 3: Chạy Database Migration
Sử dụng Liquibase để khởi tạo các bảng dữ liệu:
```bash
cd BE/migration-service
./gradlew migrate-all
```

### Bước 4: Chạy các Microservices
Mở từng thư mục service trong `BE` và chạy lệnh sau:
```bash
./gradlew bootRun
```
**Danh sách các service chính:**
1. `catalog-service` (Port: 8081) - Quản lý thực đơn.
2. `order-service` (Port: 8082) - Quản lý đơn hàng.
3. `table-service` (Port: 8083) - Quản lý bàn ăn.
4. `payment-service` (Port: 8085) - Xử lý thanh toán.
5. `notification-service` (Port: 8086) - Gửi thông báo (Real-time).
6. `users-service` (Port: 8087) - Quản lý người dùng và phân quyền.
7. `gateway-service` (Port: 8080) - **Cổng tập trung (Gateway)**.

---

## 💻 2. Hướng dẫn chạy Frontend (FE)

### 🌐 Web (Admin / Quầy)
Sử dụng React + Vite.
1. Truy cập thư mục: `cd FE/web`
2. Cài đặt dependencies: `npm install`
3. Chạy dev server: `npm run dev`
4. Truy cập tại: `http://localhost:5173`

### 📱 Mobile (Waiter / Customer)
Sử dụng Android (Kotlin).
1. Mở thư mục `FE/mobile` bằng **Android Studio**.
2. Đợi Gradle sync hoàn tất.
3. Chạy trên Emulator hoặc thiết bị thật (nhấn nút **Run**).

---

## 🔗 Thông tin API Gateway (Port 8080)

Gateway là điểm tiếp nhận duy nhất cho các request từ FE:

| Service | Prefix Path | URL thực tế |
| :--- | :--- | :--- |
| **Catalog** | `/catalog/**` | `http://localhost:8081` |
| **Order** | `/order/**` | `http://localhost:8082` |
| **Table** | `/api/tables/**` | `http://localhost:8083` |
| **User** | `/api/users-service/**` | `http://localhost:8087` |
| **Payment** | `/payment/**` | `http://localhost:8085` |
| **Notification** | `/api/notifications/**` | `http://localhost:8086` |

---

## 📧 Liên hệ hỗ trợ
Nếu gặp vấn đề trong quá trình setup, vui lòng kiểm tra file `application.properties` hoặc `application.yml` trong từng service để đảm bảo cấu hình database và kafka chính xác.
