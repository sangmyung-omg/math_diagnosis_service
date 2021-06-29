package com.tmax.WaplMath.DBUpdate.dto.problem;


import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedProblemListResponseDTO {
		private String resultCode;
		private String resultMessage;
		private List<String> acceptedProbIDs;
		private List<String> elseProbIDs;

		public UpdatedProblemListResponseDTO(String a, String b) {
			this.resultCode =a;
			this.resultMessage =b;
		}
}
