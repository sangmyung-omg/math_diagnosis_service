package com.tmax.WaplMath.AnalysisReport.controller.userknowledge;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;
import com.tmax.WaplMath.AnalysisReport.service.userknowledge.UserKnowledgeServiceBase;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class UserKnowledgeControllerV2 {
    @Autowired
    UserKnowledgeServiceBase userKnowledgeSvc;

    @PostMapping("/userknowledge")
    public ResponseEntity<Object> getUserKnowledgeList(@RequestHeader("token") String token, @RequestBody List<Integer> ukIDList) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        log.debug(String.format("getByUkIdList(%s, %s)", userID, ukIDList.toString()));
        List<UkUserKnowledgeDetailDTO> result = userKnowledgeSvc.getByUkIdList(userID, ukIDList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/userknowledge/{ukID}")
    public ResponseEntity<Object> getUserKnowledge(@RequestHeader("token") String token, @PathVariable("ukID") Integer ukID) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        log.debug(String.format("getByUkId(%s, %d)", userID, ukID));
        UkUserKnowledgeDetailDTO result = userKnowledgeSvc.getByUkId(userID, ukID);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
