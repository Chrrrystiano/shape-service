package pl.kurs.bank.account.exception;

import lombok.Value;

@Value
public class AccountNotLockedException extends RuntimeException{
    long accountId;
}
