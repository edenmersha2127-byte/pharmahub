package com.medicinelocator.paymentservice.application.command;

public class ConfirmPaymentCommand {

    private final String txRef;

    public ConfirmPaymentCommand(String txRef) {
        this.txRef = txRef;
    }

    public String getTxRef() { return txRef; }
}
