package com.techelevator.model.userprofile;

import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {
    @Size(max = 150)
    private String fullName;

    @Size(max = 32)
    private String phoneNumber;

    // getters/setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
