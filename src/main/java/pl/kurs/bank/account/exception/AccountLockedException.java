package pl.kurs.bank.account.exception;

import lombok.Value;

@Value
public class AccountLockedException extends RuntimeException{
    long accountId;
}
