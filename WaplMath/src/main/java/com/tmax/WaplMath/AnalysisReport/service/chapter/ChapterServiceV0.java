package com.tmax.WaplMath.AnalysisReport.service.chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.ChapterIDListDTO;
import com.tmax.WaplMath.AnalysisReport.dto.SkillStatDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
@Qualifier("v0")
public class ChapterServiceV0 implements ChapterServiceBase{
    @Override
    public List<ChapterDetailDTO> getAllChapterListOfUser(String userID) {
        List<ChapterDetailDTO> outputList = new ArrayList<ChapterDetailDTO>();

        for(int i=0; i < 10; i++){
            ChapterDetailDTO data = new ChapterDetailDTO();
            data.setId(UUID.randomUUID().toString());
            data.setImagePath("/dummy.png");
            data.setName("단원 더미 " + i + "번");
            

            SkillStatDTO skill = new SkillStatDTO();
            skill.setAverage(70.9 + i*0.3);
            skill.setTop10Tier(80.1 + i*0.8);
            skill.setUser(75.4 + i*0.9);

            data.setSkillData(skill);

            data.setType("중단원");


            List<UKDetailDTO> uklist = new ArrayList<UKDetailDTO>();
            for(int j=0; j<3; j++){
                UKDetailDTO ukdetail = new UKDetailDTO();
                ukdetail.setId(UUID.randomUUID().toString());
                ukdetail.setName("UK-" + i + j);
                ukdetail.setSkillScore(14.2 * j);

                uklist.add(ukdetail);
            }
            data.setUkDetailList(uklist);
        }

        return outputList;
    }

    @Override
    public List<ChapterDetailDTO> getSpecificChapterListOfUser(String userID, ChapterIDListDTO chapterIDList) {
        // List<ChapterDetailDTO> outputList = new ArrayList<ChapterDetailDTO>();
        // return outputList;

        return this.getAllChapterListOfUser(userID);
    }
}
