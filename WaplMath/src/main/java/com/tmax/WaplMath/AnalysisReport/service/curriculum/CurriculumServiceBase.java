package com.tmax.WaplMath.AnalysisReport.service.curriculum;

import java.util.List;
import java.util.Set;

import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumDataDTO;

public interface CurriculumServiceBase {
    public CurriculumDataDTO getByIdList(String userID, List<String> currIDList, Set<String> excludeSet);


    public CurriculumDataDTO searchWithConditions(String userID, String searchTerm, String typeRange, String mode, String range, boolean subSearch, String order, Set<String> excludeSet);
       

    public CurriculumDataDTO searchByYear(String userID, String schoolType, String year, String typeRange, String order, Set<String> excludeSet);
    public CurriculumDataDTO searchRecent(String userID, Integer count, String castTo, String order, Set<String> excludeSet);
}
