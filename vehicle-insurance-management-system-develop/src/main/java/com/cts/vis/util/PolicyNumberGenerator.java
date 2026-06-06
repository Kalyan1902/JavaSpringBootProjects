package com.cts.vis.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PolicyNumberGenerator {
    public String generate() {
        int year = Year.now().getValue();
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "POL-" + year + "-" + rand;
    }
}