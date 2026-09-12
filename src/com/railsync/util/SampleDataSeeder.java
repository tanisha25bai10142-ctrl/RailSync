package com.railsync.util;

import com.railsync.manager.ReservationManager;
import com.railsync.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the RailSync platform with realistic Indian Railways reference data.
 * Includes:
 * - 16 Major Stations across multiple railway zones
 * - 9 Prominent Express/Rajdhani/Vande Bharat Trains
 * - Rich Coach and Berth configurations
 * - Sample Users (Admin & Passenger)
 * - Seeded Bookings with Confirmed, RAC, and Waiting List states
 */
public class SampleDataSeeder {

    public static void seedAll(ReservationManager manager) {
        seedStations(manager);
        seedTrains(manager);
        seedUsers(manager);
        seedInitialBookings(manager);
    }

    public static void seedStations(ReservationManager manager) {
        // 16 Real Indian Railway Stations
        manager.addStation(new Station("NDLS", "New Delhi", "New Delhi", "Delhi", "NR", 0.0));
        manager.addStation(new Station("BCT", "Mumbai Central", "Mumbai", "Maharashtra", "WR", 1384.0));
        manager.addStation(new Station("HWH", "Howrah Junction", "Kolkata", "West Bengal", "ER", 1447.0));
        manager.addStation(new Station("MAS", "Chennai Central", "Chennai", "Tamil Nadu", "SR", 2180.0));
        manager.addStation(new Station("SBC", "KSR Bengaluru", "Bengaluru", "Karnataka", "SWR", 2400.0));
        manager.addStation(new Station("BPL", "Bhopal Junction", "Bhopal", "Madhya Pradesh", "WCR", 705.0));
        manager.addStation(new Station("CNB", "Kanpur Central", "Kanpur", "Uttar Pradesh", "NCR", 440.0));
        manager.addStation(new Station("PNBE", "Patna Junction", "Patna", "Bihar", "ECR", 998.0));
        manager.addStation(new Station("ADI", "Ahmedabad Junction", "Ahmedabad", "Gujarat", "WR", 934.0));
        manager.addStation(new Station("PUNE", "Pune Junction", "Pune", "Maharashtra", "CR", 1520.0));
        manager.addStation(new Station("NGP", "Nagpur Junction", "Nagpur", "Maharashtra", "CR", 1090.0));
        manager.addStation(new Station("JAT", "Jammu Tawi", "Jammu", "Jammu and Kashmir", "NR", 580.0));
        manager.addStation(new Station("TVC", "Thiruvananthapuram Central", "Thiruvananthapuram", "Kerala", "SR", 3050.0));
        manager.addStation(new Station("GHY", "Guwahati Junction", "Guwahati", "Assam", "NFR", 1920.0));
        manager.addStation(new Station("BSB", "Varanasi Junction", "Varanasi", "Uttar Pradesh", "NER", 780.0));
        manager.addStation(new Station("AGC", "Agra Cantt", "Agra", "Uttar Pradesh", "NCR", 195.0));
    }

    public static void seedTrains(ReservationManager manager) {
        Station ndls = manager.getStation("NDLS");
        Station bct  = manager.getStation("BCT");
        Station hwh  = manager.getStation("HWH");
        Station mas  = manager.getStation("MAS");
        Station sbc  = manager.getStation("SBC");
        Station bpl  = manager.getStation("BPL");
        Station cnb  = manager.getStation("CNB");
        Station bsb  = manager.getStation("BSB");
        Station adi  = manager.getStation("ADI");
        Station pune = manager.getStation("PUNE");
        Station tvc  = manager.getStation("TVC");

        // 1. 12952 Mumbai Rajdhani Express (NDLS -> BCT)
        RajdhaniExpress t1 = new RajdhaniExpress("12952", "Mumbai Rajdhani", ndls, bct,
                LocalTime.of(16, 55), LocalTime.of(8, 35), 1384.0);
        t1.addIntermediateStation(bpl);
        // Add coaches: First AC (1A), Two 2A coaches, Two 3A coaches
        t1.addCoach(new Coach("H1", SeatClass.FIRST_AC, 18, 4, 10));
        t1.addCoach(new Coach("A1", SeatClass.SECOND_AC, 36, 6, 15));
        t1.addCoach(new Coach("A2", SeatClass.SECOND_AC, 36, 6, 15));
        t1.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        t1.addCoach(new Coach("B2", SeatClass.THIRD_AC, 48, 8, 20));
        manager.addTrain(t1);

        // 2. 12951 NDLS Rajdhani (BCT -> NDLS)
        RajdhaniExpress t2 = new RajdhaniExpress("12951", "NDLS Rajdhani Express", bct, ndls,
                LocalTime.of(17, 0), LocalTime.of(8, 32), 1384.0);
        t2.addIntermediateStation(bpl);
        t2.addCoach(new Coach("H1", SeatClass.FIRST_AC, 18, 4, 10));
        t2.addCoach(new Coach("A1", SeatClass.SECOND_AC, 36, 6, 15));
        t2.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        manager.addTrain(t2);

        // 3. 22436 Vande Bharat Express (NDLS -> BSB)
        VandeBharatExpress t3 = new VandeBharatExpress("22436", "Vande Bharat Express", ndls, bsb,
                LocalTime.of(6, 0), LocalTime.of(14, 0), 780.0);
        t3.addIntermediateStation(cnb);
        t3.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 50, 10, 20));
        t3.addCoach(new Coach("C2", SeatClass.CHAIR_CAR, 50, 10, 20));
        t3.addCoach(new Coach("E1", SeatClass.FIRST_AC, 24, 4, 8));
        manager.addTrain(t3);

        // 4. 12002 Bhopal Shatabdi Express (NDLS -> BPL)
        ShatabdiExpress t4 = new ShatabdiExpress("12002", "Bhopal Shatabdi", ndls, bpl,
                LocalTime.of(6, 15), LocalTime.of(14, 40), 705.0);
        t4.addIntermediateStation(manager.getStation("AGC"));
        t4.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 45, 8, 15));
        t4.addCoach(new Coach("C2", SeatClass.CHAIR_CAR, 45, 8, 15));
        t4.addCoach(new Coach("E1", SeatClass.FIRST_AC, 20, 4, 10));
        manager.addTrain(t4);

        // 5. 12301 Howrah Rajdhani Express (HWH -> NDLS)
        RajdhaniExpress t5 = new RajdhaniExpress("12301", "Howrah Rajdhani", hwh, ndls,
                LocalTime.of(16, 50), LocalTime.of(10, 05), 1447.0);
        t5.addIntermediateStation(cnb);
        t5.addCoach(new Coach("H1", SeatClass.FIRST_AC, 18, 4, 10));
        t5.addCoach(new Coach("A1", SeatClass.SECOND_AC, 36, 6, 15));
        t5.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        manager.addTrain(t5);

        // 6. 12626 Kerala Superfast Express (NDLS -> TVC)
        SuperfastExpress t6 = new SuperfastExpress("12626", "Kerala Express", ndls, tvc,
                LocalTime.of(20, 10), LocalTime.of(18, 0), 3050.0);
        t6.addIntermediateStation(bpl);
        t6.addIntermediateStation(manager.getStation("NGP"));
        t6.addIntermediateStation(mas);
        t6.addCoach(new Coach("S1", SeatClass.SLEEPER, 60, 12, 30));
        t6.addCoach(new Coach("S2", SeatClass.SLEEPER, 60, 12, 30));
        t6.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        t6.addCoach(new Coach("A1", SeatClass.SECOND_AC, 36, 6, 15));
        manager.addTrain(t6);

        // 7. 12137 Punjab Mail (BCT -> NDLS)
        ExpressTrain t7 = new ExpressTrain("12137", "Punjab Mail", bct, ndls,
                LocalTime.of(19, 35), LocalTime.of(21, 30), 1540.0);
        t7.addIntermediateStation(bpl);
        t7.addCoach(new Coach("S1", SeatClass.SLEEPER, 60, 12, 30));
        t7.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        manager.addTrain(t7);

        // 8. 12628 Karnataka Express (NDLS -> SBC)
        SuperfastExpress t8 = new SuperfastExpress("12628", "Karnataka Express", ndls, sbc,
                LocalTime.of(21, 15), LocalTime.of(12, 0), 2400.0);
        t8.addIntermediateStation(bpl);
        t8.addCoach(new Coach("S1", SeatClass.SLEEPER, 60, 12, 30));
        t8.addCoach(new Coach("B1", SeatClass.THIRD_AC, 48, 8, 20));
        t8.addCoach(new Coach("A1", SeatClass.SECOND_AC, 36, 6, 15));
        manager.addTrain(t8);

        // 9. 12933 Karnavati Express (BCT -> ADI)
        ExpressTrain t9 = new ExpressTrain("12933", "Karnavati Express", bct, adi,
                LocalTime.of(14, 05), LocalTime.of(21, 05), 492.0);
        t9.addCoach(new Coach("C1", SeatClass.CHAIR_CAR, 50, 10, 20));
        t9.addCoach(new Coach("C2", SeatClass.CHAIR_CAR, 50, 10, 20));
        manager.addTrain(t9);
    }

    public static void seedUsers(ReservationManager manager) {
        manager.registerUser(new User("USR101", "passenger", "pass123", "Rahul Sharma",
                "rahul.sharma@example.com", "9876543210", User.Role.PASSENGER));
        manager.registerUser(new User("USR102", "priya", "pass123", "Priya Patel",
                "priya.patel@example.com", "9823456781", User.Role.PASSENGER));
        manager.registerUser(new User("ADM001", "admin", "admin123", "Chief Commercial Officer",
                "admin@railsync.gov.in", "9999900000", User.Role.ADMIN));
    }

    public static void seedInitialBookings(ReservationManager manager) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate nextWeek = LocalDate.now().plusDays(7);

        try {
            // Seed 1: Confirmed booking with multiple passengers (Adult + Senior Citizen)
            List<Passenger> passList1 = new ArrayList<>();
            passList1.add(new Passenger("Rahul Sharma", 32, "Male", "9876543210", BerthType.LOWER));
            passList1.add(new Passenger("Sunita Sharma", 62, "Female", "9876543210", BerthType.LOWER)); // Senior
            manager.bookTicket("12952", tomorrow, SeatClass.SECOND_AC, passList1, "passenger");

            // Seed 2: Confirmed booking on Vande Bharat
            List<Passenger> passList2 = new ArrayList<>();
            passList2.add(new Passenger("Priya Patel", 28, "Female", "9823456781", BerthType.WINDOW));
            manager.bookTicket("22436", tomorrow, SeatClass.CHAIR_CAR, passList2, "priya");

            // Seed 3: Booking on Kerala Express (Sleeper)
            List<Passenger> passList3 = new ArrayList<>();
            passList3.add(new Passenger("Amitabh Verma", 45, "Male", "9123456789", BerthType.UPPER));
            passList3.add(new Passenger("Aarav Verma", 8, "Male", "9123456789", BerthType.MIDDLE)); // Child
            manager.bookTicket("12626", nextWeek, SeatClass.SLEEPER, passList3, "passenger");

        } catch (Exception e) {
            System.err.println("Notice during seed bookings: " + e.getMessage());
        }
    }
}
