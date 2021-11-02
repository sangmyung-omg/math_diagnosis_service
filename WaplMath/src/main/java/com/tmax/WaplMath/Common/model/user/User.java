package com.tmax.WaplMath.Common.model.user;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.knowledge.TypeKnowledge;
import com.tmax.WaplMath.Common.model.knowledge.UserEmbedding;
import com.tmax.WaplMath.Common.model.knowledge.UserKnowledge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USER_MASTER")
public class User {
	@Id
	private String userUuid;

	private String grade;
	private String semester;
	private String name;
	private String currentCurriculumId;

	@Column(name = "EXAM_TYPE")
	private String examType;

	@Column(name = "EXAM_START_DATE")
	private Timestamp examStartDate;

	@Column(name = "EXAM_DUE_DATE")
	private Timestamp examDueDate;

	@Column(name = "EXAM_TARGET_SCORE")
	private Integer examTargetScore;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<UserKnowledge> userKnowledgeList;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy="user", cascade=CascadeType.REMOVE, orphanRemoval=true)
	private UserEmbedding userEmbedding;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy="user", cascade=CascadeType.REMOVE, orphanRemoval=true)
	private UserExamScope userExamScope;

	@OneToOne(fetch = FetchType.LAZY, mappedBy="user", cascade=CascadeType.REMOVE, orphanRemoval=true)
	private UserRecommendScope userRecommendScope;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<TypeKnowledge> typeKnowledList;

	@ManyToOne
	@JoinColumn(name = "currentCurriculumId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum currentCurriculum;
}
