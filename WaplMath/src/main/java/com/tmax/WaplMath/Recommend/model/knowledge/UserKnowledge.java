package com.tmax.WaplMath.Recommend.model.knowledge;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tmax.WaplMath.Recommend.model.uk.Uk;
import com.tmax.WaplMath.Recommend.model.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "USER_KNOWLEDGE")
@IdClass(UserKnowledgeKey.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKnowledge {
	@Id
	private String userUuid;
	@Id
	private Integer ukId;

	private Float ukMastery;
	private Timestamp updateDate;
	
	@ManyToOne
	@JoinColumn(name = "userUuid", insertable = false, updatable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "ukId", insertable = false, updatable = false)
	private Uk uk;
}
