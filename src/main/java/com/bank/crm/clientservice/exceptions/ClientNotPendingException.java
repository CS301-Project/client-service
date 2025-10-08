package com.bank.crm.clientservice.exceptions;

public class ClientNotPendingException extends RuntimeException {
  public ClientNotPendingException(String message) {
    super(message);
  }
}
