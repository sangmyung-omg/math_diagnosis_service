package com.tmax.WaplMath.Common.model.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
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

	@OneToOne
	@PrimaryKeyJoinColumn(name="userUuid", referencedColumnName="userUuid")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "startSubSectionId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum startCurriculum;

	@ManyToOne
	@JoinColumn(name = "endSubSectionId", referencedColumnName = "CURRICULUM_ID", insertable = false, updatable = false)
	private Curriculum endCurriculum;
}
