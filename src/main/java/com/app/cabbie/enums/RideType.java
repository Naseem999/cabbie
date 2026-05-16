package com.app.cabbie.enums;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum RideType {
    NORMAL (50.0, 14.0, 1.25, 80.0),
    PREMIUM (80.0, 80.0, 2.0, 120.0);

    public final double baseFare;
    public final double perKmRate;
    public final double perMinuteRate;
    public final double minimumFare;

    RideType(double baseFare, double perKmRate, double perMinuteRate, double minimumFare) {
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
        this.minimumFare = minimumFare;
    }

}
