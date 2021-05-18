package com.tmax.Recommend.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="TYPE_UK_MASTER")
public class TypeUkDAO {
	@Id
	private String typeUkUuid;
	
	private String typeUkName;
	private String typeUkDescription;
	
	@Column(name="CURRICULUM_ID")
	private String curriculumId;
}
