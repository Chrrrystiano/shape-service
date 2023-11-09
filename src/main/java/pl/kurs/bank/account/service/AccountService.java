package pl.kurs.bank.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.bank.account.exception.AccountNotFoundException;
import pl.kurs.bank.account.exception.AccountOperationException;
import pl.kurs.bank.account.model.Account;
import pl.kurs.bank.account.model.command.DepositCashCommand;
import pl.kurs.bank.account.model.command.WithdrawCashCommand;
import pl.kurs.bank.account.repository.AccountRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void withdraw(WithdrawCashCommand command) {
        Account account = accountRepository.findByIdWithPessimisticLock(command.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.getAccountId()));
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountOperationException("Amount to withdraw must be positive.");
        }
        if (account.getBalance().compareTo(command.getAmount()) < 0) {
            throw new AccountOperationException("Insufficient funds for withdrawal.");
        }
        account.withdraw(command.getAmount());
    }

    @Transactional
    public void deposit(DepositCashCommand command) {
        Account account = accountRepository.findByIdWithPessimisticLock(command.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.getAccountId()));
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountOperationException("Amount to deposit must be positive.");
        }
        account.deposit(command.getAmount());
    }

    @Transactional
    public void lock(long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        account.lock();
    }

    @Transactional
    public void unlock(long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        account.unlock();
    }

    @Transactional(readOnly = true)
    public Account getAccount(long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
