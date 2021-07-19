package com.tmax.WaplMath.Recommend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetStatementInfoDTO {
	public List<String> actionTypeList;
	public String dateFrom;
	public String dateTo;
	public Integer recentStatementNum;
	public List<String> sourceTypeList;
	public List<String> userIdList;
}
