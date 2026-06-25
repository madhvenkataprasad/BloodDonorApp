# LifeLink — Blood Donor Emergency Web App

Scalable blood donation matching platform built with **HTML, CSS, JavaScript**, **Spring Boot 3**, and **MySQL**.

## Features

| Role | Capabilities |
|------|----------------|
| **Donor** | Profile (name, age 18+, blood group, diseases, phone, address, GPS location). Upload government ID, blood certificate, and/or recent blood test report. |
| **Receiver** | Search by blood group, your location, and **dynamic radius (km)**. Results show verified donors 18+ sorted by distance with phone to call. |
| **Admin** | Review documents, approve/reject each file, approve/reject donor profile. Only **admin-approved** donors appear in search. |

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+ running locally

## Setup

1. Create MySQL database (or let the app create it):

```sql
CREATE DATABASE blood_donor_db;
```

2. Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=YOUR_MYSQL_USER
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

3. Run the application:

```bash
mvn spring-boot:run
```

4. Open [http://localhost:8080](http://localhost:8080)

## Default admin account

Created automatically on first startup:

- **Email:** `admin@blooddonor.local`
- **Password:** `admin123`

Change these in `application.properties` (`app.admin.email`, `app.admin.password`) before production.

## API overview

| Endpoint | Role |
|----------|------|
| `POST /api/auth/register` | Public (DONOR or RECEIVER) |
| `POST /api/auth/login` | Public |
| `GET /api/auth/me` | Authenticated |
| `POST /api/donor/profile` | DONOR |
| `POST /api/donor/documents` | DONOR (multipart) |
| `POST /api/search/donors` | RECEIVER, ADMIN |
| `GET /api/admin/*` | ADMIN |

## Scalability notes

- **HikariCP** connection pool (20 max connections configured)
- **Indexed** columns: email, blood group, verification status, lat/lng
- **Stateless-ready** session auth; horizontal scale possible behind a load balancer with sticky sessions or future JWT
- **Haversine** distance filter in application layer (upgrade to PostGIS/spatial index for very large datasets)

## Project structure

```
src/main/java/com/blooddonor/
  config/       Security, web, data init
  controller/   REST APIs
  entity/       JPA models
  service/      Business logic
src/main/resources/static/
  index.html, css/, js/, pages/
```

## Workflow

1. Donor registers → completes profile → uploads proofs.
2. Admin reviews each document → approves donor when ID + blood proof are approved.
3. Receiver logs in → sets location + blood group + radius → calls nearest verified donor.

Uploaded files are stored in the `uploads/` folder at the project root.
