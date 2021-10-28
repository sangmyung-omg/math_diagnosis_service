package com.tmax.WaplMath.Recommend.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.uk.TypeUkRel;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.uk.TypeUkRelRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class UkMasterySimulator {
    @Autowired private TypeUkRelRepo typeUkRelRepo;

    private Map<Integer, Set<Integer>> typeUkMap = null; //Type id --> list of UkIDs
    private Map<Integer, Set<Integer>> ukTypeMap = null; //Uk id --> list of type IDs

    /**
     * Init type uk map in constructor
     */
    public UkMasterySimulator(TypeUkRelRepo typeUkRelRepo){
        this.typeUkRelRepo = typeUkRelRepo;
        this.createTypeUkMap();
    }


    /**
     * scheduled updater for type uk --> every midnight
     */
    @Scheduled(cron="0 0 0 * * *")
    private void scheduledTypeUkMapUpdater(){
        log.debug("scheduled type map builder");
        this.createTypeUkMap();
    }

    private void createTypeUkMap() {
        log.debug("Creating Type <--> Uk relation map");

        //Get all rel
        List<TypeUkRel> relationList = (List<TypeUkRel>)typeUkRelRepo.findAll();

        //Build list (one on one case)
        // this.typeUkMap = relationList.stream().parallel().collect(Collectors.toMap(TypeUkRel::getTypeId, TypeUkRel::getUkId));
        // this.ukTypeMap = relationList.stream().parallel().collect(Collectors.toMap(TypeUkRel::getUkId, TypeUkRel::getTypeId));

        //Build map (one to many case)
        Set<Integer> ukIdSet = relationList.stream().parallel().map(TypeUkRel::getUkId).collect(Collectors.toSet());
        Set<Integer> typeIdSet = relationList.stream().parallel().map(TypeUkRel::getTypeId).collect(Collectors.toSet());

        //Init map
        Map<Integer, Set<Integer>> typeUkMap = typeIdSet.stream().collect(Collectors.toMap(id -> id, id -> new HashSet<>()));
        Map<Integer, Set<Integer>>  ukTypeMap = ukIdSet.stream().collect(Collectors.toMap(id -> id, id -> new HashSet<>()));

        //Build map
        relationList.stream().forEach(rel -> {
            typeUkMap.get(rel.getTypeId()).add(rel.getUkId());
            ukTypeMap.get(rel.getUkId()).add(rel.getTypeId());
        });

        //save after all manupulation is done
        this.typeUkMap = typeUkMap;
        this.ukTypeMap = ukTypeMap;
    }

    /** 
     * Method to create mastery from given type mastery map
     * @param typeMasteryMap
     * @return
     */
    public Map<Integer, Float> simulatedUkMastery(Map<Integer, Float> typeMasteryMap){
        //Create output map object. saves all related list of mastery
        //Init map
        Map<Integer, List<Float>> ukMasteryListMap = ukTypeMap.keySet().stream().collect(Collectors.toMap(ukid -> ukid, ukid -> new ArrayList<Float>()));

        //Fill the masteryMap
        typeMasteryMap.entrySet().stream()
                      .forEach(entry -> {
                          //Get corresponding uk id
                          Set<Integer> ukIdSet = this.typeUkMap.get(entry.getKey());
                          if(ukIdSet == null || ukIdSet.size() == 0) return;

                          //Add to list
                          ukIdSet.forEach(ukId -> ukMasteryListMap.get(ukId).add(entry.getValue()) );
                      });

        
        //Reduce map to 
        Map<Integer, Float> outputUkMasteryMap = ukMasteryListMap.entrySet().stream().parallel()
                                                                 .collect(Collectors.toMap(
                                                                    Entry::getKey, //Key
                                                                    entry -> entry.getValue().stream().reduce(0.0f, Float::sum) / entry.getValue().size() //Reduce to average
                                                                 ));
                                                                 
        return outputUkMasteryMap;
    }

        /** 
     * Method to create type mastery from given uk mastery map
     * @param ukMasteryMap
     * @return
     */
    public Map<Integer, Float> simulatedTypeMastery(Map<Integer, Float> ukMasteryMap){
        //Create output map object. saves all related list of mastery
        //Init map
        Map<Integer, List<Float>> typeMasteryListMap = typeUkMap.keySet().stream().collect(Collectors.toMap(typeid -> typeid, typeid -> new ArrayList<Float>()));

        //Fill the masteryMap
        ukMasteryMap.entrySet().stream()
                      .forEach(entry -> {
                          //Get corresponding uk id
                          Set<Integer> typeIdSet = this.ukTypeMap.get(entry.getKey());
                          if(typeIdSet == null || typeIdSet.size() == 0) return;

                          //Add to list
                          typeIdSet.forEach(typeId -> typeMasteryListMap.get(typeId).add(entry.getValue()) );
                      });

        
        //Reduce map to 
        Map<Integer, Float> outputTypeMasteryMap = typeMasteryListMap.entrySet().stream().parallel()
                                                                 .filter(entry -> entry.getValue().size() != 0)
                                                                 .collect(Collectors.toMap(
                                                                    Entry::getKey, //Key
                                                                    entry -> entry.getValue().stream().reduce(0.0f, Float::sum) / entry.getValue().size() //Reduce to average
                                                                 ));
                                                                 
        return outputTypeMasteryMap;
    }

    /**
     * Method to convert UserKnowledge to corresponding TypeKnowledge data
     * @param userKnowledgeList
     * @return
     */
    public List<TypeKnowledge> convertToTypeKnowledge(List<UserKnowledge> userKnowledgeList) {
        //For all types, average the given uk mastery

        //Build user -> userKnowledge list map (preinit with arraylist)
        Set<String> userIdList = userKnowledgeList.stream().parallel().map(UserKnowledge::getUserUuid).collect(Collectors.toSet());
        Map<String, Map<Integer, UserKnowledge> > userUkMasteryListMap = userIdList.stream().parallel()
                                                                .collect(Collectors.toMap(id->id, id -> new HashMap<>()));

        // Fill the user / userknowledge map
        Set<Integer> validUkIdSet = new HashSet<>();
        Map<String, User> userInfoMap = new HashMap<>();
        userKnowledgeList.forEach(uknow -> {
            validUkIdSet.add(uknow.getUkId());
            userUkMasteryListMap.get(uknow.getUserUuid()).put(uknow.getUkId(), uknow);
            userInfoMap.put(uknow.getUserUuid(), uknow.getUser());
        } 
        );


        //For each user
        List<TypeKnowledge> convertedList = this.typeUkMap.keySet().stream().parallel() //use type key set to get all types
                                                .flatMap(typeId -> {
                                                    //Get the uk set for the typeId
                                                    Set<Integer> ukIdSet = this.typeUkMap.get(typeId);
                                                    if(ukIdSet == null || ukIdSet.size() == 0) return Stream.empty();

                                                    //Build uk mastery list map for current type per user
                                                    List<TypeKnowledge> midKnowledgeList =
                                                        userIdList.stream().parallel()
                                                                    .map(
                                                                        userId -> {
                                                                            Float userMastery = ukIdSet.stream().filter(ukid -> validUkIdSet.contains(ukid))
                                                                                        .map( ukid -> userUkMasteryListMap.get(userId).get(ukid).getUkMastery() ) //get user's mastery of the ukid
                                                                                        .reduce(0.0f, Float::sum) / ukIdSet.size(); // Average of user's ukid mastery's sum
                                                                            return TypeKnowledge.builder()
                                                                                                .typeId(typeId)
                                                                                                .typeMastery(userMastery)
                                                                                                .userUuid(userId)
                                                                                                .user(userInfoMap.get(userId))
                                                                                                .build();
                                                                        }
                                                                    )
                                                                    .collect(Collectors.toList());

                                                    return midKnowledgeList.stream();
                                                })
                                                .collect(Collectors.toList());

        return convertedList;
    }
}
