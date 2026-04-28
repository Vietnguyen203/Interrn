package com.vietnl.sharedlibrary.core.result;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageResponse<T> {
  private List<T> data = new ArrayList<>();

  public PageResponse(Page<T> page) {
    data = page.getContent();
    metadata.page = page.getNumber() + 1;
    metadata.page_size = page.getSize();
    metadata.total = page.getTotalElements();
    metadata.total_page = page.getTotalPages();

    Metadata.Ranger ranger = metadata.getRanger();
    ranger.from = Math.toIntExact((metadata.getPage() - 1) * metadata.total + 1);
    ranger.to = Math.toIntExact(ranger.getFrom() + metadata.total - 1);
  }

  private Metadata metadata = new Metadata();

  @Data
  static class Metadata {

    private int page;
    private int page_size;
    private long total;
    private int total_page;
    private Ranger ranger = new Ranger();

    @Data
    static class Ranger {
      private int from;
      private int to;
    }
  }
}
