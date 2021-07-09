package com.tmax.WaplMath.AnalysisReport.controller.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
// import java.util.Arrays;
// import java.util.List;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.config.ARConstants;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
// import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
// import com.tmax.WaplMath.AnalysisReport.model.knowledge.UserKnowledgeJoined;
// import com.tmax.WaplMath.AnalysisReport.model.problem.ProblemCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.UserCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.problem.ProblemCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.problem.ProblemRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.WaplScoreServiceV0;
import com.tmax.WaplMath.AnalysisReport.service.statistics.curriculum.CurrStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.uk.UKStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.AnalysisReport.service.studyguide.StudyGuideServiceBase;
import com.tmax.WaplMath.Common.model.redis.RedisStringData;
import com.tmax.WaplMath.Common.repository.redis.RedisStringRepository;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.problem.Problem;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;
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


/**
 * Test REST Controller for unit testing
 * @author Jonghyun Seong
 */
@RestController
@RequestMapping(path=ARConstants.apiPrefix + "/v0")
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
        Path path = ResourceUtils.getFile("classpath:statistics/uk_avg_data.json").toPath();
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


    @Autowired
    WaplScoreServiceV0 waplScoreSvc;

    @GetMapping("/testwaplscore")
    ResponseEntity<Object> getWaplScore() {
        waplScoreSvc.getWaplScore("mkkang");
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    @Autowired
    @Qualifier("UKStatisticsServiceV0")
    private UKStatisticsServiceBase ukStatSvc;

    @GetMapping("/teststatistic")
    ResponseEntity<Object> updateStat() {
        ukStatSvc.updateAllStatistics();
        return null;
    }

    @Autowired
    private UserStatisticsServiceBase userStatSvc;

    @GetMapping("/teststatistic2")
    ResponseEntity<Object> updateStat2(@RequestParam("user") String userID) {
        userStatSvc.updateSpecificUser(userID);
        return null;
    }
    

    @Autowired
    private CurrStatisticsServiceBase currStatSvc;

    @GetMapping("/teststatistic3")
    ResponseEntity<Object> updateStat3() {
        currStatSvc.updateStatistics();
        return null;
    }

    @GetMapping("/teststatistic4")
    ResponseEntity<Object> updateStat4() {
        userStatSvc.updateAllUsers();
        return null;
    }


    @Autowired
    private LRSAPIManager lrsApiManager;

    @GetMapping("/getLRS")
    ResponseEntity<Object> getLRS(@RequestParam("userID") String userID) {
        lrsApiManager.getUserStatement(userID);
        return null;
    }
    
    @Autowired
    private RedisStringRepository redisRepo;

    @GetMapping("/testredis")
    ResponseEntity<Object> testredis(@RequestParam("id") String id,@RequestParam("data") String data) {
        redisRepo.save(RedisStringData.builder().id(id).data(data).build());

        Optional<RedisStringData> output = redisRepo.findById(id);

        return new ResponseEntity<>(output.get(), HttpStatus.OK);
    }
}
