package com.tmax.WaplMath.Recommend.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementRequestDTO;
import com.tmax.WaplMath.Common.dto.lrs.LRSStatementResultDTO;
import com.tmax.WaplMath.Common.exception.GenericInternalException;
import com.tmax.WaplMath.Recommend.dto.GetStatementInfoDTO;
import com.tmax.WaplMath.Recommend.dto.ProblemSolveListDTO;
import com.tmax.WaplMath.Recommend.dto.StatementDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Call StatementList GET API from LRS Server
 * 2021-10-16. Will be deprecated, use LRSManager instead.
 * @author Sangheon_lee
 */
@Slf4j
@Component
@PropertySources({
  @PropertySource("classpath:lrs.properties"),
  @PropertySource(value="file:${external-config.url}/lrs.properties", ignoreResourceNotFound=true),
})
public class LRSAPIManager {

  private String HOST;
  private String LRS_ADDR;

  // 2021-10-06 Added by Sangheon Lee. Set in-memory buffer size for fix DataBufferLimitException
  private final int MAX_BUFFER_SIZE = 1024 * 1024 * 50;

  private static final Integer timeout = 15; // sec

  @Autowired
  RestTemplate restTemplate;

  public GetStatementInfoDTO input;


  @Autowired
  public LRSAPIManager(@Value("${waplmath.recommend.lrs.host}") String host) {
    log.info("Using LRS server @ " + host);
    this.HOST = host;
    this.LRS_ADDR = String.format("%s/StatementList", this.HOST);
  }

  public LRSAPIManager() {
  }

  //Enums for statement
  //SourceType
  public enum SourceType {
    DIAGNOSIS("diagnosis"),
    DIAGNOSIS_SIMPLE("diagnosis_simple"),
    TYPE_QUESTION("type_question"), 
    SUPPLE_QUESTION("supple_question"), 
    SECTION_TEST_QUESTION("section_test_question"),
    CHAPTER_TEST_QUESTION("chapter_test_question"),
    ADDTL_SUPPLE_QUESTION("addtl_supple_question"),
    SECTION_EXAM_QUESTION("section_exam_question"),
    FULL_SCOPE_EXAM_QUESTION("full_scope_exam_question"),
    TRIAL_EXAM_QUESTION("trial_exam_question"),
    RETRY_QUESTION("retry_question"),
    WRONG_ANSWER_QUESTION("wrong_answer_question"),
    STARRED_QUESTION("starred_question");

    @Getter
    private String value;

    private SourceType(String value){
      this.value = value;
    }

    public static List<String> getAllSourceTypes(){
      return Stream.of(SourceType.values()).map(s -> s.getValue()).collect(Collectors.toList());
    }
  }

  //ActionType
  public enum ActionType {
    SUBMIT("submit"),
    START("start");

    @Getter
    private String value;

    private ActionType(String value){
      this.value = value;
    }

    public static List<String> getAllActionTypes(){
      return Stream.of(ActionType.values()).map(s -> s.getValue()).collect(Collectors.toList());
    }
  }

  public List<StatementDTO> getStatementListNew(GetStatementInfoDTO input) {
    //Create a http timeout handler
    try {
      log.info("LRS Input : {}", new ObjectMapper().writeValueAsString(input));
    } catch (JsonProcessingException e1) {
      e1.printStackTrace();
    }

    
    // 2021-10-06 Added by Sangheon Lee. Set in-memory buffer size for fix DataBufferLimitException
    ExchangeStrategies strategies = 
          ExchangeStrategies.builder()
                            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_BUFFER_SIZE))
                            .build();

    HttpClient httpClient = HttpClient.create()
                      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout * 1000)
                      .responseTimeout(Duration.ofMillis(timeout * 1000))
                      .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeout * 1000, TimeUnit.MILLISECONDS))
                      .addHandlerLast(new WriteTimeoutHandler(timeout * 1000, TimeUnit.MILLISECONDS)));
    //Create header
    WebClient webClient = WebClient.builder()
                     .baseUrl(HOST)
                     .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                     .clientConnector(new ReactorClientHttpConnector(httpClient))
                     .exchangeStrategies(strategies)
                     .build();

    //Call post to "/StatementList" LRS server --> get as String
    Mono<String> info = webClient.post()
         .uri("/StatementList")
         .body(Mono.just(input), GetStatementInfoDTO.class)
         .retrieve()
           .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
          .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
         .bodyToMono(String.class);
    //Convert output to result
    try {
      return Arrays.asList(new ObjectMapper().readValue(info.block(), StatementDTO[].class));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      log.warn("LRS return body : " + info.block());
      throw new GenericInternalException("ERR-LRS-501", "LRS return body cannot be parsed correctly");
    }
  }

  /**
   * Method to call update mastery lrs info from lrs
   * @author Jonghyun Seong
   * @since 2021-06-16
   */
  public ProblemSolveListDTO getLRSUpdateProblemSequence(String token) {
    //Create a http timeout handler
    HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout * 1000).responseTimeout(Duration.ofMillis(timeout * 1000))
      .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeout * 1000, TimeUnit.MILLISECONDS))
        .addHandlerLast(new WriteTimeoutHandler(timeout * 1000, TimeUnit.MILLISECONDS)));

    //Create header
    WebClient webClient = WebClient.builder().baseUrl(HOST).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .clientConnector(new ReactorClientHttpConnector(httpClient)).build();


    //Call post to "/InfoForMastery" LRS server --> get as String
    Mono<String> info = webClient.get().uri("/InfoForMastery").header("token", token).retrieve()
      .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
      .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
      .bodyToMono(String.class);

    //Convert output to result
    ProblemSolveListDTO result = null;
    try {
      result = new ObjectMapper().readValue(info.block(), ProblemSolveListDTO.class);
    } catch (Throwable e) {
      log.warn("LRS return body : " + info.block());
      throw new GenericInternalException("ERR-LRS-501", "LRS return body cannot be parsed correctly");
    }

    return result;
  }

  /**
   * Legacy diagnosis only statement getter
   * @param userID
   * @return
   */
  @Deprecated
  public List<LRSStatementResultDTO> getUserStatementDiagnosis(String userID) {
    return getUserStatement(userID, Arrays.asList("submit", "start"), Arrays.asList("diagnosis"));
  }

  /**
   * 
   * @param userID
   * @return
   */
  public List<LRSStatementResultDTO> getUserStatement(String userID, List<String> actionTypeList, List<String> sourceType) {
    //Create a http timeout handler
    HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).responseTimeout(Duration.ofMillis(5000))
      .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

    //Create header
    WebClient webClient = WebClient.builder().baseUrl(HOST).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .clientConnector(new ReactorClientHttpConnector(httpClient)).build();


    //Build the body
    LRSStatementRequestDTO body = LRSStatementRequestDTO.builder()
                              .actionTypeList(actionTypeList)
                              .sourceTypeList(sourceType)
                              .userIdList(Arrays.asList(userID))
                              .build();

    //Call post to "/InfoForMastery" LRS server --> get as String
    Mono<LRSStatementResultDTO[]> info = webClient.post().uri("/StatementList").bodyValue(body).retrieve()
                            .onStatus(HttpStatus::is4xxClientError, __ -> Mono.error(new GenericInternalException("ERR-LRS-400", "LRS 400 error")))
                            .onStatus(HttpStatus::is5xxServerError, __ -> Mono.error(new GenericInternalException("ERR-LRS-500", "LRS 500 error")))
                            .bodyToMono(LRSStatementResultDTO[].class);

    //Convert output to result
    List<LRSStatementResultDTO> result = Arrays.asList(info.block());


    return result;
  }


  
}
