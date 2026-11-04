package ru.mephi.promoit.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(LinkUnavailableException.class)
    public ProblemDetail handleUnavailable(LinkUnavailableException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.GONE,
                exception.getMessage()
        );
    }

    @ExceptionHandler(LinkConflictException.class)
    public ProblemDetail handleLinkConflict(LinkConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(exception.getMessage());
        return problem;
    }
}
