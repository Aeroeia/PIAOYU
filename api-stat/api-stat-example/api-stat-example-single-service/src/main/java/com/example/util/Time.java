package com.example.util;

import java.util.Random;

public class Time {
    
    public static void simulationTime() {
        Random random = new Random();
        int sleepTime = random.nextInt(2000);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
