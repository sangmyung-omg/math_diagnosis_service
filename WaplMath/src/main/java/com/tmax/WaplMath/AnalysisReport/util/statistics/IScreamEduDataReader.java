package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Recommend.model.user.User;
import com.tmax.WaplMath.Recommend.repository.UkRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@NoArgsConstructor
class Mastery {
    private Float mastery = 0.0f;
    private int count = 0;
    private Map<Integer, Float> map = new HashMap<>();

    public void add(Integer ukID, Float mastery) {
        this.count++;
        this.mastery += mastery;
        this.map.put(ukID, mastery);
    }

    public Float getAverage() {
        return this.mastery / this.count;
    }

    public Float getMastery(Integer ukID){
        return this.map.get(ukID);
    }

    public Map<Integer, Float> getMap() {
        return this.map;
    }
}

@NoArgsConstructor
class UserData {
    private Float mastery = 0.0f;
    private int count = 0;
    private Map<String, Float> map = new HashMap<>();

    public void add(String userID, Float mastery) {
        this.count++;
        this.mastery += mastery;
        this.map.put(userID, mastery);
    }

    public Float getAverage() {
        return this.mastery / this.count;
    }

    public Float getMastery(String userID){
        return this.map.get(userID);
    }

    public Map<String, Float> getMap() {
        return this.map;
    }
}

/**
 * Class to help read i-scream edu statistical data
 */
@Slf4j
@Component
@PropertySource("classpath:config/statistics_module.properties")
public class IScreamEduDataReader {

    @Autowired
    private UkRepository ukRepository;


    //Hyper param for iscream edu data calibration
    private static final Double PARAM_SCALE = 0.65079;
    private static final Double PARAM_BIAS = 0.4;

    private Map<String, Mastery> wholeData = null;
    private Map<Integer, UserData> ukData = null;

    //Public static map for year translation
    public static Map<Integer, Integer> yearTransLUT;
    static {
        yearTransLUT = new HashMap<>();
        yearTransLUT.put(7,1);
        yearTransLUT.put(8,2);
        yearTransLUT.put(9,3);
    }


    @Value("${statistics.config.use_iscream_data}")
    private boolean useIScreamData;

    public enum Mode {
        EXACT("EXACT"),
        LIKELY("LIKELY");

        @Getter private final String value;

        private Mode(String value){
            this.value = value;
        }
    }

    public boolean useIScreamData(){
        return this.useIScreamData;
    }

    /**
     * Return user year of iscream user from the iscream userID
     * @param userID
     * @return
     */
    public Integer getYearOfIScreamUser(String userID){
        int rawYear = Integer.valueOf(userID.substring(2, 3));

        return yearTransLUT.get(rawYear);
    }


    /**
     * Reads iscream data and makes it into UserKnowledge list with random userID
     * @return
     */
    public List<UserKnowledge> getByCurriculumID(String currID, Mode mode){
        log.info(String.format("Creating Iscream-stat for [%s] in mode Curri-(%s)",currID, mode.getValue()));
        
        //Get uk List of requested curriculum id
        Set<Integer> ukSet = new HashSet<>();

        if(mode == Mode.EXACT)
            ukRepository.findByCurriculumId(currID).forEach(uknow -> ukSet.add(uknow.getUkId()));
        else if(mode == Mode.LIKELY)
            ukRepository.findByLikelyCurriculumId(currID).forEach(uknow -> ukSet.add(uknow.getUkId()));
        else {
            throw new GenericInternalException(ARErrorCode.GENERIC_ERROR, "Invalid parameter. Curriculum ID mode is invalid");
        }

        //Get the whole data and make it into UserKnowlegdeList
        Map<String, Mastery> wholeData = getAllUserMasteryData();

        //Init output List
        List<UserKnowledge> output = new ArrayList<>();

        //For all users of iscream edu
        for(Map.Entry<String, Mastery> entry: wholeData.entrySet()){
            String userID = entry.getKey();
            
            //For all uks create and push to userknowledge
            for(Map.Entry<Integer, Float> ukentry : entry.getValue().getMap().entrySet()){
                //If uk ID is not in the searched ukSet, then continue
                if(!ukSet.contains(ukentry.getKey())){
                    continue;
                }

                output.add(UserKnowledge.builder()
                                        .userUuid(userID)
                                        .ukId(ukentry.getKey())
                                        .ukMastery(ukentry.getValue())
                                        .user(User.builder()
                                                  .userUuid(userID)
                                                  .grade(getYearOfIScreamUser(userID).toString())
                                                  .build() )
                                        .build());
            }
        }
        

        return output;
    }

    public List<UserKnowledge> getByCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.EXACT);
    }

    public List<UserKnowledge> getByLikelyCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.LIKELY);
    }

    public List<UserKnowledge> getByUkID(Integer ukID){
        log.info(String.format("Creating Iscream-stat for [%d] in uk search mode", ukID));

        //Get the whole data and make it into UserKnowlegdeList
        Map<Integer, UserData> ukData = getAllUKData();

        //Init output List
        List<UserKnowledge> output = new ArrayList<>();

        //For all users of iscream edu
        if(ukData.containsKey(ukID)){
            UserData data = ukData.get(ukID);

            //For all uks create and push to userknowledge
            for(Map.Entry<String, Float> userentry : data.getMap().entrySet()){
            
                output.add(UserKnowledge.builder()
                                        .userUuid(userentry.getKey())
                                        .ukId(ukID)
                                        .ukMastery(userentry.getValue())
                                        .build());
            }
        }
        else {
            log.warn(String.format("UKID[%s] not found in i-scream data", ukID));
        }
        

        return output;
    }

    private Map<String, Mastery> getAllUserMasteryData() {
        //If cached data exists
        if(this.wholeData != null)
            return this.wholeData;

        //get Average uk mastery data
        Path path = null;
        try {
            path = ResourceUtils.getFile("classpath:statistics/user_uk_all.json").toPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);

        //Prepare mastery map (userID, ukmastery data)
        Map<String, Mastery> output = new HashMap<>();

        //For all entry of result
        for( Map.Entry<String, JsonElement> entry :result.entrySet()){
            //For each user
            String userID = entry.getKey();

            //If key does not exist
            if(!output.containsKey(userID)){
                output.put(userID, new Mastery());
            }


            Mastery currentMastery = output.get(userID);

            JsonObject masteryJson = (JsonObject)entry.getValue();

            //For all mastery
            for(Map.Entry<String, JsonElement> ukentry : masteryJson.entrySet()){
                //Add to mastery struct
                currentMastery.add(Integer.parseInt(ukentry.getKey()), masteryCalibration(ukentry.getValue().getAsFloat()));
            }


            //Set mastery to key
            output.put(userID, currentMastery);
        }


        this.wholeData = output;
        return output;
    }

    private Map<Integer, UserData> getAllUKData() {
        //If cached data exists
        if(this.ukData != null)
            return this.ukData;

        //get Average uk mastery data
        Path path = null;
        try {
            path = ResourceUtils.getFile("classpath:statistics/uk_user_data.json").toPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);

        //Prepare mastery map (userID, ukmastery data)
        Map<Integer, UserData> output = new HashMap<>();

        //For all entry of result
        for( Map.Entry<String, JsonElement> entry :result.entrySet()){
            //For each user
            Integer ukID = Integer.parseInt(entry.getKey());

            //If key does not exist
            if(!output.containsKey(ukID)){
                output.put(ukID, new UserData());
            }


            UserData currentUser = output.get(ukID);

            JsonObject masteryJson = (JsonObject)entry.getValue();

            //For all mastery
            for(Map.Entry<String, JsonElement> userentry : masteryJson.entrySet()){
                //Add to mastery struct
                currentUser.add(userentry.getKey(), masteryCalibration(userentry.getValue().getAsFloat()));
            }


            //Set mastery to key
            output.put(ukID, currentUser);
        }

        this.ukData = output;
        return output;
    }

    private Float masteryCalibration(Float original){
        return original;
        // return (float) Math.min(PARAM_SCALE * Math.sqrt(original) + PARAM_BIAS, 1.0f);
    }
}