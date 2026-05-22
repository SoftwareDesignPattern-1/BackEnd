package com.bulgumtong.pattern.command;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.pattern.state.AttendanceState;

import java.time.LocalDateTime;

public class CheckAttendanceCommand implements Command {

    private final AttendanceRecord record;
    private final AttendanceStatus targetStatus;
    private final String executedBy;
    private AttendanceState previousState;
    private LocalDateTime previousCheckedAt;

    public CheckAttendanceCommand(AttendanceRecord record, AttendanceStatus targetStatus, String executedBy) {
        this.record = record;
        this.targetStatus = targetStatus;
        this.executedBy = executedBy;
    }

    @Override
    public void execute() {
        previousState = record.getCurrentState();
        previousCheckedAt = record.getCheckedAt();

        switch (targetStatus) {
            case PRESENT -> record.checkIn();
            case ABSENT  -> record.markAbsent();
            case LATE    -> record.markLate();
        }
    }

    @Override
    public void undo() {
        if (previousState == null) return;
        record.setState(previousState);
        record.setCheckedAt(previousCheckedAt);
    }

    @Override
    public String getDescription() {
        return String.format("[%s] 출결 변경 → %s (by %s)",
            record.getMemberId(), targetStatus.getLabel(), executedBy);
    }

    public AttendanceRecord getRecord() { return record; }
    public AttendanceStatus getTargetStatus() { return targetStatus; }
    public String getExecutedBy() { return executedBy; }
}
