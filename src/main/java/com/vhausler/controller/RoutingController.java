package com.vhausler.controller;

import com.vhausler.controller.dto.RouteResponse;
import com.vhausler.service.RoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("SpringMvcPathVariableDeclarationInspection") // intentional
@RestController
@RequestMapping("/routing")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @Operation(
            summary = "Find land route between two countries",
            description = """
                    Returns the shortest land route between origin and destination countries
                    using country border data. Countries are identified by their CCA3 code.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Route successfully found",
                            content = @Content(
                                    schema = @Schema(implementation = RouteResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "No land route exists or invalid country code",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> route(
            @Parameter(description = "Origin country CCA3 code", example = "CZE", required = true)
            @PathVariable("origin") String origin,

            @Parameter(description = "Destination country CCA3 code", example = "ITA", required = true)
            @PathVariable("destination") String destination) {

        List<String> path = routingService.findRoute(origin, destination);

        if (path == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(new RouteResponse(path));
    }
}
