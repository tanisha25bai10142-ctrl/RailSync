package com.railsync;

import com.railsync.gui.RailSyncGUI;
import com.railsync.manager.FileManager;
import com.railsync.manager.ReservationManager;
import com.railsync.model.Booking;
import com.railsync.model.Station;
import com.railsync.model.Train;
import com.railsync.model.User;
import com.railsync.util.SampleDataSeeder;

import javax.swing.*;
import java.io.File;

/**
 * Main application bootstrap and entry point for RailSync.
 * Supports:
 * - GUI Launch Mode (default)
 * - Automated Test Suite runner (--test)
 * - CLI Diagnostic Mode (--cli)
 */
public class Main {

    private static final String DATA_FILE_PATH = "data/railsync_data.ser";

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  RailSync – Train Reservation System                             ");
        System.out.println("  Core Java Academic Project | Java 17+                            ");
        System.out.println("==================================================================");

        if (args.length > 0 && "--test".equalsIgnoreCase(args[0])) {
            System.out.println("Executing RailSync Automated Test Suite...\n");
            TestRunner.main(args);
            return;
        }

        ReservationManager manager = new ReservationManager();
        File dataFile = new File(DATA_FILE_PATH);

        // Load existing serialized state or seed sample data
        loadOrSeedData(manager, dataFile);

        if (args.length > 0 && ("--cli".equalsIgnoreCase(args[0]) || "--console".equalsIgnoreCase(args[0]))) {
            com.railsync.cli.ConsoleApp.start(manager, dataFile);
            return;
        }

        if (args.length > 0 && "--diagnostics".equalsIgnoreCase(args[0])) {
            runCliDiagnostics(manager);
            return;
        }

        // Auto-fallback to ConsoleApp if running in a headless environment without display
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("[INFO] Headless environment detected. Launching interactive CLI mode...\n");
            com.railsync.cli.ConsoleApp.start(manager, dataFile);
            return;
        }

        // Launch Modern Java Swing GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                RailSyncGUI gui = new RailSyncGUI(manager, dataFile);
                gui.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to launch GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void loadOrSeedData(ReservationManager manager, File dataFile) {
        if (dataFile.exists()) {
            try {
                System.out.println("Loading existing system state from: " + dataFile.getAbsolutePath());
                FileManager.SystemState state = FileManager.loadSystemState(dataFile);
                if (state != null) {
                    if (state.stations != null) {
                        for (Station s : state.stations.values()) manager.addStation(s);
                    }
                    if (state.trains != null) {
                        for (Train t : state.trains.values()) manager.addTrain(t);
                    }
                    if (state.bookings != null) {
                        manager.getBookingMap().putAll(state.bookings);
                    }
                    if (state.users != null) {
                        for (User u : state.users.values()) manager.registerUser(u);
                    }
                    System.out.println("Successfully restored " + manager.getAllTrains().size() +
                            " trains and " + manager.getBookingMap().size() + " bookings.");
                    // Ensure sample users exist even after state reload
                    SampleDataSeeder.seedUsers(manager);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Notice: Could not deserialize previous state (" + e.getMessage() + "). Seeding fresh data.");
            }
        }

        System.out.println("Initializing fresh system state with Indian Railways reference data...");
        SampleDataSeeder.seedAll(manager);
        System.out.println("Seeded " + manager.getAllTrains().size() + " trains, " +
                manager.getAllStations().size() + " stations, and initial bookings.");
    }

    private static void runCliDiagnostics(ReservationManager manager) {
        System.out.println("\n--- CLI DIAGNOSTICS & SYSTEM STATUS ---");
        System.out.println("Total Stations Registered: " + manager.getAllStations().size());
        System.out.println("Total Trains Configured  : " + manager.getAllTrains().size());
        for (Train t : manager.getAllTrains()) {
            System.out.println("  • " + t.getTrainNumber() + " " + t.getTrainName() +
                    " (" + t.getSource().getCode() + " -> " + t.getDestination().getCode() + ") [" + t.getTrainType() + "]");
        }
        System.out.println("Total Bookings in Memory : " + manager.getBookingMap().size());
        for (Booking b : manager.getBookingMap().values()) {
            System.out.println("  • PNR: " + b.getPnr() + " | Train: " + b.getTrainNumber() +
                    " | Date: " + b.getJourneyDate() + " | Fare: ₹" + b.getTotalFare());
        }
        System.out.println("System operational. Run without --cli to open GUI.");
    }
}
