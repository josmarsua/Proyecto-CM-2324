package com.example.myapplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalExecutorService {

    private static final int num_threads = 10;
    private static ExecutorService instance;

    public static ExecutorService getInstance()
    {
        if(instance == null)
        {
            instance = Executors.newFixedThreadPool(num_threads);
        }
        return instance;
    }
}
