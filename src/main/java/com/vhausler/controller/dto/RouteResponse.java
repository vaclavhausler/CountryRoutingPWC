package com.vhausler.controller.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Land routing result")
public class RouteResponse {

    @ArraySchema(
            schema = @Schema(description = "Sequence of country CCA3 codes", example = "CZE")
    )
    private List<String> route;

    public RouteResponse(List<String> route) {
        this.route = route;
    }
}

