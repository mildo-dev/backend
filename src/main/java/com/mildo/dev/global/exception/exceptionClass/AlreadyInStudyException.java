package com.mildo.dev.global.exception.exceptionClass;

public class AlreadyInStudyException extends RuntimeException{

    public AlreadyInStudyException() {
        super();
    }

    public AlreadyInStudyException(String message) {
        super(message);
    }
}
