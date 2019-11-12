package com.eden.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.eden.api.controller.engine.Availability;
import com.edenstar.model.Payment;

public class PaymentMapper implements RowMapper<Payment> {

	@Override
	public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {

		Payment p = new Payment();

		p.setPayment_id(rs.getInt("payment_id"));
		p.setAccount_id(rs.getInt("account_id"));
		p.setDueByDate(rs.getDate("due_by_date"));
		p.setAmountDue(rs.getDouble("amount_due"));
		p.setAmountCleared(rs.getDouble("amount_cleared"));
		p.setPaymentInDate(rs.getDate("payment_in_date"));
		p.setPaymentMethod(rs.getString("payment_method"));
		p.setComments(rs.getString("comments"));
		p.setCommentHistory(processComments(p.getComments()));
		p.setOverdue(rs.getInt("overdue"));
		p.setIsPaid(rs.getInt("is_paid"));
		p.setPaymentIssue(rs.getInt("payment_issue"));
		p.setIs_archived(rs.getInt("is_archived"));
		
		if (p.getDueByDate() != null) {
			String dateStr = p.getDueByDate().toString();
			try {
				p.setDue_by_date(formatDateFoJava(dateStr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (p.getPaymentInDate() != null) {
			String dateStr = p.getPaymentInDate().toString();
			try {
				p.setPayment_in_date(formatDateFoJava(dateStr));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return p;
	}
	
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
}
