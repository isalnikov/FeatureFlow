package com.featureflow.data.config;

import com.featureflow.domain.exception.DomainException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Entity Not Found");
        problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Domain Validation Error");
        problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return problem;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Concurrent modification detected");
        problem.setTitle("Concurrent Modification");
        problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        var detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation Failed");
        problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        return problem;
    }
}
