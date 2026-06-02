package com.bulgumtong.pattern.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyFactoryTest {

    @Test
    void absencePenaltyCalculation() {
        Penalty p = PenaltyFactory.create(PenaltyFactory.PenaltyType.ABSENCE);
        assertEquals(10000, p.calculate(2));
        assertEquals(0, p.calculate(0));
    }

    @Test
    void latePenaltyCalculation() {
        Penalty p = PenaltyFactory.create(PenaltyFactory.PenaltyType.LATE);
        assertEquals(6000, p.calculate(3));
    }

    @Test
    void factoryReturnsDifferentInstances() {
        Penalty p1 = PenaltyFactory.create(PenaltyFactory.PenaltyType.ABSENCE);
        Penalty p2 = PenaltyFactory.create(PenaltyFactory.PenaltyType.ABSENCE);
        assertNotSame(p1, p2);
    }
}
