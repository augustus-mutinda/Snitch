package com.andrewodipo.snitch.Models;

public class Device {
    private String DeviceName;
    private String DeviceID;
    private String DeviceVersion;
    private String UserID;

    public Device() {
        DeviceName = "";
        DeviceID = "";
        DeviceVersion = "";
        UserID = "";
    }

    public Device(String deviceName, String deviceID, String deviceVersion, String userID) {
        DeviceName = deviceName;
        DeviceID = deviceID;
        DeviceVersion = deviceVersion;
        UserID = userID;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public String getDeviceVersion() {
        return DeviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        DeviceVersion = deviceVersion;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }
}
