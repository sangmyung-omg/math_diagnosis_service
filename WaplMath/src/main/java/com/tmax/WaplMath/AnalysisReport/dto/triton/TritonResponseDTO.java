package com.tmax.WaplMath.AnalysisReport.dto.triton;

import lombok.Data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TritonResponseDTO {
    private String id;
    private String model_name;
    private String model_version;
    private List<TritonDataDTO> outputs;
}
