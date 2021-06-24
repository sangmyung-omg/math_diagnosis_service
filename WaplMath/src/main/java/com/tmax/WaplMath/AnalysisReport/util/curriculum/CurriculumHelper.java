package com.tmax.WaplMath.AnalysisReport.util.curriculum;


import java.util.HashMap;
import java.util.Map;

import com.tmax.WaplMath.Common.exception.GenericInternalException;


/**
 * Helper class for curriculum ID calculation
 * @author Jonghyun Seong
 */
public class CurriculumHelper {
    //Enum LUT for school types
    public static enum SchoolLUT {
        PRIMARY("초등", "초", 0),
        MIDDLE("중등", "중", 1),
        HIGH("고등", "고", 2),
        UNIVERSITY("대학", "대", 3);

        private final String pre;
        private final String mid;
        private final int schoolType;

        private static final Map<Integer, SchoolLUT> lookup = new HashMap<>();
        private static final Map<String, SchoolLUT> prefixLookup = new HashMap<>();

        private SchoolLUT(String pre, String mid, int schoolType){
            this.pre = pre;
            this.mid = mid;
            this.schoolType = schoolType;
        }

        static {
            for(SchoolLUT sl: SchoolLUT.values()){
                lookup.put(sl.getSchoolType(), sl);
                prefixLookup.put(sl.getPre(), sl);
            }
        }

        public static SchoolLUT getLUTfromType(int schoolType){
            return lookup.get(schoolType);
        }
        
        public static SchoolLUT getLUTfromPrefix(String prefix){
            return prefixLookup.get(prefix);
        }

        public String getPre(){
            return this.pre;
        }

        public String getMid(){
            return this.mid;
        }

        public int getSchoolType(){
            return this.schoolType;
        }
    }

    private final static String SEMESTER_PREFIX = "학";

    public static String getCurriculumID(SchoolData data){
        return getCurriculumID(SchoolLUT.getLUTfromType(data.getSchoolType()), data.getGrade(), data.getSemester());
    }

    public static String getCurriculumID(SchoolLUT schoolType, int grade, int semester){
        //e.g.) 초등-초2-2학
        return String.format("%s-%s%d-%d%s", schoolType.getPre(), schoolType.getMid(), grade, semester, SEMESTER_PREFIX);
    }

    public static String getCurriculumID(SchoolLUT schoolType, int grade, int semester, int chapter){
        return getCurriculumID(schoolType, grade, semester) + String.format("-%02d", chapter);
    }

    public static String getCurriculumID(SchoolLUT schoolType, int grade, int semester, int chapter, int section){
        return getCurriculumID(schoolType, grade, semester, chapter) + String.format("-%02d", section);
    }
    public static String getCurriculumID(SchoolLUT schoolType, int grade, int semester, int chapter, int section, int subsection){
        return getCurriculumID(schoolType, grade, semester, chapter, section) + String.format("-%02d", subsection);
    }



    public static SchoolData increaseSemester(int schoolType, int grade, int semester){
        //Increase by 1
        semester++;

        //carry on condition
        if(semester > 2){
            semester = 1;
            grade++;
        }

        //carry on condition
        if(grade > 3){
            grade = 1;
            schoolType++;
        }

        //build school type from value
        return new SchoolData(schoolType, grade, semester);
    }
    public static SchoolData increaseSemester(SchoolData data){
        return increaseSemester(data.getSchoolType(), data.getGrade(), data.getSemester());
    }

    public static SchoolData decreaseSemester(int schoolType, int grade, int semester){
        //Increase by 1
        semester--;

        //carry on condition
        if(semester < 1){
            semester = 2;
            grade--;
        }

        //carry on condition
        if(grade < 1){
            schoolType--;

            //If primary school
            if(schoolType == 0)
                grade = 6;
            else if(schoolType == 4)
                grade = 4;
            else
                grade = 3;
        }

        //Exception handling
        if(schoolType < 0)
            throw new GenericInternalException("ERR-1001", "Invalid schoolType");

        //build school type from value
        return new SchoolData(schoolType, grade, semester);
    }
    public static SchoolData decreaseSemester(SchoolData data){
        return decreaseSemester(data.getSchoolType(), data.getGrade(), data.getSemester());
    }


    public static int getSchoolTypeFromPrefix(String prefixStr){
        return SchoolLUT.getLUTfromPrefix(prefixStr).getSchoolType();
    }

    //TODO: dummy stub for now
    public static boolean isCurriculumInRange(){
        return true;
    }




    //
    
}
