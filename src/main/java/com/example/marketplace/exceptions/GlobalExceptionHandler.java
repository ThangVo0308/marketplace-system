package com.example.marketplace.exceptions;

import com.example.marketplace.dtos.responses.other.CommonResponse;
import com.example.marketplace.exceptions.authenication.AuthenticationErrorCode;
import com.example.marketplace.exceptions.authenication.AuthenticationException;
import com.example.marketplace.exceptions.booking.BookingErrorCode;
import com.example.marketplace.exceptions.booking.BookingException;
import com.example.marketplace.exceptions.file.FileErrorCode;
import com.example.marketplace.exceptions.file.FileException;
import com.example.marketplace.exceptions.payment.PaymentErrorCode;
import com.example.marketplace.exceptions.payment.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import static com.example.marketplace.components.Translator.getLocalizedMessage;
import static com.example.marketplace.exceptions.authenication.AuthenticationErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle exceptions that are not caught by other handlers
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingRuntimeException(RuntimeException exception) {
        log.error("Exception: ", exception);
        return ResponseEntity.badRequest().body(CommonResponse.builder()
                .message(getLocalizedMessage("uncategorized"))
                .build());
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingAuthorizationDeniedException(AuthorizationDeniedException exception) {
        log.error("Authorization Denied Exception: ", exception);
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder()
                .message(getLocalizedMessage("not_have_permission"))
                .build());
    }

    // Handle exceptions about messages that are not found
    @ExceptionHandler(value = NoSuchMessageException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingNoSuchMessageException(NoSuchMessageException exception) {
        log.error("Message Not Found Exception: ", exception);
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder()
                .message(exception.getMessage())
                .build());
    }


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingAppException(AppException exception) {
        log.error("Common error: ", exception);
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder()
                .message(exception.getMessage())
                .build());
    }


    // payment exceptions
    @ExceptionHandler(value = PaymentException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingPaymentException(PaymentException exception) {
        log.error("Payment error: ", exception);
        PaymentErrorCode errorCode = exception.getPaymentErrorCode();
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder()
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .build());
    }

    // booking exceptions
    @ExceptionHandler(value = BookingException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingBookingException(BookingException exception) {
        log.error("Order error: ", exception);
        BookingErrorCode errorCode = exception.getBookingErrorCode();
        return ResponseEntity.status(BAD_REQUEST).body(CommonResponse.builder()
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .build());
    }


    // Handle authentication exceptions
    @ExceptionHandler(value = AuthenticationException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingAuthenticationExceptions(AuthenticationException exception) {
        log.error("Authentication Exception: {}", exception.toString());
        AuthenticationErrorCode errorCode = exception.getAuthenticationErrorCode();
        return ResponseEntity.status(exception.getHttpStatus()).body(CommonResponse.builder()
                .errorCode(errorCode.getCode())
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .errors(switch (exception.getAuthenticationErrorCode()) {
                    case VALIDATION_ERROR -> new HashMap<>(Map.of(
                            "email", getLocalizedMessage(VALIDATION_ERROR.getMessage()),
                            "password", getLocalizedMessage(VALIDATION_ERROR.getMessage())));

                    case EXPIRED_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(EXPIRED_PASSWORD.getMessage())));

                    case TOKEN_INVALID ->
                            new HashMap<>(Map.of("token", getLocalizedMessage(TOKEN_INVALID.getMessage())));

                    case WRONG_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(WRONG_PASSWORD.getMessage())));

                    case PASSWORD_MIS_MATCH ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(PASSWORD_MIS_MATCH.getMessage())));

                    case EMAIL_ALREADY_IN_USE ->
                            new HashMap<>(Map.of("email", getLocalizedMessage(EMAIL_ALREADY_IN_USE.getMessage())));

                    case WEAK_PASSWORD ->
                            new HashMap<>(Map.of("password", getLocalizedMessage(WEAK_PASSWORD.getMessage())));

                    case INVALID_EMAIL ->
                            new HashMap<>(Map.of("email", getLocalizedMessage(INVALID_EMAIL.getMessage())));

                    case TERMS_NOT_ACCEPTED ->
                            new HashMap<>(Map.of("termsAccepted", getLocalizedMessage(TERMS_NOT_ACCEPTED.getMessage())));

                    case CODE_INVALID ->
                            new HashMap<>(Map.of("code", getLocalizedMessage(CODE_INVALID.getMessage())));

                    default -> null;
                })
                .build());
    }

    // Handle file storage exceptions
    @ExceptionHandler(value = FileException.class)
    ResponseEntity<CommonResponse<?, ?>> handlingFileStorageExceptions(FileException exception) {
        log.error("File Storage Exception: {}", exception.toString());
        FileErrorCode errorCode = exception.getFileErrorCode();
        return ResponseEntity.status(exception.getHttpStatus()).body(CommonResponse.builder()
                .errorCode(errorCode.getCode())
                .message((exception.getMoreInfo() != null)
                        ? getLocalizedMessage(errorCode.getMessage(), exception.getMoreInfo())
                        : getLocalizedMessage(errorCode.getMessage()))
                .build());
    }

    // Handle exceptions that request data is invalid (validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?, ?>>
    handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException e) {
        log.error("Validation Exception: ", e);
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach((error) -> {
                    String field = ((FieldError) error).getField();
                    String validationType = determineValidationType((FieldError) error);

                    assert validationType != null;
                    String message = switch (validationType) {
                        case "NotBlank", "NotNull" -> getLocalizedMessage(error.getDefaultMessage(), field);
                        case "Size" -> {
                            Object min = getArgument((FieldError) error, 2);
                            Object max = getArgument((FieldError) error, 1);
                            if (min != null && max != null) {
                                yield getLocalizedMessage(error.getDefaultMessage(), field, min, max);
                            }
                            yield getLocalizedMessage(error.getDefaultMessage());
                        }
                        case "Min", "Max" -> {
                            Object value = getArgument((FieldError) error, 1);
                            if (value != null) {
                                yield getLocalizedMessage(error.getDefaultMessage(), field, value);
                            }
                            yield getLocalizedMessage(error.getDefaultMessage());
                        }
                        case "Unknown" -> getLocalizedMessage("unknown_error");
                        default -> getLocalizedMessage(error.getDefaultMessage());
                    };
                    errors.put(field, message);
                });

        return ResponseEntity.status(BAD_REQUEST).body(
                CommonResponse.builder()
                        .errorCode(VALIDATION_ERROR.getCode())
                        .message(getLocalizedMessage(VALIDATION_ERROR.getMessage()))
                        .errors(errors)
                        .build()
        );
    }

    private String determineValidationType(FieldError fieldError) {
        // Lấy mã lỗi chính (ví dụ: "NotNull", "Size", "Min", "Max", ...)
        // Có thể là getCode() hoặc getCodes()[0] tùy vào cấu hình
        String[] codes = fieldError.getCodes();
        if (codes != null && codes.length > 0) {
            // Mã lỗi thường ở cuối mảng codes
            return codes[codes.length - 1];
        }
        return "Unknown";
    }

    private Object getArgument(FieldError fieldError, int index) {
        Object[] args = fieldError.getArguments();
        if (args != null && args.length > index) {
            return args[index];
        }
        return null;
    }

}