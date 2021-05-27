package com.tmax.WaplMath.AnalysisReport.service.studyguide;

import java.util.List;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.StudyGuideDTO;
import com.tmax.WaplMath.AnalysisReport.repository.curriculum.CurriculumInfoRepo;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceBase;
import com.tmax.WaplMath.Recommend.model.Curriculum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("StudyGuideServiceV0")
public class StudyGuideServiceV0 implements StudyGuideServiceBase{
    @Autowired
    CurriculumInfoRepo currInfoRepo;

    @Autowired
    ChapterServiceBase chapterSvc;

    @Override
    public StudyGuideDTO getStudyGuideOfUser(String userID) {
        List<Curriculum> currList = currInfoRepo.getCurriculumListByRange("중등-중3-1학%");
        System.out.println(currList);


        StudyGuideDTO guide = new StudyGuideDTO();

        List<ChapterDetailDTO> chapList = chapterSvc.getAllChapterListOfUser(userID);

        guide.setChapterDetailList(chapList);
        guide.setCommentary("다음 시험을 위해서는 선수개념에 대한 개념을 보충할 필요가 있어요! 와플수학에서 %s학생을 위한 맞춤 커리큘럼을 준비해 놓았으니 다음 시험에는 90점까지 상승 가능할거에요");


        // TODO Auto-generated method stub
        return guide;
    }
}
