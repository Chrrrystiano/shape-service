package pl.kurs.bank.account.exception;


import lombok.Value;

@Value
public class AccountNotFoundException extends RuntimeException {
    long accountId;
}
