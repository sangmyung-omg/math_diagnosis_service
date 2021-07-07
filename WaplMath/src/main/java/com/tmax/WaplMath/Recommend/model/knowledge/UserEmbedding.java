package com.tmax.WaplMath.Recommend.model.knowledge;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.user.User;

import lombok.Data;

@Data
@Entity
@Table(name = "USER_EMBEDDING")
public class UserEmbedding {
	@Id
	private String userUuid;

	@Lob
	@Column(name = "USER_EMBEDDING")
	private String userEmbedding;

	private Timestamp updateDate;

	@OneToOne
	@JoinColumn(name = "userUuid", insertable = false, updatable = false)
	private User user;
}
