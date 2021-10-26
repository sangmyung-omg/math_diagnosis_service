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
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
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
  
  private String HOST_ADDR;
  private String UK_BASED_TRITON_ADDR;
  private String TYPE_BASED_TRITON_ADDR;

  private String BASE_TRITON_API_STRING = "%s/v2/models/%s/versions/%s/infer";

  @Autowired
  RestTemplate restTemplate;

  public enum TritonModelMode {
    UK_BASED,
    TYPE_BASED
  }

  /**
   * Added by Jonghyun seong. to get params from bean
   * @since 2021-06-21
   */
  @Autowired
  public MasteryAPIManager(@Value("${waplmath.recommend.masterytriton.host}") String host, 
                          @Value("${waplmath.recommend.masterytriton.ukbased.modelname}") String UK_BASED_MODEL_NAME, 
                          @Value("${waplmath.recommend.masterytriton.ukbased.modelver}") String UK_BASED_MODEL_VERSION,
                          @Value("${waplmath.recommend.masterytriton.typebased.modelname}") String TYPE_BASED_MODEL_NAME, 
                          @Value("${waplmath.recommend.masterytriton.typebased.modelver}") String TYPE_BASED_MODEL_VERSION){
    
    log.info("Using Triton server @ {}", host);

    this.HOST_ADDR = host;
    this.UK_BASED_TRITON_ADDR = String.format(this.BASE_TRITON_API_STRING, host, UK_BASED_MODEL_NAME, UK_BASED_MODEL_VERSION);
    this.TYPE_BASED_TRITON_ADDR = String.format(this.BASE_TRITON_API_STRING, host, TYPE_BASED_MODEL_NAME, TYPE_BASED_MODEL_VERSION);
  }
  public MasteryAPIManager(){}

  public JsonArray generateInputs(List<String> ukIdList, List<String> corList, List<String> levelList,
      String userEmbedding) {
    JsonArray inputs = new JsonArray();

    // UKList
    JsonObject UKListJson = new JsonObject();
    JsonArray shape = new JsonArray();
    JsonArray data = new JsonArray();

    shape.add(1);
    shape.add(ukIdList.size());

    for (String UKId : ukIdList) {
      data.add(Integer.parseInt(UKId));
    }
    UKListJson.addProperty("name", "ukList");
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
    corListJson.addProperty("name", "isCorrectList");
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
    levelListJson.addProperty("name", "difficultyList");
    levelListJson.add("shape", shape);
    levelListJson.addProperty("datatype", "INT32");
    levelListJson.add("data", data);

    // Embeddings
    JsonObject embeddingsJson = new JsonObject();
    shape = new JsonArray();
    data = new JsonArray();

    shape.add(1);
    shape.add(1);
    data.add(userEmbedding);

    embeddingsJson.addProperty("name", "inEmbedding");
    embeddingsJson.add("shape", shape);
    embeddingsJson.addProperty("datatype", "BYTES");
    embeddingsJson.add("data", data);

    inputs.add(UKListJson);
    inputs.add(corListJson);
    inputs.add(levelListJson);
    log.info("Triton request to {} with input (except print embedding) : {}", this.HOST_ADDR, inputs);
    inputs.add(embeddingsJson);
    return inputs;
  }

  public JsonArray generateOutputs() {
    JsonArray outputs = new JsonArray();
    JsonObject mastery = new JsonObject();
    JsonObject embeddings = new JsonObject();
    mastery.addProperty("name", "mastery");
    embeddings.addProperty("name", "outEmbedding");
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

  /**
   * Compat method overload
   */
  public JsonObject measureMastery(String userId, List<String> ukIdList, List<String> corList, List<String> levelList,
                String userEmbedding) throws Exception {
      return measureMastery(userId, ukIdList, corList, levelList, userEmbedding, TritonModelMode.UK_BASED);
  } 

  // Modded 2021-09-24 --> mode select for part and uk mode
  public JsonObject measureMastery(String userId, List<String> ukIdList, List<String> corList, List<String> levelList,
      String userEmbedding, TritonModelMode mode) throws Exception {

    String input = generatePostJson(userId, ukIdList, corList, levelList, userEmbedding);
    JsonObject output = new JsonObject();
    String responseString = "";

    try {
      ResponseEntity<String> tritonResponse = null;
      switch(mode){
        case UK_BASED: tritonResponse = restTemplate.postForEntity(this.UK_BASED_TRITON_ADDR, input, String.class); break;
        case TYPE_BASED: tritonResponse = restTemplate.postForEntity(this.TYPE_BASED_TRITON_ADDR, input, String.class); break;
        default : log.error("Unsupported model case");
      }

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
  public TritonMasteryDTO measureMasteryDTO(String userId, List<String> idList, List<String> corList, List<String> levelList,	String userEmbedding){
    return measureMasteryDTO(userId, idList, corList, levelList, userEmbedding, TritonModelMode.UK_BASED);
  }

  /**
   * 2021-09-24 Added by Jonghyun Seong. add mode selector for type <--> uk based model mode switching
   * @param userId user Id of user
   * @param idList type/uk id list of data
   * @param corList
   * @param levelList
   * @param userEmbedding
   * @param mode
   * @return
   */
  public TritonMasteryDTO measureMasteryDTO(String userId, List<String> idList, List<String> corList, List<String> levelList,	String userEmbedding, TritonModelMode mode){
    try {
      //Get result from original measureMastery
      JsonObject result = measureMastery(userId, idList, corList, levelList, userEmbedding, mode);

      //Create Map from the mastery json Object
      JsonObject masteryDict = JsonParser.parseString(result.get("mastery").getAsString()).getAsJsonObject();
      Map<Integer, Float> masteryMap = new HashMap<>();
      for(Map.Entry<String,JsonElement> entry: masteryDict.entrySet()){
        masteryMap.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsFloat());
      }

      return new TritonMasteryDTO(masteryMap, result.get("outEmbedding").getAsString());
    }
    catch(Throwable e) {
      throw new RecommendException(RecommendErrorCode.TRITON_INFERENCE_ERROR, StackPrinter.getStackTrace(e));
    }		
  }

}
