package com.bulgumtong.pattern.command;

public interface Command {
    void execute();
    void undo();
    String getDescription();
}
