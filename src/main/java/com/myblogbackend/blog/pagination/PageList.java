package com.myblogbackend.blog.pagination;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageList<T> {
    private int offset;
    private int limit;
//    private long totalRecords;
    private int totalPage;
    private List<T> records;
}