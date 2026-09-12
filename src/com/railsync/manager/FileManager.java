package com.railsync.manager;

import com.railsync.model.Booking;
import com.railsync.model.Station;
import com.railsync.model.Train;
import com.railsync.model.User;

import java.io.*;
import java.util.*;

/**
 * Handles File I/O, persistence, and state serialization.
 * Demonstrates:
 * - Java Serialization (ObjectOutputStream, ObjectInputStream)
 * - Java Character Streams (BufferedWriter, FileWriter, BufferedReader, FileReader)
 * - Clean exception handling with try-with-resources
 * - Deep object graph persistence
 */
public class FileManager {

    /**
     * Data Transfer Object representing complete persistent snapshot of the system state.
     */
    public static class SystemState implements Serializable {
        private static final long serialVersionUID = 1L;

        public Map<String, Train> trains;
        public Map<String, Station> stations;
        public Map<String, Booking> bookings;
        public Map<String, User> users;
        public WaitingListManager waitingListManager;

        public SystemState(Map<String, Train> trains, Map<String, Station> stations,
                           Map<String, Booking> bookings, Map<String, User> users,
                           WaitingListManager waitingListManager) {
            this.trains = trains;
            this.stations = stations;
            this.bookings = bookings;
            this.users = users;
            this.waitingListManager = waitingListManager;
        }
    }

    /**
     * Saves the entire system state using Object Serialization.
     * Serialization is appropriate here because it preserves complete object graphs,
     * polymorphic Train subclasses, seat status matrices, and queued ticket references
     * without loss of internal references or class invariants.
     */
    public static void saveSystemState(ReservationManager manager, File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        SystemState state = new SystemState(
                new HashMap<>(manager.getAllTrains().stream().collect(
                        java.util.stream.Collectors.toMap(Train::getTrainNumber, t -> t))),
                new HashMap<>(manager.getAllStations().stream().collect(
                        java.util.stream.Collectors.toMap(Station::getCode, s -> s))),
                new HashMap<>(manager.getBookingMap()),
                new HashMap<>(), // users
                manager.getWaitingListManager()
        );

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(state);
            oos.flush();
        }
    }

    /**
     * Loads the serialized system state from disk.
     */
    public static SystemState loadSystemState(File file) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof SystemState) {
                return (SystemState) obj;
            }
            throw new IOException("Corrupted state file: incompatible object type");
        }
    }

    /**
     * Exports a text report using BufferedWriter and FileWriter.
     */
    public static void writeTextFile(String content, File destinationFile) throws IOException {
        if (destinationFile.getParentFile() != null && !destinationFile.getParentFile().exists()) {
            destinationFile.getParentFile().mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile))) {
            bw.write(content);
            bw.flush();
        }
    }

    /**
     * Reads a text file line by line using BufferedReader.
     */
    public static String readTextFile(File file) throws IOException {
        if (!file.exists()) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
