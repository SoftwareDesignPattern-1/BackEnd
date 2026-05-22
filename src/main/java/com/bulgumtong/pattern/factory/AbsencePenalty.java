package com.bulgumtong.pattern.factory;

import com.bulgumtong.config.AppConfig;

public class AbsencePenalty implements Penalty {

    @Override
    public int calculate(int absenceCount) {
        return absenceCount * AppConfig.FINE_PER_ABSENCE;
    }

    @Override
    public String getDescription() {
        return "결석 벌금 (1회당 " + AppConfig.FINE_PER_ABSENCE + "원)";
    }
}
