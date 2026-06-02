package com.bulgumtong.model;

public enum Role {
    LEADER("스터디장"),
    MEMBER("일반 멤버");

    private final String label;

    Role(String label) { this.label = label; }

    public String getLabel() { return label; }
}
