package com.tmax.WaplMath.Common.model.knowledge;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeKnowledgeKey implements Serializable {
	String userUuid;
	Integer typeId;
}
