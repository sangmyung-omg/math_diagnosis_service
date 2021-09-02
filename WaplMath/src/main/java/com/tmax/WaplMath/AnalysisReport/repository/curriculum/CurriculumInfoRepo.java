package com.tmax.WaplMath.AnalysisReport.repository.curriculum;

import java.util.List;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
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

    //2021-06-29 add parts search
    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') and curr.part is not null and curr.chapter is not null and curr.section is null and curr.subSection is null order by curr.curriculumSequence")
    List<Curriculum> getPartsLikeId(@Param("currIdLike") String currIdLike);

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


    //2021-06-29 add parts search
    @Query("select curr from Curriculum curr where curr.curriculumId like concat(:currIdLike,'%') and curr.part is not null order by curr.curriculumSequence")
    List<Curriculum> getPartsNotNullLikeId(@Param("currIdLike") String currIdLike);



    //2021-07-05 added by Jonghyun Seong. For querying ranged exam scope
    // Selects all curriculum in range by curr sequence range. then excluding the exclude ID List
    @Query("Select curr from Curriculum curr " + 
           "where curr.curriculumSequence >= (Select cs.curriculumSequence from Curriculum cs where cs.curriculumId = :currStart) and " +
           "curr.curriculumSequence <= (Select ce.curriculumSequence from Curriculum ce where ce.curriculumId = :currEnd) and " + 
           "curr.curriculumId not in (:excludeIdList)")
    List<Curriculum> getCurriculumInRange(@Param("currStart") String currIdStart, @Param("currEnd") String currIdEnd, @Param("excludeIdList") List<String> excludeIdList);


    //2021-07-15 get currID of problemID
    @Query("select pt.curriculumId from Problem prob INNER JOIN ProblemType pt on prob.problemType = pt.typeId where prob.probId=:probID")
    public String getCurrIdByProbId(@Param("probID") Integer probID);



    //2021-08-25 get currID all list (only the id for faster query)
    @Query("select curr.curriculumId from Curriculum curr")
    public List<String> getAllCurrID();
}