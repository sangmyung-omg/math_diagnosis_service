package com.tmax.WaplMath.Common.model.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "USER_RECOMMEND_SCOPE")
public class UserRecommendScope {
  @Id
  private String userUuid;

  private String diagnosisScope;
  private String scheduleScope;

  @OneToOne
  @PrimaryKeyJoinColumn(name="userUuid", referencedColumnName = "userUuid")
  private User user;
}
