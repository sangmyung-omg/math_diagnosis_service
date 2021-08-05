package com.tmax.WaplMath.Common.util.auth;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.Common.exception.InvalidArgumentException;
import com.tmax.WaplMath.Common.exception.JWTFieldNotFound;
import com.tmax.WaplMath.Common.exception.JWTInvalidException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTUtil {
    private static final String USER_ID_FIELD = "userID";
    private static final String SUB_FIELD = "sub";

    private static JWTUtil inst = null;


    //Key for hs mode
    private String hsSharedSecret;
    private boolean restrictHSAlgo;
    private RSAPublicKey rsaPublicKey;
    private ECPublicKey ecPublicKey;

    @Value("${auth.jwt.debugMode}")
    private boolean debugMode;


    enum JWTVerifyCode {
        SUCCESS("Success."),
        SIGNITURE_INVALID("Signature is invalid."),
        EXPIRED("Token has expired."),
        UNSUPPORTED_ALGO("Unsupported algorithm."),
        GENERAL_ERROR("Token is invalid (general error).");

        @Getter
        private String message;

        private JWTVerifyCode(String message){
            this.message = message;
        }
    }

    //Constructor for singleton inst to get injection from beans
    public JWTUtil(@Value("${auth.jwt.hs.secret}") String sharedSecret, 
                   @Value("${auth.jwt.restrictHS}") boolean restrictHS) {
        this.hsSharedSecret = sharedSecret;
        this.restrictHSAlgo = restrictHS;

        if(restrictHS)
            log.info("Restricting use of HS jwt tokens");
        else
            log.info("Using {} for HS*'s shared secret key", sharedSecret);

        JWTUtil.inst = this;
    }

    public static JWTUtil getInstance(){ return JWTUtil.inst;}

    @Deprecated
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

    /**
     * Token verification process for various types of token
     * @since 2021-07-28
     * @author Jonghyun seong
     */
    static public JWTVerifyCode verifyToken(DecodedJWT jwt) {
        //get algorithm
        String algoString = jwt.getAlgorithm();

        //If algorithm is not found throw
        if(algoString == null)
            throw new JWTInvalidException("Algorithm not found.");

        //Get algorithm from string
        Algorithm algorithm = getAlgorithm(algoString);
        if(algorithm == null) {return JWTVerifyCode.UNSUPPORTED_ALGO;}

        //Check based on algorithm only.
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            jwt = verifier.verify(jwt);
        }
        catch (SignatureVerificationException e){
            log.error("Token signature not verified");
            return JWTVerifyCode.SIGNITURE_INVALID;
        }
        catch (TokenExpiredException e){
            log.error("Token has expired");
            return JWTVerifyCode.EXPIRED;
        }
        catch (JWTVerificationException e){
            log.error("JWT verification failed");
            return JWTVerifyCode.GENERAL_ERROR;
        }
        catch(Throwable e){
            log.error("Error token verification");
            return JWTVerifyCode.GENERAL_ERROR;
        } 

        return JWTVerifyCode.SUCCESS;
    }

    /**
     * Method to get algorithm from given string
     * @param algoString Algorithm string in the JWT token
     * @return Algorithm inst of the given string. null if not found
     */
    static private Algorithm getAlgorithm(String algoString){
        RSAPublicKey rsaPublicKey = inst.rsaPublicKey;
        ECPublicKey ecPublicKey = inst.ecPublicKey;
        String secret = inst.hsSharedSecret;


        if(inst.restrictHSAlgo && algoString.startsWith("HS")){
            throw new JWTInvalidException("HS algorithms are not allowed. current JWT algorithm is "+ algoString);
        }
        

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
        if(doVerify){
            JWTVerifyCode resultCode = verifyToken(jwt);

            if(resultCode != JWTVerifyCode.SUCCESS)
                throw new JWTInvalidException("Token verification failed. Check token validity. " + resultCode.getMessage());
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
        if(inst.debugMode)
            return getUserID(token, false);

        return getUserID(token, true);
    }

}
