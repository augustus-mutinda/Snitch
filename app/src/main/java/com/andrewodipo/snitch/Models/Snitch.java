package com.andrewodipo.snitch.Models;

import java.util.Date;

public class Snitch {
    private String DeviceID;
    private String UserID;
    private String DeviceLatitude;
    private String DeviceLongitude;
    private String SnitchTrigger;
    private String SnitchID;
    private Date DateOfEvent;
    private String DeviceName;

    public Snitch() {
        DeviceID = "";
        UserID = "";
        DeviceLatitude = "";
        DeviceLongitude = "";
        SnitchTrigger = "";
        DateOfEvent = null;
        SnitchID = "";
        DeviceName = "";
    }

    public Snitch(String deviceID, String userID, String deviceLatitude,String deviceName, String deviceLongitude, String snitchID, String snitchTrigger, Date dateOfEvent) {
        DeviceID = deviceID;
        UserID = userID;
        DeviceLatitude = deviceLatitude;
        DeviceLongitude = deviceLongitude;
        SnitchTrigger = snitchTrigger;
        DateOfEvent = dateOfEvent;
        SnitchID = snitchID;
        DeviceName = deviceName;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getDeviceLatitude() {
        return DeviceLatitude;
    }

    public void setDeviceLatitude(String deviceLatitude) {
        DeviceLatitude = deviceLatitude;
    }

    public String getDeviceLongitude() {
        return DeviceLongitude;
    }

    public void setDeviceLongitude(String deviceLongitude) {
        DeviceLongitude = deviceLongitude;
    }

    public String getSnitchTrigger() {
        return SnitchTrigger;
    }

    public void setSnitchTrigger(String snitchTrigger) {
        SnitchTrigger = snitchTrigger;
    }

    public Date getDateOfEvent() {
        return DateOfEvent;
    }

    public void setDateOfEvent(Date dateOfEvent) {
        DateOfEvent = dateOfEvent;
    }

    public String getSnitchID() {
        return SnitchID;
    }

    public void setSnitchID(String snitchID) {
        SnitchID = snitchID;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }
}
