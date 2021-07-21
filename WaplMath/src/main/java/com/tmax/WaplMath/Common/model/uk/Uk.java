package com.tmax.WaplMath.Common.model.uk;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "UK_MASTER")
public class Uk {
	@Id
	private Integer ukId;

	private String ukName;
	private String ukDescription;
	private String trainUnseen;
	private String curriculumId;

	@ManyToOne(cascade = (CascadeType.ALL))
	@JoinColumn(name = "curriculumId", insertable = false, updatable = false)
	private Curriculum curriculum;
}
