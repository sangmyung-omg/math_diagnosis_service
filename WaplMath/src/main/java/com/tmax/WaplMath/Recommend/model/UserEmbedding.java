package com.tmax.WaplMath.Recommend.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="USER_EMBEDDING")
public class UserEmbedding {
	@Id
	private String userUuid;
	
	@Lob
	@Column(name="USER_EMBEDDING")
	private String userEmbedding;
	
	private Timestamp updateDate;
}
