package com.tmax.WaplMath.AnalysisReport.controller.userdata;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.BasicProblemStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CustomStatDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.GlobalStatisticDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.WAPLScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.uk.UkDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserMasteryDataListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserStudyDataListRequestDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userdata.UserUKKnowledgeDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeScoreDTO;
import com.tmax.WaplMath.AnalysisReport.service.statistics.waplscore.WaplScoreServiceBaseV0;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class UserDataControllerV2 {
    @PostMapping("/userdata/ukknowledge")
    public ResponseEntity<Object> getStudyStats(@RequestHeader("token") String token, @RequestBody List<String> userIDList) {
        // String userID  = JWTUtil.getUserID(token);

        //Build Dummy
        List<Integer> ukIDList = Arrays.asList(103, 1502, 544);

        //userdata list
        List<UserUKKnowledgeDTO> userDataList = userIDList.stream().parallel().map(id -> {
            List<UkUserKnowledgeScoreDTO> ukDataList = ukIDList.stream().map(ukid -> {
                return UkUserKnowledgeScoreDTO.builder()
                                            .ukID(150)
                                            .mastery(PersonalScoreDTO.builder().score(54.6f).percentile(40.4f).build())
                                            .waplscore(PersonalScoreDTO.builder().score(74.3f).percentile(80.4f).build())
                                            .build();
            }).collect(Collectors.toList());
            
            return UserUKKnowledgeDTO.builder()
                                    .userID(id)
                                    .ukKnowledgeList(ukDataList)
                                    .build();
        }).collect(Collectors.toList());

        List<UkDataDTO> ukDataList = ukIDList.stream().parallel().map(id -> {
                return UkDataDTO.builder()
                                .ukID(id)
                                .name("더미 uk" + id)
                                .stats(GlobalStatisticDTO.builder()
                                                         .mean(23f)
                                                         .median(49.3f)
                                                         .std(12.3f)
                                                         .histogram(Arrays.asList(13,15,6))
                                                         .percentile(Arrays.asList(15f,28f,40f,49f))
                                                         .totalCnt(1402)
                                                         .build())
                                .build();
        }).collect(Collectors.toList());
        
        UserMasteryDataListDTO result = UserMasteryDataListDTO.builder()
                                                              .userDataList(userDataList)
                                                              .ukDataList(ukDataList)
                                                              .build();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/userdata/studystat")
    public ResponseEntity<Object> getUserUKKnowledge(@RequestHeader("token") String token,
                                                     @RequestBody UserStudyDataListRequestDTO reqBody) {
        // String userID  = JWTUtil.getUserID(token);
        

        //Dummy data
        List<String> userIDList = reqBody.getUserIDList(); 

        List<UserStudyDataDTO> result = userIDList.stream().map(id -> {
            return UserStudyDataDTO.builder()
                                   .userID(id)
                                   .basic(BasicProblemStatDTO.builder().correct(10).wrong(15).pass(5).totalcnt(30).build())
                                   .custom(CustomStatDataDTO.builder().notfocused(1).notserious(14).pick(3).unknown(4).build())
                                   .build();
        }).collect(Collectors.toList());
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
