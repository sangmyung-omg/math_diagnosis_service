package com.tmax.WaplMath.Recommend.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;

/**
 * Triton Inference Server HTTP Connection
 * 
 * @author Sangheon Lee
 */
@Slf4j
@Component
@PropertySources({
  @PropertySource("classpath:triton.properties"),
  @PropertySource(value="file:${external-config.url}/triton.properties", ignoreResourceNotFound=true),
})
public class MasteryAPIManager {
//	private static final String IP = System.getenv("KT_TRITON_IP");
//	private static final String PORT = System.getenv("KT_TRITON_PORT");
	
	private String TRITON_ADDR;

	@Autowired
	RestTemplate restTemplate;



	/**
	 * Added by Jonghyun seong. to get params from bean
	 * @since 2021-06-21
	 */
	@Autowired
	public MasteryAPIManager(@Value("${waplmath.recommend.masterytriton.host}") String host, 
							//  @Value("${waplmath.recommend.masterytriton.port}")	String PORT,
							 @Value("${waplmath.recommend.masterytriton.modelname}") String MODEL_NAME, 
							 @Value("${waplmath.recommend.masterytriton.modelver}") String MODEL_VERSION){
		
		log.info("Using Triton server @ "  + host);
		this.TRITON_ADDR = String.format("%s/v2/models/%s/versions/%s/infer", host, MODEL_NAME, MODEL_VERSION);
	}
	public MasteryAPIManager(){}

	public JsonArray generateInputs(List<String> ukIdList, List<String> corList, List<String> levelList,
			String userEmbedding) {
		JsonArray inputs = new JsonArray();

		// UKList
		JsonObject UKListJson = new JsonObject();
		JsonArray shape = new JsonArray();
		JsonArray data = new JsonArray();

		shape.add(ukIdList.size());

		for (String UKId : ukIdList) {
			data.add(Integer.parseInt(UKId));
		}
		UKListJson.addProperty("name", "UKList");
		UKListJson.add("shape", shape);
		UKListJson.addProperty("datatype", "INT32");
		UKListJson.add("data", data);

		// CorList
		JsonObject corListJson = new JsonObject();
		data = new JsonArray();

		for (String cor : corList) {
			Integer correctInt = cor.equals("true") ? 1 : 0;
			data.add(correctInt);
		}
		corListJson.addProperty("name", "IsCorrectList");
		corListJson.add("shape", shape);
		corListJson.addProperty("datatype", "INT32");
		corListJson.add("data", data);

		// LevelList
		JsonObject levelListJson = new JsonObject();
		data = new JsonArray();

		for (String level : levelList) {
			Integer levelKeyword;
			switch (level) {
			case "상":
				levelKeyword = 1;
				break;
			case "중":
				levelKeyword = 2;
				break;
			case "하":
				levelKeyword = 3;
				break;
			default:
				levelKeyword = 2;
				break;
			}
			data.add(levelKeyword);
		}
		levelListJson.addProperty("name", "DifficultyList");
		levelListJson.add("shape", shape);
		levelListJson.addProperty("datatype", "INT32");
		levelListJson.add("data", data);

		// Embeddings
		JsonObject embeddingsJson = new JsonObject();
		shape = new JsonArray();
		data = new JsonArray();

		shape.add(1);
		data.add(userEmbedding);

		embeddingsJson.addProperty("name", "Embeddings");
		embeddingsJson.add("shape", shape);
		embeddingsJson.addProperty("datatype", "BYTES");
		embeddingsJson.add("data", data);

		inputs.add(UKListJson);
		inputs.add(corListJson);
		inputs.add(levelListJson);
		log.info("Triton request to {} with input (except print embedding) : {}", TRITON_ADDR, inputs);
		inputs.add(embeddingsJson);
		return inputs;
	}

	public JsonArray generateOutputs() {
		JsonArray outputs = new JsonArray();
		JsonObject mastery = new JsonObject();
		JsonObject embeddings = new JsonObject();
		mastery.addProperty("name", "Mastery");
		embeddings.addProperty("name", "Embeddings");
		outputs.add(mastery);
		outputs.add(embeddings);

		return outputs;
	}

	public String generatePostJson(String userId, List<String> ukIdList, List<String> corList, List<String> levelList,
			String userEmbedding) {
		Gson gson = new Gson();
		JsonObject msg = new JsonObject();

		JsonArray inputs = generateInputs(ukIdList, corList, levelList, userEmbedding);
		JsonArray outputs = generateOutputs();

		msg.addProperty("id", userId);
		msg.add("inputs", inputs);
		msg.add("outputs", outputs);

		return gson.toJson(msg);
	}

	public JsonObject measureMastery(String userId, List<String> ukIdList, List<String> corList, List<String> levelList,
			String userEmbedding) throws Exception {

		String input = generatePostJson(userId, ukIdList, corList, levelList, userEmbedding);
		JsonObject output = new JsonObject();
		String responseString = "";

		try {
			ResponseEntity<String> tritonResponse = restTemplate.postForEntity(TRITON_ADDR, input, String.class);
			log.info("Triton Response with code {}", tritonResponse.getStatusCode());
			responseString = tritonResponse.getBody();
		} catch (HttpStatusCodeException e) {
			log.info("Triton Response error. Body: {}", e.getResponseBodyAsString());
			throw e;
		}

		JsonObject responseJson = JsonParser.parseString(responseString).getAsJsonObject();
		JsonArray outputsArray = (JsonArray) responseJson.get("outputs");

		for (JsonElement outputsElement : outputsArray) {
			JsonObject outputsObject = (JsonObject) outputsElement;
			String outputName = outputsObject.get("name").getAsString();
			String dataString = outputsObject.get("data").getAsJsonArray().get(0).getAsString();
			output.addProperty(outputName, dataString);
		}

		return output;
	}


	/**
	 * 2021-06-18 Added by Jonghyun Seong. overloading of measureMastery to DTO
	 */
	public TritonMasteryDTO measureMasteryDTO(String userId, List<String> ukIdList, List<String> corList, List<String> levelList,	String userEmbedding){
		try {
			//Get result from original measureMastery
			JsonObject result = measureMastery(userId, ukIdList, corList, levelList, userEmbedding);

			//Create Map from the mastery json Object
			JsonObject masteryDict = JsonParser.parseString(result.get("Mastery").getAsString()).getAsJsonObject();
			Map<Integer, Float> masteryMap = new HashMap<>();
			for(Map.Entry<String,JsonElement> entry: masteryDict.entrySet()){
				masteryMap.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsFloat());
			}

			return new TritonMasteryDTO(masteryMap, result.get("Embeddings").getAsString());
		}
		catch(Throwable e) {
			throw new RecommendException(RecommendErrorCode.TRITON_INFERENCE_ERROR, StackPrinter.getStackTrace(e));
		}		
	}

}
