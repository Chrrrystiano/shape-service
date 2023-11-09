package pl.kurs.bank.account.model.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class DepositCashCommand {
    private long accountId;
    private BigDecimal amount;
}
