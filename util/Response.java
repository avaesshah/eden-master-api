package com.eden.api.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Response {
	
	private ResponseStatus response;
	private boolean success;
	
	private Object data;
	
	
	public ResponseStatus getResponse() {
		return response;
	}

	public void setResponse(ResponseStatus response) {
		this.response = response;
	}
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	

	public static Response build(String _status, String _message, boolean isSuccess) {

		Response r = new Response();
		ResponseStatus responseStatus = r.new ResponseStatus();

		responseStatus.setStatus(_status);
		responseStatus.setMessage(_message);

		r.setResponse(responseStatus);
		r.setSuccess(isSuccess);

		return r;
	}
	public static Response build(String _status, String _code, String _message, boolean isSuccess){
		
		Response r = new Response();
		ResponseStatus responseStatus = r.new ResponseStatus();
		
		responseStatus.setStatus(_status);
		responseStatus.setCode(_code);
		responseStatus.setMessage(_message);
		
		r.setResponse(responseStatus);
		r.setSuccess(isSuccess);
		
		return r;
	}

	
	public static Response build(String _status, String _code, String _message, String _moreInfo, boolean isSuccess){
		
		Response r = Response.build(_status,_code, _message, isSuccess);
		r.getResponse().setMoreInfo(_moreInfo);
		
		return r;
	}
	
	@JsonInclude(Include.NON_NULL)
	public
	class ResponseStatus {
		private String status;
		private String code;
		private String message;
		private String moreInfo;
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getMoreInfo() {
			return moreInfo;
		}
		public void setMoreInfo(String moreInfo) {
			this.moreInfo = moreInfo;
		}
		
	}
}
