package com.tmax.WaplMath.AnalysisReport.util.auth;

import java.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JWTUtil {
    static public String getJWTPayloadField(String token, String fieldName){
        //Split jwt
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        JsonObject jsonObj = JsonParser.parseString(payload).getAsJsonObject();

        return jsonObj.get(fieldName).getAsString();
    }
}
