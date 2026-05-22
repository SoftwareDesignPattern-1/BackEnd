package com.bulgumtong.pattern.state;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;

public class PresentState implements AttendanceState {

    @Override
    public void checkIn(AttendanceRecord record) {
        throw new IllegalStateException("이미 출석 처리된 상태입니다.");
    }

    @Override
    public void markAbsent(AttendanceRecord record) {
        record.setState(new AbsentState());
        record.setCheckedAt(null);
    }

    @Override
    public void markLate(AttendanceRecord record) {
        record.setState(new LateState());
    }

    @Override
    public AttendanceStatus getStatus() {
        return AttendanceStatus.PRESENT;
    }
}
