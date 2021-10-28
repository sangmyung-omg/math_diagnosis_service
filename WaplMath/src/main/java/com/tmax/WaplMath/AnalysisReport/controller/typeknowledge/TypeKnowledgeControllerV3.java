package com.tmax.WaplMath.AnalysisReport.controller.typeknowledge;

import java.util.Arrays;
import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.typeknowledge.TypeKnowledgeScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.userknowledge.UkUserKnowledgeDetailDTO;
import com.tmax.WaplMath.AnalysisReport.service.typeknowledge.TypeKnowledgeServiceBase;
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
@RequestMapping(path=ARConstants.apiPrefix + "/v3")
public class TypeKnowledgeControllerV3 {
    @Autowired
    TypeKnowledgeServiceBase typeKnowledgeSvc;

    @PostMapping("/typeknowledge")
    public ResponseEntity<Object> getTypeKnowledgeList(@RequestHeader("token") String token, @RequestBody List<Integer> typeIDList) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        log.debug(String.format("getByTypeIdList(%s, %s)", userID, typeIDList.toString()));
        List<TypeKnowledgeScoreDTO> result = typeKnowledgeSvc.getByTypeIdList(userID, typeIDList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/typeknowledge/{typeID}")
    public ResponseEntity<Object> getTypeKnowledge(@RequestHeader("token") String token, @PathVariable("typeID") Integer typeID) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);
        
        log.debug(String.format("getByUkId(%s, %d)", userID, typeID));
        List<TypeKnowledgeScoreDTO> result = typeKnowledgeSvc.getByTypeIdList(userID, Arrays.asList(typeID));

        if(result.size() == 0){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No typeknowledge found");
        }

        return new ResponseEntity<>(result.get(0), HttpStatus.OK);
    }
}
