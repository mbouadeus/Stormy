package com.treehouse.stormy.model;

public class LocationLabel {

    private String city;

    private String state;

    public LocationLabel(String city, String state) {
        this.city = city;
        this.state = state;
    }

    public LocationLabel() {
    }

    public String getFormattedLocation() {
        return city + ", " + state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String locationLabel) {
        this.city = locationLabel;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
