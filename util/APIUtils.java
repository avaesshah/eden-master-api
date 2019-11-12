package com.eden.api.util;

public class APIUtils {

	public static Response missingParamResponse(String paramName, String service) {

		return Response.build(ResponseEnum.BAD_REQUEST.getStatus(), getErrorCode(paramName, service),
				ResponseEnum.BAD_REQUEST.getMessage(), "Required parameter '" + paramName + "' is missing", false);
	}

	public static Response badRequest(String errorCode, String moreInfo) {

		return Response.build(ResponseEnum.BAD_REQUEST.getStatus(), errorCode, ResponseEnum.BAD_REQUEST.getMessage(),
				moreInfo, false);
	}

	public static String getErrorCode(String missingParam, String service) {

		String errorCode = null;
		switch (service) {
		case (Constants.PATH_LOGIN):
			switch (missingParam) {
			case Constants.INPUT_PARAM_USERNAME:
				errorCode = Constants.REQUIRED_PARAM;
				break;
			}
			break;
		}

		return errorCode;

	}

	public static String getServiceInternalError(String service) {
		String errorCode = Constants.INTERNAL_SYSTEM_ERROR;
		return errorCode;
	}

}
