package com.tmax.WaplMath.Common.util.lrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import com.tmax.WaplMath.Common.dto.lrs.LRSStatementRequestDTO;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.exception.GenericInternalException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component("LRSManager")
@Profile("!test")
@Slf4j
public class LRSManager implements LRSManagerInterface {

    // private String HOST;
    private String API_TARGET;


    private HttpClient httpClient;
    private WebClient webClient;


    @Value("${waplmath.lrs.duplcatefilter}")
    private boolean useDuplicateFilter;


    public LRSManager(@Value("${waplmath.recommend.lrs.host}") String host) {
        log.info("Using LRS server @ " + host);
        // this.HOST = host;
        this.API_TARGET = String.format("%s/StatementList", host);

        buildWebclient();
    }

    private void buildWebclient() {
        //Create a http timeout handler
        this.httpClient = HttpClient.create()
                                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                    .responseTimeout(Duration.ofMillis(5000))
                                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                    .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        //Create header
        this.webClient = WebClient.builder()
                                    .baseUrl(API_TARGET)
                                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                                    .build();
    }

    @Override
    public List<LRSStatementResultDTO> getStatementList(LRSStatementRequestDTO lrsRequest) {
        //Call post to "/InfoForMastery" LRS server --> get as String
        Mono<LRSStatementResultDTO[]> info = webClient.post()
                                                      .bodyValue(lrsRequest)
                                                      .retrieve()
                                                      .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
                                                      .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
                                                      .bodyToMono(LRSStatementResultDTO[].class);

        //Convert output to result
        List<LRSStatementResultDTO> result = Arrays.asList(info.block());

        if(useDuplicateFilter)
            return getLrsWithoutDuplicate(result);

        return result;
    }

    @Override
    public List<LRSStatementResultDTO> getStatementList(String userID, List<String> actionTypeList,
            List<String> sourceTypeList) {
        return getStatementList(LRSStatementRequestDTO.builder()
                                                      .userIdList(Arrays.asList(userID))
                                                      .actionTypeList(actionTypeList)
                                                      .sourceTypeList(sourceTypeList)
                                                      .build()
                                                      );
    }


    /**
     * Added to get lrs and filter duplicates
     * @param userID
     * @since 2021-08-24
     * @author jonghyun seong
     * @return
     */
    private List<LRSStatementResultDTO> getLrsWithoutDuplicate(List<LRSStatementResultDTO> resultList){
        //Set to save identity(duplicate id set)
        Set<String> identitySet = new HashSet<>();

        //Result set
        List<LRSStatementResultDTO> output = new ArrayList<>();

        for(LRSStatementResultDTO lrsStatement : resultList){
            //build id list
            String identity = buildIdentityString(lrsStatement);

            //If in identity (duplicate continue and skip)
            if(identitySet.contains(identity))
                continue;

            //Add if not exist
            identitySet.add(identity);

            output.add(lrsStatement);
        }


        return output;
    }

    private String buildIdentityString(LRSStatementResultDTO input){
        return String.format("%s/%s/%s/%s/%s/%s",   input.getUserId(),
                                                    input.getActionType(), 
                                                    input.getSourceType(), 
                                                    input.getSourceId(), 
                                                    input.getTimestamp(), 
                                                    input.getPlatform());
    }
}
