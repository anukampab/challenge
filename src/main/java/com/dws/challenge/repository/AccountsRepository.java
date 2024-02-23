package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.AccountNotFoundException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	Account getAccount(String accountId) throws AccountNotFoundException, InvalidAccountException;

	void clearAccounts();
}
