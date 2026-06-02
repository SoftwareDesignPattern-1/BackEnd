package com.bulgumtong.pattern.factory;

import com.bulgumtong.config.AppConfig;

public class LatePenalty implements Penalty {

    @Override
    public int calculate(int lateCount) {
        return lateCount * AppConfig.FINE_PER_LATE;
    }

    @Override
    public String getDescription() {
        return "지각 벌금 (1회당 " + AppConfig.FINE_PER_LATE + "원)";
    }
}
