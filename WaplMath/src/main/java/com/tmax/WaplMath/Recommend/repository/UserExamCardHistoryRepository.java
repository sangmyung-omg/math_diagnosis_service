package com.tmax.WaplMath.Recommend.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tmax.WaplMath.Recommend.model.recommend.UserExamCard;

public interface UserExamCardHistoryRepository extends CrudRepository<UserExamCard, String> {

	@Query(value = "select card_sequence from user_exam_curriculum_log where user_uuid=:userUuid order by card_sequence desc limit 1", nativeQuery = true)
	Optional<Integer> findLastCardSequenceByUserUuid(@Param("userUuid") String userUuid);

	@Query(value = "select card_sequence from user_exam_curriculum_log where user_uuid = :userUuid and card_type='보충' order by card_sequence desc limit 1", nativeQuery = true)
	Optional<Integer> findLastSupCardSequence(@Param("userUuid") String userUuid);

	@Query(value = "select * from user_exam_curriculum_log where user_uuid=:userUuid and recommended_date=:recommendedDate order by card_sequence asc", nativeQuery = true)
	List<UserExamCard> findAllByRecommendedDate(@Param("userUuid") String userUuid,
			@Param("recommendedDate") Timestamp targetDate);

	@Query(value="select section_id from user_exam_curriculum_log where user_uuid=:userUuid and card_type='중간평가' order by card_sequence asc",
			nativeQuery=true)
	List<String> findAllSectionId(@Param("userUuid") String userUuid);
	
	@Query(value = "select type_uk_uuid from user_exam_curriculum_log where user_uuid=:userUuid and card_type='유형' order by card_sequence asc", nativeQuery = true)
	List<String> findAllTypeUkUuid(@Param("userUuid") String userUuid);

	@Query(value = "select distinct cp.uk_uuid from user_exam_curriculum_log ue, card_problem_mapping cp where ue.user_uuid=:userUuid and ue.card_type='보충' and ue.card_id = cp.card_id", nativeQuery = true)
	List<String> findAllSupCardUkUuid(@Param("userUuid") String userUuid);

	/*
	@Query("select distinct uk "
				+ "from UserExamCardHistory ue, ExamCardProblem cp, UserKnowledge uk " 
			+ "where "
				+ "ue.userUuid=:userUuid and " 
				+ "ue.cardSequence>:supCardSequence and " 
				+ "ue.cardType='유형' and "
				+ "ue.cardId = cp.cardId and "
				+ "cp.ukUuid not in :recommendedUks and "
				+ "uk.ukUuid = cp.ukUuid and uk.userUuid = ue.userUuid and "
				+ "uk.ukMastery < :threshold " 
			+ "order by ue.cardSequence asc")
	List<UserKnowledge> findAllLowMasteryUkUuid(@Param("userUuid") String userUuid,
			@Param("supCardSequence") Integer supCardSequence,
			@Param("recommendedUks")List<String> supplementCardUkList, @Param("threshold") Float SUP_UK_MASTERY_THRESHOLD);
			*/
}
