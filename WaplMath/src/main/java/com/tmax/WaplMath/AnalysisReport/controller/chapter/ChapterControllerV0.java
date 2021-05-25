package com.tmax.WaplMath.AnalysisReport.controller.chapter;

import java.util.List;

import javax.websocket.server.PathParam;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;

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
    List<ChapterDetailDTO> getChaptersList(@RequestHeader("token") String token){
        return null;
    }

    @PostMapping("/chapters")
    List<ChapterDetailDTO> getChaptersListFromChapterIDList(@RequestHeader("token") String token, 
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        return null;
    }

    @GetMapping("/chapters/{userID}")
    List<ChapterDetailDTO> getUserChaptersList(@RequestHeader("token") String token,
                                               @PathParam("userID") String userID){
        return null;
    }

    @PostMapping("/chapters/{userID}")
    List<ChapterDetailDTO> getUserChaptersFromChapterIDList(@RequestHeader("token") String token, 
                                                            @PathParam("userID") String userID,
                                                            @RequestBody ChapterIDListDTO chapterIDList){
        return null;
    }
}
