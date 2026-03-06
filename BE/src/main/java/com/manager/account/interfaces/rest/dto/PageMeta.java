package com.manager.account.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageMeta {
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
}
