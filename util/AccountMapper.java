package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.eden.api.controller.engine.Availability;
import com.edenstar.model.Account;

public class AccountMapper implements RowMapper<Account> {

	@Override
	public Account mapRow(ResultSet rs, int rowNum) throws SQLException {

		Account a = new Account();

		a.setAccount_id(rs.getInt("account_id"));
		a.setLease_id(rs.getInt("lease_id"));
		a.setDepositAmount(rs.getDouble("deposit_amount"));
		a.setDepositClearedDate(rs.getDate("deposit_cleared_date"));
		a.setDepositCleared(rs.getInt("deposit_cleared"));
		a.setDepositRefunded(rs.getInt("deposit_refunded"));
		a.setDepositRefundedDate(rs.getDate("deposit_refunded_date"));
		a.setComments(rs.getString("comments"));
		a.setCommmentsHistory(processComments(a.getComments()));
		a.setFlagForManager(rs.getInt("flag_for_manager"));
		a.setLeaseTotal(rs.getDouble("lease_total"));
		a.setLeaseRemaining(rs.getDouble("lease_remaining"));
		a.setNoPaymentsReceived(rs.getInt("no_payments_received"));
		a.setNoPaymentsRemaining(rs.getInt("no_payments_remaining"));
		a.setDocumentUploadUrl(rs.getString("document_upload_url"));
		a.setIs_archived(rs.getInt("is_archived"));
		
		if (a.getDepositClearedDate() != null) {
			String dateStr = a.getDepositClearedDate().toString();
			try {
				a.setDeposit_cleared_date(formatDateFoJava(dateStr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (a.getDepositRefundedDate() != null) {
			String dateStr = a.getDepositRefundedDate().toString();
			try {
				a.setDeposit_refunded_date(formatDateFoJava(dateStr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return a;
	}
	
	private String[] processComments(String comments) {
		String[] tempArray = null;
		if (comments == null || comments.isEmpty())
			return tempArray;

		String stringToSplit = comments;
		String delimiter = "\\[";
		tempArray = stringToSplit.split(delimiter);
		for (int i = 1; i < tempArray.length; i++) {
			tempArray[i] = "[" + tempArray[i];
			System.out.println(tempArray[i]);
		}

		return tempArray;
	} // processComments

	private String formatDateFoJava(String dateStr) throws Exception {

		Availability availability = new Availability();
		String dateForJava = "";

		try {

			dateForJava = availability.formatDateForJava(dateStr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dateForJava;

	} // formatDateForJava
}
