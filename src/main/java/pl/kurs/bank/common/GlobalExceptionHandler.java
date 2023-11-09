package pl.kurs.bank.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.kurs.bank.account.exception.AccountLockedException;
import pl.kurs.bank.account.exception.AccountNotFoundException;
import pl.kurs.bank.account.exception.AccountNotLockedException;
import pl.kurs.bank.common.dto.AccountLockedDto;
import pl.kurs.bank.common.dto.NotFoundDto;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<NotFoundDto> handleAccountNotFoundException(AccountNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new NotFoundDto("ACCOUNT", exc.getAccountId()));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<AccountLockedDto> handleAccountLockedException(AccountLockedException exc) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new AccountLockedDto(exc.getAccountId(), "ACCOUNT_LOCKED"));
    }

    @ExceptionHandler(AccountNotLockedException.class)
    public ResponseEntity<AccountLockedDto> handleAccountNotLockedException(AccountNotLockedException exc) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new AccountLockedDto(exc.getAccountId(), "ACCOUNT_NOT_LOCKED"));
    }

}
