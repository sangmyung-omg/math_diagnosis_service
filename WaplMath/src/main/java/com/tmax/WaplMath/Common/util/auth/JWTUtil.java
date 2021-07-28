package com.tmax.WaplMath.Common.util.auth;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Common.exception.InvalidTokenException;
import com.tmax.WaplMath.Common.exception.JWTFieldNotFound;
import com.tmax.WaplMath.Common.exception.JWTInvalidException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTUtil {
    private static final String USER_ID_FIELD = "userID";
    private static final String SUB_FIELD = "sub";


    static public String getJWTPayloadField(String token, String fieldName){
        JsonObject jsonObj = null;


        if(token == null)
            throw new InvalidArgumentException("Token is null");

        try{
            //Split jwt
            String[] chunks = token.split("\\.");

            Base64.Decoder decoder = Base64.getDecoder();

            String payload = new String(decoder.decode(chunks[1]));

            jsonObj = JsonParser.parseString(payload).getAsJsonObject();
        }
        catch(IllegalArgumentException illargs){
            throw new JWTInvalidException("base64 decode error");
        }
        catch(JsonParseException e){
            throw new JWTInvalidException("jwt parse error");
        }
        catch(Throwable e){
            throw new JWTInvalidException();
        }

        if(!jsonObj.has(fieldName))
            throw new JWTFieldNotFound(fieldName);

        return jsonObj.get(fieldName).getAsString();
    }

    static public boolean verifyToken(DecodedJWT jwt) {
        //get algorithm
        String algoString = jwt.getAlgorithm();

        //If algorithm is not found throw
        if(algoString == null)
            throw new JWTInvalidException("Algorithm not found.");

        Algorithm algorithm = getAlgorithm(algoString);

        //Check based on algorithm only. TODO. add iat checks too
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            jwt = verifier.verify(jwt);
        }
        catch (SignatureVerificationException e){
            log.error("Token signature not verified");
            return false;
        }
        catch(Throwable e){
            log.error("Error token verification");
            return false;
        }    

        return true;
    }

    static private Algorithm getAlgorithm(String algoString){
        RSAPublicKey rsaPublicKey = null;
        ECPublicKey ecPublicKey = null;

        String secret = "hello";
        

        switch(algoString){
            case "HS256":
                return Algorithm.HMAC256(secret);
            case "HS384":
                return Algorithm.HMAC384(secret);
            case "HS512":
                return Algorithm.HMAC512(secret);
            case "RS256":
                return Algorithm.RSA256(rsaPublicKey, null);
            case "RS384":
                return Algorithm.RSA384(rsaPublicKey, null);
            case "RS512":
                return Algorithm.RSA512(rsaPublicKey, null);
            case "ES256":
                return Algorithm.ECDSA256(ecPublicKey, null);
            case "ES256K":
                return Algorithm.ECDSA256K(ecPublicKey, null);
            case "ES384":
                return Algorithm.ECDSA384(ecPublicKey, null);
            case "ES512":
                return Algorithm.ECDSA512(ecPublicKey, null);
            default:
                return null;
        }
    }

    /**
     * API to get userID
     * @since 2021-07-28
     * @author Jonghyun seong
     */
    public static String getUserID(String token, boolean doVerify){
        //Decode token
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(token);
        }
        catch(JWTDecodeException e){
            throw new JWTInvalidException("Token cannot be decoded. " + token);
        }
                
        //If not verified. throw
        if(!verifyToken(jwt) && doVerify){
            throw new JWTInvalidException("Token signature cannot be verified. Check token validity.");
        }

        //Try user id field => "userID" -> "sub"
        Claim userIDClaim = jwt.getClaim(USER_ID_FIELD);
        if(!userIDClaim.isNull())
            return userIDClaim.asString();

        Claim subClaim = jwt.getClaim(SUB_FIELD);
        if(!subClaim.isNull())
            return subClaim.asString();

        //Else? => none of the userID is found
        throw new JWTInvalidException(String.format("No user ID field found in body. Nor %s or %s", USER_ID_FIELD, SUB_FIELD));
    }

    public static String getUserID(String token){
        return getUserID(token, true);
    }

}
