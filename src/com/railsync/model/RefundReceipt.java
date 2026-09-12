package com.railsync.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a formal refund voucher generated upon ticket cancellation.
 * Uses StringBuilder for high-performance receipt formatting.
 */
public class RefundReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String receiptId;
    private final String pnr;
    private final String passengerName;
    private final String trainNumber;
    private final LocalDateTime cancellationTime;
    private final double originalFare;
    private final double cancellationCharge;
    private final double netRefundAmount;
    private final String reason;

    public RefundReceipt(String pnr, String passengerName, String trainNumber,
                         double originalFare, double cancellationCharge,
                         double netRefundAmount, String reason) {
        this.receiptId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.cancellationTime = LocalDateTime.now();
        this.originalFare = originalFare;
        this.cancellationCharge = cancellationCharge;
        this.netRefundAmount = netRefundAmount;
        this.reason = reason;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getPnr() {
        return pnr;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }

    public double getOriginalFare() {
        return originalFare;
    }

    public double getCancellationCharge() {
        return cancellationCharge;
    }

    public double getNetRefundAmount() {
        return netRefundAmount;
    }

    public String getReason() {
        return reason;
    }

    public String getFormattedReceipt() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("========================================================\n");
        sb.append("           RAILSYNC - OFFICIAL REFUND RECEIPT           \n");
        sb.append("========================================================\n");
        sb.append(String.format("Receipt ID         : %s\n", receiptId));
        sb.append(String.format("PNR Number         : %s\n", pnr));
        sb.append(String.format("Passenger Name     : %s\n", passengerName));
        sb.append(String.format("Train Number       : %s\n", trainNumber));
        sb.append(String.format("Cancellation Date  : %s\n",
                cancellationTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"))));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("Original Fare Paid : ₹ %10.2f\n", originalFare));
        sb.append(String.format("Cancellation Fee   : ₹ %10.2f\n", cancellationCharge));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("NET REFUND PAYABLE : ₹ %10.2f\n", netRefundAmount));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("Status/Remarks     : %s\n", reason));
        sb.append("Refund will be credited back to the original payment\n");
        sb.append("method within 3-5 business days as per IR rules.\n");
        sb.append("========================================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "RefundReceipt[" + receiptId + " | PNR: " + pnr + " | Refund: ₹" + netRefundAmount + "]";
    }
}
