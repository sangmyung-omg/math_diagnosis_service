package com.tmax.Recommend.dao;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="USER_EMBEDDING")
public class UserEmbeddingDAO {
	@Id
	private String userUuid;
	
	private String userEmbedding;
	private Timestamp updateDate;
}
