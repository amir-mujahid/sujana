Good—now we can formalize this properly like a **real enterprise / FYP system documentation**. I’ll write this in a structured “Chapter-style” format so you can directly reuse it for report submission.

---

# 📘 **I-SUJANA: Smart Municipal Waste Collection & Logistics System**

---

# **CHAPTER 1: INTRODUCTION**

## 1.1 Project Title

**I-Sujana: Smart Municipal Waste Collection and Logistics Management System for Schools and Communities**

---

## 1.2 Background

Waste management in many regions is still handled using manual communication methods such as phone calls or messaging applications (e.g., WhatsApp). In the context of schools and local communities in Selangor, waste collection requests are often unstructured, inefficient, and lack real-time tracking.

Currently:

* Schools manually contact MPS (Majlis Perbandaran Selayang)
* No centralized scheduling system exists
* No real-time tracking of waste collection
* Contributors (households or collection points) depend on informal coordination

This leads to:

* Delays in waste collection
* Poor coordination between schools, riders, and MPS
* Lack of operational transparency
* Inefficient resource allocation

---

## 1.3 Problem Statement

1. Waste collection requests are handled manually via messaging apps
2. No centralized platform for schools and MPS coordination
3. Lack of real-time tracking for riders and collection status
4. Inefficient routing between contributors, schools, and MPS centers
5. No structured role-based system for waste management operations

---

## 1.4 Objectives

* To develop a centralized waste management system for schools and MPS
* To digitize waste pickup requests and scheduling
* To implement real-time tracking for riders
* To improve efficiency in waste collection routing
* To support role-based access control (RBAC) for different stakeholders
* To integrate contributor → rider → school → MPS workflow

---

## 1.5 Project Scope

The system includes:

### Users:

* Super Admin
* MPS Admin
* MPS Dispatcher
* School Admin
* School Staff
* Rider/Driver
* Contributor (households / waste points)

### Functional Scope:

* Waste pickup request system
* Multi-stage logistics workflow
* Rider assignment and tracking
* Role-based access control
* Real-time status updates
* Analytics dashboard for MPS

---

## 1.6 System Overview (Concept)

The system operates in **two integrated models**:

### A. Grab-like Collection Model (Core Feature)

* Contributor requests waste pickup
* Rider collects waste from contributor house
* Rider delivers waste to designated school collection center

### B. School-to-MPS Model (New Expansion)

* Schools submit waste pickup requests to MPS
* MPS schedules and assigns riders
* Riders collect and transport waste accordingly

---

# **CHAPTER 2: SYSTEM ANALYSIS**

---

## 2.1 Proposed System

The proposed system is a **multi-tenant municipal waste management platform** that integrates logistics, scheduling, and real-time tracking.

---

## 2.2 User Roles (RBAC System)

### 🔴 Super Admin

* System-wide control
* Manage MPS tenants
* Global analytics and monitoring

---

### 🟠 MPS Level

#### MPS Admin

* Manage schools under jurisdiction
* Manage users and roles
* View all waste operations

#### MPS Dispatcher

* Receive waste requests
* Assign riders
* Schedule pickups
* Optimize routes

---

### 🟡 School Level

#### School Admin

* Submit waste pickup requests
* Manage school waste points
* Track request status

#### School Staff

* Assist in submitting requests
* Monitor pickups

---

### 🟢 Operational Level

#### Rider / Driver

* Collect waste from contributors
* Transport to schools or disposal points
* Update delivery status

---

### 🔵 Contributor

* Request waste pickup
* Provide pickup location
* Track request status

---

## 2.3 System Workflow

### Workflow 1: Contributor Collection Flow (Grab Concept)

1. Contributor submits pickup request
2. System assigns rider
3. Rider collects waste from contributor house
4. Rider transports waste to school collection center
5. Status updated to COMPLETED

---

### Workflow 2: School to MPS Flow

1. School submits waste pickup request
2. MPS Dispatcher receives request
3. Dispatcher assigns rider
4. Rider collects and transports waste
5. MPS updates system records

---

## 2.4 Use Case Summary

* Submit waste pickup request
* Assign rider to task
* Track live pickup status
* Manage users and roles
* View waste analytics
* Update delivery status
* Maintain audit logs

---

# **CHAPTER 3: SYSTEM DESIGN**

---

## 3.1 Architecture Overview

The system follows:

> **Clean Architecture + MVVM + Modular Design**

---

### Presentation Layer

* Jetpack Compose UI
* Navigation Compose
* ViewModel (state management)

---

### Domain Layer

* Use Cases (business logic)
* Entities (core models)
* Repository interfaces

---

### Data Layer

* Firebase Firestore
* Firebase Auth
* Room Database (offline cache)
* Retrofit (future APIs)
* DataStore (session storage)

---

## 3.2 High-Level Architecture Flow

```
Jetpack Compose UI
        ↓
ViewModel (MVVM)
        ↓
Use Cases (Domain Layer)
        ↓
Repository
        ↓
Firebase / Room / API
```

---

## 3.3 Database Design (Firestore)

### users

* userId
* name
* role
* tenantId
* contact

---

### requests

* requestId
* type (CONTRIBUTOR / SCHOOL)
* status (PENDING / ASSIGNED / COMPLETED)
* location
* createdAt

---

### assignments

* assignmentId
* requestId
* riderId
* dispatcherId
* status

---

### schools

* schoolId
* mpsId
* location

---

### audit_logs

* logId
* actorId
* action
* timestamp

---

## 3.4 Role-Based Access Control (RBAC)

Implemented at:

* Firebase Security Rules
* Application Layer
* Backend validation (Cloud Functions)

---

# **CHAPTER 4: SYSTEM IMPLEMENTATION**

---

## 4.1 Technology Stack

### Mobile App

* Kotlin
* Jetpack Compose
* Material 3

### Architecture

* MVVM
* Clean Architecture
* Hilt Dependency Injection

### Data & Networking

* Firebase (Auth, Firestore, Storage)
* Retrofit
* Room
* Paging 3
* DataStore

### Background Processing

* WorkManager

### Image Loading

* Coil

---

## 4.2 Key Features

* Waste pickup request system
* Real-time rider tracking
* Multi-role system (RBAC)
* School-MPS integration
* Contributor pickup system
* Analytics dashboard
* Offline caching support

---

# **CHAPTER 5: SYSTEM SECURITY**

---

## 5.1 Critical Security Measures

* HTTPS enforcement
* Firebase Authentication
* Role-based access control
* Secure token storage (DataStore)
* Backend validation
* Firebase security rules
* Input validation
* Dependency updates
* Logging and monitoring

---

## 5.2 High Priority Security

* Certificate pinning
* Database encryption (sensitive fields)
* Code obfuscation (R8/Proguard)
* Rate limiting (Cloud Functions)
* Audit logs

---

## 5.3 Enterprise Security

* Root detection (Play Integrity API)
* Anti-tampering mechanisms
* SIEM integration (BigQuery / external logs)
* Zero Trust architecture (verify every request)

---

# **CHAPTER 6: SYSTEM OPTIMIZATION**

---

## 6.1 Database Optimization Strategy

| Priority | Technique                   |
| -------- | --------------------------- |
| 1        | Proper indexing             |
| 2        | Pagination (Paging 3)       |
| 3        | Aggregate once, read many   |
| 4        | Cache frequently used data  |
| 5        | Avoid N+1 queries           |
| 6        | Select only required fields |
| 7        | Batch writes                |
| 8        | Archive old data            |
| 9        | Server-side calculations    |
| 10       | Query monitoring            |

---

## 6.2 Performance Enhancements

* Baseline Profiles (faster app startup)
* Lazy loading (Compose)
* Offline caching (Room)
* Background sync (WorkManager)

---

# **CHAPTER 7: RELIABILITY & SCALABILITY**

* Multi-tenant architecture
* Cloud-based backend (Firebase scalable)
* Modular app design
* Stateless API design (future expansion)
* Horizontal scaling ready

---

# **CHAPTER 8: TESTING & QUALITY ASSURANCE**

* Unit testing (ViewModel + Use Cases)
* Integration testing (Repository layer)
* UI testing (Compose testing)
* Security testing (rules validation)
* Performance testing (baseline profiling)

---

# **CHAPTER 9: OBSERVABILITY**

* Firebase Crashlytics
* Firebase Analytics
* Performance Monitoring
* Audit logs system
* Request tracing IDs

---

# **CHAPTER 10: CONCLUSION**

The I-Sujana system evolves from a simple recycling logistics app into a **fully scalable municipal waste management platform** integrating:

* Grab-style pickup logistics
* School-to-MPS waste management system
* Role-based enterprise architecture
* Real-time tracking and analytics
* Strong security and scalability design

This system is designed to support expansion across multiple municipalities and can evolve into a national-level smart waste infrastructure platform.

---
