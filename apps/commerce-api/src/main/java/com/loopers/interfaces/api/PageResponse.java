package com.loopers.interfaces.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PageableInfo pageable;
    private final boolean last;
    private final int totalPages;
    private final long totalElements;
    private final int size;
    private final int number;
    private final SortInfo sort;
    private final boolean first;
    private final int numberOfElements;
    private final boolean empty;

    @JsonCreator
    public PageResponse(@JsonProperty("content") List<T> content,
                        @JsonProperty("pageable") PageableInfo pageable,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("totalElements") long totalElements,
                        @JsonProperty("size") int size,
                        @JsonProperty("number") int number,
                        @JsonProperty("sort") SortInfo sort,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("numberOfElements") int numberOfElements,
                        @JsonProperty("empty") boolean empty) {
        this.content = content;
        this.pageable = pageable;
        this.last = last;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.number = number;
        this.sort = sort;
        this.first = first;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                PageableInfo.from(page.getPageable()),
                page.isLast(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                SortInfo.from(page.getSort()),
                page.isFirst(),
                page.getNumberOfElements(),
                page.isEmpty()
        );
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof PageResponse<?> that)) return false;

        return last == that.last
                && totalPages == that.totalPages
                && totalElements == that.totalElements
                && size == that.size
                && number == that.number
                && first == that.first
                && numberOfElements == that.numberOfElements
                && empty == that.empty
                && Objects.equals(content, that.content)
                && Objects.equals(pageable, that.pageable)
                && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(content);
        result = 31 * result + Objects.hashCode(pageable);
        result = 31 * result + Boolean.hashCode(last);
        result = 31 * result + totalPages;
        result = 31 * result + Long.hashCode(totalElements);
        result = 31 * result + size;
        result = 31 * result + number;
        result = 31 * result + Objects.hashCode(sort);
        result = 31 * result + Boolean.hashCode(first);
        result = 31 * result + numberOfElements;
        result = 31 * result + Boolean.hashCode(empty);
        return result;
    }

    public record PageableInfo(int pageNumber, int pageSize) {
        @JsonCreator
        public PageableInfo(@JsonProperty("pageNumber") int pageNumber,
                            @JsonProperty("pageSize") int pageSize) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }

        public static PageableInfo from(Pageable pageable) {
            return new PageableInfo(pageable.getPageNumber(), pageable.getPageSize());
        }
    }

    public record SortInfo(boolean sorted, boolean unsorted, boolean empty) {
        @JsonCreator
        public SortInfo(@JsonProperty("sorted") boolean sorted,
                        @JsonProperty("unsorted") boolean unsorted,
                        @JsonProperty("empty") boolean empty) {
            this.sorted = sorted;
            this.unsorted = unsorted;
            this.empty = empty;
        }

        public static SortInfo from(org.springframework.data.domain.Sort sort) {
            return new SortInfo(sort.isSorted(), sort.isUnsorted(), sort.isEmpty());
        }
    }
}
