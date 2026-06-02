package com.bulgumtong.pattern.command;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

public class CommandHistory {

    private final Deque<Command> history = new ArrayDeque<>();

    public void push(Command command) {
        history.push(command);
    }

    public void undoLast() {
        if (history.isEmpty()) {
            throw new IllegalStateException("되돌릴 이력이 없습니다.");
        }
        history.pop().undo();
    }

    public List<String> getDescriptions() {
        List<String> result = new ArrayList<>();
        for (Command cmd : history) {
            result.add(cmd.getDescription());
        }
        return Collections.unmodifiableList(result);
    }

    public boolean isEmpty() { return history.isEmpty(); }
    public int size()        { return history.size(); }
}
