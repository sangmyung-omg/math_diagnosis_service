package com.tmax.WaplMath.AnalysisReport.controller.chapter;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class ChapterControllerV0 {
    
    @GetMapping("/chapters")
    List<ChapterDetailDTO> getChapterList(@RequestHeader("token") String token, @RequestBody ChapterIDListDTO chapterIDList){
        return null;
    }
}
