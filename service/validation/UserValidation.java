package com.eden.api.service.validation;

import com.edenstar.model.dash.CreateUser;

public class UserValidation {
	
//	@Autowired
//	private UserService userService;
	
	public boolean fieldsAreEmpty(CreateUser nUser) {

		// check to make sure that the mandatory parameters are not empty
		if (nUser.getEmailID().contentEquals("") || nUser.getPassword().contentEquals("")
				|| nUser.getAddress().contentEquals("") || nUser.getFirstName().contentEquals("")
				|| nUser.getLastName().contentEquals("") || nUser.getDob().contentEquals("")
				|| nUser.getPhoneNumber().contentEquals("") || nUser.getStaff_email().contentEquals("")) {

			return true;

		}

		return false;

	} // fieldsAreEmpty
	
	public boolean userLevelOK(CreateUser u) {
		// check to see if the userLevel field contains one of the following roles :
		// admin, manager, manager_admin, accounts, sales
		String ul = u.getUserLevel().toString().toLowerCase();

		switch (ul) {
		case "admin":
			return true;
		case "manager_admin":
			return true;
		case "manager":
			return true;
		case "accounts":
			return true;
		case "sales":
			return true;
		} // switch

		return false;

	} // userLevelOK
	
	public boolean isNull(CreateUser u) {
		// make sure that none of the mandatory fields are null

		if (u.getAddress() == null)
			return true;
		if (u.getUserLevel() == null)
			return true;
		if (u.getPassword() == null)
			return true;
		if (u.getEmailID() == null)
			return true;
		if (u.getFirstName() == null)
			return true;
		if (u.getLastName() == null)
			return true;
		if (u.getDob() == null)
			return true;
		if (u.getPhoneNumber() == null)
			return true;
		if (u.getStaff_email() == null)
			return true;

		return false;

	} // isNull
	
	public CreateUser formatFields(CreateUser u) {

		String tempStr;

		// Capitalise first and last names ...
		tempStr = u.getFirstName().toString();
		tempStr = tempStr.substring(0, 1).toUpperCase() + tempStr.substring(1).toLowerCase();
		u.setFirstName(tempStr);

		tempStr = u.getLastName().toString();
		tempStr = tempStr.substring(0, 1).toUpperCase() + tempStr.substring(1).toLowerCase();
		u.setLastName(tempStr);

		// lowercase the userLevel
		tempStr = u.getUserLevel().toString();
		tempStr = tempStr.toLowerCase();
		u.setUserLevel(tempStr);

		// lowercase the email_id
		tempStr = u.getEmailID().toString();
		tempStr = tempStr.toLowerCase();
		u.setEmailID(tempStr);

		// set the isBlocked to default value = 0
		u.setIsblocked(0);
		
		return u;

	} // formatFields
	
//	public boolean userExists(String staff_email) {
//
//		User r = new User();
//		boolean u_exists = true;
//
//		try {
//			r = userService.getUserDetails(staff_email);
//			if (r.getEmailID() == null) return false;
//
//			System.out.println("staff_email = " + staff_email);
//			System.out.println("result r = " + r.getEmailID());
//			// first check to see if the staff email exists on the database
//			if (!r.getEmailID().contentEquals(staff_email)) {
//				// does not match
//				System.out.println("user " + staff_email + " does not exist");
//				u_exists = false;
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return u_exists;
//
//	} // userExists
	
//	public CreateUser checkChanges(CreateUser u) {
//
//		// gets the user record from database and compares
//		// to see if the new data is null, and if so copy the database value over
//
//		// get the record
//		User r = new User();
//
//		try {
//			r = userService.getUserDetails(u.getEmailID());
//
//			if (u.getAddress() == null)
//				u.setAddress(r.getAddress().toString());
//			u.setEmployeeID(r.getEmployeeID()); // this value cannot be changed
//			if (u.getPassword() == null)
//				u.setPassword(r.getPassword().toString());
//			if (u.getUserLevel() == null)
//				u.setUserLevel(r.getUserLevel().toString());
//			if (u.getPasswordHint() == null)
//				u.setPasswordHint(r.getPasswordHint().toString());
//			u.setIsblocked(r.getIsblocked()); // this value cannot be changed in the dash
//			if (u.getFirstName() == null)
//				u.setFirstName(r.getFirstName().toString());
//			if (u.getLastName() == null)
//				u.setLastName(r.getLastName().toString());
//			if (u.getDob() == null)
//				u.setDob(r.getDob().toString());
//			if (u.getPhoneNumber() == null)
//				u.setPhoneNumber(r.getPhoneNumber().toString());
//			if (u.getProfileUrl() == null)
//				u.setProfileUrl(r.getProfileUrl().toString());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} // try-catch
//
//		return u;
//	} // checkChanges

}
