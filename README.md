# Office Inventory - Backend

Backend service for the Office Inventory application.

## Technology Stack

- Java 21
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA
- PostgreSQL
- Maven

## Backend Port

The backend runs on:

http://localhost:9001

## Prerequisites

Make sure the following are installed:

- Java 21
- Maven
- PostgreSQL

Check Java:

```bash
java -version


Steps to add users

Steps to create DB

1. sudo -u postgres psql
2. CREATE DATABASE office_inventory;
3.
INSERT INTO users (id, username, password, role)
VALUES
(gen_random_uuid(), 'admin', 'password', 'ADMIN'),
(gen_random_uuid(), 'creator', 'password', 'CREATOR'),
(gen_random_uuid(), 'purchaser', 'password', 'PURCHASER'),
(gen_random_uuid(), 'manager', 'password', 'MANAGER');
