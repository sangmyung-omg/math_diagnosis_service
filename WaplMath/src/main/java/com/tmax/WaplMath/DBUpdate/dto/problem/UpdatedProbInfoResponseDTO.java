package com.tmax.WaplMath.DBUpdate.dto.problem;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedProbInfoResponseDTO {
		private String resultCode;
		private String resultMessage;
		private List<UpdatedProbInfoResponseBaseDTO> problems;
		
		public UpdatedProbInfoResponseDTO(String a, String b) {
			this.resultCode =a;
			this.resultMessage =b;
		}
}
