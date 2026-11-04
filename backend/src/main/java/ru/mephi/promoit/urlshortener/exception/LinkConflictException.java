package ru.mephi.promoit.urlshortener.exception;

public class LinkConflictException extends RuntimeException {

    public LinkConflictException(String message) {
        super(message);
    }
}
