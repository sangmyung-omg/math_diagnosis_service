package com.tmax.WaplMath.Common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericErrorDTO {
    private String errorCode = "ERR-1001";
    private String message = "Invalid Token";
}
