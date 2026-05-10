package com.boilerplate.domain.common.exceptions;

public class NotFoundException extends DomainException{
    public NotFoundException(String message) {
        super(message);
    }
}
