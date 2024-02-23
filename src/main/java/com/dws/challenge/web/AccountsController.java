package com.dws.challenge.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAccountException;
import com.dws.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException dae) {
      return new ResponseEntity<>(dae.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) throws AccountNotFoundException, InvalidAccountException {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(path="/transfer",consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferAmount(@RequestBody @Valid TransferAmountRequest transferRequest)  {
	  log.info("Transfering ammount from account {} to account {}", transferRequest.getFromAccountNo(),transferRequest.getToAccountNo());
    try {
    this.accountsService.transferAmount(transferRequest.getFromAccountNo(),transferRequest.getToAccountNo(),transferRequest.getAmount());
    }  
    catch (Exception e) {
		 return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
	}
    
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
