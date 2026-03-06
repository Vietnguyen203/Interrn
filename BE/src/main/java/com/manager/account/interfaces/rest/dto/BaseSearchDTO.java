package com.manager.account.interfaces.rest.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseSearchDTO {
    @ApiModelProperty(notes = "Page Number", example = "0", required = true)
    int pageNumber = 0;
    @ApiModelProperty(notes = "Page Size", example = "10", required = true)
    int pageSize = 10;
}




