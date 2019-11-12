package com.eden.api.service;

import java.util.Date;
import java.util.List;


import com.edenstar.model.Account;
import com.edenstar.model.Payment;
import com.edenstar.model.account.AccountStream;
import com.edenstar.model.account.ProcessAccount;
import com.edenstar.model.reports.ReportStream;

public interface AccountService {

	int addAccount(Account account) throws Exception;

	int addPayment(Payment payment) throws Exception;

	void updateNumOfPaymentsRem(int account_id, int size) throws Exception;

	void updateSecurityDeposit(int account_id, double securityDeposit) throws Exception;

	int addCommentsToAccount(int account_id, String comments) throws Exception;

	int addCommentsToPayment(int payment_id, String comments) throws Exception;

	Account getAccount(int account_id) throws Exception;
	
	Payment getPayment(int payment_id) throws Exception;

	Account getAccountByLeaseID(int lease_id) throws Exception;

	List<Payment> getPaymentSchedule(int account_id) throws Exception;

	int updateAccDocumentURL(int account_id, String urlToDocumenttScan) throws Exception;

	int updateAccount(ProcessAccount a) throws Exception;

	int updatePayment(ProcessAccount a) throws Exception;

	int updateAccountBooks(Account account) throws Exception;

	List<AccountStream> getAllAccountsList(String mode) throws Exception;

	List<ReportStream> getReport(String mode, int field_id, Date reportStartDate, Date reportEndDate) throws Exception;

	int resetPayment(Payment payment) throws Exception;

	List<Payment> getActivePayments() throws Exception;

	int setPaymentOverdueFlag(int payment_id, boolean b) throws Exception;

}
