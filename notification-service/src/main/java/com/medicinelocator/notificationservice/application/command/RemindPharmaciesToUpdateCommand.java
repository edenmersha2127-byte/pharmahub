package com.medicinelocator.notificationservice.application.command;

/**
 * Fired by the nightly scheduler to notify every approved pharmacy to keep
 * its medicines up to date. Carries no data — the audience is resolved from
 * the Auth Service inside the handler.
 */
public class RemindPharmaciesToUpdateCommand {

    public RemindPharmaciesToUpdateCommand() {
    }
}
