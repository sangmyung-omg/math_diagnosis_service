package com.tmax.WaplMath.Common.model.knowledge;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.tmax.WaplMath.Common.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "TYPE_KNOWLEDGE")
@IdClass(TypeKnowledgeKey.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeKnowledge {
	@Id
	private String userUuid;
	@Id
	private Integer typeId;

	private Float typeMastery;
	private Timestamp updateDate;
	
	@ManyToOne
	@JoinColumn(name = "userUuid", insertable = false, updatable = false)
	private User user;

	// @ManyToOne
	// @JoinColumn(name = "ukId", insertable = false, updatable = false)
	// private Uk uk;
}
