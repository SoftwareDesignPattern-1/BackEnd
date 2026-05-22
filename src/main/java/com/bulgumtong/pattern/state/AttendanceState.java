package com.bulgumtong.pattern.state;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;

public interface AttendanceState {
    void checkIn(AttendanceRecord record);
    void markAbsent(AttendanceRecord record);
    void markLate(AttendanceRecord record);
    AttendanceStatus getStatus();
}
