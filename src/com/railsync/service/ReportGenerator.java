package com.railsync.service;

import com.railsync.model.Booking;
import com.railsync.model.SeatClass;
import com.railsync.model.Ticket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates formatted ASCII tickets and administrative audit reports.
 * Demonstrates:
 * - String Processing & StringBuilder
 * - Java File I/O (FileWriter, BufferedWriter)
 * - Clean tabular ASCII report presentation
 */
public class ReportGenerator {

    /**
     * Generates an official formatted Indian Railways e-Ticket as a String.
     */
    public static String generateTicketText(Booking booking) {
        if (booking == null) return "No booking details available.";

        StringBuilder sb = new StringBuilder(1024);
        sb.append("========================================================================================\n");
        sb.append("                       INDIAN RAILWAYS - ELECTRONIC RESERVATION SLIP                     \n");
        sb.append("                               POWERED BY RAILSYNC SYSTEM                               \n");
        sb.append("========================================================================================\n");
        sb.append(String.format(" PNR NUMBER       : %-20s  BOOKING ID    : %s\n", booking.getPnr(), booking.getBookingId()));
        sb.append(String.format(" TRAIN NO & NAME  : %-5s %-20s  TRAVEL CLASS  : %s\n",
                booking.getTrainNumber(), booking.getTrainName(), booking.getSeatClass().getDisplayName()));
        sb.append(String.format(" FROM STATION     : %-20s  TO STATION    : %s\n",
                booking.getSource().toString(), booking.getDestination().toString()));
        sb.append(String.format(" JOURNEY DATE     : %-20s  BOOKED ON     : %s\n",
                booking.getFormattedJourneyDate(), booking.getFormattedBookingTime()));
        sb.append(String.format(" OVERALL STATUS   : %s\n", booking.isCancelled() ? "CANCELLED" : "ACTIVE"));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-4s | %-22s | %-5s | %-6s | %-16s | %-20s | %-10s\n",
                "SNO", "PASSENGER NAME", "AGE", "GENDER", "CONCESSION", "STATUS / BERTH", "FARE (₹)"));
        sb.append("----------------------------------------------------------------------------------------\n");

        int index = 1;
        for (Ticket t : booking.getTickets()) {
            sb.append(String.format(" %-4d | %-22s | %-5d | %-6s | %-16s | %-20s | %10.2f\n",
                    index++,
                    truncate(t.getPassenger().getName(), 22),
                    t.getPassenger().getAge(),
                    t.getPassenger().getGender(),
                    truncate(t.getPassenger().getConcession().name(), 16),
                    t.getStatusDescription(),
                    t.getIndividualFare()));
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" TOTAL PASSENGERS : %-20d  TOTAL FARE PAID: ₹ %10.2f\n",
                booking.getPassengerCount(), booking.getTotalFare()));
        sb.append("========================================================================================\n");
        sb.append(" [BARCODE: ||| |||| | |||||| | ||||||| | ||||| | |||| ||| " + booking.getPnr() + "]\n");
        sb.append(" Terms & Conditions:\n");
        sb.append(" 1. One original valid Photo ID card must be presented during journey.\n");
        sb.append(" 2. RAC passengers are entitled to seating accommodation.\n");
        sb.append(" 3. Waiting list passengers cannot board reserved coaches if not confirmed.\n");
        sb.append(" 4. RailSync 24x7 Customer Support Helpline: 139\n");
        sb.append("========================================================================================\n");

        return sb.toString();
    }

    /**
     * Saves the ticket text to a file on disk.
     */
    public static File saveTicketToFile(Booking booking, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File ticketFile = new File(targetDir, "Ticket_" + booking.getPnr() + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ticketFile))) {
            writer.write(generateTicketText(booking));
        }
        return ticketFile;
    }

    /**
     * Generates a comprehensive railway administrative audit report.
     */
    public static String generateAdminReport(AnalyticsService analytics) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("========================================================================================\n");
        sb.append("                       RAILSYNC SYSTEM - EXECUTIVE AUDIT & ANALYTICS                    \n");
        sb.append("                         Generated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")) + "                         \n");
        sb.append("========================================================================================\n\n");

        sb.append("1. FLEET & RESERVATION SUMMARY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Total Scheduled Trains       : %d\n", analytics.getTotalTrains()));
        sb.append(String.format(" Total Booking Transactions   : %d\n", analytics.getTotalBookings()));
        sb.append(String.format(" Total Individual Tickets     : %d\n", analytics.getTotalTicketsBooked()));
        sb.append(String.format(" Active Confirmed Passengers  : %d\n", analytics.getConfirmedTicketsCount()));
        sb.append(String.format(" Passengers in RAC Queue      : %d\n", analytics.getRacTicketsCount()));
        sb.append(String.format(" Passengers in Waiting List   : %d\n", analytics.getWaitingListTicketsCount()));
        sb.append(String.format(" Cancelled Tickets Count      : %d\n", analytics.getCancelledTicketsCount()));
        sb.append(String.format(" Fleet Average Occupancy Rate : %.2f%%\n\n", analytics.getAverageOccupancyPercentage()));

        sb.append("2. FINANCIAL METRICS\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Gross Revenue Generated      : ₹ %12.2f\n", analytics.getTotalGrossRevenue()));
        sb.append(String.format(" Net Active Retained Revenue  : ₹ %12.2f\n\n", analytics.getTotalActiveRevenue()));

        sb.append("3. TRAIN PERFORMANCE & POPULARITY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Most Occupied Train          : %s\n", analytics.getMostOccupiedTrain()));
        sb.append(String.format(" Highest Traffic Route        : %s\n\n", analytics.getMostPopularRoute()));

        sb.append("4. CLASS-WISE PASSENGER DISTRIBUTION\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        Map<SeatClass, Integer> classMap = analytics.getClassWiseOccupancy();
        for (Map.Entry<SeatClass, Integer> entry : classMap.entrySet()) {
            sb.append(String.format(" %-28s : %d passengers\n", entry.getKey().getDisplayName(), entry.getValue()));
        }
        sb.append("\n========================================================================================\n");
        sb.append("                           END OF ADMINISTRATIVE AUDIT REPORT                           \n");
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 2) + "..";
    }
}
