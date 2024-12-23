package com.skndan.rdp.config;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaginatedResponse<T> {
  private List<T> items;
  private int currentPage;
  private int pageSize;
  private long totalItems;
  private int totalPages;

  public PaginatedResponse(List<T> items, int currentPage, int pageSize, long totalItems) {
    this.items = items;
    this.currentPage = currentPage;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
    this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
  }
}
