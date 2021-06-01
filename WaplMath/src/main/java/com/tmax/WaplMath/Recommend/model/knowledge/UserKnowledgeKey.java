package com.tmax.WaplMath.Recommend.model.knowledge;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserKnowledgeKey implements Serializable {
	String userUuid;
	Integer ukId;
}
