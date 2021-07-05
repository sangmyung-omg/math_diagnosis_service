package com.tmax.WaplMath.Recommend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Call StatementList GET API from LRS Server
 * @author Sangheon_lee
 */
@Component
@PropertySource("classpath:lrs.properties")
public class LRSAPIManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private String HOST;
	private String LRS_ADDR;

	@Autowired
	RestTemplate restTemplate;

	public GetStatementInfoDTO input;

	@Autowired
	public LRSAPIManager(@Value("${waplmath.recommend.lrs.host}") String IP, @Value("${waplmath.recommend.lrs.port}") String PORT) {
		logger.info("constructor" + IP + PORT);
		this.HOST = String.format("http://%s:%s", IP, PORT);
		this.LRS_ADDR = String.format("%s/StatementList", this.HOST, PORT);
	}

	public LRSAPIManager() {
	}

	public String covertToISO8601Format(String date) throws ParseException {
		SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+09:00");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date_obj = dateFormat.parse(date);
		// logger.info(ISO8601.format(date_obj));
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
			//logger.info("{}", input.getDateTo());
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

	public List<StatementDTO> getStatementListNew(GetStatementInfoDTO input) throws ParseException {
		//Create a http timeout handler
		HttpClient httpClient = HttpClient.create()
										  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
										  .responseTimeout(Duration.ofMillis(5000))
										  .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
										  .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
		//Create header
		WebClient webClient = WebClient.builder()
									   .baseUrl(HOST)
									   .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
									   .clientConnector(new ReactorClientHttpConnector(httpClient))
									   .build();
		//Call post to "/StatementList" LRS server --> get as String
		Mono<String> info = webClient.post()
				 .uri("/StatementList")
				 .body(Mono.just(input), GetStatementInfoDTO.class)
				 .retrieve()
			  	 .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
			 	 .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
				 .bodyToMono(String.class);
		//Convert output to result
		try {
			logger.info(info.block());
			return Arrays.asList(new ObjectMapper().readValue(info.block(), StatementDTO[].class));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.warn("LRS return body : " + info.block());
			throw new GenericInternalException("ERR-LRS-501", "LRS return body cannot be parsed correctly");
		}
	}

	/**
	 * Method to call update mastery lrs info from lrs
	 * @author Jonghyun Seong
	 * @since 2021-06-16
	 */
	public ProblemSolveListDTO getLRSUpdateProblemSequence(String token) {
		//Create a http timeout handler
		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).responseTimeout(Duration.ofMillis(5000))
			.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
				.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

		//Create header
		WebClient webClient = WebClient.builder().baseUrl(HOST).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.clientConnector(new ReactorClientHttpConnector(httpClient)).build();


		//Call post to "/InfoForMastery" LRS server --> get as String
		Mono<String> info = webClient.get().uri("/InfoForMastery").header("token", token).retrieve()
			.onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
			.onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
			.bodyToMono(String.class);

		//Convert output to result
		ProblemSolveListDTO result = null;
		try {
			result = new ObjectMapper().readValue(info.block(), ProblemSolveListDTO.class);
		} catch (Throwable e) {
			logger.warn("LRS return body : " + info.block());
			throw new GenericInternalException("ERR-LRS-501", "LRS return body cannot be parsed correctly");
		}

		return result;
	}
}
