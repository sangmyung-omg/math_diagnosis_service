package com.tmax.WaplMath.Common.model.uk;

import java.io.Serializable;

import lombok.Data;

@Data
public class UkRelKey implements Serializable {
	String baseUkId;
	String preUkId;
	String relationReference;
}
