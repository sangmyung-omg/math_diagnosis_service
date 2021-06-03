package com.tmax.WaplMath.Recommend.dto;

import java.util.List;

import lombok.Data;

@Data
public class GetStatementInfoDTO {
	public List<String> actionTypeList;
	public String dateFrom;
	public String dateTo;
	public Integer recentStatementNum;
	public List<String> sourceTypeList;
	public List<String> userIdList;
}
