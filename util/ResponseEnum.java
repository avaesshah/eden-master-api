package com.eden.api.util;

public enum ResponseEnum {
	
	OK("200", "OK"),
	CREATED("201", "Created"),
	NOT_MODIFIED("304", "Not Modified"),
	BAD_REQUEST("400", "Bad Request"),
	UNAUTHORIZED("401", "Unauthorized"),
	FORBIDDEN("403", "Forbidden"),
	NOT_FOUND("404", "Not Found"),
	INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
	DATA_NOT_FOUND("204", "No data found"),
	;
	
	
	private String status;
	private String message;
	
	private ResponseEnum(String _status, String _message) {
		this.status= _status;
		this.message = _message;
	}

	
	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
}

