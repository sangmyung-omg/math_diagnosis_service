package com.tmax.WaplMath.AdditionalLearning.util.lrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import com.tmax.WaplMath.Recommend.util.LRSAPIManager;

@Component("AddLearn-LRSProblemRecord")
public class LRSProblemRecord {
	
	//조건에 맞는 문제 리스트를 반환하는 LRS 메소드
	@Autowired
	LRSAPIManager lrsAPIManager = new LRSAPIManager();
	
	private final Integer MAX_RECENET_STATEMENT_NUM = 300;
	
	public Set<Integer> getLRSProblemIdList(String userId, String dateTo, String dateFrom, List<String> sourceTypeList){
		Set<Integer> probIdSet = new HashSet<Integer>();
		GetStatementInfoDTO LRSinput = new GetStatementInfoDTO();
		List<StatementDTO> LRSResult;
		LRSinput.setUserIdList(new ArrayList<String>(Arrays.asList(userId)));
		LRSinput.setDateTo(dateTo);
		if (dateFrom != "")
			LRSinput.setDateFrom(dateFrom);
		LRSinput.setSourceTypeList(sourceTypeList);
		LRSinput.setActionTypeList(new ArrayList<String>(Arrays.asList("submit")));
		LRSinput.setRecentStatementNum(MAX_RECENET_STATEMENT_NUM);
		
		try {
			LRSResult = lrsAPIManager.getStatementListNew(LRSinput);
		} catch (Throwable  e) {
			throw new GenericInternalException("ERR-LRS-500", e.getMessage());
		}
		if (LRSResult.size() != 0) {
			for (StatementDTO statement : LRSResult) {
				String sourceId = statement.getSourceId();
				try {
					probIdSet.add(Integer.parseInt(sourceId));
				} catch (NumberFormatException e) {
					System.out.println(e.getMessage() + " is not number.");
				}
			}
		}
		return probIdSet;
	}

}
