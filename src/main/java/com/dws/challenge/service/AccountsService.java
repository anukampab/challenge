package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.exception.LockException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@Slf4j
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;

    private final Lock transferLock = new ReentrantLock();
    private final Lock withdrawLock = new ReentrantLock();
    private final Lock depositLock = new ReentrantLock();
    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) throws InvalidAccountException, AccountNotFoundException {
        return this.accountsRepository.getAccount(accountId);
    }

    public void clearAccounts() {
        accountsRepository.clearAccounts();
    }

    /**
     * transferAmount: this method validates the account and then initiates the
     * transfer.
     *
     * @param fromAccountId
     * @param toAccountId
     * @param amount
     * @throws InsufficientFundsException
     * @throws InterruptedException
     * @throws InvalidAccountException
     * @throws InvalidAmountException
     * @throws AccountNotFoundException
     * @throws LockException
     */
    public boolean transferAmount(String fromAccountId, String toAccountId, BigDecimal amount)
            throws InsufficientFundsException, InterruptedException, InvalidAccountException, InvalidAmountException,
            AccountNotFoundException, LockException {
        if (validatePositiveAmount(amount)) {
            Account fromAccount = accountsRepository.getAccount(fromAccountId);
            Account toAccount = accountsRepository.getAccount(toAccountId);
            return transfer(fromAccount, toAccount, amount);
        }
        return false;
    }

    /**
     * Transfers a specified amount from one account to another.
     *
     * @param fromAccount The account from which the amount is to be transferred.
     * @param toAccount   The account to which the amount is to be transferred.
     * @param amount      The amount to be transferred.
     * @return True if the transfer is successful, false otherwise.
     * @throws InsufficientFundsException If there are insufficient funds in the source account.
     * @throws InterruptedException       If the thread is interrupted during the transfer.
     * @throws InvalidAmountException     If the amount to be transferred is invalid.
     * @throws LockException              If unable to acquire locks on the accounts.
     */
    private boolean transfer(Account fromAccount, Account toAccount, BigDecimal amount)
            throws InsufficientFundsException, InterruptedException, InvalidAmountException, LockException {
        // Sort accounts based on their IDs to ensure consistent locking order
        Account[] sortedAccounts = {fromAccount, toAccount};
        Arrays.sort(sortedAccounts, Comparator.comparing(Account::getAccountId));

        try {
            transferLock.lock();  // Acquire the transfer lock to ensure atomicity
            if (withdrawAmount(fromAccount, amount)) {
                if (depositAmount(toAccount, amount)) {
                    notificationService.notifyAboutTransfer(fromAccount,
                            "Amount: " + amount + " transferred from account: " + fromAccount.getAccountId()
                                    + " to account: " + toAccount.getAccountId());
                    logTransaction(amount, fromAccount.getAccountId(), toAccount.getAccountId(),
                            fromAccount.getBalance(), toAccount.getBalance());
                    return true;
                } else {
                    // if Deposit fails, rollback withdrawal
                    fromAccount.setBalance(fromAccount.getBalance().add(amount));
                }
            }
        } finally {
            transferLock.unlock();  // Release the transfer lock
        }

        LockException lockException = new LockException("Unable to acquire locks on the accounts");
        log.error(lockException.getMessage(), lockException);
        throw lockException;
    }

    /**
     * Withdraws a specified amount from the given account.
     *
     * @param account The account from which the amount is to be withdrawn.
     * @param amount  The amount to be withdrawn.
     * @return True if the withdrawal is successful, false otherwise.
     * @throws InsufficientFundsException If there are insufficient funds in the account.
     * @throws InterruptedException       If the thread is interrupted during the withdrawal.
     * @throws InvalidAmountException     If the amount to be withdrawn is invalid.
     */
    public boolean withdrawAmount(Account account, BigDecimal amount)
            throws InsufficientFundsException, InterruptedException, InvalidAmountException {
        withdrawLock.lock();  // Acquire the withdraw lock
        try {
            if (validatePositiveAmount(amount)) {
                if (account.getBalance().compareTo(amount) >= 0) {
                    account.setBalance(account.getBalance().subtract(amount));
                    return true;
                } else {
                    InsufficientFundsException insufficientFundsException = new InsufficientFundsException(
                            "Insufficient funds in account:" + account.getAccountId() + " withdrawn amount :" + amount
                                    + " is greater than Account balance : " + account.getBalance());
                    log.error(insufficientFundsException.getMessage(), insufficientFundsException);
                    throw insufficientFundsException;
                }
            }
            return false;
        } finally {
            withdrawLock.unlock();  // Release the withdraw lock
        }
    }

    /**
     * Deposits a specified amount to the given account.
     *
     * @param account The account to which the amount is to be deposited.
     * @param amount  The amount to be deposited.
     * @return True if the deposit is successful, false otherwise.
     * @throws InterruptedException   If the thread is interrupted during the deposit.
     * @throws InvalidAmountException If the amount to be deposited is invalid.
     */
    public boolean depositAmount(Account account, BigDecimal amount)
            throws InterruptedException, InvalidAmountException {
        depositLock.lock();  // Acquire the deposit lock
        try {
            if (validatePositiveAmount(amount)) {
                account.setBalance(account.getBalance().add(amount));
                return true;
            }
            return false;
        } finally {
            depositLock.unlock();  // Release the deposit lock
        }
    }


    /**
     * validatePositiveAmount: validates if amount is positive else throw
     * InvalidAmmountException.
     *
     * @param amount
     * @return
     * @throws InvalidAmountException
     */
    private boolean validatePositiveAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        } else {
            throw new InvalidAmountException("Amount should not be negative");
        }
    }

    /**
     * logTransaction is used for test purpose.
     *
     * @param amount
     * @param fromAccountID
     * @param toAccountID
     * @param fromAccountBalance
     * @param toAccountBalance
     */
    private void logTransaction(BigDecimal amount, String fromAccountId, String toAccountId,
                                BigDecimal fromAccountBalance, BigDecimal toAccountBalance) {
        log.info("{} transferred {} from {} to {}. From Account balance: {} and To Account balance: {}",
                Thread.currentThread().getName(), amount, fromAccountId, toAccountId, fromAccountBalance,
                fromAccountBalance);
    }
}