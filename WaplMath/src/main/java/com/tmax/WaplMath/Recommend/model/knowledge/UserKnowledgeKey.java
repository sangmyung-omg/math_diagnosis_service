package com.tmax.WaplMath.Recommend.model.knowledge;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserKnowledgeKey implements Serializable {
	String userUuid;
	Integer ukId;
}
