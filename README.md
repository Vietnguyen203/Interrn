# Food Order System (RMS)

This is a comprehensive Food Order System comprising a Backend (Microservices), a Frontend Web application, and a Frontend Mobile application.

## Prerequisites
Before you begin, ensure you have the following installed on your machine:
- **Java Development Kit (JDK)**: For building and running the Backend services.
- **Node.js**: Version 18 or higher (for the Web application).
- **Docker & Docker Compose**: To run infrastructure services like Kafka.
- **Android Studio & Android SDK**: For compiling and running the Mobile app.

---

## 1. Backend Services (BE)

The backend is built using a microservice architecture (Spring Boot).

### How to Run
1. **Start Kafka / Infrastructure**:
   ```bash
   cd BE
   docker-compose up -d
   ```
2. **Run Database Migration**:
   ```bash
   cd BE/migration-service
   ./gradlew migrate-all
   ```
3. **Run Individual Services**:
   Open separate terminal windows for each service you need and run:
   ```bash
   cd BE/<service-name>
   ./gradlew bootRun
   ```
   *Available services*: `gateway-service` (Port 8080), `users-service` (8087), `order-service` (8082), `catalog-service` (8081), `table-service` (8083), `payment-service` (8085), `notification-service` (8086).

---

## 2. Web Application (FE Web)

The web application is built for Admins and Cashiers using React and Vite.

### How to Run
1. **Navigate to the web directory**:
   ```bash
   cd FE/web
   ```
2. **Install dependencies**:
   ```bash
   npm install
   ```
3. **Start the development server**:
   ```bash
   npm run dev
   ```
4. **Access the web app**: Open your browser and go to `http://localhost:5173`.

---

## 3. Mobile Application (FE Mobile)

The mobile application is a native Android app built with Kotlin for waiters and customers.

### How to Run
1. Open **Android Studio**.
2. Select **Open** and choose the `FE/mobile` directory.
3. Allow Gradle to sync the project and download all necessary dependencies.
4. Set up an **Android Emulator** (AVD) or connect a physical Android device via USB Debugging.
5. Click the **Run 'app'** button (or press `Shift + F10`) to build and launch the app.

*Note: If you are using an Android Emulator, the app connects to the backend gateway via `http://10.0.2.2:8080` (which points to your computer's localhost).*
