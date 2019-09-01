package com.andrewodipo.snitch.Models;

public class Profile {
    private String FirstName;
    private String SecondName;
    private String AlphaDeviceID;
    private String BetaDeviceID;
    private String GammaDeviceID;
    private String ProfilePicture;
    private String Email;

    public Profile() {
        FirstName = "";
        SecondName = "";
        AlphaDeviceID = "";
        BetaDeviceID = "";
        GammaDeviceID = "";
        ProfilePicture = "";
        Email = "";
    }

    public Profile(String firstName, String secondName, String alphaDeviceID, String betaDeviceID, String gammaDeviceID, String profilePicture, String email) {
        FirstName = firstName;
        SecondName = secondName;
        AlphaDeviceID = alphaDeviceID;
        BetaDeviceID = betaDeviceID;
        GammaDeviceID = gammaDeviceID;
        ProfilePicture = profilePicture;
        Email = email;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getSecondName() {
        return SecondName;
    }

    public void setSecondName(String secondName) {
        SecondName = secondName;
    }

    public String getAlphaDeviceID() {
        return AlphaDeviceID;
    }

    public void setAlphaDeviceID(String alphaDeviceID) {
        AlphaDeviceID = alphaDeviceID;
    }

    public String getBetaDeviceID() {
        return BetaDeviceID;
    }

    public void setBetaDeviceID(String betaDeviceID) {
        BetaDeviceID = betaDeviceID;
    }

    public String getGammaDeviceID() {
        return GammaDeviceID;
    }

    public void setGammaDeviceID(String gammaDeviceID) {
        GammaDeviceID = gammaDeviceID;
    }

    public String getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        ProfilePicture = profilePicture;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
