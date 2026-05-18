package com.kotak.onboarding.model;

public class OnboardingRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String state;

    public String getFirstName()  { return firstName; }
    public String getLastName()   { return lastName; }
    public String getEmail()      { return email; }
    public String getPhone()      { return phone; }
    public String getState()      { return state; }

    public void setFirstName(String v)  { firstName = v; }
    public void setLastName(String v)   { lastName = v; }
    public void setEmail(String v)      { email = v; }
    public void setPhone(String v)      { phone = v; }
    public void setState(String v)      { state = v; }
}
