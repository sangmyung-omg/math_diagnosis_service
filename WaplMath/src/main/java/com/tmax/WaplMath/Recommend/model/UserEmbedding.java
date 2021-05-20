package com.tmax.WaplMath.Recommend.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="USER_EMBEDDING")
public class UserEmbedding {
	@Id
	private String userUuid;
	
	private String userEmbedding;
	private Timestamp updateDate;
}
