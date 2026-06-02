package com.bulgumtong.model;

import java.util.UUID;

public class Member {
    private final String id;
    private final String name;
    private final Role role;

    public Member(String name, Role role) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
    }

    public Member(String id, String name, Role role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isLeader() { return role == Role.LEADER; }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, role.getLabel());
    }
}
