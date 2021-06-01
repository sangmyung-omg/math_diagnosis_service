package com.tmax.WaplMath.Recommend.model.recommend;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;
import com.tmax.WaplMath.Recommend.model.problem.ProblemType;
import com.tmax.WaplMath.Recommend.model.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER_EXAM_CARD")
public class UserExamCard {
	@Id
	private String cardId;

	private String userUuid;
	private String cardType;
	private String cardTitle;
	private String sectionId;
	private String typeId;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "userUuid", insertable = false, updatable = false)
	private User user;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "sectionId", insertable = false, updatable = false)
	private Curriculum section;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "typeId", insertable = false, updatable = false)
	private ProblemType problemType;
}
