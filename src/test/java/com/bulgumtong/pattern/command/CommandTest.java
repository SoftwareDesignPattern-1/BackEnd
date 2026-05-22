package com.bulgumtong.pattern.command;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private AttendanceRecord freshRecord() {
        return new AttendanceRecord("m-001", LocalDate.now(), LocalDateTime.now());
    }

    @Test
    void executeChangesStatus() {
        AttendanceRecord r = freshRecord();
        CheckAttendanceCommand cmd = new CheckAttendanceCommand(r, AttendanceStatus.LATE, "leader");
        cmd.execute();
        assertEquals(AttendanceStatus.LATE, r.getStatus());
    }

    @Test
    void undoRestoresPreviousState() {
        AttendanceRecord r = freshRecord();
        CheckAttendanceCommand cmd = new CheckAttendanceCommand(r, AttendanceStatus.LATE, "leader");
        cmd.execute();
        cmd.undo();
        assertEquals(AttendanceStatus.ABSENT, r.getStatus());
    }

    @Test
    void commandHistoryStoresDescriptions() {
        CommandHistory history = new CommandHistory();
        AttendanceRecord r = freshRecord();
        CheckAttendanceCommand cmd = new CheckAttendanceCommand(r, AttendanceStatus.LATE, "leader");
        cmd.execute();
        history.push(cmd);
        assertEquals(1, history.size());
        assertFalse(history.getDescriptions().isEmpty());
    }

    @Test
    void undoLastFromHistory() {
        CommandHistory history = new CommandHistory();
        AttendanceRecord r = freshRecord();
        CheckAttendanceCommand cmd = new CheckAttendanceCommand(r, AttendanceStatus.LATE, "leader");
        cmd.execute();
        history.push(cmd);
        history.undoLast();
        assertEquals(AttendanceStatus.ABSENT, r.getStatus());
    }
}
