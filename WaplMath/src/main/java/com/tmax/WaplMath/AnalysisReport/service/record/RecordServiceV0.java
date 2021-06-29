package com.tmax.WaplMath.AnalysisReport.service.record;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmax.WaplMath.AnalysisReport.dto.LRSGetStatementDTO;
// import com.tmax.WaplMath.AnalysisReport.dto.LRSGetStatementResponseDTO;
import com.tmax.WaplMath.AnalysisReport.dto.LevelDiagnosisRecordDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UserIDListDTO;

import org.springframework.context.annotation.Primary;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Record service v0 implementation
 * @author Jonghyun Seong
 */
@Service("RecordServiceV0")
@Primary
public class RecordServiceV0 implements RecordServiceBase{
    @Override
    public LevelDiagnosisRecordDTO getRecordOfUser(String userID) {
        


        RestTemplate restTemplate = new RestTemplate();
        LRSGetStatementDTO getStatementDTO = new LRSGetStatementDTO();
        
        List<String> sourceTypeList = new ArrayList<>();
        sourceTypeList.add("diagnosis");

        List<String> userIdList = new ArrayList<>();
        userIdList.add(userID);

        List<String> verbTypeList = new ArrayList<>();
        verbTypeList.add("submit");

        getStatementDTO.setSourceTypeList(sourceTypeList);
        getStatementDTO.setUserIdList(userIdList);
        getStatementDTO.setVerbTypeList(verbTypeList);

        URI lrsServer = URI.create("http://192.168.153.132:8080/StatementList");
        String result = null;
        
        try {
            restTemplate.postForObject(lrsServer, getStatementDTO, String.class);


            JsonArray resultArray = JsonParser.parseString(result).getAsJsonArray();

            System.out.println(resultArray.size());

            int countCorrect = 0;
            int countWrong = 0;
            int countPass = 0;
            int duration = 0;
        
            for(JsonElement eachElem : resultArray){
                JsonObject object = (JsonObject)eachElem;

                // System.out.println(object.toString());

                if(object.get("userAnswer").getAsString().equals("PASS")){
                    countPass++;
                    continue;
                }

                if(!object.get("duration").isJsonNull()){
                    if(object.get("duration").getAsInt() == 0){
                        duration++;
                        continue;
                    }
                }

                if(!object.get("isCorrect").isJsonNull()){
                    if(object.get("isCorrect").getAsInt() == 0){
                        countWrong++;
                        continue;
                    }
                    else
                        countCorrect++;
                }

                

            }

            // Dummy svc
            LevelDiagnosisRecordDTO output = new LevelDiagnosisRecordDTO();
            output.setNumCorrect(countCorrect);
            output.setNumDontknow(countPass);
            output.setNumWrong(countWrong);
            output.setTimeConsumed(duration);

            return output;

        }
        catch(Throwable e){
            //CASE: LRS server error
            return null;
        }
    }

    @Override
    public List<LevelDiagnosisRecordDTO> getRecordOfUserList(UserIDListDTO userIDList) {
        List<LevelDiagnosisRecordDTO> outputList = new ArrayList<LevelDiagnosisRecordDTO>();
        
        for (String userID : userIDList.getUserIDList()) {
            outputList.add(this.getRecordOfUser(userID));
        }

        return outputList;
    }
}
