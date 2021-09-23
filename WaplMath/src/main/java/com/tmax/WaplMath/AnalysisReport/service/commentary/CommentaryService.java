package com.tmax.WaplMath.AnalysisReport.service.commentary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

import com.tmax.WaplMath.AnalysisReport.dto.ChapterDetailDTO;
import com.tmax.WaplMath.AnalysisReport.dto.commentary.CommentaryDataDTO;
import com.tmax.WaplMath.AnalysisReport.dto.commentary.CommentaryResponseDTO;
import com.tmax.WaplMath.AnalysisReport.dto.curriculum.CurriculumSimpleDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.CorrectRateDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.PersonalScoreDTO;
import com.tmax.WaplMath.AnalysisReport.dto.statistics.SolveSpeedDTO;
import com.tmax.WaplMath.AnalysisReport.service.chapter.ChapterServiceV1;
import com.tmax.WaplMath.AnalysisReport.service.statistics.score.ScoreServiceBase;
import com.tmax.WaplMath.AnalysisReport.util.temp.CommentaryGenerator;
import com.tmax.WaplMath.Common.exception.UserNotFoundException;
import com.tmax.WaplMath.Common.model.curriculum.Curriculum;
import com.tmax.WaplMath.Common.model.user.User;
import com.tmax.WaplMath.Common.repository.curriculum.CurriculumRepo;
import com.tmax.WaplMath.Common.repository.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("CommentaryService")
public class CommentaryService {
    @Autowired
    ChapterServiceV1 chapterSvcv1;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ScoreServiceBase scoreSvc;

    @Autowired private CurriculumRepo currRepo;
    

    public CommentaryResponseDTO getCommentaryFromTemplate(String userID, String template, Set<String> excludeSet){
        Optional<User> userOpt = userRepo.findById(userID);
        if(!userOpt.isPresent()){
            throw new UserNotFoundException(userID);
        }
        
        CommentaryDataDTO data = getCommentaryData(userID,excludeSet);

        //Change
        String commentary = template.replace("${name}", userOpt.get().getName())
                                    .replace("${score}", Integer.toString((int)Math.floor(data.getScore().getScore())))
                                    .replace("${percentile}", Integer.toString((int)Math.floor(data.getScore().getPercentile())) )
                                    .replace("${solvespeed.comment}", CommentaryGenerator.getSpeedComment(data.getSolveSpeed().getSatisfyRate()))
                                    .replace("${correctrate.comment}", CommentaryGenerator.getCorrectComment(data.getCorrectRate().getCorrectrate()))
                                    .replace("${highpartlist}", data.getHighPartList().stream().map(curr -> curr.getName()).collect(Collectors.toList()).toString())
                                    .replace("${lowpartlist}", data.getLowPartList().stream().map(curr -> curr.getName()).collect(Collectors.toList()).toString());
                                    

        return CommentaryResponseDTO.builder().commentary(commentary).build();
    }

    public CommentaryDataDTO getCommentaryData(String userID, Set<String> excludeSet){
        Optional<User> userOpt = userRepo.findById(userID);
        if(!userOpt.isPresent()){
            throw new UserNotFoundException(userID);
        }

        PersonalScoreDTO score = scoreSvc.getUserScore(userID, excludeSet);
        CorrectRateDTO correctRate = scoreSvc.getCorrectRate(userID, excludeSet);
        SolveSpeedDTO solveSpeed = scoreSvc.getSolveSpeedRate(userID, excludeSet);

        List<CurriculumSimpleDTO> sortedList =  getSortedPartListV2(userOpt.get());

        Set<CurriculumSimpleDTO> lowList = new HashSet<>();
        Set<CurriculumSimpleDTO> highList = new HashSet<>();

        if(sortedList.size() <= 1) {
            lowList = null;
            highList = null;
        }
        else if(sortedList.size() >= 4){
            //Two from each side
            lowList.add(sortedList.get(0));
            lowList.add(sortedList.get(1));

            highList.add(sortedList.get(sortedList.size() - 2));
            highList.add(sortedList.get(sortedList.size() - 1));
        }
        else {
            lowList.add(sortedList.get(0));

            highList.add(sortedList.get(sortedList.size() - 1));
        }
        
        return CommentaryDataDTO.builder()
                                .score(score)
                                .correctRate(correctRate)
                                .solveSpeed(solveSpeed)
                                .lowPartList(new ArrayList<>(lowList))
                                .highPartList(new ArrayList<>(highList))
                                .build();
    }

    private List<CurriculumSimpleDTO> getSortedPartList(User user){
        //Get commentary
        String currentCurriculum = String.format("중등-중%s-%s학", user.getGrade(), user.getSemester());
        List<ChapterDetailDTO> resultListRaw = chapterSvcv1.getChapterListOfUserInRange(user.getUserUuid(), "year", currentCurriculum + "*partinc", true);

        //filter name
        Set<String> nameFilterSet = new HashSet<>();
        List<ChapterDetailDTO> resultList = new ArrayList<>();
        resultListRaw.stream().forEach(result -> {
            if(nameFilterSet.contains(result.getName())){
                return;
            }

            nameFilterSet.add(result.getName());
            resultList.add(result);
        });

        //sort list
        resultList.sort((a,b) -> new Double(a.getSkillData().getUser()).compareTo(b.getSkillData().getUser()) );

        return resultList.stream().map(chap -> CurriculumSimpleDTO.builder()
                                                                  .name(chap.getName())
                                                                  .id(chap.getId())
                                                                  .type(chap.getType())
                                                                  .seq(chap.getSequence())
                                                                  .build())
                                                                  .collect(Collectors.toList());
    }

    //TODO: Temporary solution
    private List<CurriculumSimpleDTO> getSortedPartListV2(User user){
        String currentCurriculum = String.format("중등-중%s", user.getGrade());//, user.getSemester());
        List<ChapterDetailDTO> resultListRaw = chapterSvcv1.getChapterListOfUserInRange(user.getUserUuid(), "year", currentCurriculum + "*partinc", true);

        //filter name without duplicates
        Set<String> idFilterSet = resultListRaw.stream().map(ChapterDetailDTO::getId).collect(Collectors.toSet());

        //Get full curr details. --> create currID part name map
        List<Curriculum> currList = ((List<Curriculum>)currRepo.findAllById(idFilterSet)).stream().filter(curr -> curr.getPart() != null).collect(Collectors.toList());
        Map<String, String> currIdPartMap = currList.stream().collect(Collectors.toMap(Curriculum::getCurriculumId, Curriculum::getPart));
        Set<String> partSet = currList.stream().map(Curriculum::getPart).collect(Collectors.toSet());

        //Remove duplicates
        List<ChapterDetailDTO> resultList = resultListRaw.stream().filter(result -> idFilterSet.contains(result.getId())).collect(Collectors.toList());

        //Average mastery to get part mastery. init by part map
        Map<String,  List<Double> > partMasteryMap = partSet.stream().collect(Collectors.toMap(key -> key, key -> new ArrayList<Double>()));
        resultList.stream().forEach(result -> {
            String partName = currIdPartMap.get(result.getId());
            partMasteryMap.get(partName).add(result.getSkillData().getUser());
        });


        //Reduce each part to average mastery
        Map<String, Double> partScoreMap = partMasteryMap.keySet().stream()
                                                         .collect(Collectors.toMap(key -> key, key -> { 
                                                                                                    List<Double> masteryList = partMasteryMap.get(key);
                                                                                                    return masteryList.stream().reduce(0.0, Double::sum) / masteryList.size();
                                                                                                }));


        //sort list
        List<CurriculumSimpleDTO> partListData = partSet.stream().map(part -> CurriculumSimpleDTO.builder().name(part).type("파트").build()).collect(Collectors.toList());
        Collections.sort(partListData, (a,b) -> partScoreMap.get(a.getName()).compareTo(partScoreMap.get(b.getName())));

        return partListData; 
        
        /*resultList.stream().map(chap -> CurriculumSimpleDTO.builder()
                                                                  .name(chap.getName())
                                                                  .id(chap.getId())
                                                                  .type(chap.getType())
                                                                  .seq(chap.getSequence())
                                                                  .build())
                                                                  .collect(Collectors.toList());
                                                                  */
    }
}
