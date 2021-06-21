package com.tmax.WaplMath.AnalysisReport.util.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class struct to hold school academic data
 * @author Jonghyun Seong
 */
@Data
@AllArgsConstructor
public class SchoolData {
    private int schoolType;
    private int grade;
    private int semester;

    public String toString() {
        return String.format("%s-%d학년-%d학기", CurriculumHelper.SchoolLUT.getLUTfromType(this.schoolType).getPre(), grade, semester);
    }
}
