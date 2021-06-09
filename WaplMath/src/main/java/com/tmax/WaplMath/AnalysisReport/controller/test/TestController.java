package com.tmax.WaplMath.AnalysisReport.controller.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
// import java.util.Arrays;
// import java.util.List;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.config.Constants;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
// import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
// import com.tmax.WaplMath.AnalysisReport.model.knowledge.UserKnowledgeJoined;
// import com.tmax.WaplMath.AnalysisReport.model.problem.ProblemCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.UserCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.problem.ProblemCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.problem.ProblemRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.studyguide.StudyGuideServiceBase;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.AnalysisReport.repository.knowledge.UserKnowledgeRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path=Constants.apiPrefix + "/v0")
public class TestController {
    @Autowired
    ProblemCurriculumRepo repo;

    @Autowired
    @Qualifier("AR-UserKnowledgeRepo")
    UserKnowledgeRepo knowledgeRepo;

    @Autowired
    UserCurriculumRepo currRepo;

    @Autowired
    @Qualifier("AR-ProblemRepo")
    ProblemRepo probRepo;

    @GetMapping("/test")
    // @ResponseBody
    ResponseEntity<Object> getData(@RequestParam("probList") String probList, @RequestParam("userID") String userID) throws FileNotFoundException {
        Path path = ResourceUtils.getFile("classpath:uk_avg_data.json").toPath();
        FileReader reader = new FileReader(path.toString());
        JsonObject result = (JsonObject)JsonParser.parseReader(reader);

        // List<ProblemCurriculum> data = repo.getCurriculumProblemMappingOfProbIDList(Arrays.asList(probList.split(",")));

        // List<UserKnowledgeJoined> result2 = knowledgeRepo.getUKListOfProbList(userID, Arrays.asList(probList.split(",")));

        // List<UserMasteryCurriculum> result3= currRepo.getUserCurriculum(userID);

        List<UserKnowledge> output = knowledgeRepo.getUserKnowledge(userID);
        

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @GetMapping("/test2")
    // @ResponseBody
    ResponseEntity<Object> getProbData(@RequestParam("currID") String currID) {

        System.out.println("CurrID: " + currID);
        
        List<Problem> output = probRepo.getProblemByCurriculumID(currID);
        

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Autowired
    @Qualifier("AR-StudyGuideServiceV1")
    StudyGuideServiceBase studyGuideSvc;

    @GetMapping("/test3")
    ResponseEntity<Object> get() {
        StudyGuideDTO output = studyGuideSvc.getStudyGuideOfUser("mkkang");
        return new ResponseEntity<>(output, HttpStatus.OK);
    }


    @Autowired
    @Qualifier("ChapterServiceV1")
    ChapterServiceBase chapterServicev1;

    @GetMapping("/testchap")
    ResponseEntity<Object> getchap() {
        chapterServicev1.getAllChapterListOfUser("mkkang");
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
