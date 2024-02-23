# Banking Transaction System
# Overview
This Java code represents a banking transaction system facilitating the transfer of funds between accounts. The system is designed to handle debit and credit transactions while ensuring thread safety and consistency. Key features include:

Transfer Amount: Allows the transfer of funds between two accounts.
Withdraw Amount: Enables the withdrawal of funds from a specified account.
Deposit Amount: Facilitates the deposit of funds into a specified account.

# Validation: Ensures the validation of positive amounts and throws exceptions when necessary.

# Usage
The primary method for transferring funds is transferAmount, which validates accounts and initiates the transfer. The process involves withdrawing the amount from the source account and depositing it into the destination account. 
The application also provides methods for withdrawing and depositing amounts individually.

# Exception Handling
The system handles various exceptions to maintain the integrity of transactions:

InsufficientFundsException: Thrown when attempting to withdraw more funds than available in the account.
InvalidAccountException: Thrown when an invalid account is provided.
InvalidAmountException: Thrown when attempting to perform an operation with an invalid (non-positive) amount.
LockException: Thrown when unable to acquire locks on the accounts during the transfer process.
Concurrency Control
Concurrency control is implemented using synchronized blocks to ensure atomicity and avoid potential deadlocks. The transfer method synchronizes on both accounts, ensuring consistent locking order based on account IDs to prevent deadlocks.

# Logging
The system includes logging for transactions, providing insights into the flow of funds between accounts. The logTransaction method outputs relevant information for testing purposes.

# Testability
The logTransaction method serves as a utility for test purposes, providing a detailed log of transactions.

#Notes
This code is initial commit and  require additional enhancements for production use, 
such as incorporating security measures.
Ensure proper exception handling and logging mechanisms are in place for a real-world application.
Consider implementing locking mechanisms