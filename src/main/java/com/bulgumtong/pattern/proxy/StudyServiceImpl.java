package com.bulgumtong.pattern.proxy;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;
import com.bulgumtong.model.Role;
import com.bulgumtong.repository.MemberRepository;
import com.bulgumtong.service.AttendanceService;

import java.time.LocalDate;
import java.util.List;

public class StudyServiceImpl implements StudyService {

    private final MemberRepository memberRepository;
    private final AttendanceService attendanceService;

    public StudyServiceImpl(MemberRepository memberRepository, AttendanceService attendanceService) {
        this.memberRepository  = memberRepository;
        this.attendanceService = attendanceService;
    }

    @Override
    public Member addMember(String requesterId, String name, String roleStr) {
        Role role = "LEADER".equalsIgnoreCase(roleStr) ? Role.LEADER : Role.MEMBER;
        Member member = new Member(name, role);
        memberRepository.save(member);
        return member;
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public AttendanceRecord checkAttendance(String requesterId, String targetMemberId,
                                            LocalDate date, AttendanceStatus status) {
        return attendanceService.checkAttendance(requesterId, targetMemberId, date, status);
    }

    @Override
    public List<AttendanceRecord> getRecordsByMember(String requesterId, String memberId) {
        return attendanceService.getRecordsByMember(memberId);
    }

    @Override
    public List<AttendanceRecord> getAllRecords(String requesterId) {
        return attendanceService.getAllRecords();
    }

    @Override
    public List<String> getCommandHistory(String requesterId) {
        return attendanceService.getHistory();
    }
}
