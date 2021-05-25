package com.tmax.WaplMath.AnalysisReport.controller.chapter;

import java.util.List;

// import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
// import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class ChapterControllerV0 {

    @Autowired
    @Qualifier("v0")
    private ChapterServiceBase chapterSvc;
    
    @GetMapping("/chapters")
    ResponseEntity<Object> getChaptersList(@RequestHeader("token") String token){
        //TODO: get userID from token
        String userID = token;

        List<ChapterDetailDTO> output = chapterSvc.getAllChapterListOfUser(userID);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @PostMapping("/chapters")
    ResponseEntity<Object> getChaptersListFromChapterIDList(@RequestHeader("token") String token, 
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        //TODO: get userID from token
        String userID = token;

        List<ChapterDetailDTO> output = chapterSvc.getSpecificChapterListOfUser(userID, chapterIDList);
        return new ResponseEntity<>(output, HttpStatus.OK);
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
       
        List<ChapterDetailDTO> output = chapterSvc.getSpecificChapterListOfUser(userID, chapterIDList);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}
