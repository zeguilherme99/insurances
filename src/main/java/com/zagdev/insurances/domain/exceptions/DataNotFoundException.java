package com.zagdev.insurances.domain.exceptions;

public class DataNotFoundException extends GenericException {

  public DataNotFoundException(ErrorCode errorCodes) {
    super(errorCodes);
  }
}
