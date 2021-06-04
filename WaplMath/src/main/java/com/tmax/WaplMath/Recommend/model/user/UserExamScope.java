package com.tmax.WaplMath.Recommend.model.user;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

	private String startSubSection;
	private String endSubSection;

	@OneToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name="userUuid", referencedColumnName="userUuid", insertable=false, updatable=false)
	private User user;
	
	@OneToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "startSubSection", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum startCurriculum;

	@OneToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "endSubSection", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum endCurriculum;
}
