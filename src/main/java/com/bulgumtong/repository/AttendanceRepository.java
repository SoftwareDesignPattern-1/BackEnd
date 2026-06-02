package com.bulgumtong.repository;

import com.bulgumtong.model.AttendanceRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AttendanceRepository {

    private final ConcurrentHashMap<String, AttendanceRecord> store = new ConcurrentHashMap<>();

    public synchronized void save(AttendanceRecord record) {
        store.put(record.getId(), record);
    }

    public Optional<AttendanceRecord> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<AttendanceRecord> findByMemberAndDate(String memberId, LocalDate date) {
        return store.values().stream()
            .filter(r -> r.getMemberId().equals(memberId) && r.getSessionDate().equals(date))
            .findFirst();
    }

    public List<AttendanceRecord> findAllByMember(String memberId) {
        return store.values().stream()
            .filter(r -> r.getMemberId().equals(memberId))
            .sorted((a, b) -> b.getSessionDate().compareTo(a.getSessionDate()))
            .collect(Collectors.toList());
    }

    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<AttendanceRecord> findAllByDate(LocalDate date) {
        return store.values().stream()
            .filter(r -> r.getSessionDate().equals(date))
            .collect(Collectors.toList());
    }
}
