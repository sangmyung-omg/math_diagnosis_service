package com.tmax.WaplMath.Recommend.dto.schedule;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleConfigDTO {
	public List<CardConfigDTO> cardConfigList;
	public Set<String> addtlSubSectionIdSet;
  public Boolean isScopeCompleted;
}
