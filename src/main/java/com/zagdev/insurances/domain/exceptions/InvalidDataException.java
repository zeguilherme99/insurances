package com.zagdev.insurances.domain.exceptions;

public class InvalidDataException extends GenericException {

  public InvalidDataException(ErrorCode errorCodes) {
    super(errorCodes);
  }
}
