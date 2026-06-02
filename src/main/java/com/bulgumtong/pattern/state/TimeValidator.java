package com.bulgumtong.pattern.state;

import com.bulgumtong.config.AppConfig;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeValidator {

    private TimeValidator() {}

    public static boolean isWithinWindow(LocalDateTime sessionStart) {
        Duration elapsed = Duration.between(sessionStart, LocalDateTime.now());
        long minutes = elapsed.toMinutes();
        return minutes >= 0 && minutes <= AppConfig.ATTENDANCE_WINDOW_MINUTES;
    }
}
