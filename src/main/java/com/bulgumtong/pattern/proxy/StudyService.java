package com.bulgumtong.pattern.proxy;

import com.bulgumtong.model.AttendanceRecord;
import com.bulgumtong.model.AttendanceStatus;
import com.bulgumtong.model.Member;

import java.time.LocalDate;
import java.util.List;

public interface StudyService {
    Member addMember(String requesterId, String name, String roleStr);
    List<Member> getAllMembers();
    AttendanceRecord checkAttendance(String requesterId, String targetMemberId,
                                     LocalDate date, AttendanceStatus status);
    List<AttendanceRecord> getRecordsByMember(String requesterId, String memberId);
    List<AttendanceRecord> getAllRecords(String requesterId);
    List<String> getCommandHistory(String requesterId);
}
