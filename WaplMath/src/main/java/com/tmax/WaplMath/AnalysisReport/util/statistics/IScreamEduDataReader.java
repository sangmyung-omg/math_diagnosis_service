package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
// import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import com.tmax.WaplMath.Common.model.uk.Uk;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.util.redis.RedisUtil;
import com.tmax.WaplMath.Recommend.repository.UkRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Mastery implements Serializable {
    @Builder.Default
    private Float mastery = 0.0f;

    @Builder.Default
    private int count = 0;

    @Builder.Default
    private Map<Integer, Float> map = new HashMap<>();

    public void add(Integer ukID, Float mastery) {
        this.count++;
        this.mastery += mastery;
        this.map.put(ukID, mastery);
    }

    //Method to update passively
    public void updateMastery() {
        this.count = map.size();
        this.mastery = 0.0f; //clear

        for(Map.Entry<Integer, Float> entry : this.map.entrySet()){
            this.mastery += entry.getValue();
        }
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class UserData {
    @Builder.Default
    private Float mastery = 0.0f;

    @Builder.Default
    private int count = 0;

    @Builder.Default
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

    //Method to update passively
    public void updateMastery() {
        this.count = map.size();
        this.mastery = 0.0f; //clear

        for(Map.Entry<String, Float> entry : this.map.entrySet()){
            this.mastery += entry.getValue();
        }
    }
}

@Data
@AllArgsConstructor
class UkLUT {
    private Integer id;
    private Integer map;
}


@Data
@AllArgsConstructor
class DataTable {
    private String username;
    private List<Float> ukMasteryList;
}

/**
 * Class to help read i-scream edu statistical data
 */
@Slf4j
@Component
@PropertySources({
	@PropertySource("classpath:statistics_module.properties"),
	@PropertySource(value="file:${external-config.url}/statistics_module.properties", ignoreResourceNotFound=true),
    @PropertySource("classpath:application.properties"),
    @PropertySource(value="file:${external-config.url}/application.properties", ignoreResourceNotFound=true),
})
public class IScreamEduDataReader {

    @Autowired
    @Qualifier("RE-UkRepo")
    private UkRepo ukRepository;

    private String externalConfigURL;

    @Autowired
    RedisUtil redisUtil;


    //Hyper param for iscream edu data calibration
    // private static final Double PARAM_SCALE = 0.65079;
    // private static final Double PARAM_BIAS = 0.4;

    private Map<String, Mastery> wholeData = null;
    private Map<Integer, UserData> ukData = null;


    //Uk LUT
    private Map<Integer, Integer> ukLUT = null;
    private Map<String, List<Float>> dataTable = null;

    //Public static map for year translation
    public static Map<Integer, Integer> yearTransLUT;
    static {
        yearTransLUT = new HashMap<>();
        yearTransLUT.put(7,1);
        yearTransLUT.put(8,2);
        yearTransLUT.put(9,3);
    }


    // @Value("${statistics.config.use_iscream_data}")
    private boolean useIScreamData;

    public enum Mode {
        EXACT("EXACT"),
        LIKELY("LIKELY");

        @Getter private final String value;

        private Mode(String value){
            this.value = value;
        }
    }


    public IScreamEduDataReader(@Value("${statistics.config.use_iscream_data}") boolean useIScreamData,
                                @Value("${external-config.url}") String externalConfigURL) {
        
        this.externalConfigURL = externalConfigURL;
        this.useIScreamData = useIScreamData;
    }

    public boolean useIScreamData(){
        return this.useIScreamData;
    }

    public void loadData() {
        if(useIScreamData){
            log.info("Using I-scream data");

            log.info("Load uk LUT and data table");
            this.loadUkLUT();
            this.loadDataTable();

            log.info("Parse all User mastery data");
            this.parseUserMasteryMap();

            log.info("Parse all UK data");
            this.parseUkMap();
        }
    }

    private void loadUkLUT() {
        //get UK LUT
        Path path = null;
        String filepathSuffix = "statistics/uk_lut.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.debug("File not found internally: "+ filepathSuffix);}

        if(path == null){ //external file read
            try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
            catch (FileNotFoundException e) {log.error("File also not found externally.: "+ filepathSuffix);}
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);
        
        this.ukLUT = result.entrySet().stream()
                            .parallel()
                            .map(entry -> new UkLUT(   Integer.parseInt(entry.getKey()) - 1, //for java index matching. lut file start from index 1 
                                                       Integer.parseInt(entry.getValue().getAsString()) 
                                                    ) 
                            ).collect(Collectors.toMap(UkLUT::getId, UkLUT::getMap));
        
        return;
    }

    private void loadDataTable() {
        //get UK LUT
        Path path = null;
        String filepathSuffix = "statistics/data_table.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.debug("File not found internally: "+ filepathSuffix);}

        if(path == null){ //external file read
            try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
            catch (FileNotFoundException e) {log.error("File also not found externally.: "+ filepathSuffix);}
        }

        FileReader reader = null;
        try {
            reader = new FileReader(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject result = (JsonObject)JsonParser.parseReader(reader);
        
        this.dataTable = result.entrySet().stream()
                                .parallel()
                                .map(entry -> {
                                    //Get ukList
                                    Type listType = new TypeToken<List<Float>>(){}.getType();
                                    List<Float> ukMasteryList = new Gson().fromJson(entry.getValue(), listType);

                                    return new DataTable(
                                                entry.getKey(),
                                                ukMasteryList
                                            );
                                })
                                .collect(Collectors.toMap(DataTable::getUsername, DataTable::getUkMasteryList));
        
        return;
    }

    private void parseUserMasteryMap() {
        //TODO: optimize flow more
        //Parse to Map<String, Mastery> . dataTable + ukLut
        this.wholeData = this.dataTable.entrySet().stream()
                             .parallel()
                             .collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
                                List<Float> masteryList = entry.getValue(); // +ukLUT will become the map
                                Map<Integer,Float> masteryMap = new HashMap<>();

                                IntStream.range(0, entry.getValue().size())
                                        .forEach(idx -> masteryMap.put(this.ukLUT.get(idx), masteryList.get(idx)));

                                Mastery mastery =  Mastery.builder()
                                                        .map(masteryMap)
                                                        .build();
                                mastery.updateMastery();
                                return mastery;
                             }));
        return;
    }

    private void parseUkMap() {
        //TODO: optimize flow more
        //Parse to Map<String, Mastery> . dataTable + ukLut
        this.ukData = this.ukLUT.entrySet().stream().parallel()
                          .collect(Collectors.toMap(entry -> entry.getValue(), entry -> {
                                int ukindex = entry.getKey();

                                UserData userdata =  new UserData();

                                for(Map.Entry<String,List<Float>> dataEntry : this.dataTable.entrySet()){
                                    userdata.add(dataEntry.getKey(),dataEntry.getValue().get(ukindex));
                                }                      

                                return userdata;
                          }));
        return;
    }
    /**
     * Return user year of iscream user from the iscream userID
     * @param userID
     * @return
     */
    public Integer getYearOfIScreamUser(String userID){
        int rawYear = Integer.valueOf(userID.substring(0, 1));

        return yearTransLUT.get(rawYear);
    }


    /**
     * Reads iscream data and makes it into UserKnowledge list with random userID
     * @return
     */
    public List<UserKnowledge> getByCurriculumID(String currID, Mode mode){
        log.debug(String.format("Creating Iscream-stat for [%s] in mode Curri-(%s)",currID, mode.getValue()));

        //Mode exception handling
        if(mode != Mode.EXACT && mode != Mode.LIKELY){
            throw new GenericInternalException(ARErrorCode.GENERIC_ERROR, "Invalid parameter. Curriculum ID mode is invalid");
        }

        //Get uk List of requested curriculum id
        Set<Integer> ukSet = mode == Mode.EXACT ? //Exact or likely. only two can exist
                             ukRepository.findByCurriculumId(currID).stream().parallel().map(Uk::getUkId).collect(Collectors.toSet()) :
                             ukRepository.findByLikelyCurriculumId(currID).stream().parallel().map(Uk::getUkId).collect(Collectors.toSet());

        //Get the whole data and make it into UserKnowlegdeList
        Map<String, Mastery> wholeData = getAllUserMasteryData();
        
        //Parallel optimization
        List<UserKnowledge> output = 
            wholeData.entrySet().stream()
                                // .parallel()
                                .flatMap(entry -> {
                                    String userID = entry.getKey();
                
                                    //For all uks create and push to userknowledge
                                    List<UserKnowledge> subout =
                                        entry.getValue()
                                            .getMap()
                                            .entrySet()
                                            .stream()
                                            // .parallel()
                                            .filter(ukentry -> ukSet.contains(ukentry.getKey()))
                                            .map(ukentry -> UserKnowledge.builder()
                                                                .userUuid(userID)
                                                                .ukId(ukentry.getKey())
                                                                .ukMastery(ukentry.getValue())
                                                                .user(User.builder()
                                                                            .userUuid(userID)
                                                                            .grade(getYearOfIScreamUser(userID).toString())
                                                                            .build() )
                                                                .build()
                                            )
                                            .collect(Collectors.toList());
                                    
                                    return subout.stream();
                                })
                                .collect(Collectors.toList());

        return output;
    }

    public List<UserKnowledge> getByCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.EXACT);
    }

    public List<UserKnowledge> getByLikelyCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.LIKELY);
    }

    public List<UserKnowledge> getByUkID(Integer ukID){
        log.debug(String.format("Creating Iscream-stat for [%d] in uk search mode", ukID));

        //Get the whole data and make it into UserKnowlegdeList
        Map<Integer, UserData> ukData = getAllUKData();

        //Exception handling if ukID is unavailable;
        if(!ukData.containsKey(ukID)){
            log.debug("UKID [{}] not found in i-scream data", ukID);
            return new ArrayList<>();
        }

        //For all uks create and push to userknowledge
        List<UserKnowledge> output = 
            ukData.get(ukID).getMap().entrySet().stream().parallel()
                                .map(userentry -> {
                                    return UserKnowledge.builder()
                                                    .userUuid(userentry.getKey())
                                                    .ukId(ukID)
                                                    .ukMastery(userentry.getValue())
                                                    .build();
                                })
                                .collect(Collectors.toList());
        

        return output;
    }

    @Deprecated
    private Map<String, Mastery> getAllUserMasteryData() {
        //If cached data exists
        if(this.wholeData != null)
            return this.wholeData;

        //get Average uk mastery data
        Path path = null;
        String filepathSuffix = "statistics/compressed_user_uk_all.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.debug("File not found internally: "+ filepathSuffix);}

        if(path == null){ //external file read
            try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
            catch (FileNotFoundException e) {log.error("File also not found externally.: "+ filepathSuffix);}
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

    @Deprecated
    private Map<Integer, UserData> getAllUKData() {
        //If cached data exists
        if(this.ukData != null)
            return this.ukData;

        //get Average uk mastery data
        Path path = null;
        String filepathSuffix = "statistics/compressed_uk_user_data.json";
        try {path = ResourceUtils.getFile("classpath:" + filepathSuffix).toPath();}
        catch (FileNotFoundException e) {log.debug("File not found internally: "+ filepathSuffix);}

        if(path == null){ //external file read
            try {path = ResourceUtils.getFile("file:" + externalConfigURL + "/" + filepathSuffix).toPath();} 
            catch (FileNotFoundException e) {log.error("File also not found externally.: "+ filepathSuffix);}
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