package com.bulgumtong.report;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.Member;
import com.bulgumtong.repository.MemberRepository;
import com.bulgumtong.service.FineCalculatorService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportPrinter {

    private final MemberRepository memberRepository;
    private final FineCalculatorService fineCalculatorService;

    public ReportPrinter(MemberRepository memberRepository,
                         FineCalculatorService fineCalculatorService) {
        this.memberRepository      = memberRepository;
        this.fineCalculatorService = fineCalculatorService;
    }

    public String generateFineReport() {
        List<Member> members = memberRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("=== 벌금 정산 리포트 ===\n");

        int totalFine = 0;
        for (Member m : members) {
            int absence = fineCalculatorService.getAbsenceCount(m.getId());
            int late    = fineCalculatorService.getLateCount(m.getId());
            int fine    = fineCalculatorService.calculateTotalFine(m.getId());
            totalFine  += fine;
            sb.append(String.format("%-12s | 결석 %d회 | 지각 %d회 | 벌금 %,d원\n",
                m.getName(), absence, late, fine));
        }

        sb.append("----------------------\n");
        sb.append(String.format("총 벌금 합계: %,d원\n", totalFine));
        return sb.toString();
    }

    public String generateAttendanceReport(List<AttendanceRecord> records) {
        if (records.isEmpty()) return "출결 기록이 없습니다.\n";

        Map<String, List<AttendanceRecord>> byDate = records.stream()
            .collect(Collectors.groupingBy(r -> r.getSessionDate().toString()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== 출결 현황 ===\n");
        byDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> {
                sb.append("[ ").append(e.getKey()).append(" ]\n");
                e.getValue().forEach(r ->
                    sb.append(String.format("  멤버: %-10s | %s\n",
                        r.getMemberId(), r.getStatus().getLabel()))
                );
            });
        return sb.toString();
    }
}
