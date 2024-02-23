package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.exception.LockException;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;


@ExtendWith(SpringExtension.class)
@RunWith(ConcurrentTestRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountTransferServiceTest {
	
	private final String nonExistingAccount = "Id-000";
	private final String Account1 = "Id-191";
	private final String Account2 = "Id-192";
	private final String Account3 = "Id-193";
	private final String Account4 = "Id-194";
	private final String Account5 = "Id-195";
	private final String Account6 = "Id-196";
	private static AtomicInteger transactionCounter = new AtomicInteger(0);
	private static AtomicInteger transactionOppositeCounter = new AtomicInteger(
			0);
	private static AtomicInteger transactionNegativeTestCounter = new AtomicInteger(
			0);
	final int threadcount =2000;
	
	
	private AccountsService accountsService = new AccountsService(
			new AccountsRepositoryInMemory(),new EmailNotificationService());
	
	@BeforeAll
	public void setup() throws AccountNotFoundException, InvalidAccountException  {
		
		this.accountsService.createAccount(new Account(Account1,
				new BigDecimal("8000")));
		
		this.accountsService.createAccount(new Account(Account2,
				new BigDecimal("16000")));
		
		this.accountsService.createAccount(new Account(Account3,
				new BigDecimal("20")));
		
		this.accountsService.createAccount(new Account(Account4,
				new BigDecimal("50")));
		this.accountsService.createAccount(new Account(Account5,
				new BigDecimal("9000")));
		this.accountsService.createAccount(new Account(Account6,
				new BigDecimal("17000")));
		
		assertEquals(this.accountsService.getAccount(Account1).getBalance(),
				new BigDecimal(8000));
		assertEquals(this.accountsService.getAccount(Account2).getBalance(),
				new BigDecimal(16000));
		assertEquals(this.accountsService.getAccount(Account3).getBalance(),
				new BigDecimal(20));
		assertEquals(this.accountsService.getAccount(Account4).getBalance(),
				new BigDecimal(50));

	}
	
	@Test
	public void testNegativeAmountTransferExpectingThrowsInvalidAmmountException()
			throws InsufficientFundsException, InterruptedException,
			AccountNotFoundException, InvalidAmountException, LockException, InvalidAccountException {

		try {
			transactionNegativeTestCounter.incrementAndGet();	
			this.accountsService.transferAmount(Account2, Account1,
					new BigDecimal("-1"));
			fail("InvalidAmmountException should have thrown when transfering negative Amount");
		} catch (InvalidAmountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Amount should not be negative");
		}
	}
	
	@Test
	public void testTransferWithNullFromAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(null, Account2, new BigDecimal(
					"1"));
			fail("InvalidAccountException should have thrown when trasnffering with FromAccount as null");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}
	
	@Test
	public void testTransferWithNullToAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, null, new BigDecimal(
					"1"));
			fail("InvalidAccountException should have thrown when trasnffering with ToAccount as null");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}

	@Test
	public void testTransferWithEmptyFromAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount("", Account2, new BigDecimal(
					"1"));
			fail("InvalidAccountException should have thrown when trasnffering with empty FromAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}
	@Test
	public void testTransferWithEmptyToAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, "", new BigDecimal(
					"1"));
			fail("InvalidAccountException should have thrown when trasnffering with empty ToAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}
	
	@Test
	public void testAmountTransferWithInvalidFromAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(nonExistingAccount, Account2,
					new BigDecimal("1"));
			fail("InvalidAccountException should have thrown when trasnffering with not existing FromAccount");
		} catch (AccountNotFoundException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account with id : " + nonExistingAccount
							+ " does not exist");
		}
	}
	@Test
	public void testTransferWithInvalidToAccount()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, nonExistingAccount,
					new BigDecimal("1"));
			fail("AccountNotFoundException should have thrown when trasnffering with not existing ToAccount");
		} catch (AccountNotFoundException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account with id : " + nonExistingAccount
							+ " does not exist");
		}
	}
	
	@Test
	public void testTransferAmountIsGreaterThenAccountBalance()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {

		try {
			transactionNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account3, Account4,
					new BigDecimal("21"));
			fail("InsufficientFundsException should have thrown when transfering Amount greater then balance");
		} catch (InsufficientFundsException e) {
			assertThat(e.getMessage())
					.isEqualTo(
							"Insufficient funds in account:"
									+ Account3
									+ " withdrawn amount :"
									+ 21
									+ " is greater then Account balanace : "
									+ accountsService.getAccount(Account3).getBalance());
		}
	}
	
	/** testTransferAmountConcurrentlyFromAccToToAccdestroyShallHaveSameValue
	 *  is transferring amount 1 
	 * from_account to to_account with multiple threads 
	 * same amount transfer in opposite order in next test method.
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmountException
	 * @throws LockException
	 * @throws AccountNotFoundException 
	 */
	@Test
	@ThreadCount(threadcount)
	public void testTransferAmountConcurrentlyFromAccToToAccdestroyShallHaveSameValue()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		transactionCounter.incrementAndGet();
		this.accountsService.transferAmount(Account1, Account2, new BigDecimal(
				"1"));
	}
	
	/** testTransferConcurrentlyToAccToFromAccdestroyAccountShallHaveSameValue 
	 * is transferring amount 1 in
	 * opposite order of above test method to make test simple.
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmountException
	 * @throws LockException
	 * @throws AccountNotFoundException 
	 */
	@Test
	@ThreadCount(threadcount)
	public void testTransferConcurrentlyToAccToFromAccdestroyAccountShallHaveSameValue()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmountException, LockException, AccountNotFoundException {
		transactionOppositeCounter.incrementAndGet();		
		
			this.accountsService.transferAmount(Account2, Account1, new BigDecimal(
					"1"));
		
	}
	

	@AfterAll
	public void destroy() throws InvalidAccountException, InterruptedException, AccountNotFoundException {

		assertEquals(this.accountsService.getAccount(Account1).getBalance(),
				new BigDecimal(8000));
		assertEquals(this.accountsService.getAccount(Account2).getBalance(),
				new BigDecimal(16000));
		assertEquals(this.accountsService.getAccount(Account3).getBalance(),
				new BigDecimal(20));

		assertEquals(this.accountsService.getAccount(Account4).getBalance(),
				new BigDecimal(50));

		assertThat(this.accountsService.getAccount(Account5).getBalance().intValue()==
				new BigDecimal(8999).intValue());

		assertThat(this.accountsService.getAccount(Account6).getBalance().intValue()==
				new BigDecimal(17001).intValue());
		assertThat(transactionCounter.get()==threadcount);
		assertThat(transactionOppositeCounter.get()==threadcount);
		assertThat(transactionNegativeTestCounter.get()==10);
		


	}

}
