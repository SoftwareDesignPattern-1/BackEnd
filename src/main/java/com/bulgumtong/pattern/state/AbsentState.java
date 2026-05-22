package com.bulgumtong.pattern.state;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;

import java.time.LocalDateTime;

public class AbsentState implements AttendanceState {

    @Override
    public void checkIn(AttendanceRecord record) {
        if (!TimeValidator.isWithinWindow(record.getSessionStartTime())) {
            throw new IllegalStateException(
                "출석 인정 시간이 초과되었습니다. 세션 시작 후 " +
                com.bulgumtong.config.AppConfig.ATTENDANCE_WINDOW_MINUTES + "분 이내에만 출석 가능합니다."
            );
        }
        record.setState(new PresentState());
        record.setCheckedAt(LocalDateTime.now());
    }

    @Override
    public void markAbsent(AttendanceRecord record) {
        throw new IllegalStateException("이미 결석 상태입니다.");
    }

    @Override
    public void markLate(AttendanceRecord record) {
        record.setState(new LateState());
        record.setCheckedAt(LocalDateTime.now());
    }

    @Override
    public AttendanceStatus getStatus() {
        return AttendanceStatus.ABSENT;
    }
}
