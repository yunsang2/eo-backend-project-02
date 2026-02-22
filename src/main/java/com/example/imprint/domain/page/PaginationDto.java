package com.example.imprint.domain.page;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;

@Getter
@ToString
public class PaginationDto {
    private final long totalElements;
    private final int pageSize;
    private final int lastPage;
    private final int pageNumber;
    private final int pagesPerViewport;
    private final int startPage;
    private final int endPage;
    private final boolean prev;
    private final boolean next;

    private PaginationDto(Pageable pageable, long totalElements, int lastPage, int pagesPerViewport) {
        this.totalElements = totalElements;
        this.pageSize = pageable.getPageSize();
        this.lastPage = lastPage;
        this.pageNumber = pageable.getPageNumber() + 1;
        this.pagesPerViewport = pagesPerViewport;

        int endPage = (int)(Math.ceil((double)pageNumber / pagesPerViewport)) * pagesPerViewport;
        int startPage = endPage - pagesPerViewport + 1;

        if (startPage <= 0) startPage = 1;
        if (lastPage < endPage) endPage = lastPage;

        this.startPage = startPage;
        this.endPage = endPage;

        this.prev = this.startPage > 1;
        this.next = this.endPage < lastPage;
    }

    public static PaginationDto of(Pageable pageable, long totalElements, int lastPage) {
        return new PaginationDto(pageable, totalElements, lastPage, 10);
    }

    public static PaginationDto of(Pageable pageable, long totalElements) {
        return new PaginationDto(pageable, totalElements,
                (int)(Math.ceil((double)totalElements / pageable.getPageSize())), 10);
    }
}