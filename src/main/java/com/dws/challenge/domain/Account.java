package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class Account implements Comparable<Account> {

	@NotEmpty(message = "Account Id cannot be null or empty")
	private final String accountId;

	@NotNull(message = "Initial balance can not be null")
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	public Account(String accountId, BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	@Override
	public int compareTo(Account o) {
		return this.accountId.compareTo(o.accountId);
	}

}
