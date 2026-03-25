package com.atheboy.shiftpilot.web;

import com.atheboy.shiftpilot.service.AssignmentRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {RecommendationApiController.class, DashboardApiController.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(AssignmentRuleViolationException.class)
    public ProblemDetail handleAssignmentRuleViolation(AssignmentRuleViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Assignment rule violated");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        problemDetail.setTitle("Unexpected server error");
        return problemDetail;
    }
}
