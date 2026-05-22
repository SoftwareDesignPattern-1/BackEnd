package com.bulgumtong.pattern.state;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    private AttendanceRecord recordWithStart(LocalDateTime sessionStart) {
        return new AttendanceRecord("m-001", LocalDate.now(), sessionStart);
    }

    @Test
    void defaultStateIsAbsent() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now());
        assertEquals(AttendanceStatus.ABSENT, r.getStatus());
    }

    @Test
    void checkInWithinWindowTransitionsToPresentState() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now().minusMinutes(5));
        r.checkIn();
        assertEquals(AttendanceStatus.PRESENT, r.getStatus());
    }

    @Test
    void checkInAfterWindowThrowsException() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now().minusMinutes(20));
        assertThrows(IllegalStateException.class, r::checkIn);
    }

    @Test
    void cannotCheckInTwice() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now());
        r.checkIn();
        assertThrows(IllegalStateException.class, r::checkIn);
    }

    @Test
    void markAbsentFromPresent() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now());
        r.checkIn();
        r.markAbsent();
        assertEquals(AttendanceStatus.ABSENT, r.getStatus());
    }

    @Test
    void markLateFromAbsent() {
        AttendanceRecord r = recordWithStart(LocalDateTime.now());
        r.markLate();
        assertEquals(AttendanceStatus.LATE, r.getStatus());
    }
}
