package com.tmax.WaplMath.AnalysisReport.util.triton;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.triton.TritonDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.triton.TritonRequestDTO;
import com.tmax.WaplMath.AnalysisReport.dto.triton.TritonResponseDTO;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.util.exception.StackPrinter;
import com.tmax.WaplMath.Recommend.dto.WaplScoreProbDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component("WAPLScoreTriton")
@PropertySource("classpath:triton.properties")
public class WAPLScoreTriton {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Value("${triton.waplscore.host}")
    private String tritonURL;

    private TritonResponseDTO callTritonInference(String bodyPayload){
        //Create a http timeout handler
		HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofMillis(5000))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
            );

        //Create header
        WebClient webClient = WebClient.builder()
            .baseUrl(tritonURL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();


        logger.info("POSTING to " + tritonURL);
        //Call post to "/InfoForMastery" LRS server --> get as String
        Mono<String> info =  webClient.post()
                .body(Mono.just(bodyPayload), String.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException(ARErrorCode.TRITON_400_ERROR)))
                .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException(ARErrorCode.TRITON_500_ERROR)))
                .bodyToMono(String.class);

        try {
            return new ObjectMapper().readValue(info.block(), TritonResponseDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new GenericInternalException(ARErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    private Map<String, String> getTypeUKListMap(List<WaplScoreProbDTO> probList){
        Map< String, String> output = new HashMap<>();
        
        //Iterate the input and bind all types
        for(WaplScoreProbDTO probDto : probList) {
            String type = probDto.getType();

            //If key doesn't exist, create a new ArrayList
            if(!output.containsKey(type)){
                //Put the starting bracket of list
                output.put(type, "[");
            }

            //push the current UK List to the key's list
            String currentList = output.get(type);
            currentList += probDto.getUkList().toString() + ",";

            output.put(type, currentList);
        }

        return output;
    }

    private TritonDataDTO buildUKListInput( Map< String, String> typeUKListMap){
        //Build UKList input
        TritonDataDTO output = new TritonDataDTO();
        output.setName("ukList");
        output.setDatatype("BYTES");
        
        List<String> dataList = new ArrayList<>();
        for(Map.Entry<String, String> entry: typeUKListMap.entrySet()){
            //Get value
            String value = entry.getValue();

            //Length exception handling
            if(value.length() == 0)
                continue;

            //Add to Data list
            dataList.add(value.substring(0, value.length() - 1) + "]");

            
        }

        //Types logging
        logger.info("Types: " + typeUKListMap.keySet().toString());

        output.setData(dataList);
        output.setShape(Arrays.asList(dataList.size()));

        return output;
    }

    private TritonDataDTO buildEmbeddingsInput(String embeddings){
        TritonDataDTO output = new TritonDataDTO();
        output.setName("embeddings");
        output.setDatatype("BYTES");
        output.setData(Arrays.asList(embeddings));
        output.setShape(Arrays.asList(1));

        return output;
    }


    public List< Map<Integer, Float> > calculateFromSequenceList(List<WaplScoreProbDTO> probList, String embeddings) {
        //Create a type UKList map
        Map<String,String> typeUKListMap = getTypeUKListMap(probList);       


        //Post process the end of string for each sequence + build triton request
        TritonDataDTO inputUKList = buildUKListInput(typeUKListMap);

        //Build Embeddings DTO
        TritonDataDTO inputEmbedding =buildEmbeddingsInput(embeddings);

        //Build Output
        TritonDataDTO outputDto = new TritonDataDTO("masteryList", null, null, null);

        //Build the Triton request body
        List<TritonDataDTO> inputList = Arrays.asList(inputUKList, inputEmbedding);
        TritonRequestDTO requestDTO = new TritonRequestDTO("1", inputList, Arrays.asList(outputDto));


        //Call the triton server
        List<Map<Integer, Float>> output = new ArrayList<>();
        try {
            String payload = new ObjectMapper().writeValueAsString(requestDTO);

            //Call triton and get response
            TritonResponseDTO responseDTO = callTritonInference(payload);
            
            //For loop but actually 1 loop
            for(TritonDataDTO responseOut : responseDTO.getOutputs()){
                //For each mid-stage mastery data
                Map<Integer, Float> ukMasteryMap = new HashMap<>();
                for(String data: responseOut.getData()){
                    //Foreach UK, parse and push to ukMastery map
                    JsonObject ukMap = JsonParser.parseString(data).getAsJsonObject();
                    for(Map.Entry<String, JsonElement> entry : ukMap.entrySet()){
                        ukMasteryMap.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsFloat());
                    }
                }

                //Push to mastery list (mid stage data save)
                output.add(ukMasteryMap);
            }
        } catch(NumberFormatException e){
            e.printStackTrace();
            throw new GenericInternalException(ARErrorCode.NUMBER_PARSE_ERROR, StackPrinter.getStackTrace(e));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new GenericInternalException(ARErrorCode.JSON_PROCESSING_ERROR, StackPrinter.getStackTrace(e));
        }
        catch (JsonParseException e) {
            e.printStackTrace();
            throw new GenericInternalException(ARErrorCode.JSON_PROCESSING_ERROR, StackPrinter.getStackTrace(e));
        }
        catch(Throwable e){
            e.printStackTrace();
            throw new GenericInternalException(ARErrorCode.GENERIC_ERROR, StackPrinter.getStackTrace(e));
        }
        
        return output;
    }
}
