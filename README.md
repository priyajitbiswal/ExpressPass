# Ticket Booking System

A simple Java-based Ticket Booking System for booking train tickets, managing users, and handling train data. Built with Gradle and uses JSON files for local data storage.

## Features

- User registration and management
- Train listing and search
- Ticket booking and management
- Data persistence using JSON files

## Project Structure

- `app/src/main/java/ticket/booking/entities/` - Entity classes (`User`, `Train`, `Ticket`)
- `app/src/main/java/ticket/booking/service/` - Service classes for business logic
- `app/src/main/java/ticket/booking/util/` - Utility classes
- `app/src/main/java/ticket/booking/localDb/` - Local JSON data files (`users.json`, `trains.json`)
- `app/src/main/java/ticket/booking/App.java` - Main application entry point

## Getting Started

### Prerequisites

- Java 11 or higher
- Gradle (wrapper included)

### Build & Run

```sh
./gradlew build
./gradlew run --project-dir app
