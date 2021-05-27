package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.UserCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.Recommend.model.Curriculum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("StudyGuideServiceV0")
public class StudyGuideServiceV0 implements StudyGuideServiceBase{
    @Autowired
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    UserCurriculumRepo userCurrRepo;

    @Autowired
    ChapterServiceBase chapterSvc;

    @Override
    public StudyGuideDTO getStudyGuideOfUser(String userID) {
        List<Curriculum> currList = currInfoRepo.getCurriculumListByRangeSectionOnly("중등-중3-1학%");

        int currNumber = Math.max(currList.size() - 2 - 1, 0); //cut 1st and last + 0 start indexing
        

        // //pick 3 random number (힘들어서 그냥 0.3 범위헤서 3개 뽑음)
        // List<Integer> indexList = new ArrayList<>();
        // indexList.add(0);
        // indexList.add((int)Math.floor(currNumber * (Math.random() / 3.0)) );
        // indexList.add((int)Math.floor(currNumber * ( (1.0/3.0) + Math.random() / 3.0)) );
        // indexList.add((int)Math.floor(currNumber * ( (2.0/3.0) + Math.random() / 3.0)) );
        // indexList.add(currNumber);

        StudyGuideDTO guide = new StudyGuideDTO();

        //Create a blank array
        List<String> currIDList = new ArrayList<>();
        for(Curriculum curr: currList){
            currIDList.add(curr.getCurriculumId());
        }

        // for(Integer index: indexList){
        //     currIDList.add(currList.get(index).getCurriculumId());
        // }
        
        //가져온후 sorting
        List<ChapterDetailDTO> chapList = chapterSvc.getSpecificChapterListOfUser(userID, currIDList, "section");
        Collections.sort(chapList, (h1, h2) -> new Integer(h1.getSequence()).compareTo(h2.getSequence()) );
        
        //강제로 초기 값을 5로 맞춤
        final int INIT_SIZE = 5;
        if(chapList.size() > INIT_SIZE){
            int diff = chapList.size() - INIT_SIZE;

            for(int i =0; i < diff; i++)
                chapList.remove(1);
        }

        System.out.println(chapList.size());



        //가져온 List의 각 Chapter에서 UK중 수치를 정렬하여 hashmap에 정리
        Map<ChapterDetailDTO,String> ChapterWorstUKMap = new HashMap<>();
        for(ChapterDetailDTO chapDetail: chapList){
            //Get the UK list
            List<UKDetailDTO> ukList = chapDetail.getUkDetailList();

            //Sort by skillLevel
            Collections.sort(ukList, (uk1, uk2)-> new Double(uk1.getSkillScore()).compareTo(uk2.getSkillScore()));

            //Get the first UK
            System.out.println(chapDetail.getName() + ") worst UK : " + ukList.get(ukList.size()-1).toString());

            ChapterWorstUKMap.put(chapDetail, ukList.get(0).getId());
        }



        guide.setChapterDetailList(chapList);
        guide.setCommentary("다음 시험을 위해서는 선수개념에 대한 개념을 보충할 필요가 있어요! 와플수학에서 %s학생을 위한 맞춤 커리큘럼을 준비해 놓았으니 다음 시험에는 90점까지 상승 가능할거에요");


        // TODO Auto-generated method stub
        return guide;
    }
}
