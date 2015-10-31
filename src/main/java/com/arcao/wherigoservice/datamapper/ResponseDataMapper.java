package com.arcao.wherigoservice.datamapper;

import java.io.IOException;
import java.util.Map;

public interface ResponseDataMapper {
	void writeErrorResponse(ResponseCode r, Object... responseTextParam) throws IOException;
	void writeLoginResponse(String session) throws IOException;
	void writeGetCartridgeResponse(String cartridgeGuid) throws IOException;
	void writeGetCartridgeDownloadDataResponse(Map<String, String> formData, String session) throws IOException;
	void writeGetCacheCodeFromGuidResponse(String cacheGuid) throws IOException;
	void writeTimeResponse(long time) throws IOException;

	void flush() throws IOException;

}
