package com.tmax.WaplMath.AnalysisReport.repository.curriculum;

import java.util.List;

import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for curriculum data related to userID
 * @author Jonghyun Seong
 */
@Repository("AR-CurriculumInfoRepo")
public interface CurriculumInfoRepo extends CrudRepository<Curriculum, String> {
    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') order by curr.curriculumSequence")
    List<Curriculum> getCurriculumLikeId(@Param("currIdLike") String currIdLike);

    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') and curr.chapter is not null and curr.section is null and curr.subSection is null order by curr.curriculumSequence")
    List<Curriculum> getChaptersLikeId(@Param("currIdLike") String currIdLike);

    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') and curr.section is not null and curr.subSection is null order by curr.curriculumSequence")
    List<Curriculum> getSectionsLikeId(@Param("currIdLike") String currIdLike);

    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') and curr.subSection is not null order by curr.curriculumSequence")
    List<Curriculum> getSubSectionLikeId(@Param("currIdLike") String currIdLike);

    @Query("select curr from Curriculum curr where curr.curriculumId in :currIDList")
    List<Curriculum> getFromCurrIdList(@Param("currIDList") List<String> currIDList);

    @Query("select curr from Curriculum curr left outer join Uk uk on curr.curriculumId = uk.curriculumId left outer join UserKnowledge know on know.ukId = uk.ukId where know.userUuid = :userID order by curr.curriculumSequence")
    List<Curriculum> getAllCurriculumOfUser(@Param("userID") String userID);

    @Query("select curr from Curriculum curr left outer join Uk uk on curr.curriculumId = uk.curriculumId left outer join UserKnowledge know on know.ukId = uk.ukId where know.userUuid = :userID order by know.updateDate")
    List<Curriculum> getRecentCurriculumOfUser(@Param("userID") String userID);
}