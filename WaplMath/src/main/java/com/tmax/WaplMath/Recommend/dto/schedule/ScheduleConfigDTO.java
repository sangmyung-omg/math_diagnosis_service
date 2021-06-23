package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class ScheduleConfigDTO {
	public List<CardConfigDTO> cardConfigList;
	public Set<String> addtlSubSectionIdSet;
}
