package com.tmax.WaplMath.AnalysisReport.controller.chapter;

// import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
// import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;

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
    
    @GetMapping("/chapters")
    ResponseEntity<Object> getChaptersList(@RequestHeader("token") String token){
        return null;
    }

    @PostMapping("/chapters")
    ResponseEntity<Object> getChaptersListFromChapterIDList(@RequestHeader("token") String token, 
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        return null;
    }

    @GetMapping("/chapters/{userID}")
    ResponseEntity<Object> getUserChaptersList(@RequestHeader("token") String token,
                                               @PathParam("userID") String userID){
        return null;
    }

    @PostMapping("/chapters/{userID}")
    ResponseEntity<Object> getUserChaptersFromChapterIDList(@RequestHeader("token") String token, 
                                                            @PathParam("userID") String userID,
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        return null;
    }
}
