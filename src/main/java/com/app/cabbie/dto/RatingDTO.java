// Purpose: Data Transfer Object (DTO) for ride review/rating requests from API clients.
// Notes: Uses Lombok for boilerplate; validates rating range (1-5) and required fields via Jakarta annotations.

package com.app.cabbie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RatingDTO {

    @NotBlank
    private Long reviewerId;

    @NotBlank
    private Long targetId;

    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

}
