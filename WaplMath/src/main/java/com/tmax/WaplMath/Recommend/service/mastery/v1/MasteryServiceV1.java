package com.tmax.WaplMath.Recommend.service.mastery.v1;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.tmax.WaplMath.Common.exception.LRSStatementEmptyException;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.problem.Problem;
import com.tmax.WaplMath.Common.model.problem.ProblemUkRel;
import com.tmax.WaplMath.Common.model.redis.RedisObjectData;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.knowledge.UserKnowledgeRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import com.tmax.WaplMath.Common.util.auth.JWTUtil;
import com.tmax.WaplMath.Common.util.lrs.ActionType;
import com.tmax.WaplMath.Common.util.lrs.LRSManager;
import com.tmax.WaplMath.Common.util.lrs.SourceType;
import com.tmax.WaplMath.Common.util.redis.RedisIdGenerator;
import com.tmax.WaplMath.Common.util.redis.RedisUtil;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.ResultMessageDTO;
import com.tmax.WaplMath.Recommend.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Recommend.dto.mastery.TritonMasteryDTO;
import com.tmax.WaplMath.Recommend.event.mastery.MasteryEventPublisher;
import com.tmax.WaplMath.Recommend.exception.RecommendException;
import com.tmax.WaplMath.Recommend.repository.ProblemRepo;
import com.tmax.WaplMath.Recommend.repository.ProblemUkRelRepo;
import com.tmax.WaplMath.Recommend.repository.UserEmbeddingRepo;
import com.tmax.WaplMath.Recommend.util.MasteryAPIManager;
import com.tmax.WaplMath.Recommend.util.RecommendErrorCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("MasteryServiceV1")
public class MasteryServiceV1 implements MasteryServiceBaseV1{   
    //LRS API Manager
    @Autowired
    LRSManager lrsManager;

    @Autowired
    MasteryAPIManager masteryAPIManager;

    @Autowired
    @Qualifier("RE-UserEmbeddingRepo")
    UserEmbeddingRepo userEmbeddingRepo;

    @Autowired
    @Qualifier("RE-ProblemUkRelRepo")
    ProblemUkRelRepo probUkRelRepo;

    @Autowired
    @Qualifier("RE-ProblemRepo")
    ProblemRepo probRepo;
      
    @Autowired
    UserKnowledgeRepo userKnowledgeRepository;


    @Autowired
    UserRepo userRepo;

    @Autowired
    RedisUtil redisUtil;


    //Event publisher
    @Autowired
    MasteryEventPublisher masteryEventPublisher;

    enum ResultMessage {
        SUCCESS("Update successfull"),
        FAILED("Update failed");

        private String message;
        private ResultMessage(String message){this.message = message;}

        public String toString(){return this.message;}
    }


    //Scaler for uk mastery
    // private static final float SCALE_PARAM = 0.65079f;
    // private static final float BIAS_PARAM = 0.4f;
    // private static final float SCALE_THRESHOLD = 0.85f;
    
    private float correctUkMastery(float currentMastery){
        // return currentMastery >= SCALE_THRESHOLD ? 1.0f : (float)( SCALE_PARAM * Math.sqrt(currentMastery) + BIAS_PARAM);
        return currentMastery;
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

        log.info("Updating mastery for user " + userId);

        //Throw condition. Major error
        if (correctList.size() != probIdList.size()){
            throw new RecommendException(RecommendErrorCode.DATA_MISMATCH_ERROR,  "List size does not match");
        }
        
        //Pass condition check (any of the two lists' size == 0 or size doesn't match) => throw. must abort all other tasks
        if(probIdList.size() == 0 || correctList.size() == 0){
            throw new LRSStatementEmptyException(userId);
        }

        //Convert probIdList to IntList
        List<Integer> probIdIntList = new ArrayList<>();
        for(String probId : probIdList){probIdIntList.add(Integer.parseInt(probId));}

        //Build List difficulty and UK id from probIDList
        // log.info("ProbList data: " + probIdList.toString());
        List<ProblemUkRel> probUKList = probUkRelRepo.getByProblemIDList(probIdIntList);

        //order list by probIDList order (Map<Prob ID, List<UK ID> >) 
        Map<Integer, List<Integer>> probIDUKMap = new HashMap<>();

        //Fill the map
        probUKList.forEach(puk -> {
            //Declare list placeholder
            List<Integer> ukList = null;

            //If nothing exists
            if(!probIDUKMap.containsKey(puk.getProbId())){
                //Create new List and add current ukId
                ukList = new ArrayList<>();
            }
            else {
                //Get the existing UKList
                ukList = probIDUKMap.get(puk.getProbId());
            }
            
            ukList.add(puk.getUkId());
            probIDUKMap.put(puk.getProbId(), ukList);
        });
        
        //Get the difficulty info of problems
        List<Problem> probDataList = probRepo.getProblemsByProbIdList(probIdIntList);
        
        //Build probId --> info map
        Map<Integer, String> probDiffMap = new HashMap<>();
        probDataList.forEach(prob->probDiffMap.put(prob.getProbId(), prob.getDifficulty()));

        //Using the map + probIDList(ordered), build the lists required for the mastery api manager
        List<String> ukIDList = new ArrayList<>();
        List<String> ukCorrectList = new ArrayList<>();
        List<String> diffList = new ArrayList<>();

        //Build the list
        int index = 0;
        for(String probId : probIdList){
            //Current correctness and diff level
            String correct = correctList.get(index++);
            String diffLevel = probDiffMap.get(Integer.parseInt(probId));

            //Get the UK List of the probId
            List<Integer> ukList = probIDUKMap.get(Integer.parseInt(probId));

            //Log
            log.debug("correct: " +  correct + ", diff: " + diffLevel + ", probId: " + probId);

            if(ukList == null){
                continue;
            }

            //For each ukList. push the values to list            
            ukList.forEach(uk -> {
                ukIDList.add(uk.toString());
                ukCorrectList.add(correct);
                diffList.add(diffLevel);
            });
        };


        //Get the current embedding
        // UserEmbedding userEmbedding = userEmbeddingRepo.getEmbedding(userId);
        // String embeddingStr = userEmbedding != null ? userEmbedding.getUserEmbedding() : "";
        String embeddingStr = "";

        //Measure the current mastery
        TritonMasteryDTO tritonMastery = masteryAPIManager.measureMasteryDTO(userId, ukIDList, ukCorrectList, diffList, embeddingStr);


        //Update or insert the new mastery to DB 
        //userEmbeddingRepo.updateEmbedding(userId, tritonMastery.getEmbedding());
        userEmbeddingRepo.save(UserEmbedding.builder()
                                            .userUuid(userId)
                                            .userEmbedding(tritonMastery.getEmbedding())
                                            .updateDate(Timestamp.valueOf(LocalDateTime.now()))
                                            .build());
    

        //Save each UK data to UK table
        Set<UserKnowledge> updateUKSet = new HashSet<>();
        for(Map.Entry<Integer, Float> ukEntry: tritonMastery.getMastery().entrySet()){
            //Builder pattern for user knowledge
            UserKnowledge uknow = UserKnowledge.builder()
                                    .userUuid(userId)
                                    .ukId(ukEntry.getKey())
                                    .ukMastery(correctUkMastery(ukEntry.getValue()))
                                    .updateDate(Timestamp.valueOf(LocalDateTime.now()))
                                    .build();   

            updateUKSet.add(uknow);
        }
        userKnowledgeRepository.saveAll(updateUKSet);


        //Propagate update event to inform mastery update
        masteryEventPublisher.publishChangeEvent(userId);

        //Save redis key -> lifetime = 12hours = 12*60*60sec
        redisUtil.saveWithExpire(RedisObjectData.builder().id(redisID).data(1).build(), 12*60*60);

        return new ResultMessageDTO(ResultMessage.SUCCESS.toString());
    }

    @Override
    public ResultMessageDTO updateMasteryFromLRS(String token) {
        String userID = JWTUtil.getUserID(token);
        return updateMasteryWithLRS(userID);
    }

    /**
     * Added to get lrs and filter duplicates
     * @param userID
     * @since 2021-08-02
     * @author jonghyun seong
     * @return
     */
    private ProblemSolveListDTO getLrsWithoutDuplicate(String userID, List<LRSStatementResultDTO> resultList){
        //Result probIdList + correctList
        List<String> probIdList = new ArrayList<>();
        List<String> correctList = new ArrayList<>();

        //Set to save identity(duplicate id set)
        Set<String> identitySet = new HashSet<>();
        for(LRSStatementResultDTO lrsStatement : resultList){
            //build id list
            String identity = buildIdentityString(lrsStatement);

            //If in identity (duplicate continue and skip)
            if(identitySet.contains(identity))
                continue;

            //Add if not exist
            identitySet.add(identity);

            // prob id and correct for this loop
            String probId = null;
            String correct = null;

            if(lrsStatement.getSourceId() == null){
                log.warn("Invalid source ID from LRS {}. user {}. skipping", lrsStatement.getSourceId(), userID);
                continue;
            }

            //Set probid = source id
            probId = lrsStatement.getSourceId();

            //Check correct . Priority. isCorrect value  -> userAnswer Pass
            //Check is correct
            if(lrsStatement.getIsCorrect() == null){
                log.warn("Invalid isCorrect value from LRS {}, user {}, skipping", lrsStatement.getIsCorrect(), userID);
                continue;
                
            }
            
            correct = lrsStatement.getIsCorrect() > 0 ? "true" : "false";

            //Check pass. pass is treated as false even if isCorrect > 0
            if(lrsStatement.getUserAnswer() != null && lrsStatement.getUserAnswer().equals("PASS")){
                correct = "false";
            }


            if(correct == null || probId == null){
                log.warn("One of correct, probId is invalid. skipping entry. {} {} . user {}", correct, probId, userID);
                continue;
            }


            //Add to list
            probIdList.add(probId);
            correctList.add(correct);
        }


        return ProblemSolveListDTO.builder().probIdList(probIdList).correctList(correctList).build();
    }

    private String buildIdentityString(LRSStatementResultDTO input){
        return String.format("%s/%s/%s/%s/%s/%s",   input.getUserId(),
                                                    input.getActionType(), 
                                                    input.getSourceType(), 
                                                    input.getSourceId(), 
                                                    input.getTimestamp(), 
                                                    input.getPlatform());
    }

    @Override
    public ResultMessageDTO updateMasteryWithLRS(String userID) {
        //Check userValidity
        Optional<User> user = userRepo.findById(userID);
        if(!user.isPresent()){
            log.error("User invalid {}. Cannot update mastery for unregistered user" , userID);
            throw new UserNotFoundException(userID);
        }
        
        
        log.debug("update mastery from LRS {}", userID);
        // ProblemSolveListDTO result =  lrsapiManager.getLRSUpdateProblemSequence(token);    
        
        //FIXME: duplicate filter temp.
        List<String> actionTypeList = ActionType.getAllActionTypes();
        List<String> sourceTypeList = SourceType.getAllSourceTypes();

        List<LRSStatementResultDTO> resultList =  lrsManager.getStatementList(userID, actionTypeList, sourceTypeList);
        ProblemSolveListDTO result = getLrsWithoutDuplicate(userID, resultList);

        return this.updateMastery(userID, result.getProbIdList(), result.getCorrectList());
    }
}
