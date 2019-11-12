package com.eden.api.dao;

import java.util.Date;
import java.util.List;

import com.edenstar.model.Account;
import com.edenstar.model.Payment;
import com.edenstar.model.account.AccountStream;
import com.edenstar.model.account.ProcessAccount;
import com.edenstar.model.reports.ReportStream;

public interface AccountDAO {

	int addAccount(Account account) throws Exception;

	int addPayment(Payment payment) throws Exception;

	void updateNumOfPaymentRem(int account_id, int numOfPayments) throws Exception;

	void updateSecurityDeposit(int account_id, double securityDeposit) throws Exception;

	int addCommentsToAccount(int account_id, String comments) throws Exception;

	int addCommentsToPayment(int payment_id, String comments) throws Exception;

	Account getAccount(int account_id) throws Exception;

	Payment getPayment(int payment_id) throws Exception;

	Account getAccountByLeaseID(int lease_id) throws Exception;

	List<Payment> getPaymentSchedule(int account_id) throws Exception;

	int uploadAccDocument(int account_id, String urlToDocumenttScan) throws Exception;

	int updateAccount(ProcessAccount a) throws Exception;

	int updatePayment(ProcessAccount a) throws Exception;

	int updateAccountBooks(Account account) throws Exception;

	List<AccountStream> getAllAccounts(String mode) throws Exception;

	List<ReportStream> getReport(String mode, int field_id, Date reportStartDate, Date reportEndDate) throws Exception;

	int resetPayment(Payment payment) throws Exception;

	List<Payment> getActivePayments() throws Exception;

	int setPaymentOverdueFlag(int payment_id, boolean flag) throws Exception;

}
