package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.dto.UKDetailDTO;
import com.tmax.WaplMath.AnalysisReport.model.curriculum.UserMasteryCurriculum;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.repository.legacy.curriculum.UserCurriculumRepo;
import com.tmax.WaplMath.AnalysisReport.repository.user.UserInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Studyguide service v0 implementation
 * @author Jonghyun Seong
 */
@Service("StudyGuideServiceV0")
public class StudyGuideServiceV0 implements StudyGuideServiceBase{
    @Autowired
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    UserCurriculumRepo userCurrRepo;

    @Autowired
    @Qualifier("ChapterServiceV0")
    ChapterServiceBase chapterSvc;


    @Autowired
    @Qualifier("AR-UserInfoRepo")
    UserInfoRepo userInfoRepo;

    
    @Override
    public StudyGuideDTO getStudyGuideOfUser(String userID) {
        List<Curriculum> currList = currInfoRepo.getCurriculumListByRangeSectionOnly("중등-중3-1학%");

        // int currNumber = Math.max(currList.size() - 2 - 1, 0); //cut 1st and last + 0 start indexing
        

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
        // List<ChapterDetailDTO> chapList = chapterSvc.getAllChapterListOfUser(userID, currIDList, "section");
        List<ChapterDetailDTO> chapList = chapterSvc.getAllChapterListOfUser(userID);

        Collections.sort(chapList, (h1, h2) -> new Integer(h1.getSequence()).compareTo(h2.getSequence()) );
        
        //강제로 초기 값을 5로 맞춤
        final int INIT_SIZE = 5;
        if(chapList.size() > INIT_SIZE){
            int diff = chapList.size() - INIT_SIZE;

            for(int i =0; i < diff; i++)
                chapList.remove(1);
        }

        // System.out.println(chapList.size());



        //선별한 currID에 따라 chapList 삽입 index를 저장하는 map
        // Map<String,Integer> currIDListIndexMap = new HashMap<>();
        Map<Integer,ChapterDetailDTO> indexCurrIDMap = new HashMap<>();

        for(int index = 0; index < chapList.size(); index++){
            ChapterDetailDTO chapDetail = chapList.get(index);

            //Get the UK list
            List<UKDetailDTO> ukList = chapDetail.getUkDetailList();

            //Sort by skillLevel
            Collections.sort(ukList, (uk1, uk2)-> new Double(uk1.getSkillScore()).compareTo(uk2.getSkillScore()));

            //Get the first UK
            // System.out.println(chapDetail.getName() + ") worst UK : " + ukList.get(0).toString());

            //If worst is below 0.5
            if(ukList.get(0).getSkillScore() > 0.53)
                continue;

            List<UserMasteryCurriculum> ukcurrList = userCurrRepo.getUserCurriculumOfPreUKWithUkID(userID, ukList.get(0).getId());

            // List<String> extraCurrIDList = new ArrayList<>();
            // System.out.println("test: " + ukList.get(0).getId()  + " || " + ukcurrList.toString());
            for(UserMasteryCurriculum ukmas: ukcurrList){
                if(ukmas.getCurriculumId().contains("중등-중3-")){
                    // System.out.println(ukmas.getCurriculumId() +" 중3 패스함");
                    continue;
                }

                // extraCurrIDList.add(ukmas.getCurriculumId()); //일단 하나만 넣자
                // System.out.println(ukmas.getCurriculumId() +"해야함");

                //ChapDTO 가져옴
                // List<ChapterDetailDTO> chap = chapterSvc.getSpecificChapterListOfUser(userID, Arrays.asList(ukmas.getCurriculumId()), "section");
                List<ChapterDetailDTO> chap = chapterSvc.getAllChapterListOfUser(userID);

                // System.out.println("DDDD" + chap);

                indexCurrIDMap.put(index, chap.get(0));
                break;
            }
        }

        // System.out.println(indexCurrIDMap.toString());

        //무조건 순서대로 되므로 넣을때마다 +1식해서 넣자
        int insertOffset=0;
        for(Map.Entry<Integer,ChapterDetailDTO> entry : indexCurrIDMap.entrySet()){
            ChapterDetailDTO chap = entry.getValue();
            chap.setType("보충-" + chap.getType());
            
            //하나 넣으니 offset 추가하면서 입력
            chapList.add(entry.getKey() + insertOffset++, chap);
        }   


        guide.setChapterDetailList(chapList);


        //Get commentary
        User data = userInfoRepo.getUserInfoByUUID(userID);
        // guide.setCommentary("다음 시험을 위해서는 선수개념에 대한 개념을 보충할 필요가 있어요! 와플수학에서 %s학생을 위한 맞춤 커리큘럼을 준비해 놓았으니 다음 시험에는 90점까지 상승 가능할거에요");
        guide.setCommentary(String.format("다음 시험을 위해서는 선수개념에 대한 개념을 보충할 필요가 있어요! 와플수학에서 %s학생을 위한 맞춤 커리큘럼을 준비해 놓았으니 다음 시험에는 잘 할 수 있을꺼에요!",
            data.getName()        
        ));

        return guide;
    }
}
