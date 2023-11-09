package pl.kurs.bank.account.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.kurs.bank.account.model.Account;
import pl.kurs.bank.account.model.command.DepositCashCommand;
import pl.kurs.bank.account.model.command.WithdrawCashCommand;
import pl.kurs.bank.account.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{id}/withdraw")
    public void withdraw(@PathVariable long id, @RequestBody WithdrawCashCommand command) {
        accountService.withdraw(command);
    }

    @PostMapping("/{id}/deposit")
    public void deposit(@PathVariable long id, @RequestBody DepositCashCommand command) {
        accountService.deposit(command);
    }

    @PostMapping("/{id}/lock")
    public void lock(@PathVariable long id) {
        accountService.lock(id);
    }

    @PostMapping("/{id}/unlock")
    public void unlock(@PathVariable long id) {
        accountService.unlock(id);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable long id) {
        return accountService.getAccount(id);
    }


}
