package com.arcao.wherigoservice.datamapper;

public enum ResponseCode {
	Ok(0, "OK"),
	InvalidCreditials(1, "Invalid creditials"),
	InvalidSession(2, "Invalid session"),
	CartridgeNotFound(10, "Cartridge not found"),
	CacheNotFound(11, "Cache not found"),
	ApiError(500, "API Error: %s"),
	ConnectionError(501, "Error during connection: %s");
	
	private final int responseCode;
	private final String responseText;
	
	private ResponseCode(int responseCode, String responseText) {
		this.responseCode = responseCode;
		this.responseText = responseText;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getResponseText() {
		return responseText;
	}
}
