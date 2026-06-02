package com.bulgumtong.service;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.pattern.factory.Penalty;
import com.bulgumtong.pattern.factory.PenaltyFactory;
import com.bulgumtong.repository.AttendanceRepository;

import java.util.List;

public class FineCalculatorService {

    private final AttendanceRepository attendanceRepository;
    private final Penalty absencePenalty;
    private final Penalty latePenalty;

    public FineCalculatorService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
        this.absencePenalty = PenaltyFactory.create(PenaltyFactory.PenaltyType.ABSENCE);
        this.latePenalty    = PenaltyFactory.create(PenaltyFactory.PenaltyType.LATE);
    }

    public int calculateTotalFine(String memberId) {
        List<AttendanceRecord> records = attendanceRepository.findAllByMember(memberId);
        int absenceCount = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        int lateCount = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        return absencePenalty.calculate(absenceCount) + latePenalty.calculate(lateCount);
    }

    public int getAbsenceCount(String memberId) {
        return (int) attendanceRepository.findAllByMember(memberId).stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
    }

    public int getLateCount(String memberId) {
        return (int) attendanceRepository.findAllByMember(memberId).stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
    }

    public String getPenaltyDescription(PenaltyFactory.PenaltyType type) {
        return PenaltyFactory.create(type).getDescription();
    }
}
