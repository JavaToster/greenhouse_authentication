package com.example.greenhouse.exceptions.logic;

public class DataConflictException extends  RuntimeException{
    public DataConflictException(String message) {
        super(message);
    }
}
