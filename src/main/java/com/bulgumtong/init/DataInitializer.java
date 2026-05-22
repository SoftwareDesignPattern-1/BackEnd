package com.bulgumtong.init;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;
import com.bulgumtong.model.Role;
import com.bulgumtong.pattern.state.AbsentState;
import com.bulgumtong.pattern.state.LateState;
import com.bulgumtong.pattern.state.PresentState;
import com.bulgumtong.repository.AttendanceRepository;
import com.bulgumtong.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DataInitializer {

    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    public DataInitializer(MemberRepository memberRepository,
                           AttendanceRepository attendanceRepository) {
        this.memberRepository    = memberRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public void load() {
        // 멤버 5명: 스터디장 1 + 일반 멤버 4
        Member leader = new Member("m-001", "김민준", Role.LEADER);
        Member alice  = new Member("m-002", "이수연", Role.MEMBER);
        Member bob    = new Member("m-003", "박준혁", Role.MEMBER);
        Member carol  = new Member("m-004", "정유나", Role.MEMBER);
        Member dave   = new Member("m-005", "최성현", Role.MEMBER);

        memberRepository.save(leader);
        memberRepository.save(alice);
        memberRepository.save(bob);
        memberRepository.save(carol);
        memberRepository.save(dave);

        // 세션 1: 2주 전
        LocalDate s1 = LocalDate.now().minusWeeks(2);
        LocalDateTime s1Start = s1.atTime(14, 0);
        saveRecord("r-s1-m001", leader.getId(), s1, s1Start, new PresentState());
        saveRecord("r-s1-m002", alice.getId(),  s1, s1Start, new PresentState());
        saveRecord("r-s1-m003", bob.getId(),    s1, s1Start, new AbsentState());
        saveRecord("r-s1-m004", carol.getId(),  s1, s1Start, new LateState());
        saveRecord("r-s1-m005", dave.getId(),   s1, s1Start, new PresentState());

        // 세션 2: 1주 전
        LocalDate s2 = LocalDate.now().minusWeeks(1);
        LocalDateTime s2Start = s2.atTime(14, 0);
        saveRecord("r-s2-m001", leader.getId(), s2, s2Start, new PresentState());
        saveRecord("r-s2-m002", alice.getId(),  s2, s2Start, new LateState());
        saveRecord("r-s2-m003", bob.getId(),    s2, s2Start, new AbsentState());
        saveRecord("r-s2-m004", carol.getId(),  s2, s2Start, new PresentState());
        saveRecord("r-s2-m005", dave.getId(),   s2, s2Start, new AbsentState());
    }

    private void saveRecord(String id, String memberId, LocalDate date,
                            LocalDateTime sessionStart, com.bulgumtong.pattern.state.AttendanceState state) {
        AttendanceRecord record = new AttendanceRecord(id, memberId, date, sessionStart, state);
        attendanceRepository.save(record);
    }
}
