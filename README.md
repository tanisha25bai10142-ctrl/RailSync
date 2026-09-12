# RailSync – Intelligent Train Reservation & Dynamic Seat Management System

[![Java Version](https://img.shields.io/badge/Java-17%2B%20%7C%2025-orange.svg)](https://openjdk.org/)
[![GUI Framework](https://img.shields.io/badge/GUI-Java%20Swing-blue.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%2F%20Layered-green.svg)]()
[![Verification](https://img.shields.io/badge/Tests-15%2F15%20Passed-brightgreen.svg)]()

> A comprehensive, industrial-grade, and 100% pure Core Java desktop application designed for the **"Programming in Java"** course curriculum. Developed strictly without web frameworks or external third-party dependencies, demonstrating advanced Core Java concepts: OOP, Collections Framework, Multithreading & Synchronization, Exception Handling, File I/O & Serialization, Generics, Lambdas, and Modern Swing GUI design.

---

## 📑 Table of Contents
- [Project Overview](#-project-overview)
- [Problem Statement](#-problem-statement)
- [Key Objectives](#-key-objectives)
- [Core Features](#-core-features)
- [Core Java Concepts Demonstrated](#-core-java-concepts-demonstrated)
- [Project Architecture & Directory Structure](#-project-architecture--directory-structure)
- [Sample Credentials](#-sample-credentials)
- [How to Build and Run](#-how-to-build-and-run)
- [Automated Verification Suite](#-automated-verification-suite)
- [Step-by-Step Viva & Demo Workflow](#-step-by-step-viva--demo-workflow)

---

## 🚆 Project Overview
**RailSync** simulates an authentic Indian Railways passenger reservation and dynamic seat inventory system. It models the end-to-end lifecycle of railway operations: searching trains across routes, calculating dynamic distance-based fares with age concessions, allocating confirmed coach berths, managing FIFO queues for **Reservation Against Cancellation (RAC)** and **Waiting List (WL)**, orchestrating automatic promotion cascades upon cancellations, providing interactive PNR enquiries, and simulating high-concurrency surge booking traffic with thread-safe synchronization.

---

## 🎯 Problem Statement
Conventional student railway reservation projects are typically simplistic, sequential console scripts that hard-code booking outcomes and lack real-world inventory dynamics. In contrast, real railway platforms require:
1. **Dynamic Inventory Allocation**: Transitioning seamlessly from Confirmed berths to RAC shared seats, and subsequently to Waiting List queues.
2. **Cancellation Promotion Cascade**: When a confirmed passenger cancels, an RAC passenger must instantly be promoted to Confirmed status with a physical berth assigned, and the foremost Waiting List passenger must be elevated into the RAC queue.
3. **Thread Safety & Race Conditions**: High-surge booking traffic where multiple concurrent threads target the exact same remaining seats must guarantee zero duplicate allocations without deadlocks.
4. **Data Persistence**: Preserving deep object graphs (trains, coaches, seats, queued tickets) without relying on heavyweight SQL databases or third-party ORMs.

---

## 🌟 Key Objectives
- To implement an authentic, object-oriented simulation of train ticketing using pure Java standard libraries.
- To demonstrate **polymorphism and inheritance** via specialized train classes (`RajdhaniExpress`, `VandeBharatExpress`, `ShatabdiExpress`, `SuperfastExpress`, `ExpressTrain`).
- To employ the **Java Collections Framework** (`HashMap`, `ArrayList`, `HashSet`, `LinkedList`, `PriorityQueue`) purposefully according to time complexity needs.
- To model real-world **concurrency and thread synchronization** to eliminate race conditions.
- To provide a modern, responsive **Java Swing GUI** adhering to the separation of concerns (presentation separated from domain logic).

---

## 🚀 Core Features

### 1. User Roles & Access Control
- **Passenger Role**:
  - Search trains between 16+ real stations with date and class filters.
  - Multi-passenger booking in a single transaction (up to 6 passengers).
  - Dynamic fare calculation with auto-detected senior citizen, child, and infant concessions.
  - Real-time PNR status enquiry with printable ASCII ticket vouchers.
  - One-click cancellation with automated refund calculation and promotion updates.
- **Admin Role**:
  - Fleet management: add, configure, or decommission trains.
  - Station management across railway zones.
  - Live inspection of active RAC and Waiting List queues.
  - Master booking search across all passenger manifests.
  - Dynamic analytics dashboard (occupancy rate, revenue, route ranking).
  - Built-in **Concurrency Stress Lab** to test thread safety.

### 2. Realistic Dynamic Seat & Queue Management
- **Coach & Berth Modeling**: Realistic Indian coach configurations (`1A`, `2A`, `3A`, `SL`, `CC`) with lower, middle, upper, side lower, side upper, window, and aisle berth allocations.
- **Promotion Cascade**:
  $$\text{Confirmed Cancelled} \xrightarrow{\text{promotes}} \text{RAC Head} \xrightarrow{\text{promotes}} \text{Waiting List Head}$$

### 3. Concurrency Simulation Lab
- Interactive simulator spawning 2 to 30 concurrent worker threads competing for the last remaining seats on an isolated test train.
- Real-time event logging demonstrating thread lock acquisition, millisecond execution latency, and verification of zero duplicate seats.

---

## ☕ Core Java Concepts Demonstrated

| Concept | Implementation in RailSync |
|---|---|
| **Inheritance & Abstraction** | `Train` abstract class extended by `RajdhaniExpress`, `ShatabdiExpress`, `VandeBharatExpress`, `SuperfastExpress`, and `ExpressTrain`. |
| **Interfaces & Polymorphism** | `FareCalculator` interface implemented by `DynamicFareCalculator`. Polymorphic train speed, surcharges, and catering attributes. |
| **Encapsulation & Validation** | Defensive copying, private domain fields, and regex validation in `ValidationUtils` (names, phones, PNRs). |
| **Collections Framework** | `HashMap` for $O(1)$ PNR & Station lookups; `LinkedList` as `Queue` for FIFO RAC/WL queues; `ArrayList` for dynamic seat rosters; `HashSet` for collision-free PNR generation. |
| **Multithreading & Locks** | `Thread`, `Runnable`, `CountDownLatch`, and `synchronized` blocks in `ReservationManager` preventing race conditions. |
| **Custom Exceptions** | Hierarchy under `RailSyncException`: `SeatUnavailableException`, `InvalidPNRException`, `InvalidPassengerException`, `InvalidStationException`, etc. |
| **File I/O & Serialization** | `ObjectOutputStream` / `ObjectInputStream` for deep state persistence; `BufferedWriter` & `FileWriter` for ticket and audit exports. |
| **Java Time API** | `LocalDate`, `LocalTime`, `LocalDateTime`, `Duration`, and `DateTimeFormatter` for accurate scheduling and cancellation fee tiers. |
| **Generics & Lambdas** | Generic repositories, Streams for multi-criteria searching, and `Comparator` lambdas for train sorting. |

---

## 📂 Project Architecture & Directory Structure

```
RailSync/
├── build.bat                  # One-click Windows CMD compilation script
├── run.bat                    # One-click Windows CMD application launcher
├── test.bat                   # Runs 15-scenario automated verification suite
├── build_and_run.ps1          # Universal PowerShell build and launch script
├── README.md                  # Project overview and instructions
├── PROJECT_REPORT.md          # 18-Section comprehensive academic submission report
├── VIVA_QUESTIONS.md          # 40+ In-depth viva questions and detailed answers
├── bin/                       # Compiled bytecode (.class files)
├── data/                      # Persistent state storage
│   ├── railsync_data.ser      # Serialized Java object graph
│   └── tickets/               # Exported electronic tickets (.txt)
└── src/
    └── com/
        └── railsync/
            ├── Main.java                 # Bootstrap entry point (GUI / CLI)
            ├── TestRunner.java           # Automated 15-scenario test runner
            ├── model/                    # Domain entities
            │   ├── Station.java
            │   ├── SeatClass.java
            │   ├── BerthType.java
            │   ├── Seat.java
            │   ├── Coach.java
            │   ├── Train.java            # Abstract base class
            │   ├── RajdhaniExpress.java
            │   ├── ShatabdiExpress.java
            │   ├── VandeBharatExpress.java
            │   ├── SuperfastExpress.java
            │   ├── ExpressTrain.java
            │   ├── Passenger.java
            │   ├── BookingStatus.java
            │   ├── Ticket.java
            │   ├── Booking.java
            │   ├── RefundReceipt.java
            │   └── User.java
            ├── service/                  # Business services
            │   ├── FareCalculator.java
            │   ├── DynamicFareCalculator.java
            │   ├── SearchService.java
            │   ├── AnalyticsService.java
            │   └── ReportGenerator.java
            ├── manager/                  # State & queue orchestrators
            │   ├── ReservationManager.java
            │   ├── WaitingListManager.java
            │   └── FileManager.java
            ├── exception/                # Custom exception hierarchy
            │   ├── RailSyncException.java
            │   ├── InvalidPassengerException.java
            │   ├── InvalidTrainException.java
            │   ├── TrainNotFoundException.java
            │   ├── SeatUnavailableException.java
            │   ├── InvalidBookingException.java
            │   ├── InvalidPNRException.java
            │   ├── DuplicateBookingException.java
            │   ├── InvalidStationException.java
            │   └── CancellationNotAllowedException.java
            ├── util/                     # Utilities & Seed Data
            │   ├── PNRGenerator.java
            │   ├── ValidationUtils.java
            │   └── SampleDataSeeder.java
            ├── thread/                   # Concurrency simulation
            │   ├── BookingTask.java
            │   └── ConcurrencySimulation.java
            └── gui/                      # Presentation layer (Swing)
                ├── ModernTheme.java
                ├── LoginDialog.java
                ├── PassengerPanel.java
                ├── AdminPanel.java
                ├── ConcurrencySimulationPanel.java
                └── RailSyncGUI.java
```

---

## 🔑 Sample Credentials

The application is pre-seeded with sample users. You can authenticate via the dialog or click the **1-Click Demo Buttons**:

| Role | Username | Password | Full Name / Access Level |
|---|---|---|---|
| **Passenger** | `passenger` | `pass123` | Rahul Sharma (Standard Passenger) |
| **Passenger** | `priya` | `pass123` | Priya Patel (Standard Passenger) |
| **Administrator** | `admin` | `admin123` | Chief Commercial Officer (Full Admin Controls) |

---

## 🛠️ How to Build and Run

### Prerequisites
- Java Development Kit (JDK 17 or newer, tested on JDK 25).
- Operating System: Windows, Linux, or macOS.

### Running on Windows

#### Option 1: One-Click Batch Scripts (Recommended)
1. **Build the project**:
   ```cmd
   build.bat
   ```
2. **Launch the GUI**:
   ```cmd
   run.bat
   ```
3. **Run the Automated Test Suite**:
   ```cmd
   test.bat
   ```

#### Option 2: PowerShell Script
```powershell
# Run the GUI application
.\build_and_run.ps1

# Run the 15-test verification suite
.\build_and_run.ps1 --test

# Run CLI diagnostics mode
.\build_and_run.ps1 --cli
```

---

## 🧪 Automated Verification Suite

RailSync includes a standalone automated test harness (`TestRunner.java`) verifying all core business logic without opening the GUI:

```
==========================================================================
            RAILSYNC AUTOMATED CORE JAVA VERIFICATION SUITE               
==========================================================================

1. Successful Booking Allocation                                 : [PASS]
2. Booking When Confirmed Full -> Automatic RAC                  : [PASS]
3. RAC Allocation Tracking                                       : [PASS]
4. Waiting List Allocation When RAC Full                         : [PASS]
5. Ticket Cancellation and Refund Receipt                        : [PASS]
6. Automatic RAC to Confirmed Promotion Cascade                  : [PASS]
7. Automatic Waiting-List to RAC Promotion Cascade               : [PASS]
8. Invalid Passenger Input Validation                            : [PASS]
9. Invalid PNR Exception Handling                                : [PASS]
10. Concurrent Booking Simulation & Race Condition Prevention    : [PASS]
11. System State Serialization (Saving)                          : [PASS]
12. System State Deserialization (Loading)                       : [PASS]
13. Dynamic Fare Calculation & Concession Discounts              : [PASS]
14. Train Search, Route Filtering & Custom Comparators           : [PASS]
15. Real-Time Dynamic Administrative Analytics                   : [PASS]

==========================================================================
 VERIFICATION COMPLETE: 15 PASSED, 0 FAILED (TOTAL 15)
==========================================================================
```

---

## 🎬 Step-by-Step Viva & Demo Workflow (10 Minutes)

1. **Launch App**: Execute `run.bat`. The authentication dialog appears.
2. **Click "Passenger Demo"**: Log in instantly as *Rahul Sharma*.
3. **Search Trains**: Notice New Delhi (`NDLS`) to Mumbai Central (`BCT`). Click **"Search Trains"**.
4. **Book Multi-Passenger Ticket**:
   - Add two passengers: an adult (30 yrs) and a senior citizen (65 yrs).
   - Observe automatic 40% senior citizen concession and live fare preview calculation.
   - Click **"Confirm & Book Ticket"**. A unique PNR is generated (e.g. `RS507054`).
5. **Enquire PNR**: Switch to the **PNR Status Enquiry** tab. View the full ASCII Electronic Reservation Slip and click **"Export Ticket to File"**.
6. **Cancel Ticket & Verify Promotion**:
   - Navigate to **My Bookings & Cancellation**.
   - Select a ticket and click **"Cancel Selected Passenger Ticket"**.
   - Notice the refund breakdown receipt dialog and the message indicating that an RAC passenger was automatically promoted to Confirmed status!
7. **Switch to Admin View**:
   - Click **"Switch Role"** in the top-right banner.
   - Click **"Admin Demo"**.
8. **Inspect Queues & Analytics**:
   - In **RAC & Waiting List Monitors**, inspect the live queue positions.
   - In **Executive Analytics & Financials**, observe dynamic occupancy percentage, gross revenue, and class breakdown. Click **"Export Official Railway Audit Report"**.
9. **Run Concurrency Simulation Lab**:
   - Switch to the **Concurrency Stress Lab** tab.
   - Set simultaneous threads to `10` and click **"Launch Simulation"**.
   - Watch real-time terminal output showing thread locks, seat allocations, and the green verdict: **"PASSED: 100% THREAD-SAFE (0 RACE CONDITIONS)"**.
