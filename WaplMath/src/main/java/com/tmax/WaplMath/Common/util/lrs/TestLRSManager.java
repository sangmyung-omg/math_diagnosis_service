package com.tmax.WaplMath.Common.util.lrs;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.tmax.WaplMath.Common.dto.lrs.LRSStatementRequestDTO;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.util.error.CommonErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component("TestLRSManager")
@Profile("test")
@Slf4j
public class TestLRSManager implements LRSManagerInterface {
    @Value("${lrs.testmanager.sampleno}")
    private Integer sampleNumber;

    @Value("${lrs.testmanager.platformname}")
    private String platformName;


    @Override
    public List<LRSStatementResultDTO> getStatementList(LRSStatementRequestDTO lrsRequest) {
        return statementGenerator(sampleNumber, 0.5, lrsRequest.getUserIdList(), lrsRequest.getActionTypeList(), lrsRequest.getSourceTypeList());
    }

    @Override
    public List<LRSStatementResultDTO> getStatementList(String userID, List<String> actionTypeList, List<String> sourceTypeList) {
        return getStatementList(LRSStatementRequestDTO.builder()        
                                                      .userIdList(Arrays.asList(userID))
                                                      .actionTypeList(actionTypeList)
                                                      .sourceTypeList(sourceTypeList)
                                                      .build());
    }


    private List<LRSStatementResultDTO> statementGenerator(int noOfStatements, double correctPossibility, List<String> userIDList, List<String> possibleActionTypes, List<String> possibleSourceTypes){
        if(noOfStatements <= 0){
            log.error("Invalid statement length");
            return null;
            // throw new (CommonErrorCode.INVALID_ARGUMENT, );
        }

        if(possibleActionTypes.size() == 0 || possibleSourceTypes.size() == 0){
            log.error("No action/source type is given");
            return null;
        }

        //Base timestamp
        ZonedDateTime timebase = ZonedDateTime.now();

        //Randomly generate statements
        return IntStream.range(0, noOfStatements)
                        .parallel()
                        .mapToObj(idx -> {
                            //Select from userIDList
                            String userID = userIDList.get( (int)Math.floor(userIDList.size() * Math.random())  );

                            //Select sourceType. random
                            String sourceType = possibleSourceTypes.get( 
                                                    (int)Math.floor(possibleSourceTypes.size() * Math.random()) 
                                                    );
                            
                            //Select actionType. random
                            String actionType = possibleActionTypes.get( 
                                                    (int)Math.floor(possibleActionTypes.size() * Math.random()) 
                                                    );

                            //Select is correct by given possibility
                            int isCorrect = Math.random() < correctPossibility ? 1 : 0;

                            //Select duration random between 60s (1min) ~ 360s (6min)
                            int duration = 60*1000 + (int)Math.round(360 * 1000 * Math.random());

                            //Calculate time by giving offset to timebase + 2* idx minutes
                            String timestamp = timebase.plusMinutes(2 *idx).toString();

                            return LRSStatementResultDTO.builder()
                                                        .userId(userID)
                                                        .sourceType(sourceType)
                                                        .actionType(actionType)
                                                        .isCorrect(isCorrect)
                                                        .userAnswer("dummy")
                                                        .platform(platformName)
                                                        .timestamp(timestamp)
                                                        .duration(Integer.toString(duration))
                                                        .build();
                        }).collect(Collectors.toList());
    }
}
