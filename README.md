# RailSync – Intelligent Train Reservation & Dynamic Seat Management System

[![Java Version](https://img.shields.io/badge/Java-17%2B%20%7C%2025-orange.svg)](https://openjdk.org/)
[![GUI Framework](https://img.shields.io/badge/GUI-Java%20Swing-blue.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%2F%20Layered-green.svg)]()
[![Verification](https://img.shields.io/badge/Tests-15%2F15%20Passed-brightgreen.svg)]()

> A complete Core Java desktop application designed for the **"Programming in Java"** course curriculum. Developed using standard Java libraries without external third-party dependencies, demonstrating fundamental and advanced Core Java concepts: OOP, Collections Framework, Multithreading & Synchronization, Exception Handling, File I/O & Serialization, Generics, Lambdas, and Java Swing GUI design.

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
**RailSync** is a Java desktop application that simulates a train reservation and dynamic seat management system. It models the core operations of passenger booking: searching trains across routes, calculating distance-based fares with age concessions, allocating confirmed coach berths, managing FIFO queues for **Reservation Against Cancellation (RAC)** and **Waiting List (WL)**, automatically promoting passengers upon cancellations, providing PNR status enquiries, and demonstrating concurrent booking with thread-safe synchronization.

---

## 🎯 Problem Statement
Standard classroom projects often treat railway booking as a simple counter decrement. RailSync models the more realistic behavior of a reservation system:
1. **Dynamic Inventory Allocation**: Transitioning from Confirmed berths to RAC shared seats, and subsequently to Waiting List queues.
2. **Cancellation Promotion Cascade**: When a confirmed passenger cancels, an RAC passenger is promoted to Confirmed status with a berth assigned, and the first Waiting List passenger moves into RAC.
3. **Thread Safety & Race Conditions**: Managing simultaneous booking requests on the same train to prevent double-booking.
4. **Data Persistence**: Preserving object state across application restarts using standard Java serialization and file I/O.

---

## 🌟 Key Objectives
- To implement a train ticketing simulation using standard Java libraries.
- To demonstrate **polymorphism and inheritance** via specialized train classes (`RajdhaniExpress`, `VandeBharatExpress`, `ShatabdiExpress`, `SuperfastExpress`, `ExpressTrain`).
- To employ the **Java Collections Framework** (`HashMap`, `ArrayList`, `HashSet`, `LinkedList`, `PriorityQueue`) based on data structure requirements.
- To apply **multithreading and synchronization** to avoid race conditions during concurrent bookings.
- To provide a clean **Java Swing GUI** with separated presentation and business logic layers.

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
├── package.bat                # Packages application into runnable RailSync.jar
├── build_and_run.ps1          # Universal PowerShell build and launch script
├── RailSync.jar               # Pre-packaged runnable distribution JAR
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
            ├── cli/                      # Command-Line Console Interface
            │   └── ConsoleApp.java       # Interactive terminal reservation application
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

## ⚙️ System Requirements & Environment Setup

- **Java Runtime / Compiler**: JDK 17 or newer (tested on Java 17, 21, and 25).
- **Dependencies**: **Zero** external third-party dependencies. Built 100% on standard Java SE packages (`java.time`, `java.util`, `java.io`, `java.util.concurrent`, `javax.swing`).
- **Operating System**: Cross-platform (Windows, Linux, macOS).
- **Configuration**: No database setup or external configuration needed. Data is persisted automatically in `data/railsync_data.ser` via Java Object Serialization.

---

## 🛠️ Step-by-Step Compilation

You can compile the project using standard Java command-line tools without any IDE:

### A. Windows (CMD / Batch)
```cmd
build.bat
```
*Or manual compilation from repository root:*
```cmd
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt
```

### B. Linux / macOS / Bash
```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```

### C. Universal PowerShell
```powershell
.\build_and_run.ps1
```

---

## 💻 Step-by-Step Command-Line Execution (CLI Mode)

The project includes an interactive terminal-based console application (`ConsoleApp`) that executes directly in the terminal without opening a graphical window.

### How to Run CLI Mode:

#### Option 1: Direct Class Execution
```bash
java -cp bin com.railsync.cli.ConsoleApp
```

#### Option 2: Via Main Application Flag
```bash
java -cp bin com.railsync.Main --cli
```

#### Option 3: Using the Executable JAR
```bash
java -jar RailSync.jar --cli
```

#### Option 4: Using Windows Batch Script
```cmd
run.bat --cli
```

### Example CLI Interactive Menu:
```
==================================================================
        RailSync – Interactive Command-Line Console Application   
               Core Java Train Reservation System                 
==================================================================
------------------------- MAIN MENU ------------------------------
  1. Search Trains (by Source & Destination)
  2. View All Trains Catalog
  3. Book Train Ticket
  4. PNR Status Enquiry
  5. Cancel Ticket (with Promotion Cascade & Refund)
  6. View RAC & Waiting List Queue Status
  7. Dynamic Fare & Concession Calculator
  8. Save System State to Disk
  9. Exit
------------------------------------------------------------------
Enter your choice (1-9):
```

### Example Booking Flow in CLI:
1. Choose option **`3`** (Book Train Ticket).
2. Enter Train Number: `12952` (Mumbai Rajdhani).
3. Press **Enter** to select the default date (tomorrow).
4. Select Class: `1` for AC First Class (`1A`).
5. Enter number of passengers: `1`.
6. Enter details: `Rajesh Kumar`, Age `35`, Gender `M`, Phone `9876543210`, Berth `1` (Lower).
7. System processes transaction, allocates confirmed berth `H1-1`, and prints:
```
================ BOOKING CONFIRMATION ================
PNR Number        : RS989137
Booking ID        : BK10004
Train             : 12952 - Mumbai Rajdhani
Route             : NDLS -> BCT
Journey Date      : 2026-09-13
Travel Class      : AC First Class (1A)
Total Amount Paid : ₹4880.00

Allocated Passenger Tickets:
Ticket ID      | Passenger        | Age  | Status          | Seat/Berth              | Fare      
------------------------------------------------------------------------------------------------
TK-RS989137-1  | Rajesh Kumar     | 35   | Confirmed (CNF) | H1-1 (Lower Berth (LB)) | ₹4880.00  
======================================================
```

---

## 🖥️ Graphical User Interface (GUI) Execution

To launch the Java Swing desktop application:

#### Option 1: One-Click Batch Script (Windows)
```cmd
run.bat
```

#### Option 2: Running Standalone JAR
```bash
java -jar RailSync.jar
```
*(Or double-click `RailSync.jar` on Windows/macOS).*

#### Option 3: Direct Class Execution
```bash
java -cp bin com.railsync.Main
```

---

## 🧪 Test Execution Instructions

RailSync includes a standalone 15-scenario verification test harness testing all reservation rules, promotions, thread safety, and persistence:

#### Option 1: One-Click Batch Script (Windows)
```cmd
test.bat
```

#### Option 2: Direct Test Runner Execution
```bash
java -cp bin com.railsync.TestRunner
```

#### Option 3: Via Executable JAR
```bash
java -jar RailSync.jar --test
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
