package com.tmax.WaplMath.Recommend.dto.user;

import java.util.List;
import lombok.Data;

@Data
public class UserScheduleScopeInDTO {
  private Boolean toDefault;
  private List<String> sectionIdList;
}
