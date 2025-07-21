package com.zagdev.insurances.application.adapters.config;

import com.zagdev.insurances.domain.exceptions.DataNotFoundException;
import com.zagdev.insurances.domain.exceptions.ErrorCode;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class CustomResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomResponseExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String endpoint = ((ServletWebRequest) request).getRequest().getRequestURI();

        List<ResponseError.FieldError> fieldsErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldErrorFound -> {
            String fieldName = fieldErrorFound.getField();
            String errorMessage = fieldErrorFound.getDefaultMessage();

            Optional<ResponseError.FieldError> fieldErrorIfFound = fieldsErrors.stream()
                    .filter(fieldError -> fieldName.equals(fieldError.name()))
                    .findAny();

            if (fieldErrorIfFound.isPresent()) {
                fieldErrorIfFound.get().details().add(errorMessage);
            } else {
                fieldsErrors.add(new ResponseError.FieldError(fieldName, new ArrayList<>(Arrays.asList(errorMessage))));
            }
        });

        logger.info("Invalid request to [{}]. Invalid content found: [{}]", endpoint, fieldsErrors);

        return new ResponseEntity<>(ResponseError.build(ErrorCode.INVALID_DATA, fieldsErrors), HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        String endpoint = ((ServletWebRequest) request).getRequest().getRequestURI();

        logger.info("Invalid request to [{}]. Error: [{}]", endpoint, ex.getMessage());

        return new ResponseEntity<>(ResponseError.build(ErrorCode.INVALID_DATA), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError.ResponseErrorMessage> exception(Exception ex) {
        return unexpectedErrorException(new UnexpectedErrorException(ErrorCode.UNEXPECTED_ERROR, ex));
    }

    @ExceptionHandler(UnexpectedErrorException.class)
    public ResponseEntity<ResponseError.ResponseErrorMessage> unexpectedErrorException(UnexpectedErrorException ex) {
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ResponseError.build(ex.getErrorCode()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ResponseError.ResponseErrorMessage> invalidDataException(InvalidDataException ex) {
        logger.info(ex.getMessage(), ex);
        return new ResponseEntity<>(ResponseError.build(ex.getErrorCode()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ResponseError.ResponseErrorMessage> dataNotFoundException(DataNotFoundException ex) {
        logger.info(ex.getMessage(), ex);
        return new ResponseEntity<>(ResponseError.build(ex.getErrorCode()), HttpStatus.NOT_FOUND);
    }
}
