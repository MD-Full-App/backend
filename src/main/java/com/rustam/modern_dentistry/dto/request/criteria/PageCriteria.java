package com.rustam.modern_dentistry.dto.request.criteria;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.USE_DEFAULTS;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageCriteria {
    @Builder.Default
    @JsonInclude(USE_DEFAULTS)
    private Integer page = 0;

    @Builder.Default
    @JsonInclude(USE_DEFAULTS)
    private Integer count = 10;
}
