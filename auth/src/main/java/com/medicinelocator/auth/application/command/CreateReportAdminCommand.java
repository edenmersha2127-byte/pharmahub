package com.medicinelocator.auth.application.command;

public class CreateReportAdminCommand {

    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public CreateReportAdminCommand(String email, String password,
                                    String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
