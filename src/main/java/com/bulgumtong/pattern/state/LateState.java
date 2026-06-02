package com.bulgumtong.pattern.state;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;

import java.time.LocalDateTime;

public class LateState implements AttendanceState {

    @Override
    public void checkIn(AttendanceRecord record) {
        record.setState(new PresentState());
        record.setCheckedAt(LocalDateTime.now());
    }

    @Override
    public void markAbsent(AttendanceRecord record) {
        record.setState(new AbsentState());
        record.setCheckedAt(null);
    }

    @Override
    public void markLate(AttendanceRecord record) {
        throw new IllegalStateException("이미 지각 상태입니다.");
    }

    @Override
    public AttendanceStatus getStatus() {
        return AttendanceStatus.LATE;
    }
}
