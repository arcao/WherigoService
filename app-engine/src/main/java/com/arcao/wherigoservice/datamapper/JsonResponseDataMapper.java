package com.arcao.wherigoservice.datamapper;

import com.google.gson.stream.JsonWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

public class JsonResponseDataMapper implements ResponseDataMapper {
    private JsonWriter writer;
    private OutputStream os;
    private HttpServletResponse resp;

    public JsonResponseDataMapper(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writer = getJsonWriter(req, resp);
        os = resp.getOutputStream();
        this.resp = resp;
    }

    private static JsonWriter getJsonWriter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonWriter w = new JsonWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));

            if (req.getParameter("debug") != null)
                w.setIndent("    ");

            return w;
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e);
        }
    }

    protected void writeResponseCode(ResponseCode r, Object... responseTextParam) throws IOException {
        writer.name("Status")
                .beginObject()
                .name("Code").value(r.getResponseCode())
                .name("Text").value(String.format(r.getResponseText(), responseTextParam))
                .endObject();
    }

    public void writeErrorResponse(ResponseCode r, Object... responseTextParam) throws IOException {
        writer.beginObject();
        writeResponseCode(r, responseTextParam);
        writer.endObject();
    }

    public void writeLoginResponse(String session) throws IOException {
        writer.beginObject();
        writeResponseCode(ResponseCode.Ok);

        writer.name("LoginResult")
                .beginObject()
                .name("Session").value(session)
                .endObject();

        writer.endObject();
    }

    public void writeGetCartridgeResponse(String cartridgeGuid) throws IOException {
        writer.beginObject();
        writeResponseCode(ResponseCode.Ok);

        writer.name("CartridgeResult")
                .beginObject()
                .name("CartridgeGUID").value(cartridgeGuid)
                .endObject();

        writer.endObject();
    }

    public void writeGetCartridgeDownloadDataResponse(Map<String, String> formData, String session) throws IOException {
        writer.beginObject();
        writeResponseCode(ResponseCode.Ok);

        writer.name("CartridgeDownloadDataResult")
                .beginObject()
                .name("FormData");

        writer.beginObject();
        for (Entry<String, String> entry : formData.entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();

        writer.name("Session").value(session);
        writer.endObject();

        writer.endObject();
    }

    public void writeGetCacheCodeFromGuidResponse(String cacheCode) throws IOException {
        writer.beginObject();
        writeResponseCode(ResponseCode.Ok);

        writer.name("CacheResult")
                .beginObject()
                .name("CacheCode").value(cacheCode)
                .endObject();

        writer.endObject();
    }

    @Override
    public void writeTimeResponse(long time) throws IOException {
        writer.beginObject();
        writeResponseCode(ResponseCode.Ok);

        writer.name("TimeResult")
                .beginObject()
                .name("Time").value(time)
                .endObject();

        writer.endObject();
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void writeGuidToReferenceCode(String referenceCode) throws IOException {
        writer.beginObject();
        writer.name("referenceCode").value(referenceCode);
        writer.endObject();
    }

    @Override
    public void writeErrorV2Response(int statusCode, String statusMessage, String errorMessage) throws IOException {
        resp.setStatus(statusCode);

        writer.beginObject();
        writer.name("statusCode").value(statusCode);
        writer.name("statusMessage").value(statusMessage);
        writer.name("errorMessage").value(errorMessage);
        writer.endObject();
    }
}
