package com.tmax.WaplMath.Recommend.service.mastery.v2;

import java.util.Set;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.tmax.WaplMath.AnalysisReport.service.part.PartService;
import com.tmax.WaplMath.AnalysisReport.service.statistics.user.UserStatisticsServiceBase;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.exception.UserOrientedException;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.repository.knowledge.TypeKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.problem.ProblemRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Common.util.lrs.ActionType;
import com.tmax.WaplMath.Common.util.lrs.LRSManager;
import com.tmax.WaplMath.Common.util.lrs.SourceType;
import com.tmax.WaplMath.Common.util.redis.RedisIdGenerator;
import com.tmax.WaplMath.Common.util.redis.RedisUtil;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.service.mastery.v1.MasteryServiceBaseV1;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;
import com.tmax.WaplMath.Recommend.util.UkMasterySimulator;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager.TritonModelMode;

import com.tmax.WaplMath.AnalysisReport.service.statistics.Statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("MasteryServiceV2")
@Slf4j
public class MasteryServiceV2 implements MasteryServiceBaseV1 {
    @Autowired private MasteryAPIManager masteryAPIManager;
    @Autowired private LRSManager lrsManager;
    @Autowired private ProblemRepo problemRepo;
    @Autowired private RedisUtil redisUtil;
    @Autowired private UserRepo userRepo;

    @Autowired private UkMasterySimulator ukMasterySimulator;

    @Autowired private TypeKnowledgeRepo typeKnowledgeRepo;
    @Autowired private UserKnowledgeRepo userKnowledgeRepo;

    @Autowired private UserStatisticsServiceBase userStatSvc;
    @Autowired private PartService partService;

    @Value("${recommend.setting.useSimulatedUkKnowledge}")
    private boolean useSimulatedUkKnowledge;

    enum ResultMessage {
        SUCCESS("Update successfull"),
        FAILED("Update failed");

        private String message;
        private ResultMessage(String message){this.message = message;}

        public String toString(){return this.message;}
    }

    @Override
    public ResultMessageDTO updateMastery(String userId, List<String> probIdList, List<String> correctList) {
        //Redis cache checker
        String redisID = RedisIdGenerator.userOrientedID(this.getClass().getSimpleName(), userId, probIdList, correctList);
        if( redisUtil.hasID(redisID) ){
            //skip mastery
            log.info("Found identical user's mastery created from same lrs record. ({}). skipping mastery+stat update", userId);
            return new ResultMessageDTO(ResultMessage.SUCCESS.toString());
        }

        // //Check if user of id exists
        // if(!userRepo.findById(userId).isPresent()){
        //     log.error("User {} does not exist", userId);
        //     throw new UserNotFoundException(userId);
        // }

        //Exception handling for invalid data
        if(probIdList.size() != correctList.size() || probIdList.size() == 0 || correctList.size() == 0){
            log.error("User {} has invalid data size for updating mastery {} {}",userId,  probIdList.size(), correctList.size());
            throw new RecommendException(RecommendErrorCode.DATA_MISMATCH_ERROR,  "List size does not match or is 0");
        }


        //Get type id from probID
        List<Integer> probIdIntList = probIdList.stream()
                                                .flatMap(id -> {
                                                    try {return Stream.of(Integer.parseInt(id));} 
                                                    catch(Exception e){e.printStackTrace(); return Stream.empty();}
                                                    })
                                                .collect(Collectors.toList());
        List<Problem> problemInfoList = (List<Problem>)problemRepo.findAllById(probIdIntList);
        Map<Integer, String> probDiffMap = problemInfoList.stream().collect(Collectors.toMap(Problem::getProbId, Problem::getDifficulty));
        Map<Integer, Integer> probTypeIdMap = problemInfoList.stream().collect(Collectors.toMap(Problem::getProbId, Problem::getTypeId));


        List<String> typeIdList = probIdIntList.stream().map(id -> probTypeIdMap.get(id).toString()).collect(Collectors.toList());
        List<String> diffList = probIdIntList.stream().map(id -> probDiffMap.get(id).toString()).collect(Collectors.toList());


        TritonMasteryDTO inferResult = masteryAPIManager.measureMasteryDTO(userId, typeIdList, correctList, diffList, "", TritonModelMode.TYPE_BASED);

        if(inferResult == null){
            log.error("Invalid triton response. {}", userId);
            throw new UserOrientedException(RecommendErrorCode.TRITON_INFERENCE_ERROR, userId);
        }

        //Save each mastery to each table
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        log.debug("Saving [{}]'s type knowledge", userId);
        Set<TypeKnowledge> typeKnowledgeDbSet = inferResult.getMastery().entrySet()
                                                           .stream().parallel()
                                                           .map(entry -> TypeKnowledge.builder().userUuid(userId).typeId(entry.getKey()).typeMastery(entry.getValue()).updateDate(now).build())
                                                           .collect(Collectors.toSet());
        typeKnowledgeRepo.saveAll(typeKnowledgeDbSet);
        log.debug("saved [{}]'s type knowledge {}", userId, typeKnowledgeDbSet.size());

        //Get uk simulated mastery
        if(this.useSimulatedUkKnowledge){
            log.debug("Saving simulated uk user knowledge {}", userId);
            Map<Integer, Float> ukMastery = ukMasterySimulator.simulatedUkMastery(inferResult.getMastery());
            Set<UserKnowledge> userKnowledgeDbSet = ukMastery.entrySet()
                                                        .stream().parallel()
                                                        .map(entry -> UserKnowledge.builder().userUuid(userId).ukId(entry.getKey()).ukMastery(entry.getValue()).updateDate(now).build())
                                                        .collect(Collectors.toSet());
            userKnowledgeRepo.saveAll(userKnowledgeDbSet);
            log.debug("saved [{}]'s uk knowledge {}", userId, userKnowledgeDbSet.size());
        }


        //2021.11.04 - get the part mastery and save to stat table
        Map<String, Float> partMastery = partService.calculatePartMastery(inferResult.getMastery());
        userStatSvc.updateCustomUserStat(userId, UserStatisticsServiceBase.STAT_USER_PART_MASTERY_MAP, Statistics.Type.JSON, new Gson().toJson(partMastery));

        

        return new ResultMessageDTO(ResultMessage.SUCCESS.toString());
    }
    
    @Override
    public ResultMessageDTO updateMasteryFromLRS(String token) {
        String userID = JWTUtil.getUserID(token);
        return updateMasteryWithLRS(userID);
    }

    @Override
    public ResultMessageDTO updateMasteryWithLRS(String userID) {
        //Check if user of id exists
        if(!userRepo.findById(userID).isPresent()){
            log.error("User {} does not exist", userID);
            throw new UserNotFoundException(userID);
        }

        log.debug("update mastery from LRS {} v2 with type based kdb", userID);

        List<SourceType> sourceTypeList = SourceType.getAllSourceTypes();
        List<LRSStatementResultDTO> resultList =  lrsManager.getStatementList(userID, Arrays.asList(ActionType.SUBMIT), sourceTypeList);

        if(resultList.size() == 0){
            throw new RecommendException(RecommendErrorCode.LRS_NO_STATEMENT,  "No data found in LRS. check LRS status");
        }
        
        //Get appropriate probid and correct list
        //filter invalid fields
        resultList = resultList.stream().filter(statement -> statement.getSourceId() != null && statement.getIsCorrect() != null)
                                        .collect(Collectors.toList());


        List<String> probIdList = resultList.stream().map(LRSStatementResultDTO::getSourceId).collect(Collectors.toList());
        List<String> correctList = resultList.stream().map(LRSStatementResultDTO::getIsCorrect).map(c -> c.toString()).collect(Collectors.toList());
        
        return this.updateMastery(userID, probIdList, correctList);
    }
}
