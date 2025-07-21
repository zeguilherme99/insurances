package com.zagdev.insurances.domain.exceptions;

public class InvalidDataException extends GenericException {

  public InvalidDataException(ErrorCode errorCodes) {
    super(errorCodes);
  }

  public InvalidDataException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode, throwable);
  }

}
