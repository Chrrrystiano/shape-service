package pl.kurs.bank.account.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kurs.bank.account.exception.AccountLockedException;
import pl.kurs.bank.account.exception.AccountNotFoundException;
import pl.kurs.bank.account.exception.AccountOperationException;
import pl.kurs.bank.account.model.Account;
import pl.kurs.bank.account.model.command.DepositCashCommand;
import pl.kurs.bank.account.model.command.WithdrawCashCommand;
import pl.kurs.bank.account.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {
    private AccountRepository mockAccountRepository;
    private AccountService underTest;

    @BeforeEach
    void init() {
        mockAccountRepository = mock(AccountRepository.class);
        underTest = new AccountService(mockAccountRepository);
    }

    @Test
    void shouldWithdrawMoney() {
        //given:
        WithdrawCashCommand withdrawCashCommand = new WithdrawCashCommand();
        withdrawCashCommand.setAccountId(100L);
        withdrawCashCommand.setAmount(new BigDecimal("1000"));

        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("2000"));

        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));

        //when:
        underTest.withdraw(withdrawCashCommand);
        //then:
        Assertions.assertEquals(new BigDecimal("1000"), testAccount.getBalance());
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);

    }

    @Test
    void shouldNotWithdrawMoney_limitExceed() {
        //given:
        WithdrawCashCommand withdrawCashCommand = new WithdrawCashCommand();
        withdrawCashCommand.setAccountId(100L);
        withdrawCashCommand.setAmount(new BigDecimal("1000"));

        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("500"));

        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));

        //when:
        Assertions.assertThrows(AccountOperationException.class, () -> underTest.withdraw(withdrawCashCommand));
        //then:
        Assertions.assertEquals(new BigDecimal("500"), testAccount.getBalance());
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldDepositMoney() {
        //given:
        DepositCashCommand depositCashCommand = new DepositCashCommand();
        depositCashCommand.setAccountId(100L);
        depositCashCommand.setAmount(new BigDecimal("1000"));

        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("1000"));

        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));

        //when:
        underTest.deposit(depositCashCommand);
        //then:
        Assertions.assertEquals(new BigDecimal("2000"), testAccount.getBalance());
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldNotDepositMoney_accountNotFound() {
        //given:
        DepositCashCommand depositCashCommand = new DepositCashCommand();
        depositCashCommand.setAccountId(100L);
        depositCashCommand.setAmount(new BigDecimal("1000"));
        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.empty());
        //when:
        Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.deposit(depositCashCommand));
        //then:
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldNotWithdrawMoney_accountNotFound() {
        //given:
        WithdrawCashCommand withdrawCashCommand = new WithdrawCashCommand();
        withdrawCashCommand.setAccountId(100L);
        withdrawCashCommand.setAmount(new BigDecimal("1000"));
        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.empty());
        //when:
        Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.withdraw(withdrawCashCommand));
        //then:
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldNotWithdrawMoney_accountLocked() {
        //given:
        WithdrawCashCommand withdrawCashCommand = new WithdrawCashCommand();
        withdrawCashCommand.setAccountId(100L);
        withdrawCashCommand.setAmount(new BigDecimal("1000"));
        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("1000"));
        testAccount.setLocked(true);

        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));
        //when:
        Assertions.assertThrows(AccountLockedException.class, () -> underTest.withdraw(withdrawCashCommand));
        //then:
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldNotDepositMoney_accountLocked() {
        //given:
        DepositCashCommand depositCashCommand = new DepositCashCommand();
        depositCashCommand.setAccountId(100L);
        depositCashCommand.setAmount(new BigDecimal("1000"));

        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("1000"));
        testAccount.setLocked(true);

        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));
        //when:
        Assertions.assertThrows(AccountLockedException.class, () -> underTest.deposit(depositCashCommand));
        //then:
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }

    @Test
    void shouldLockAccount_whenAccountExists() {
        // given:
        long accountId = 1L;
        Account mockAccount = mock(Account.class);
        when(mockAccountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // when:
        underTest.lock(accountId);

        // then:
        verify(mockAccountRepository, times(1)).findById(accountId);
        verify(mockAccount, times(1)).lock();
    }

    @Test
    void shouldThrowAccountNotFoundException_whenLockingNonExistentAccount() {
        // given:
        long accountId = 1L;
        when(mockAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // when & then:
        Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.lock(accountId));
        verify(mockAccountRepository, times(1)).findById(accountId);
    }

    @Test
    void shouldUnlockAccount_whenAccountExists() {
        // given:
        long accountId = 1L;
        Account mockAccount = mock(Account.class);
        when(mockAccountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // when:
        underTest.unlock(accountId);

        // then:
        verify(mockAccountRepository, times(1)).findById(accountId);
        verify(mockAccount, times(1)).unlock();
    }

    @Test
    void shouldThrowAccountNotFoundException_whenUnlockingNonExistentAccount() {
        // given:
        long accountId = 1L;
        when(mockAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        // when & then:
        Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.unlock(accountId));
        verify(mockAccountRepository, times(1)).findById(accountId);
    }

    @Test
    void shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        //given:
        long nonExistentAccountId = 5000L;
        when(mockAccountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        //when & then:
        Assertions.assertThrows(AccountNotFoundException.class, () -> underTest.getAccount(nonExistentAccountId));
        verify(mockAccountRepository, times(1)).findById(nonExistentAccountId);
    }

    @Test
    void shouldThrowAccountOperationException_whenDepositAmountIsNegative() {
        // given:
        DepositCashCommand depositCashCommand = new DepositCashCommand();
        depositCashCommand.setAccountId(100L);
        depositCashCommand.setAmount(new BigDecimal("-500"));
        Account testAccount = new Account();
        testAccount.setId(100L);
        testAccount.setBalance(new BigDecimal("1000"));
        when(mockAccountRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(testAccount));

        // when:
        Exception exception = Assertions.assertThrows(AccountOperationException.class, () -> underTest.deposit(depositCashCommand));

        // then:
        Assertions.assertEquals("Amount to deposit must be positive.", exception.getMessage());
        verify(mockAccountRepository, times(1)).findByIdWithPessimisticLock(100L);
    }


}