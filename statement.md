# RailSync – Problem Statement & Scope

## 1. Problem Statement
Railway ticket reservation involves managing passenger demand against fixed seating capacity across various travel classes. When confirmed seats are exhausted, systems must handle queue-based reservations such as Reservation Against Cancellation (RAC) and Waiting Lists (WL). In addition, when passengers cancel their tickets, the system must automatically recalculate refunds, free up seats, and cascade promotions up the queue (promoting RAC passengers to Confirmed, and Waiting List passengers to RAC). When multiple users attempt to book tickets simultaneously, race conditions can lead to duplicate seat assignments unless access to shared resources is properly synchronized.

RailSync addresses these problems by providing a desktop-based railway reservation simulation built with Core Java, demonstrating object-oriented modeling, thread synchronization, queue cascading, dynamic fare calculation, and persistent storage.

## 2. Scope of the Project
The scope of the project encompasses:
- Core railway reservation workflows: train catalog search, passenger registration, seat allocation, and PNR generation.
- Dynamic queue management for Confirmed, RAC, and Waiting List booking statuses.
- Automatic promotion cascade and cancellation refund calculations based on departure timing.
- Thread-safe concurrent booking operations using Java synchronization.
- Dual interface support: a command-line interface (CLI) for terminal use and a graphical user interface (GUI) built with Java Swing.
- Object serialization and text file export for saving and restoring system state between sessions.

*Out of Scope:* Real bank payment gateway integration, live GPS train tracking, and external client-server web deployments.

## 3. Target Users
- **Passengers / Customers**: Search train routes, view availability and fare quotes, book tickets for single or multiple passengers, check PNR statuses, and cancel reservations.
- **Railway Administrators / Ticket Clerks**: View fleet schedules, inspect live RAC and Waiting List queues, search passenger manifests, monitor booking statistics, and run multithreaded simulations to test system integrity.
- **Academic Evaluators**: Review and verify Core Java concepts including Object-Oriented Programming (OOP), Java Collections Framework, Multithreading, Exception Handling, File I/O, and Swing GUI.

## 4. High-Level Features
- **Train Search & Route Filtering**: Search trains between origin and destination stations with date and seat class filters.
- **Multi-Passenger Booking**: Book up to 6 passengers in one transaction with berth allocation across various coach classes (1A, 2A, 3A, CC, SL).
- **RAC & Waiting List Cascade**: Automated FIFO queue management that promotes passengers upon cancellations.
- **Dynamic Fare & Concession Calculation**: Distance-based fare calculation with age concessions (senior citizen discount, free infant travel).
- **Cancellation & Refunds**: Time-tiered refund processing based on the hours remaining before departure.
- **Thread-Safe Concurrency**: Synchronized seat allocation to prevent duplicate seat assignments during simultaneous bookings.
- **Data Persistence**: Java object serialization to save and reload train and booking data across sessions.
