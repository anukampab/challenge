package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidAmmountException;
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
	 * @throws InvalidAmmountException
	 * @throws AccountNotFoundException
	 * @throws LockException
	 */
	public boolean transferAmount(String fromAccountId, String toAccountId, BigDecimal amount)
			throws InsufficientFundsException, InterruptedException, InvalidAccountException, InvalidAmmountException,
			AccountNotFoundException, LockException {
		if (validatePositiveAmount(amount)) {
			Account fromAccount = accountsRepository.getAccount(fromAccountId);
			Account toAccount = accountsRepository.getAccount(toAccountId);
			return transfer(fromAccount, toAccount, amount);
		}
		return false;
	}

	/**
	 * transfer: This method will synchronizing on and both the accounts to ensure
	 * atomicity. And will rollback in case Withdrawal is successful and Deposit is
	 * unsuccessful
	 * 
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @return
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	private boolean transfer(Account fromAccount, Account toAccount, BigDecimal amount)
			throws InsufficientFundsException, InterruptedException, InvalidAmmountException, LockException {
		// Sort accounts based on their IDs to ensure consistent locking order
		Account[] sortedAccounts = { fromAccount, toAccount };
		Arrays.sort(sortedAccounts, Comparator.comparing(Account::getAccountId));

		synchronized (sortedAccounts[0]) {
			synchronized (sortedAccounts[1]) {
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
			}
		}
		LockException LockException = new LockException("Unable to acquire locks on the accounts");
		log.error(LockException.getMessage(), LockException);
		throw LockException;
	}

	/**
	 * withdrawAmount: This method will withdraw the amount to given account.
	 * 
	 * @param account
	 * @param amount
	 * @return
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 */

	public boolean withdrawAmount(Account account, BigDecimal amount)
			throws InsufficientFundsException, InterruptedException, InvalidAmmountException {
		if (validatePositiveAmount(amount)) {
			if (account.getBalance().compareTo(amount) >= 0) {
				account.setBalance(account.getBalance().subtract(amount));
				return true;
			} else {
				InsufficientFundsException insufficientFundsException = new InsufficientFundsException(
						"Insufficient funds in account:" + account.getAccountId() + " withdrawn amount :" + amount
								+ " is greater then Account balanace : " + account.getBalance());
				log.error(insufficientFundsException.getMessage(), insufficientFundsException);
				throw insufficientFundsException;
			}
		}
		return false;
	}

	/**
	 * depositAmount: will deposit the amount to given account.
	 * 
	 * @param account
	 * @param amount
	 * @return
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 */
	public boolean depositAmount(Account account, BigDecimal amount)
			throws InterruptedException, InvalidAmmountException {
		if (validatePositiveAmount(amount)) {
			account.setBalance(account.getBalance().add(amount));
			return true;
		}
		return false;
	}

	/**
	 * validatePositiveAmount: validates if amount is positive else throw
	 * InvalidAmmountException.
	 * 
	 * @param amount
	 * @return
	 * @throws InvalidAmmountException
	 */
	private boolean validatePositiveAmount(BigDecimal amount) throws InvalidAmmountException {
		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			return true;
		} else {
			throw new InvalidAmmountException("Amount should not be negative");
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