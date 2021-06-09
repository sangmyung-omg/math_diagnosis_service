package com.tmax.WaplMath.AnalysisReport.util.auth;

import java.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.exception.InvalidTokenException;

public class JWTUtil {
    static public String getJWTPayloadField(String token, String fieldName){
        JsonObject jsonObj = null;

        try{
            //Split jwt
            String[] chunks = token.split("\\.");

            Base64.Decoder decoder = Base64.getDecoder();

            String payload = new String(decoder.decode(chunks[1]));

            jsonObj = JsonParser.parseString(payload).getAsJsonObject();
        }
        catch(IllegalArgumentException illargs){
            throw new InvalidTokenException("base64 decode error");
        }
        catch(JsonParseException e){
            throw new InvalidTokenException("jwt parse error");
        }
        catch(Throwable e){
            throw new InvalidTokenException();
        }

        return jsonObj.get(fieldName).getAsString();
    }
}
