package com.myco.util.v8n;


import com.myco.util.values.ErrorMessage;

public class V8NException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private ErrorMessage errorMessage;

  public V8NException(ErrorMessage errorMessage) {
    super(errorMessage.getMessage());
    this.errorMessage = errorMessage;
  }

  public static void ifError(ErrorMessage errorMessage) {
    if (errorMessage != null) {
      throw new V8NException(errorMessage);
    }
  }

  public ErrorMessage getErrorMessage() {
    return errorMessage;
  }
}
