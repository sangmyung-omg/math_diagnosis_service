package com.tmax.WaplMath.AnalysisReport.controller.commentary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.commentary.CommentaryDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.commentary.CommentaryResponseDTO;
import com.tmax.WaplMath.AnalysisReport.dto.commentary.CommentaryTemplateInDTO;
import com.tmax.WaplMath.AnalysisReport.service.commentary.CommentaryService;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path=ARConstants.apiPrefix + "/v2")
public class CommentaryControllerV2 {
    @Autowired
    CommentaryService commentarySvc;

    @GetMapping("/commentarydata")
    public ResponseEntity<Object> getCommentaryData(@RequestHeader("token") String token, 
                                                    @RequestParam(defaultValue = "", name="exclude") String exclude) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);

        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));

        CommentaryDataDTO result = commentarySvc.getCommentaryData(userID, excludeSet);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/commentary")
    public ResponseEntity<Object> getCommentaryFromTemplate(@RequestHeader("token") String token, 
                                                            @RequestBody CommentaryTemplateInDTO body,
                                                            @RequestParam(defaultValue = "", name="exclude") String exclude) {
        //Parse jwt to get userID
        String userID  = JWTUtil.getUserID(token);

        //split to get exclude list
        Set<String> excludeSet = new HashSet<>(Arrays.asList(exclude.split(",")));
        CommentaryResponseDTO result = commentarySvc.getCommentaryFromTemplate(userID, body.getTemplate(), excludeSet);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
