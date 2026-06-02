package com.bulgumtong.model;

import com.bulgumtong.pattern.state.AbsentState;
import com.bulgumtong.pattern.state.AttendanceState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceRecord {
    private final String id;
    private final String memberId;
    private final LocalDate sessionDate;
    private final LocalDateTime sessionStartTime;
    private AttendanceState state;
    private LocalDateTime checkedAt;

    public AttendanceRecord(String memberId, LocalDate sessionDate, LocalDateTime sessionStartTime) {
        this.id = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.sessionDate = sessionDate;
        this.sessionStartTime = sessionStartTime;
        this.state = new AbsentState();
    }

    public AttendanceRecord(String id, String memberId, LocalDate sessionDate,
                            LocalDateTime sessionStartTime, AttendanceState initialState) {
        this.id = id;
        this.memberId = memberId;
        this.sessionDate = sessionDate;
        this.sessionStartTime = sessionStartTime;
        this.state = initialState;
    }

    public void checkIn()      { state.checkIn(this); }
    public void markAbsent()   { state.markAbsent(this); }
    public void markLate()     { state.markLate(this); }

    public AttendanceStatus getStatus() { return state.getStatus(); }
    public AttendanceState getCurrentState() { return state; }
    public void setState(AttendanceState state) { this.state = state; }

    public String getId()                      { return id; }
    public String getMemberId()                { return memberId; }
    public LocalDate getSessionDate()          { return sessionDate; }
    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
    public LocalDateTime getCheckedAt()        { return checkedAt; }
    public void setCheckedAt(LocalDateTime t)  { this.checkedAt = t; }

    @Override
    public String toString() {
        return String.format("AttendanceRecord{memberId='%s', date=%s, status=%s}",
            memberId, sessionDate, state.getStatus().getLabel());
    }
}
