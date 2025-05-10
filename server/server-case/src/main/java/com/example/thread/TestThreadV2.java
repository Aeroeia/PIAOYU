package com.example.thread;

public class TestThreadV2 implements Runnable{
    @Override
    public void run() {
        System.out.println("任务执行");
    }
}
