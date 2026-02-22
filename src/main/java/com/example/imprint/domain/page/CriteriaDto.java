package com.example.imprint.domain.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CriteriaDto {
    private int page = 1;
    private int size = 10;
}
