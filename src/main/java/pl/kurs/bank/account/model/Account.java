package pl.kurs.bank.account.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.kurs.bank.account.exception.AccountLockedException;
import pl.kurs.bank.account.exception.AccountNotLockedException;
import pl.kurs.bank.account.exception.AccountOperationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private BigDecimal balance;
    @CreatedDate
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    private Type type;

    private boolean locked;



    public void withdraw(BigDecimal amount) {
        checkLock();
        if (balance.compareTo(amount) < 0) {
            throw new AccountOperationException("Unable to withdraw!");
        }
        this.balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        checkLock();
        this.balance = balance.add(amount);
    }

    public void lock() {
        checkLock();
        this.locked = true;
    }

    public void unlock() {
        if (!locked) {
            throw new AccountNotLockedException(id);
        }
        this.locked = false;
    }


    private void checkLock() {
        if (locked) {
            throw new AccountLockedException(id);
        }
    }

    public enum Type {
        REGULAR, SAVINGS
    }
}
