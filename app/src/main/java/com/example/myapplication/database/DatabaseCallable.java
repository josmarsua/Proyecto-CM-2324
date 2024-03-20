package com.example.myapplication.database;

import com.example.myapplication.database.AppDatabase;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class DatabaseCallable<T> implements Callable<T> {

    AppDatabase appDatabase;

    public final Function<AppDatabase,T> function;

    public DatabaseCallable(AppDatabase appDatabase, Function<AppDatabase, T> function)
    {
        this.function = function;
        this.appDatabase = appDatabase;
    }

    @Override
    public T call() throws Exception
    {
        return function.apply(appDatabase);
    }
}
