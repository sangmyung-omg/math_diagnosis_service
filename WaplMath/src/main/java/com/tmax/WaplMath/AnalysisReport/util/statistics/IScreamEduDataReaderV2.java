package com.tmax.WaplMath.AnalysisReport.util.statistics;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tmax.WaplMath.AnalysisReport.util.error.ARErrorCode;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.problem.ProblemType;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.problem.ProblemTypeRepo;
import com.tmax.WaplMath.Common.util.redis.RedisUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Data
@AllArgsConstructor
class TypeLUT {
    private Integer id;
    private Integer map;
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
public class IScreamEduDataReaderV2 {

    @Autowired
    private ProblemTypeRepo problemTypeRepo;

    private String externalConfigURL;

    @Autowired
    RedisUtil redisUtil;


    //Hyper param for iscream edu data calibration
    // private static final Double PARAM_SCALE = 0.65079;
    // private static final Double PARAM_BIAS = 0.4;

    private Map<String, Mastery> wholeData = null;
    private Map<Integer, UserData> typeData = null;


    //type LUT
    private Map<Integer, Integer> typeLUT = null;
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


    public IScreamEduDataReaderV2(@Value("${statistics.config.use_iscream_data}") boolean useIScreamData,
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

            log.info("Load type LUT and data table");
            this.loadTypeLUT();
            this.loadDataTable();

            log.info("Parse all User mastery data");
            this.parseUserMasteryMap();

            log.info("Parse all Type data");
            this.parseTypeMap();
        }
    }

    private void loadTypeLUT() {
        //get Type LUT
        Path path = null;
        String filepathSuffix = "statistics/type_lut.json";
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
        
        this.typeLUT = result.entrySet().stream()
                            .parallel()
                            .map(entry -> new TypeLUT(   Integer.parseInt(entry.getKey()) - 1, //for java index matching. lut file start from index 1 
                                                       Integer.parseInt(entry.getValue().getAsString()) 
                                                    ) 
                            ).collect(Collectors.toMap(TypeLUT::getId, TypeLUT::getMap));
        
        return;
    }

    private void loadDataTable() {
        //get Type LUT
        Path path = null;
        String filepathSuffix = "statistics/type_data_table.json";
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
                                    //Get TypeList
                                    Type listType = new TypeToken<List<Float>>(){}.getType();
                                    List<Float> typeMasteryList = new Gson().fromJson(entry.getValue(), listType);

                                    return new DataTable(
                                                entry.getKey(),
                                                typeMasteryList
                                            );
                                })
                                .collect(Collectors.toMap(DataTable::getUsername, DataTable::getMasteryList));
        
        return;
    }

    private void parseUserMasteryMap() {
        //TODO: optimize flow more
        //Parse to Map<String, Mastery> . dataTable + TypeLUT
        this.wholeData = this.dataTable.entrySet().stream()
                             .parallel()
                             .collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
                                List<Float> masteryList = entry.getValue(); // +TypeLUT will become the map
                                Map<Integer,Float> masteryMap = new HashMap<>();

                                IntStream.range(0, entry.getValue().size())
                                        .forEach(idx -> masteryMap.put(this.typeLUT.get(idx), masteryList.get(idx)));

                                Mastery mastery =  Mastery.builder()
                                                        .map(masteryMap)
                                                        .build();
                                mastery.updateMastery();
                                return mastery;
                             }));
        return;
    }

    private void parseTypeMap() {
        //TODO: optimize flow more
        //Parse to Map<String, Mastery> . dataTable + TypeLUT
        this.typeData = this.typeLUT.entrySet().stream().parallel()
                          .collect(Collectors.toMap(entry -> entry.getValue(), entry -> {
                                int typeindex = entry.getKey();

                                UserData userdata =  new UserData();

                                for(Map.Entry<String,List<Float>> dataEntry : this.dataTable.entrySet()){
                                    userdata.add(dataEntry.getKey(),dataEntry.getValue().get(typeindex));
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
    public List<TypeKnowledge> getByCurriculumID(String currID, Mode mode){
        log.debug(String.format("Creating Iscream-stat for [%s] in mode Curri-(%s)",currID, mode.getValue()));

        //Mode exception handling
        if(mode != Mode.EXACT && mode != Mode.LIKELY){
            throw new GenericInternalException(ARErrorCode.GENERIC_ERROR, "Invalid parameter. Curriculum ID mode is invalid");
        }

        //Get part List of requested curriculum id
        Set<Integer> typeSet = mode == Mode.EXACT ? //Exact or likely. only two can exist
                             problemTypeRepo.findByCurriculumId(currID).stream().parallel().map(ProblemType::getTypeId).collect(Collectors.toSet()) :
                             problemTypeRepo.findByCurriculumIdStartsWith(currID).stream().parallel().map(ProblemType::getTypeId).collect(Collectors.toSet());
        
        //Parallel optimization
        List<TypeKnowledge> output = 
            this.wholeData.entrySet().stream()
                                // .parallel()
                                .flatMap(entry -> {
                                    String userID = entry.getKey();
                
                                    //For all types create and push to userknowledge
                                    List<TypeKnowledge> subout =
                                        entry.getValue()
                                            .getMap()
                                            .entrySet()
                                            .stream()
                                            // .parallel()
                                            .filter(typeentry -> typeSet.contains(typeentry.getKey()))
                                            .map(typeentry -> TypeKnowledge.builder()
                                                                .userUuid(userID)
                                                                .typeId(typeentry.getKey())
                                                                .typeMastery(typeentry.getValue())
                                                                .user(User.builder()
                                                                            .userUuid(userID)
                                                                            .grade(getYearOfIScreamUser(userID).toString())
                                                                            .build() )
                                                                .build()
                                            )
                                            .filter(typeknowledge -> typeknowledge.getTypeMastery() > 0) // Filter -1 values
                                            .collect(Collectors.toList());
                                    
                                    return subout.stream();
                                })
                                .collect(Collectors.toList());

        return output;
    }

    public List<TypeKnowledge> getByCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.EXACT);
    }

    public List<TypeKnowledge> getByLikelyCurriculumID(String currID){
        return getByCurriculumID(currID, Mode.LIKELY);
    }

    public List<TypeKnowledge> getByTypeID(Integer typeId){
        log.debug(String.format("Creating Iscream-stat for [%d] in type search mode", typeId));

        //Exception handling if typeID is unavailable;
        if(!this.typeData.containsKey(typeId)){
            log.debug("TYPEID [{}] not found in i-scream data", typeId);
            return new ArrayList<>();
        }

        //For all Types create and push to userknowledge
        List<TypeKnowledge> output = 
            this.typeData.get(typeId).getMap().entrySet().stream().parallel()
                                .map(typeentry -> {
                                    return TypeKnowledge.builder()
                                                    .userUuid(typeentry.getKey())
                                                    .typeId(typeId)
                                                    .typeMastery(typeentry.getValue())
                                                    .build();
                                })
                                .filter(typeknowledge -> typeknowledge.getTypeMastery() > 0 ) //filter -1 values
                                .collect(Collectors.toList());
        

        return output;
    }

    // private Float masteryCalibration(Float original){
    //     return original;
    //     // return (float) Math.min(PARAM_SCALE * Math.sqrt(original) + PARAM_BIAS, 1.0f);
    // }
}