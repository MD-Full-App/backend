package com.rustam.modern_dentistry.exception;

import com.rustam.modern_dentistry.dto.response.message.ExceptionResponseMessages;
import com.rustam.modern_dentistry.exception.custom.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomError.class)
    public ResponseEntity<ExceptionResponseMessages> handleUsernameNotFoundException(CustomError ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidDateOrTimeException.class)
    public ResponseEntity<ExceptionResponseMessages> invalidDateOrTimeException(InvalidDateOrTimeException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.BAD_REQUEST) ,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(WorkersWorkScheduleNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> workersWorkScheduleNotFoundException(WorkersWorkScheduleNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(WarehouseEntryNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> warehouseEntryNotFoundException(WarehouseEntryNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(TeethExaminationNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> teethExaminationNotFoundException(TeethExaminationNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DoctorIsPatientsWereNotFound.class)
    public ResponseEntity<ExceptionResponseMessages> doctorIsPatientsWereNotFound(DoctorIsPatientsWereNotFound ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoSuchPatientWasFound.class)
    public ResponseEntity<ExceptionResponseMessages> noSuchPatientWasFound(NoSuchPatientWasFound ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoTeethFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> noTeethFoundException(NoTeethFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ExaminationNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> examinationNotFoundException(ExaminationNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ExceptionResponseMessages> doctorNotFoundException(DoctorNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(UserNotFountException.class)
    public ResponseEntity<ExceptionResponseMessages> userNotFoundException(UserNotFountException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.NOT_FOUND) ,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ProductDoesnotQuantityThatMuchException.class)
    public ResponseEntity<ExceptionResponseMessages> productDoesnotQuantityThatMuchException(ProductDoesnotQuantityThatMuchException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.BAD_REQUEST) ,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidUUIDFormatException.class)
    public ResponseEntity<ExceptionResponseMessages> invalidUuidFormatException(InvalidUUIDFormatException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.BAD_REQUEST) ,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AmountSendException.class)
    public ResponseEntity<ExceptionResponseMessages> amountSendException(AmountSendException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.BAD_REQUEST) ,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ExistsException.class)
    public ResponseEntity<ExceptionResponseMessages> existsException(ExistsException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.CONFLICT) ,
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponseMessages> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        log.error("Database constraint violation exception: ", ex);
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        String fullMsg = (msg + " " + rootMsg).toLowerCase();

        String userFriendlyMessage = "Məlumat bazasında təkrar məlumat xətası baş verdi.";

        if (fullMsg.contains("fin_code") || fullMsg.contains("fincode") || fullMsg.contains("fin_code_key")) {
            userFriendlyMessage = "Bu FIN kod artıq sistemdə var";
        } else if (fullMsg.contains("username") || fullMsg.contains("username_key")) {
            userFriendlyMessage = "Bu istifadəçi adı artıq sistemdə var";
        } else if (fullMsg.contains("email") || fullMsg.contains("email_key")) {
            userFriendlyMessage = "Bu e-poçt ünvanı artıq sistemdə var";
        }

        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), userFriendlyMessage, HttpStatus.CONFLICT),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ExceptionResponseMessages> incorrectPasswordException(IncorrectPasswordException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage(), HttpStatus.UNAUTHORIZED) ,
                HttpStatus.UNAUTHORIZED
        );
    }

    // Validation exceptions
    // Learn:
    //  MethodArgumentNotValidException is specific to Spring and is thrown when there are validation errors during the binding of method parameters, typically in a Spring MVC controller method.
    //  It is commonly used when validating incoming request data, such as form submissions or JSON payloads,
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    // Learn:
    //  It is typically thrown when there are constraint violations during the validation of entities or objects, usually when using annotations like @NotNull, @Size, @Email, etc., on fields or properties of a Java class.
    //  This exception can occur when validating entities outside the context of Spring MVC, for example, in a JPA (Java Persistence API) environment.
    //  If you try to persist a User object with a null username using JPA, a ConstraintViolationException may be thrown
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        return new ResponseEntity<>(errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponseMessages> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), "Bu HTTP metodu dəstəklənmir: " + ex.getMethod(), HttpStatus.METHOD_NOT_ALLOWED),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    // For unhandled exceptions:
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponseMessages> generalExceptionHandler(Exception ex){
        log.error("Unhandled exception occurred: ", ex);
        return new ResponseEntity<>(
                new ExceptionResponseMessages(ex.getClass().getName(), ex.getMessage() != null ? ex.getMessage() : "Daxili server xətası baş verdi.", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
