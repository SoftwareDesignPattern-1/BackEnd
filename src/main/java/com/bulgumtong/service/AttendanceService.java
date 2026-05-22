package com.bulgumtong.service;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;
import com.bulgumtong.pattern.command.CheckAttendanceCommand;
import com.bulgumtong.pattern.command.CommandHistory;
import com.bulgumtong.repository.AttendanceRepository;
import com.bulgumtong.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final CommandHistory commandHistory;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             MemberRepository memberRepository,
                             CommandHistory commandHistory) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository     = memberRepository;
        this.commandHistory       = commandHistory;
    }

    public AttendanceRecord checkAttendance(String executedBy, String memberId,
                                            LocalDate date, AttendanceStatus status) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다: " + memberId));

        AttendanceRecord record = attendanceRepository
            .findByMemberAndDate(memberId, date)
            .orElseGet(() -> {
                AttendanceRecord r = new AttendanceRecord(memberId, date, LocalDateTime.now());
                attendanceRepository.save(r);
                return r;
            });

        CheckAttendanceCommand cmd = new CheckAttendanceCommand(record, status, executedBy);
        cmd.execute();
        commandHistory.push(cmd);

        return record;
    }

    public List<AttendanceRecord> getRecordsByMember(String memberId) {
        return attendanceRepository.findAllByMember(memberId);
    }

    public List<AttendanceRecord> getAllRecords() {
        return attendanceRepository.findAll();
    }

    public List<String> getHistory() {
        return commandHistory.getDescriptions();
    }

    public void undoLast() {
        commandHistory.undoLast();
    }
}
