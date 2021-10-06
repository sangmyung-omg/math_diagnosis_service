package com.tmax.WaplMath.Common.util.lrs;

import java.util.List;

import com.tmax.WaplMath.Common.dto.lrs.LRSStatementRequestDTO;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;

public interface LRSManagerInterface {
    public List<LRSStatementResultDTO> getStatementList(LRSStatementRequestDTO lrsRequest);
    public List<LRSStatementResultDTO> getStatementList(String userID, List<ActionType> actionTypeList, List<SourceType> sourceTypeList);
    
    // 2021-10-06 Added by Sangheon Lee. 
    public List<LRSStatementResultDTO> getStatementList(String userID, List<ActionType> actionTypeList, List<SourceType> sourceTypeList, String dateFrom, String dateTo);
}
