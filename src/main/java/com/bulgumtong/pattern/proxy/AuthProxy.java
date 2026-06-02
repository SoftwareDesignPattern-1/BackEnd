package com.bulgumtong.pattern.proxy;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;
import com.bulgumtong.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;

public class AuthProxy implements StudyService {

    private final StudyService real;
    private final MemberRepository memberRepository;

    public AuthProxy(StudyService real, MemberRepository memberRepository) {
        this.real             = real;
        this.memberRepository = memberRepository;
    }

    @Override
    public Member addMember(String requesterId, String name, String roleStr) {
        requireLeader(requesterId);
        return real.addMember(requesterId, name, roleStr);
    }

    @Override
    public List<Member> getAllMembers() {
        return real.getAllMembers();
    }

    @Override
    public AttendanceRecord checkAttendance(String requesterId, String targetMemberId,
                                            LocalDate date, AttendanceStatus status) {
        Member requester = getRequester(requesterId);
        if (!requester.isLeader() && !requesterId.equals(targetMemberId)) {
            throw new UnauthorizedException("본인 출결만 체크할 수 있습니다.");
        }
        return real.checkAttendance(requesterId, targetMemberId, date, status);
    }

    @Override
    public List<AttendanceRecord> getRecordsByMember(String requesterId, String memberId) {
        Member requester = getRequester(requesterId);
        if (!requester.isLeader() && !requesterId.equals(memberId)) {
            throw new UnauthorizedException("본인 기록만 조회할 수 있습니다.");
        }
        return real.getRecordsByMember(requesterId, memberId);
    }

    @Override
    public List<AttendanceRecord> getAllRecords(String requesterId) {
        requireLeader(requesterId);
        return real.getAllRecords(requesterId);
    }

    @Override
    public List<String> getCommandHistory(String requesterId) {
        requireLeader(requesterId);
        return real.getCommandHistory(requesterId);
    }

    private Member getRequester(String requesterId) {
        return memberRepository.findById(requesterId)
            .orElseThrow(() -> new UnauthorizedException("인증되지 않은 사용자입니다: " + requesterId));
    }

    private void requireLeader(String requesterId) {
        Member requester = getRequester(requesterId);
        if (!requester.isLeader()) {
            throw new UnauthorizedException("스터디장만 접근할 수 있는 기능입니다.");
        }
    }
}
