package com.bulgumtong.repository;

import com.bulgumtong.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemberRepository {

    private final ConcurrentHashMap<String, Member> store = new ConcurrentHashMap<>();

    public synchronized void save(Member member) {
        store.put(member.getId(), member);
    }

    public Optional<Member> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean exists(String id) {
        return store.containsKey(id);
    }
}
