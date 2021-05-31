package com.tmax.WaplMath.AnalysisReport.controller.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
// import java.util.Arrays;
// import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.config.Constants;
// import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
// import com.tmax.WaplMath.AnalysisReport.model.knowledge.UserKnowledgeJoined;
// import com.tmax.WaplMath.AnalysisReport.model.problem.ProblemCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.UserCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.problem.ProblemCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.uk.UserKnowledgeRepo;

import org.springframework.beans.factory.annotation.Autowired;
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
    UserKnowledgeRepo knowledgeRepo;

    @Autowired
    UserCurriculumRepo currRepo;

    @GetMapping("/test")
    @ResponseBody
    String getData(@RequestParam("probList") String probList, @RequestParam("userID") String userID) throws FileNotFoundException {
        Path path = ResourceUtils.getFile("classpath:uk_avg_data.json").toPath();
        FileReader reader = new FileReader(path.toString());
        JsonObject result = (JsonObject)JsonParser.parseReader(reader);

        // List<ProblemCurriculum> data = repo.getCurriculumProblemMappingOfProbIDList(Arrays.asList(probList.split(",")));

        // List<UserKnowledgeJoined> result2 = knowledgeRepo.getUKListOfProbList(userID, Arrays.asList(probList.split(",")));

        // List<UserMasteryCurriculum> result3= currRepo.getUserCurriculum(userID);
        

        return result.toString();
    }
}
