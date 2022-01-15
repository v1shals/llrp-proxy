package com.wp.llrp.reader;

public class LLRPException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LLRPException(Exception e) {
        super(e);
    }

    public LLRPException() {
    }
}
