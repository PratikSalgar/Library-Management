package com.libraryManagement.Exception;

public class BadCredentialsException extends RuntimeException
{
    public BadCredentialsException (String message)
    {
        super(message);
    }
}
