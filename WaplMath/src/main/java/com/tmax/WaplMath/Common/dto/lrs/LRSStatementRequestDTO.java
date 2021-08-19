package com.tmax.WaplMath.Common.dto.lrs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LRSStatementRequestDTO {
    private List<String> actionTypeList;
    private List<String> sourceTypeList;
    private List<String> userIdList;
}