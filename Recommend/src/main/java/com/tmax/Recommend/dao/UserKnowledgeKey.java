package com.tmax.Recommend.dao;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserKnowledgeKey implements Serializable {
	String userUuid;
	String ukUuid;
}
