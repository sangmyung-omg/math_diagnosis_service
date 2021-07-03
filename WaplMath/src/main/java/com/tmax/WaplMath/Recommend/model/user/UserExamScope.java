package com.tmax.WaplMath.Recommend.model.user;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.curriculum.Curriculum;

import lombok.Data;

@Data
@Entity
@Table(name = "USER_EXAM_SCOPE")
public class UserExamScope {
	@Id
	private String userUuid;

	private String startSubSectionId;
	private String endSubSectionId;
	private String exceptSubSectionIdList;

	@OneToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name="userUuid", referencedColumnName="userUuid", insertable=false, updatable=false)
	private User user;
	
	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "startSubSectionId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum startCurriculum;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "endSubSectionId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum endCurriculum;
}
