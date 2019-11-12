package com.eden.api.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eden.api.dao.AccountDAO;
import com.edenstar.model.Account;
import com.edenstar.model.Payment;
import com.edenstar.model.account.AccountStream;
import com.edenstar.model.account.ProcessAccount;
import com.edenstar.model.reports.ReportStream;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountDAO accountDAO;

	@Override
	public int addAccount(Account account) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.addAccount(account);
	}

	@Override
	public int addPayment(Payment payment) throws Exception {
		return accountDAO.addPayment(payment);
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNumOfPaymentsRem(int account_id, int numOfPayments) throws Exception {
		// TODO Auto-generated method stub
		accountDAO.updateNumOfPaymentRem(account_id, numOfPayments);
	}

	@Override
	public void updateSecurityDeposit(int account_id, double securityDeposit) throws Exception {
		// TODO Auto-generated method stub
		accountDAO.updateSecurityDeposit(account_id, securityDeposit);
	}

	@Override
	public int addCommentsToAccount(int account_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.addCommentsToAccount(account_id, comments);
	}

	@Override
	public int addCommentsToPayment(int payment_id, String comments) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.addCommentsToPayment(payment_id, comments);
	}

	@Override
	public Account getAccount(int account_id) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getAccount(account_id);
	}

	@Override
	public Payment getPayment(int payment_id) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getPayment(payment_id);
	}

	@Override
	public Account getAccountByLeaseID(int lease_id) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getAccountByLeaseID(lease_id);
	}

	@Override
	public List<Payment> getPaymentSchedule(int account_id) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getPaymentSchedule(account_id);
	}

	@Override
	public int updateAccDocumentURL(int account_id, String urlToDocumenttScan) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.uploadAccDocument(account_id, urlToDocumenttScan);
	}

	@Override
	public int updateAccount(ProcessAccount a) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.updateAccount(a);
	}

	@Override
	public int updatePayment(ProcessAccount a) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.updatePayment(a);
	}

	@Override
	public int updateAccountBooks(Account account) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.updateAccountBooks(account);
	}

	@Override
	public List<AccountStream> getAllAccountsList(String mode) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getAllAccounts(mode);
	}

	@Override
	public List<ReportStream> getReport(String mode, int field_id, Date reportStartDate, Date reportEndDate) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getReport(mode, field_id, reportStartDate, reportEndDate);
	}

	@Override
	public int resetPayment(Payment payment) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.resetPayment(payment);
	}

	@Override
	public List<Payment> getActivePayments() throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.getActivePayments();
	}

	@Override
	public int setPaymentOverdueFlag(int payment_id, boolean flag) throws Exception {
		// TODO Auto-generated method stub
		return accountDAO.setPaymentOverdueFlag(payment_id, flag);
	}

	
	
}
