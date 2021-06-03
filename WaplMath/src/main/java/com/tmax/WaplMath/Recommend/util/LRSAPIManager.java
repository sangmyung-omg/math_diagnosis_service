package com.tmax.WaplMath.Recommend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;

/**
 * Call StatementList GET API from LRS Server
 * 
 * @author sangheonLee
 */
@Component
public class LRSAPIManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private static final String HOST = "http://192.168.153.132:8080";
//	private static final String HOST = System.getenv("LRS_HOST");
	private static final String LRS_ADDR = String.format("%s/StatementList", HOST);

	@Autowired
	RestTemplate restTemplate;

	public GetStatementInfoDTO input;

	public String covertToISO8601Format(String date) throws ParseException {
		SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+09:00");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date_obj = dateFormat.parse(date);
		logger.info(ISO8601.format(date_obj));
		return ISO8601.format(date_obj);
	}

	public JsonArray convertToJsonArray(List<String> array) {
		JsonArray jsonArray = new JsonArray();
		array.forEach(var0 -> jsonArray.add(var0));
		return jsonArray;
	}

	public String generateInput() throws ParseException {
		Gson gson = new Gson();
		JsonObject msg = new JsonObject();

		if (input.getActionTypeList() != null) {
			msg.add("actionTypeList", convertToJsonArray(input.getActionTypeList()));
			// logger.info(convertToJsonArray(userIdList));
		}

		if (input.getSourceTypeList() != null) {
			msg.add("sourceTypeList", convertToJsonArray(input.getSourceTypeList()));
			// logger.info(convertToJsonArray(userIdList));
		}

		if (input.getUserIdList() != null) {
			msg.add("userIdList", convertToJsonArray(input.getUserIdList()));
			// logger.info(convertToJsonArray(userIdList));
		}

		if (input.getDateFrom() != null) {
			msg.addProperty("dateFrom", covertToISO8601Format(input.getDateFrom()));
			// logger.info(convertToJsonArray(userIdList));
		}

		if (input.getDateTo() != null) {
			msg.addProperty("dateTo", covertToISO8601Format(input.getDateTo()));
			// logger.info(convertToJsonArray(userIdList));
		}

		if (input.getRecentStatementNum() != null) {
			msg.addProperty("recentStatementNum", input.getRecentStatementNum());
			// logger.info(convertToJsonArray(userIdList));
		}

		logger.info("[LRS] Request to " + LRS_ADDR + " with input: " + msg);
		return gson.toJson(msg);
	}

	public JsonArray getStatementList(GetStatementInfoDTO input) throws ParseException {
		JsonArray output = new JsonArray();
		this.input = input;
		String inputJson = generateInput();
		String responseString = "";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(inputJson, headers);

		try {
			ResponseEntity<String> lrsResponse = restTemplate.postForEntity(LRS_ADDR, entity, String.class);
			logger.info("LRS Server Response with code {}", lrsResponse.getStatusCode());
			responseString = lrsResponse.getBody();
		} catch (HttpStatusCodeException e) {
			logger.info("LRS Server Response error. Body: {}", e.getResponseBodyAsString());
			throw e;
		}
		output = JsonParser.parseString(responseString).getAsJsonArray();
		return output;
	}
}
