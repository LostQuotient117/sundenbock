package de.nak.iaa.sundenbock.dto;

import java.util.List;

public record PageDTO<T>(List<T> items, long total, int page, int pageSize) {
    public static <T> PageDTO<T> of(List<T> items, long total, int page, int pageSize) {
        return new PageDTO<>(items, total, page, pageSize);
    }
}
