package pl.kurs.bank.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.bank.Main;
import pl.kurs.bank.account.model.Account;
import pl.kurs.bank.account.model.command.DepositCashCommand;
import pl.kurs.bank.account.repository.AccountRepository;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ActiveProfiles("it")
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldDepositCash() throws Exception {
        //given:
        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        DepositCashCommand command = new DepositCashCommand();
        command.setAccountId(savedAccount.getId());
        command.setAmount(new BigDecimal("1000"));

        String requestJson = objectMapper.writeValueAsString(command);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        //then:
        Account accountFromDb = accountRepository.findById(savedAccount.getId()).get();
        Assertions.assertTrue(new BigDecimal("1000").compareTo(accountFromDb.getBalance()) == 0);
    }

    @Test
    void shouldNotDepositCash_accountNotFound() throws Exception {
        //given:
        DepositCashCommand command = new DepositCashCommand();
        command.setAccountId(-1);
        command.setAmount(new BigDecimal("1000"));

        String requestJson = objectMapper.writeValueAsString(command);

        //when:
        mockMvc.perform(post("/api/v1/accounts/-1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("ACCOUNT"))
                .andExpect(jsonPath("$.id").value(-1));

    }

    @Test
    void shouldNotDepositCash_accountLocked() throws Exception {
        //given:

        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setLocked(true);

        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        DepositCashCommand command = new DepositCashCommand();
        command.setAccountId(savedAccount.getId());
        command.setAmount(new BigDecimal("1000"));

        String requestJson = objectMapper.writeValueAsString(command);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.accountId").value(savedAccount.getId()));

    }

    @Test
    void shouldLockAccount() throws Exception {
        //given:
        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/lock"))
                .andExpect(status().isOk());

        //then:
        Account accountFromDb = accountRepository.findById(savedAccount.getId()).get();
        Assertions.assertTrue(accountFromDb.isLocked());
    }

    @Test
    void shouldNotLockAccount_accountAlreadyLocked() throws Exception {
        //given:
        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setLocked(true);
        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.accountId").value(savedAccount.getId()));

        //then:
        Account accountFromDb = accountRepository.findById(savedAccount.getId()).get();
        Assertions.assertTrue(accountFromDb.isLocked());
    }

    @Test
    void shouldUnlockAccount() throws Exception {
        //given:
        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setLocked(true);
        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/unlock"))
                .andExpect(status().isOk());

        //then:
        Account accountFromDb = accountRepository.findById(savedAccount.getId()).get();
        Assertions.assertFalse(accountFromDb.isLocked());
    }

    @Test
    void shouldNotUnlockAccount_accountNotLocked() throws Exception {
        //given:
        Account testAccount = new Account();
        testAccount.setType(Account.Type.REGULAR);
        testAccount.setBalance(BigDecimal.ZERO);
        testAccount.setLocked(false);
        Account savedAccount = accountRepository.saveAndFlush(testAccount);

        //when:
        mockMvc.perform(post("/api/v1/accounts/" + savedAccount.getId() + "/unlock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ACCOUNT_NOT_LOCKED"))
                .andExpect(jsonPath("$.accountId").value(savedAccount.getId()));

        //then:
        Account accountFromDb = accountRepository.findById(savedAccount.getId()).get();
        Assertions.assertFalse(accountFromDb.isLocked());
    }


}