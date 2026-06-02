package com.bulgumtong.model;

public enum AttendanceStatus {
    PRESENT("출석"),
    ABSENT("결석"),
    LATE("지각");

    private final String label;

    AttendanceStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
