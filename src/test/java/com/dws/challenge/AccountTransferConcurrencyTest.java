package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidAmmountException;
import com.dws.challenge.exception.LockException;
import com.dws.challenge.service.AccountsService;


@ExtendWith(SpringExtension.class)
@RunWith(ConcurrentTestRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountTransferConcurrencyTest {
	
	@Autowired
	public AccountsService accountsService;
	private final String Account1 = "Id-123";
	private final String Account2 = "Id-124";
	public static AtomicInteger transactionCounter = new AtomicInteger(0);
	public static AtomicInteger transactionOppositeCounter = new AtomicInteger(
			0);
	public static AtomicInteger transactionNegativeTestCounter = new AtomicInteger(
			0);
	public final int threadcount =2000;

	
	@BeforeAll
	public void setup() throws InvalidAccountException {
		this.accountsService.createAccount(new Account(Account1,
				new BigDecimal("8000")));
		
		this.accountsService.createAccount(new Account(Account2,
				new BigDecimal("16000")));	
	}
	@Test
	public void testTransferAmountConcurrentlyToAccFromAccThenIndestroyAcctwilHaveSameValue() {
		IntStream.range(0, threadcount).parallel().forEach(
				thread -> {
					CompletableFuture.runAsync(() -> {						
						try {
							transferAmmtAndIncrementTrCounter(Account1,
									Account2, "1", transactionCounter);
						} catch (InsufficientFundsException
								| InterruptedException
								| InvalidAccountException
								| InvalidAmmountException | LockException | AccountNotFoundException e) {
							
							fail("Exception should not occur while transfering amount from Account1 to Account2");
						}
					});
					CompletableFuture.runAsync(() -> {						
						try {
							transferAmmtAndIncrementTrCounter(Account2,
									Account1, "1",
									transactionOppositeCounter);
						} catch (InsufficientFundsException
								| InterruptedException
								| InvalidAccountException
								| InvalidAmmountException | LockException | AccountNotFoundException e) {
							
							fail("Exception should not occur while transfering amount from Account2 to Account1");
						}
					}
					);
				});
	}
	
	private void transferAmmtAndIncrementTrCounter(String fromAccount,
			String toAccount, String amount, AtomicInteger counter)
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException, AccountNotFoundException {
		counter.incrementAndGet();
		accountsService.transferAmount(fromAccount, toAccount, new BigDecimal(
				amount));
		
	}

	@AfterAll
	public void destroy() throws InvalidAccountException, InterruptedException, AccountNotFoundException {
		Thread.sleep(5000);
		System.out.println("transactionCounter "+transactionCounter.get());
		System.out.println("transactionOppositeCounter "+transactionOppositeCounter.get());
		assertEquals(new BigDecimal(8000),
				this.accountsService.getAccount(Account1).getBalance());
		assertEquals(new BigDecimal(16000),this.accountsService.getAccount(Account2).getBalance()
				);
		assertThat(transactionCounter.get()==threadcount);
		assertThat(transactionOppositeCounter.get()==threadcount);
		
	}

}
