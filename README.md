# 🚕 Cabbie - Real-Time Ride Sharing Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Enabled-red.svg)](https://kafka.apache.org/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-yellow.svg)](https://stomp.github.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#license)

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [System Design](#system-design)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Real-Time Notification Flow](#real-time-notification-flow)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

**Cabbie** is a modern, scalable ride-sharing platform built with **Spring Boot 4.0.5** that enables real-time ride requests, driver assignments, dynamic fare calculation, and payment processing. The system leverages **Apache Kafka** for event-driven architecture and **WebSocket** for real-time bidirectional communication between passengers and drivers.

### Core Services:
- ✅ **User Authentication** - JWT-based secure login/registration
- ✅ **Ride Management** - Request, assign, track, and complete rides
- ✅ **Real-Time Notifications** - Instant updates via WebSocket/SSE
- ✅ **Driver Assignment** - Nearest driver selection using Google Maps API
- ✅ **Dynamic Fare Calculation** - Distance-based with surge pricing support
- ✅ **Payment Processing** - Razorpay integration for secure transactions
- ✅ **Reviews & Ratings** - Passenger and driver rating system

---

## ⭐ Key Features

### 🔐 Authentication & Authorization
- JWT token-based authentication with refresh token support
- Role-based access control (PASSENGER, DRIVER, ADMIN)
- Secure password hashing with Spring Security
- WebSocket JWT validation for real-time endpoints

### 🚗 Ride Management
- Create real-time ride requests
- Intelligent driver assignment (nearest available driver)
- Multi-state ride lifecycle (REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED)
- Ride cancellation with refund processing
- Detailed ride history and analytics

### 📍 Location Services
- Integration with Google Maps Distance Matrix API
- Real-time driver location tracking
- Pickup and drop-off location management
- Optimal driver selection based on proximity

### 💰 Payment System
- **Razorpay** payment gateway integration
- HMAC signature verification for payment security
- Multiple payment methods (UPI, Credit Card, Net Banking)
- Webhook support for payment confirmations
- Payment history and receipts

### 🔔 Real-Time Notifications
- **Event-driven architecture** using Apache Kafka
- **Dual notification delivery**:
  - WebSocket (STOMP) for web clients
  - Server-Sent Events (SSE) for lightweight connections
- Automatic notification persistence to database
- Real-time driver and passenger updates

### ⭐ Reviews & Ratings
- 5-star rating system (1-5)
- Optional comments/feedback
- Passenger-to-driver and driver-to-passenger ratings
- Driver reputation scoring

---

## 🏗️ Architecture

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER (Web/Mobile)                  │
│            ┌──────────────────────────────────────┐                │
│            │   WebSocket (STOMP)  │  SSE Stream   │                │
│            └──────────────────────────────────────┘                │
└────────────────────┬─────────────────────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────────────────────┐
│                    API GATEWAY / CONTROLLER LAYER                  │
│  ┌────────────────────────────────────────────────���─────────────┐ │
│  │  /api/users/*     /api/rides/*    /api/payments/*           │ │
│  │  /api/reviews/*   /api/notifications/*   /ws                │ │
│  └──────────────────────────────────────────────────────────────┘ │
└────────────────────┬──────────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
┌────────▼──────────────┐  ┌────▼──────────────────────┐
│  SERVICE LAYER        │  │  KAFKA EVENT BROKER       │
├──────────────────────┤  ├──────────────────────────┤
│ RideService          │  │  Topics:                 │
│ PaymentService       │  │  • ride-request          │
│ NotificationService  │  │  • ride-notifications    │
│ ReviewService        │  ├──────────────────────────┤
│ KafkaProducerService │  │ Consumers:               │
│ JWTService           │  │ • Dispatch Consumer      │
│ DynamicFareService   │  │ • Persistence Consumer   │
└───────────┬──────────┘  └────┬───────────────────────┘
            │                  │
    ┌───────┴──────────────────┴──────────┐
    │                                      │
┌──▼───────────────┐            ┌────────▼──────────┐
│   DATA LAYER     │            │  THIRD-PARTY      │
├──────────────────┤            ├──────────────────┤
│ Repositories:    │            │ Google Maps API  │
│ • UserRepo       │            │ • Distance Matrix│
│ • RideRepo       │            │ • Direction API  │
│ • DriverRepo     │            ├──────────────────┤
│ • PaymentRepo    │            │ Razorpay API     │
│ • ReviewRepo     │            │ • Order Creation │
│ • NotifRepo      │            │ • Payment Verify │
│                  │            │ • Webhook        │
└──────��───────────┘            └──────────────────┘
         │
┌────────▼────────────────┐
│   MYSQL DATABASE         │
├──────────────────────────┤
│ users, drivers, rides    │
│ payments, reviews        │
│ notifications            │
└──────────────────────────┘
```

### Real-Time Communication Flow

```
┌─────────────┐                    ┌──────────────┐
│  Passenger  │◄──────WebSocket────┤   Server     │
│   (Client)  │      (STOMP)       │              │
└──────┬──────┘                    └──────┬───────┘
       │ requestRide()                    │
       │─────────────────────────────────►│
       │                                  │
       │                          ┌────────────────┐
       │                          │ Save Ride      │
       │                          │ Publish Kafka  │
       │                          │ ride-request   │
       │                          └────────┬───────┘
       │                                  │
       │           ┌──────────────────────┴────────────────────┐
       │           ▼                                           ▼
       │     ┌──────────────┐                        ┌──────────────────┐
       │     │ Persistence  │                        │ Dispatch Consumer│
       │     │ Consumer     │                        │ (Driver Assign)  │
       │     │ (Save to DB) │                        │                  │
       │     └──────────────┘                        └────────┬─────────┘
       │                                                     │
       │                            ┌────────────────────────┴──────┐
       │                            ▼                               ▼
       │                    ┌──────────────────┐        ┌─────────────────┐
       │                    │ Find Nearest     │        │ Publish Kafka   │
       │                    │ Available Driver │        │ ride-notifications
       │                    └──────────────────┘        └────────┬────────┘
       │                                                        │
       │       ◄──────────── Notification Dispatch ────────────┤
       │       │ SSE / WebSocket Push Update              │
       │       │ "Driver Assigned: <Name>"                │
       │◄──────┴───────────────────────────────────────────────┤
       │
       ▼
   [Update UI with driver info + location map]


┌──────────────┐                    ┌──────────────┐
│  Driver App  │◄──────WebSocket────┤   Server     │
│   (Client)   │      (STOMP)       │              │
└──────┬───────┘                    └──────┬───────┘
       │ acceptRide()                      │
       │─────────────────────────────────►│
       │                          ┌────────────────┐
       │                          │ Update Status  │
       │                          │ Publish Kafka  │
       │                          │ ride-notifi... │
       │                          └────────┬───────┘
       │                                  │
       │    ◄───────────────────────────────────┐
       │    │ Notification: "Ride Accepted"     │
       │◄───┴────────────────────────────────────┤
       │
       ▼
   [Show pickup route to passenger location]
```

---

## 🛠️ Technology Stack

### Backend Framework
| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 4.0.5 |
| **Java** | JDK | 24 |
| **Build Tool** | Maven | 3.8+ |
| **ORM** | Hibernate JPA | Included |

### Database & Cache
| Component | Technology | Version |
|-----------|-----------|---------|
| **Primary DB** | MySQL | 8.0+ |
| **Message Broker** | Apache Kafka | 3.x+ |
| **Cache** | In-Memory (Optional) | - |

### Authentication & Security
| Component | Technology | Purpose |
|-----------|-----------|---------|
| **JWT** | JJWT | Token-based authentication |
| **Security** | Spring Security | Authorization & Access Control |
| **Password Hashing** | BCrypt | Credential encryption |

### APIs & Integrations
| Service | Integration | Purpose |
|---------|-------------|---------|
| **Google Maps** | Distance Matrix API | Driver-Passenger distance calculation |
| **Razorpay** | Payment Gateway | Secure payment processing |

### Real-Time Communication
| Technology | Purpose |
|-----------|---------|
| **WebSocket** | STOMP protocol for real-time bidirectional messaging |
| **Server-Sent Events (SSE)** | Lightweight real-time updates (SockJS fallback) |
| **Apache Kafka** | Event streaming for loose coupling & scalability |

### Libraries & Tools
| Library | Version | Purpose |
|---------|---------|---------|
| **Lombok** | Latest | Boilerplate reduction |
| **Jackson** | Latest | JSON serialization |
| **ModelMapper** | 3.1.1 | DTO mapping |
| **Razorpay Java SDK** | 1.4.3 | Payment SDK |

---

## 💻 System Design

### 1. Authentication Flow
```
User Registration/Login
         │
         ▼
    Validate Input
         │
         ▼
    Check Existing User
         │
    ┌────┴────┐
    ▼         ▼
  Yes       No
   │         │
   │     Create User
   │      Hash Password
   │         │
   └────┬────┘
        ▼
   Generate JWT Token
        │
        ▼
   Return Token + Refresh Token
```

### 2. Ride Request Flow
```
Passenger Request
       │
       ▼
Calculate Fare (Google Maps + Distance)
       │
       ▼
Create Ride (REQUESTED status)
       │
       ▼
Publish to Kafka: ride-request
       │
    ┌──┴──┐
    ▼     ▼
  Save  Dispatch
  to DB  Assigned Driver
    │     │
    └──┬──┘
       ▼
Notify Both Parties
```

### 3. Payment Processing
```
Ride Completion
      │
      ▼
Generate Razorpay Order
      │
      ▼
Client Pays via Razorpay
      │
      ▼
Verification (Client + Webhook)
      │
   ┌──┴──┐
   ▼     ▼
Success Failure
   │     │
   ▼     ▼
 PAID  FAILED
   │     │
   └──┬──┘
      ▼
Notify Driver & Passenger
      │
      ▼
Publish Kafka: ride-notifications
```

---

## 📋 Prerequisites

Before running the project, ensure you have:

### Required Software
- ✅ **Java 24+** - [Download JDK 24](https://www.oracle.com/java/technologies/downloads/)
- ✅ **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/mysql/)
- ✅ **Apache Kafka 3.x+** - [Download Kafka](https://kafka.apache.org/downloads)
- ✅ **Maven 3.8+** - [Download Maven](https://maven.apache.org/download.cgi)
- ✅ **Git** - [Download Git](https://git-scm.com/)

### API Keys & Credentials
- 🔑 **Google Maps API Key** - [Get API Key](https://developers.google.com/maps/documentation/distance-matrix/get-api-key)
- 💳 **Razorpay Account** - [Sign Up](https://razorpay.com/) (Test keys for development)
- 🗄️ **MySQL Database** - Create database `cabbie`

### Optional (for Frontend Integration)
- 📱 **Browser with WebSocket Support** - Chrome, Firefox, Edge, Safari
- 🔗 **REST Client** - Postman or Insomnia
- 📡 **STOMP Client Library** - stompjs for JavaScript

---

## ⚙️ Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/Naseem999/cabbie.git
cd cabbie
```

### Step 2: Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/cabbie
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT Configuration
security.jwt.secret-key=your_super_secret_key_here
security.jwt.expiration-time=360000

# Google Maps API
google.maps.api.key=YOUR_GOOGLE_MAPS_API_KEY

# Razorpay Configuration
razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.group-id=cab-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### Step 3: Create MySQL Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE cabbie;
USE cabbie;
```

### Step 4: Start Apache Kafka

**From Kafka directory, in 2 separate terminals:**

**Terminal 1 - Start Zookeeper:**
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

**Terminal 2 - Start Kafka Broker:**
```bash
bin/kafka-server-start.sh config/server.properties
```

Kafka will start at `localhost:9092`

### Step 5: Build & Run the Application

```bash
# Build the project
./mvnw.cmd clean package -DskipTests=false

# Run the Spring Boot application
./mvnw.cmd spring-boot:run
```

The application will start at: **http://localhost:8080**

### Step 6: Verify Everything is Running

```bash
# Check API health
curl http://localhost:8080/actuator/health

# Test WebSocket endpoint
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  http://localhost:8080/ws
```

---

## 📊 Configuration

### JWT Configuration
```properties
# Token Secret (Production: use environment variable)
security.jwt.secret-key=jwtTokenSecurityAuthAndRegistrationNaseemWorkingOnAProjectCabbie

# Token Expiration Time (in milliseconds)
# 360000ms = 6 minutes (for testing), increase for production
security.jwt.expiration-time=360000
```

### Kafka Topics (Auto-Created)
The application automatically creates these Kafka topics on startup:

| Topic | Consumer Groups | Purpose |
|-------|-----------------|---------|
| `ride-request` | rideRequest-dispatch, rideRequest-persistence | New ride requests |
| `ride-notifications` | notification-dispatch, notification-persistence | All notification events |

### WebSocket Configuration
- **Endpoint**: `/ws`
- **Broker Prefix**: `/queue`
- **App Destination Prefix**: `/app`
- **User Destination Prefix**: `/user`
- **Protocol**: STOMP over SockJS
- **Authentication**: JWT Bearer token in CONNECT frame

---

## 📁 Project Structure

```
cabbie/
├── src/
│   ├── main/
│   │   ├── java/com/app/cabbie/
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   ├── RidesController.java
│   │   │   │   ├── PaymentsController.java
│   │   │   │   ├── ReviewsController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── FareEstimationController.java
│   │   │   │   ├── DriverController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RideService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── NotificationService.java
│   ��   │   │   ├── ReviewAndRatingService.java
│   │   │   │   ├── KafkaProducerService.java
│   │   │   │   ├── KafkaConsumerService.java (2 implementations)
│   │   │   │   ├── DynamicFareCalculationService.java
│   │   │   │   ├── JWTService.java
│   │   │   │   └── DriverService.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RidesRepository.java
│   │   │   │   ├── DriverRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   ��   ├── ReviewRepository.java
│   │   │   │   └── NotificationRepository.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Ride.java
│   │   │   │   ├── Driver.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Review.java
│   │   │   │   └── Notification.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── UserRegisterDTO.java
│   │   │   │   ├── UserLoginDTO.java
│   │   │   │   ├── RideRequestDTO.java
│   │   │   │   ├── LocationDTO.java
│   │   │   │   ├── PaymentDetailsDTO.java
│   │   │   │   ├── RatingDTO.java
│   │   │   │   ├── NotificationDTO.java
│   │   │   │   ├── KafkaEventDTO.java
│   │   │   │   └── ErrorResponseDTO.java
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   ├── DriverStatus.java
│   │   │   │   ├── RideStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   ├── PaymentMethod.java
│   │   │   │   └── UserRole.java
│   │   │   │
│   │   │   ��── configuration/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JWTFilter.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── KafkaTopicConfig.java
│   │   │   │   ├── AppConfig.java
│   │   │   │   └── JwtChannelInterceptorForWebSocketSecurity.java
│   │   │   │
│   │   │   ├── sse/
│   │   │   │   ├── SseController.java
│   │   │   │   └── SseEmitterRegistry.java
│   │   │   │
│   │   │   ├── exceptions/
│   │   │   │   └── UserNotFoundException.java
│   │   │   │
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java
│   │   │   │
│   │   │   └── CabbieApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/
│   │       └── static/
│   │
│   └── test/
│       ├── java/com/app/cabbie/
│       │   └── CabbieApplicationTests.java
│       └── resources/
│
├── pom.xml
├── mvnw & mvnw.cmd
├── README.md
├── AGENTS.md
└── .gitignore
```

---

## 🔌 API Documentation

### Authentication APIs

#### 1. User Registration
```http
POST /api/users/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "phoneNumber": "+919876543210",
  "role": "PASSENGER"
}

Response (201 Created):
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+919876543210",
  "role": "PASSENGER",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### 2. User Login
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 360000
}
```

### Ride APIs

#### 3. Request a Ride
```http
POST /api/rides/request
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 1,
  "pickupLocation": "Times Square, NYC",
  "dropLocation": "Central Park, NYC",
  "pickupLocationDTO": {
    "latitude": 40.7580,
    "longitude": -73.9855
  },
  "dropLocationDTO": {
    "latitude": 40.7829,
    "longitude": -73.9654
  },
  "rideType": "ECONOMY"
}

Response (201 Created):
{
  "id": 1,
  "passengerId": 1,
  "pickupLocation": "Times Square, NYC",
  "dropLocation": "Central Park, NYC",
  "fare": 15.50,
  "rideStatus": "REQUESTED",
  "createdAt": "2024-01-15T10:35:00"
}
```

#### 4. Accept Ride (Driver)
```http
POST /api/rides/{rideId}/accept
Authorization: Bearer {driverToken}
Content-Type: application/json

Response (200 OK):
{
  "id": 1,
  "rideStatus": "ACCEPTED",
  "driverId": 5,
  "updatedAt": "2024-01-15T10:36:00"
}
```

#### 5. Update Ride Status
```http
PUT /api/rides/{rideId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "COMPLETED"
}

Response (200 OK):
{
  "id": 1,
  "rideStatus": "COMPLETED",
  "updatedAt": "2024-01-15T10:45:00"
}
```

#### 6. Get Ride Details
```http
GET /api/rides/{rideId}
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "passengerId": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "driverId": {
    "id": 5,
    "user": {
      "name": "Ahmed Khan",
      "email": "ahmed@example.com"
    },
    "vechicalNumber": "NY-AB-1234"
  },
  "fare": 15.50,
  "rideStatus": "COMPLETED",
  "createdAt": "2024-01-15T10:35:00",
  "updatedAt": "2024-01-15T10:45:00"
}
```

### Payment APIs

#### 7. Create Payment Order
```http
POST /api/payments/create-order
Authorization: Bearer {token}
Content-Type: application/json

{
  "rideId": 1,
  "amount": 15.50
}

Response (201 Created):
{
  "orderId": "order_1A2B3C4D",
  "amount": 1550,
  "currency": "INR",
  "keyId": "rzp_test_xxxxxx"
}
```

#### 8. Verify Payment
```http
POST /api/payments/verify-payment
Authorization: Bearer {token}
Content-Type: application/json

{
  "razorpayOrderId": "order_1A2B3C4D",
  "razorpayPaymentId": "pay_1A2B3C4D",
  "razorpaySignature": "9ef4dffbfd84f1318f6739a3ce19f9d85851857ae648f114332d8401e0949a3d"
}

Response (200 OK):
{
  "status": "PAID",
  "message": "Payment Successful",
  "Payment Id": "pay_1A2B3C4D"
}
```

### Review APIs

#### 9. Submit Review
```http
POST /api/reviews/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "reviewerId": 1,
  "targetId": 5,
  "rating": 5,
  "comment": "Great driver, very professional!"
}

Response (201 Created):
{
  "message": "Review Created"
}
```

#### 10. Get Driver Reviews
```http
GET /api/reviews/driver/{driverId}
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 1,
    "reviewerId": 1,
    "targetId": 5,
    "rating": 5,
    "comment": "Great driver, very professional!",
    "createdAt": "2024-01-15T10:50:00"
  }
]
```

### Notification APIs

#### 11. Subscribe to SSE Notifications
```http
GET /api/user/notifications/subscribe?t=Bearer%20{token}
Content-Type: text/event-stream

# Response is a stream of events:
data: {
  "userId": 1,
  "userEmail": "john@example.com",
  "title": "Driver Assigned",
  "message": "Ahmed Khan is on the way!",
  "createdAt": "2024-01-15T10:36:00"
}
```

---

## 🔔 Real-Time Notification Flow

### WebSocket (STOMP) Connection

#### Connect with JWT Authentication
```javascript
const token = localStorage.getItem('token');
const socket = new SockJS('http://localhost:8080/ws');
const stomp = Stomp.over(() => socket);

stomp.connect(
  {
    'Authorization': 'Bearer ' + token
  },
  function(frame) {
    console.log('Connected:', frame);
    
    // Subscribe to personal notification queue
    stomp.subscribe('/user/queue/notifications', function(message) {
      const notification = JSON.parse(message.body);
      console.log('Notification received:', notification);
      updateUI(notification);
    });
  },
  function(error) {
    console.error('Connection error:', error);
  }
);
```

#### Listen to Notifications
```javascript
stomp.subscribe('/user/queue/notifications', function(message) {
  const notification = JSON.parse(message.body);
  
  console.log('Title:', notification.title);
  console.log('Message:', notification.message);
  console.log('Timestamp:', notification.createdAt);
  
  // Show toast/alert to user
  showNotification(notification.title, notification.message);
});
```

### Notification Types

| Event | When | Message Example |
|-------|------|-----------------|
| **RIDE_REQUESTED** | Passenger requests ride | "Your ride has been requested. Finding driver..." |
| **DRIVER_ASSIGNED** | Driver assigned to ride | "Ahmed Khan is on the way! ETA: 5 min" |
| **RIDE_ACCEPTED** | Driver accepts ride | "Ahmed Khan has accepted your ride" |
| **RIDE_STARTED** | Ride begins | "Ahmed Khan is pickin you up now" |
| **RIDE_COMPLETED** | Ride ends | "Ride completed! Fare: ₹15.50" |
| **RIDE_CANCELED** | Ride canceled | "Ride has been canceled" |
| **PAYMENT_SUCCESSFUL** | Payment confirmed | "Payment of ₹15.50 successful!" |
| **PAYMENT_FAILED** | Payment failed | "Payment failed. Please retry." |
| **REVIEW_REQUEST** | Requesting user review | "Please rate your ride with Ahmed Khan" |

---

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20),
  role ENUM('PASSENGER', 'DRIVER', 'ADMIN') DEFAULT 'PASSENGER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Drivers Table
```sql
CREATE TABLE drivers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNIQUE NOT NULL,
  vehicle_model VARCHAR(50),
  vehicle_number VARCHAR(20),
  status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') DEFAULT 'OFFLINE',
  current_location_lat DOUBLE,
  current_location_lng DOUBLE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Rides Table
```sql
CREATE TABLE rides (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  passenger_id BIGINT NOT NULL,
  driver_id BIGINT,
  pickup_location VARCHAR(255),
  drop_location VARCHAR(255),
  pickup_location_latitude DOUBLE,
  pickup_location_longitude DOUBLE,
  drop_location_latitude DOUBLE,
  drop_location_longitude DOUBLE,
  fare DOUBLE NOT NULL,
  ride_type VARCHAR(50),
  status ENUM('REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED') DEFAULT 'REQUESTED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (passenger_id) REFERENCES users(id),
  FOREIGN KEY (driver_id) REFERENCES drivers(id),
  INDEX idx_passenger (passenger_id),
  INDEX idx_driver (driver_id),
  INDEX idx_status (status)
);
```

### Payments Table
```sql
CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ride_id BIGINT UNIQUE NOT NULL,
  amount DOUBLE NOT NULL,
  payment_status ENUM('UNPAID', 'PAID', 'FAILED') DEFAULT 'UNPAID',
  payment_method ENUM('UPI', 'CREDIT_CARD', 'NET_BANKING', 'MOCK') DEFAULT 'MOCK',
  payment_gateway_order_id VARCHAR(255),
  payment_gateway_payment_id VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
  INDEX idx_status (payment_status)
);
```

### Reviews Table
```sql
CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reviewer_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  rating INT CHECK (rating >= 1 AND rating <= 5),
  comment VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (target_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_target (target_id)
);
```

---

## 🧪 Testing

### Running Unit Tests
```bash
./mvnw.cmd test
```

### Running Integration Tests
```bash
./mvnw.cmd integration-test
```

### Testing with Postman

1. **Import Collection** - Use the provided Postman collection
2. **Set Environment Variables**:
   - `base_url`: http://localhost:8080
   - `token`: (auto-populated after login)
3. **Run Test Suite**: Send requests in sequence

### Manual WebSocket Testing

**Using websocat (CLI tool):**
```bash
# Install: npm install -g websocat
websocat ws://localhost:8080/ws
```

**Using JavaScript (Browser Console):**
```javascript
// Open browser developer console

const socket = new SockJS('http://localhost:8080/ws');
const stomp = Stomp.over(() => socket);
stomp.connect(
  {'Authorization': 'Bearer YOUR_TOKEN'},
  () => console.log('Connected!')
);
```

---

## 🐛 Troubleshooting

### Issue: "Connection refused" to MySQL
**Solution**: Ensure MySQL is running:
```bash
# Windows
net start MySQL80

# Mac
brew services start mysql

# Linux
sudo service mysql start
```

### Issue: Kafka bootstrap server not reachable
**Solution**: Start Kafka broker:
```bash
bin/kafka-server-start.sh config/server.properties
```

### Issue: JWT token expired
**Solution**: Login again to get a new token:
```bash
POST /api/users/login
```

### Issue: "No message found for status 404"
**Solution**: Ensure the ride ID exists:
```bash
GET /api/rides/{existing_ride_id}
```

### Issue: PaymentService NullPointerException
**Solution**: Verify Razorpay credentials are set in `application.properties`

### Issue: WebSocket connection fails
**Solution**: Ensure JWT token is valid and passed in CONNECT frame headers

### Issue: Kafka topics not created
**Solution**: Check Kafka logs and restart Kafka broker:
```bash
# Kill existing Kafka
pkill -f kafka

# Start Kafka fresh
bin/kafka-server-start.sh config/server.properties
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the Repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/cabbie.git
   cd cabbie
   ```

2. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Changes & Commit**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

4. **Push to Branch**
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Open Pull Request**
   - Provide detailed description
   - Reference related issues
   - Include screenshots if UI-related

### Code Style Guide
- Follow Google Java Style Guide
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features
- Keep methods small and focused

---

## 📝 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

```
MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 📞 Contact & Support

- **Report Issues**: [GitHub Issues](https://github.com/Naseem999/cabbie/issues)
- **Email**: naseem@example.com
- **Discord**: Join our community server
- **Documentation**: [Wiki](https://github.com/Naseem999/cabbie/wiki)

---

## 🎓 Learning Resources

### Spring Boot
- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/projects/spring-security)
- [Spring Data JPA Tutorial](https://spring.io/projects/spring-data-jpa)

### Kafka
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Guide](https://spring.io/projects/spring-kafka)

### Real-Time Communication
- [WebSocket Protocol](https://datatracker.ietf.org/doc/html/rfc6455)
- [STOMP Protocol](https://stomp.github.io/)
- [Server-Sent Events MDN](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)

---

## 🚀 Roadmap

- [ ] Mobile App (React Native/Flutter)
- [ ] Admin Dashboard with Analytics
- [ ] Advanced Surge Pricing Algorithm
- [ ] Multi-language Support
- [ ] In-app Chat between Driver & Passenger
- [ ] Rating Analytics & Driver Performance Metrics
- [ ] Scheduled Rides Feature
- [ ] Split Fare (Ride Sharing) Function
- [ ] Emergency Support Button
- [ ] Blockchain-based Payment Verification

---

**Made with ❤️ by Naseem | Last Updated: June 9, 2026**

⭐ **If you find this project helpful, please star it on GitHub!** ⭐


