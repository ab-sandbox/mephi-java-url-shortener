package ru.mephi.promoit.urlshortener.exception;

public class LinkUnavailableException extends RuntimeException {

    public LinkUnavailableException(String message) {
        super(message);
    }
}
