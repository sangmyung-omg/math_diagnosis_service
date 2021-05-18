package com.tmax.Recommend.dao;

import java.io.Serializable;

import lombok.Data;

@Data
public class UkRelKey implements Serializable {
	String baseUkUuid;
	String preUkUuid;
	String relationReference;
}
