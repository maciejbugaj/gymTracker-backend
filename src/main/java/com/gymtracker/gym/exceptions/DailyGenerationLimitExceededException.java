package com.gymtracker.gym.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class DailyGenerationLimitExceededException extends RuntimeException {
    public DailyGenerationLimitExceededException(String message) {
        super(message);
    }
}
