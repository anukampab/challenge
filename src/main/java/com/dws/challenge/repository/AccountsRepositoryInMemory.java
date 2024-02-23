package com.dws.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAccountException;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) throws InvalidAccountException, AccountNotFoundException {
        if (StringUtils.isEmpty(accountId)) {
            InvalidAccountException invalidAccountException = new InvalidAccountException(
                    "Account can not be null or empty");
            log.error(invalidAccountException.getMessage(), invalidAccountException);
            throw invalidAccountException;
        }
        Account account = accounts.get(accountId);
        if (account == null) {
            AccountNotFoundException accountNotFoundException = new AccountNotFoundException(
                    "Account with id : " + accountId + " does not exist");
            log.error(accountNotFoundException.getMessage(), accountNotFoundException);
            throw accountNotFoundException;
        }
        return account;
    }

    @Override
    public void clearAccounts() {
        accounts.clear();

    }

}
