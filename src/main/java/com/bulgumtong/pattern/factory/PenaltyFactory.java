package com.bulgumtong.pattern.factory;

public class PenaltyFactory {

    public enum PenaltyType {
        ABSENCE, LATE
    }

    public static Penalty create(PenaltyType type) {
        return switch (type) {
            case ABSENCE -> new AbsencePenalty();
            case LATE    -> new LatePenalty();
        };
    }

    private PenaltyFactory() {}
}
