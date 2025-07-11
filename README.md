## 🟩 Backend – `README.md` for the Java Spring Boot API

# Movie Matchmaker – Backend (Spring Boot)

This is the backend REST API for **Movie Matchmaker**, built using **Java Spring Boot**. It handles user management, preference storage, authentication, and coordination with the Flask-based recommendation engine.

---

## 🔍 Overview

The backend authenticates users using JWT tokens, manages user preferences in a relational schema, and facilitates communication between the frontend and the recommendation engine. It also enriches movie data using the RapidAPI Streaming Availability service.

---

## ✅ Key Features

- ✅ **JWT-based Authentication System** with Spring Security and BCrypt
- ✅ **User Preference Submission** (ranked genres, streaming services)
- ✅ **Dual-user Matching Support**
- ✅ **Relational Database Schema**
  - `User → Preferences → StreamingServices`
- ✅ **Cross-Service Communication**
  - Calls Python Flask microservice for recommendations all in real-time
    - Integrates with RapidAPI for movie metadata (poster, overview, platform availability)

---

## 🔄 In Progress

- 🔄 Integrating enriched metadata (poster, overview, platform availability) via RapidAPI
- 🔄 Add filtering by release year or rating
- 🔄 Logging and audit trail for security

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security + JWT**
- **JPA (Hibernate) + PostgreSQL/MySQL**
- **WebClient (WebFlux)** for async HTTP calls
- **RapidAPI Integration** for movie data enrichment
- **Docker** for containerization
- **Maven** for dependency management
- **JUnit + Mockito** for testing
- **Postman** for API testing

---

## 🧩 Architecture Diagram
- Frontend (Next.js)
- ↓
- Spring Boot Backend
- ↓    _____________    ↘
- DB (MySQL)   Python Recommender (Flask)
- ↓
- Top N Similar Movies


## 📦 Getting Started

```bash
git clone https://github.com/your-username/movie-matchmaker-backend.git
cd movie-matchmaker-backend
./mvnw spring-boot:run

Notes:
- Ensure you have your own application.properties file with database and JWT configurations.
- The Flask recommendation engine should be running and accessible.

