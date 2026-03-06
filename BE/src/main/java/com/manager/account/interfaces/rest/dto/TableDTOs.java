package com.manager.account.interfaces.rest.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class TableDTOs {

    @Getter
    @Setter
    public static class TableRequest {
        @NotBlank(message = "Table name is required")
        private String tableName;
        private String createdBy;
        private LocalDateTime createdAt;
        private String server;
    }

    @Getter
    @Setter
    public static class CopyItemsRequest {
        @NotBlank(message = "Source table is not blank")
        private String sourceTableId;

        @NotBlank(message = "Target table is not blank")
        private String targetTableId;
    }
}
