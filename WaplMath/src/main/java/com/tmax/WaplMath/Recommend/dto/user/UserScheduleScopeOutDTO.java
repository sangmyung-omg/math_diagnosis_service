package com.tmax.WaplMath.Recommend.dto.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScheduleScopeOutDTO {
	public String message;
  private List<String> sectionIdList;
}
