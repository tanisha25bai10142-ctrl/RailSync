# RailSync – Train Reservation & Dynamic Seat Management System

![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)
![GUI Framework](https://img.shields.io/badge/GUI-Java%20Swing-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-Layered%20OOP-green.svg)
![Verification](https://img.shields.io/badge/Tests-15%2F15%20Passed-brightgreen.svg)

RailSync is a Core Java desktop application that simulates train reservation, seat allocation, RAC/WL management, fare calculation, cancellation, and passenger management. It demonstrates object-oriented programming, collections, exception handling, multithreading, file I/O, serialization, and Java Swing.

---

## 📑 Table of Contents
- [Project Overview](#-project-overview)
- [Problem Statement](#-problem-statement)
- [Key Objectives](#-key-objectives)
- [Core Features](#-core-features)
- [Core Java Concepts Demonstrated](#-core-java-concepts-demonstrated)
- [Project Structure](#-project-structure)
- [Sample Credentials](#-sample-credentials)
- [System Requirements & Environment Setup](#️-system-requirements--environment-setup)
- [Step-by-Step Compilation](#️-step-by-step-compilation)
- [Command-Line Execution (CLI Mode)](#-command-line-execution-cli-mode)
- [Graphical User Interface (GUI) Execution](#️-graphical-user-interface-gui-execution)
- [Automated Verification](#-automated-verification)
- [Demo Flow](#-demo-flow)

---

## 🚆 Project Overview
**RailSync** is a Java desktop application that simulates a train reservation and dynamic seat management system. It models passenger booking operations: searching trains across routes, calculating distance-based fares with age concessions, allocating confirmed coach berths, managing FIFO queues for **Reservation Against Cancellation (RAC)** and **Waiting List (WL)**, automatically promoting passengers upon cancellations, providing PNR status enquiries, and demonstrating concurrent booking with thread-safe synchronization.

---

## 🎯 Problem Statement
Standard classroom projects often treat railway booking as a simple counter decrement. This project models key technical challenges in reservation systems:
1. **Seat Allocation**: Handling confirmed berth assignments and transitioning to RAC and Waiting List queues when capacity is reached.
2. **Queue Promotion Cascade**: Automatically promoting RAC passengers to confirmed status and Waiting List passengers to RAC upon cancellation.
3. **Thread Safety**: Managing simultaneous booking requests on the same train to prevent race conditions and duplicate seat allocations.
4. **Data Persistence**: Preserving application state across sessions using Java serialization and file I/O.

---

## 🌟 Key Objectives
- To implement a train ticketing simulation using standard Java libraries.
- To demonstrate **polymorphism and inheritance** via specialized train classes (`RajdhaniExpress`, `VandeBharatExpress`, `ShatabdiExpress`, `SuperfastExpress`, `ExpressTrain`).
- To employ the **Java Collections Framework** (`HashMap`, `ArrayList`, `HashSet`, `LinkedList`, `PriorityQueue`) based on data structure requirements.
- To apply **multithreading and synchronization** to avoid race conditions during concurrent bookings.
- To provide a **Java Swing GUI** with separated presentation and business logic layers.

---

## 🚀 Core Features

### 1. User Roles & Access Control
- **Passenger Role**:
  - Search trains between registered stations with date and class filters.
  - Multi-passenger booking in a single transaction (up to 6 passengers).
  - Dynamic fare calculation with auto-detected senior citizen, child, and infant concessions.
  - PNR status enquiry with printable ticket summaries.
  - Cancellation with automated refund calculation and promotion updates.
- **Admin Role**:
  - Fleet management: add, configure, or decommission trains.
  - Station management across railway routes.
  - Live inspection of active RAC and Waiting List queues.
  - Master booking search across passenger manifests.
  - Analytics dashboard (occupancy rate, revenue, route metrics).
  - Built-in concurrency simulator to test thread synchronization.

### 2. Seat & Queue Management
- **Coach & Berth Allocation**: Coach configurations (`1A`, `2A`, `3A`, `SL`, `CC`) with lower, middle, upper, side lower, side upper, window, and aisle berth allocations.
- **Promotion Cascade**: When a confirmed seat is cancelled, the first RAC passenger is allocated a confirmed berth, and the first Waiting List passenger moves into RAC.

### 3. Concurrency Simulation
- Spawns concurrent worker threads competing for remaining seats on a test train to demonstrate thread synchronization and verify that no duplicate seats are allocated.

---

## ☕ Core Java Concepts Demonstrated

| Concept | Implementation in RailSync |
|---|---|
| **Inheritance & Abstraction** | `Train` abstract class extended by `RajdhaniExpress`, `ShatabdiExpress`, `VandeBharatExpress`, `SuperfastExpress`, and `ExpressTrain`. |
| **Interfaces & Polymorphism** | `FareCalculator` interface implemented by `DynamicFareCalculator`. Polymorphic train speed, surcharges, and catering attributes. |
| **Encapsulation & Validation** | Defensive copying, private domain fields, and input validation in `ValidationUtils` (names, phones, PNRs). |
| **Collections Framework** | `HashMap` for PNR and station lookups; `LinkedList` as FIFO queues for RAC and Waiting List; `ArrayList` for seat rosters; `HashSet` for PNR generation. |
| **Multithreading & Synchronization** | `Thread`, `Runnable`, `CountDownLatch`, and `synchronized` blocks in `ReservationManager` preventing race conditions. |
| **Custom Exceptions** | Hierarchy under `RailSyncException`: `SeatUnavailableException`, `InvalidPNRException`, `InvalidPassengerException`, `InvalidStationException`, etc. |
| **File I/O & Serialization** | `ObjectOutputStream` / `ObjectInputStream` for state persistence; `BufferedWriter` & `FileWriter` for ticket and audit exports. |
| **Java Time API** | `LocalDate`, `LocalTime`, `LocalDateTime`, `Duration`, and `DateTimeFormatter` for scheduling and cancellation refund tiers. |
| **Generics & Lambdas** | Generic collections, Streams for criteria searching, and `Comparator` lambdas for train sorting. |

---

## 📂 Project Structure

```
RailSync/
├── .gitignore
├── PROJECT_REPORT.md          # Course project report
├── README.md                  # Project documentation
├── RailSync.jar               # Executable application JAR
├── VIVA_QUESTIONS.md          # Viva preparation reference
├── build.bat                  # Windows build script
├── build_and_run.ps1          # PowerShell build and run script
├── package.bat                # JAR packaging script
├── run.bat                    # Windows run script
├── test.bat                   # Automated test script
├── data/                      # Directory for serialized state and exported tickets
└── src/
    └── com/
        └── railsync/
            ├── Main.java                 # Application entry point
            ├── TestRunner.java           # 15-scenario verification runner
            ├── cli/
            │   └── ConsoleApp.java       # Interactive terminal application
            ├── exception/                # Custom exception hierarchy
            │   ├── CancellationNotAllowedException.java
            │   ├── DuplicateBookingException.java
            │   ├── InvalidBookingException.java
            │   ├── InvalidPNRException.java
            │   ├── InvalidPassengerException.java
            │   ├── InvalidStationException.java
            │   ├── InvalidTrainException.java
            │   ├── RailSyncException.java
            │   ├── SeatUnavailableException.java
            │   └── TrainNotFoundException.java
            ├── gui/                      # Java Swing user interface
            │   ├── AdminPanel.java
            │   ├── ConcurrencySimulationPanel.java
            │   ├── LoginDialog.java
            │   ├── ModernTheme.java
            │   ├── PassengerPanel.java
            │   └── RailSyncGUI.java
            ├── manager/                  # Domain state & queue management
            │   ├── FileManager.java
            │   ├── ReservationManager.java
            │   └── WaitingListManager.java
            ├── model/                    # Domain model entities
            │   ├── BerthType.java
            │   ├── Booking.java
            │   ├── BookingStatus.java
            │   ├── Coach.java
            │   ├── ExpressTrain.java
            │   ├── Passenger.java
            │   ├── RajdhaniExpress.java
            │   ├── RefundReceipt.java
            │   ├── Seat.java
            │   ├── SeatClass.java
            │   ├── ShatabdiExpress.java
            │   ├── Station.java
            │   ├── SuperfastExpress.java
            │   ├── Ticket.java
            │   ├── Train.java
            │   ├── User.java
            │   └── VandeBharatExpress.java
            ├── service/                  # Business services
            │   ├── AnalyticsService.java
            │   ├── DynamicFareCalculator.java
            │   ├── FareCalculator.java
            │   ├── ReportGenerator.java
            │   └── SearchService.java
            ├── thread/                   # Concurrency simulation
            │   ├── BookingTask.java
            │   └── ConcurrencySimulation.java
            └── util/                     # Validation, PNR generator, seed data
                ├── PNRGenerator.java
                ├── SampleDataSeeder.java
                └── ValidationUtils.java
```
*(Note: The `bin/` directory containing compiled bytecode and runtime state files in `data/` are generated during build and execution).*

---

## 🔑 Sample Credentials

The application is pre-seeded with sample users. You can authenticate via the login dialog or use the demo buttons:

| Role | Username | Password | Full Name / Access Level |
|---|---|---|---|
| **Passenger** | `passenger` | `pass123` | Rahul Sharma (Standard Passenger) |
| **Passenger** | `priya` | `pass123` | Priya Patel (Standard Passenger) |
| **Administrator** | `admin` | `admin123` | Chief Commercial Officer (Full Admin Controls) |

---

## ⚙️ System Requirements & Environment Setup

- **Java**: JDK 17 or newer; verified with OpenJDK 25.
- **Dependencies**: Zero external dependencies (uses standard Java library: `java.time`, `java.util`, `java.io`, `java.util.concurrent`, `javax.swing`).
- **Operating System**: Windows, Linux, or macOS.
- **Configuration**: None required. State is persisted in `data/` using standard Java serialization.

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

## 💻 Command-Line Execution (CLI Mode)

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
1. Choose option `3` (Book Train Ticket).
2. Enter Train Number: `12952` (Mumbai Rajdhani).
3. Press Enter to select the default date.
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

#### Option 3: Direct Class Execution
```bash
java -cp bin com.railsync.Main
```

---

## 🧪 Automated Verification

RailSync includes an automated test harness (`TestRunner.java`) verifying core booking rules, queue transitions, thread safety, and persistence without launching the GUI.

**Result: 15 passed, 0 failed.**

### How to Run Tests:
- **Windows Batch**: `test.bat`
- **Command Line**: `java -cp bin com.railsync.TestRunner`
- **Via JAR**: `java -jar RailSync.jar --test`

---

## 🎬 Demo Flow

1. **Launch the Application**: Start the GUI via `run.bat` (or `java -jar RailSync.jar`). The authentication dialog appears.
2. **Passenger Search & Booking**: Log in using the Passenger Demo button. Search trains between stations (e.g. `NDLS` to `BCT`), choose a class, enter passenger details, and confirm booking to generate a PNR.
3. **PNR Status & Ticket Export**: Open the PNR Status tab to view booking details, berth allocation, and optionally export the ticket to a text file.
4. **Cancellation & Cascade Promotion**: Cancel a ticket under My Bookings to view the calculated refund receipt and observe the automatic promotion of an RAC passenger to confirmed status.
5. **Admin Inspection & Concurrency**: Switch to the Admin role to inspect queue depths and system analytics. Open the Concurrency Stress Lab and run concurrent booking threads to verify thread-safe seat allocation; the concurrency test completes with no duplicate seat allocations.
