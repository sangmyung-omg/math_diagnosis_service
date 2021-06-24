package com.tmax.WaplMath.AnalysisReport.controller.chapter;

import java.util.List;

// import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
// import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UnsupportedErrorDTO;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Chapter related REST controller
 * @author Jonghyun Seong
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
public class ChapterControllerV0 {

    @Autowired
    @Qualifier("ChapterServiceV1")
    private ChapterServiceBase chapterSvc;
    
    @GetMapping("/chapters")
    ResponseEntity<Object> getChaptersList(@RequestHeader("token") String token, 
                                           @RequestParam(name="range", required = false) String range,
                                           @RequestParam(name="subrange", required = false) String subrange){
        //Extract userID from token                                         
        String userID = JWTUtil.getJWTPayloadField(token, "userID");

    
        List<ChapterDetailDTO> output = chapterSvc.getChapterListOfUserInRange(userID, range, subrange);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/chapters")
    ResponseEntity<Object> getChaptersListFromChapterIDList(@RequestHeader("token") String token, 
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        // String userID =  JWTUtil.getJWTPayloadField(token, "userID");;

        // List<ChapterDetailDTO> output = chapterSvc.getSpecificChapterListOfUser(userID, chapterIDList);
        return new ResponseEntity<>(new UnsupportedErrorDTO(), HttpStatus.NOT_FOUND);
    }

    @GetMapping("/chapters/{userID}")
    ResponseEntity<Object> getUserChaptersList(@RequestHeader("token") String token,
                                               @PathParam("userID") String userID){
                                       
        List<ChapterDetailDTO> output = chapterSvc.getAllChapterListOfUser(userID);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/chapters/{userID}")
    ResponseEntity<Object> getUserChaptersFromChapterIDList(@RequestHeader("token") String token, 
                                                            @PathParam("userID") String userID,
                                                            @RequestBody ChapterIDListDTO chapterIDList){
       
        // List<ChapterDetailDTO> output = chapterSvc.getSpecificChapterListOfUser(userID, chapterIDList);
        return new ResponseEntity<>(new UnsupportedErrorDTO(), HttpStatus.NOT_FOUND);
    }
}
