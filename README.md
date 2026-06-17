# CodePrep

An interactive Android learning platform designed to help users prepare for coding interviews through gamified lessons, quizzes, and interactive coding challenges. Built with modern Android technologies including Jetpack Compose, Firebase, and Room database.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Setup & Installation](#setup--installation)
- [Build & Development](#build--development)
- [Key Conventions](#key-conventions)
- [Database Schema](#database-schema)
- [Contributing](#contributing)

## ✨ Features

- **Gamified Learning**: Track progress with hearts, XP, levels, and streaks
- **Interactive Lessons**: Learn coding concepts with structured lessons and code snippets
- **Quiz System**: Test your knowledge with multiple-choice and coding quizzes
- **User Progress Tracking**: Monitor lesson completion, perfect runs, and achievement history
- **Offline Support**: Local Room database caching with Firestore synchronization
- **AI-Powered Explanations**: Integration with OpenRouter API for code explanations
- **Fun Facts Widget**: Home screen widget for daily coding fun facts
- **Session Management**: Heart refill system and lesson unlock progression

## 🛠 Tech Stack

### Frontend
- **Jetpack Compose**: Modern declarative UI framework
- **Material 3**: Design system and components
- **Compose Navigation**: Type-safe navigation between screens

### Backend & Data
- **Firebase Authentication**: Secure user login and registration
- **Firestore**: Cloud database for courses, lessons, and user data
- **Room Database**: Local caching and offline support
- **Retrofit + OkHttp**: HTTP client for API calls
- **Gson**: JSON serialization/deserialization

### Architecture & DI
- **Hilt**: Dependency injection framework
- **Coroutines & Flow**: Asynchronous programming and reactive data streams
- **ViewModel**: UI state management

### Other Libraries
- **Multiplatform Markdown Renderer**: Markdown rendering with code highlighting
- **WorkManager**: Background task scheduling (with Hilt integration)
- **Play Services Auth**: Google authentication support

## 🏗 Architecture

### High-Level Overview

CodePrep follows a **single-module, layered architecture** with clear separation of concerns:
