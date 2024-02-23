package com.dws.challenge.web;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TransferAmountRequest {
	
	 @NotNull
	  @NotEmpty
	  private final String fromAccountNo;
	 
	 @NotNull
	  @NotEmpty
	  private final String toAccountNo;

	  @NotNull
	  @Min(value = 0, message = "Initial balance must be positive.")
	  private BigDecimal amount;

}
