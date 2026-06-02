package com.bulgumtong.pattern.proxy;

import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;
import com.bulgumtong.model.Role;
import com.bulgumtong.pattern.command.CommandHistory;
import com.bulgumtong.repository.AttendanceRepository;
import com.bulgumtong.repository.MemberRepository;
import com.bulgumtong.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AuthProxyTest {

    private StudyService service;
    private Member leader;
    private Member member;

    @BeforeEach
    void setUp() {
        MemberRepository     memberRepo    = new MemberRepository();
        AttendanceRepository attendanceRepo = new AttendanceRepository();
        CommandHistory       cmdHistory    = new CommandHistory();

        AttendanceService attendanceSvc = new AttendanceService(attendanceRepo, memberRepo, cmdHistory);
        StudyServiceImpl  real          = new StudyServiceImpl(memberRepo, attendanceSvc);
        service = new AuthProxy(real, memberRepo);

        leader = new Member("leader-1", "김민준", Role.LEADER);
        member = new Member("member-1", "이수연", Role.MEMBER);
        memberRepo.save(leader);
        memberRepo.save(member);
    }

    @Test
    void leaderCanAddMember() {
        Member added = service.addMember(leader.getId(), "신규", "MEMBER");
        assertNotNull(added);
        assertEquals("신규", added.getName());
    }

    @Test
    void memberCannotAddMember() {
        assertThrows(UnauthorizedException.class,
            () -> service.addMember(member.getId(), "신규", "MEMBER"));
    }

    @Test
    void memberCanCheckOwnAttendance() {
        assertDoesNotThrow(() ->
            service.checkAttendance(member.getId(), member.getId(), LocalDate.now(), AttendanceStatus.LATE));
    }

    @Test
    void memberCannotCheckOthersAttendance() {
        assertThrows(UnauthorizedException.class, () ->
            service.checkAttendance(member.getId(), leader.getId(), LocalDate.now(), AttendanceStatus.PRESENT));
    }

    @Test
    void leaderCanViewAllRecords() {
        assertDoesNotThrow(() -> service.getAllRecords(leader.getId()));
    }

    @Test
    void memberCannotViewAllRecords() {
        assertThrows(UnauthorizedException.class, () -> service.getAllRecords(member.getId()));
    }

    @Test
    void leaderCanViewHistory() {
        assertDoesNotThrow(() -> service.getCommandHistory(leader.getId()));
    }

    @Test
    void memberCannotViewHistory() {
        assertThrows(UnauthorizedException.class, () -> service.getCommandHistory(member.getId()));
    }
}
