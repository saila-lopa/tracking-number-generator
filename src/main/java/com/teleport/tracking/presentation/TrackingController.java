package com.teleport.tracking.presentation;

import com.teleport.tracking.app.GetNextTrackingNumberUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/next-tracking-number")
public class TrackingController {

    GetNextTrackingNumberUseCase getNextTrackingNumberUseCase;

    @Autowired
    public TrackingController(GetNextTrackingNumberUseCase getNextTrackingNumberUseCase) {
        this.getNextTrackingNumberUseCase = getNextTrackingNumberUseCase;
    }

    @GetMapping
    public Mono<TrackingResponse> next(
            @Parameter(example = "MY") @RequestParam String origin_country_id,
            @Parameter(example = "ID") @RequestParam String destination_country_id,
            @Parameter(example = "2.211") @RequestParam String weight,
            @Parameter(example = "2018-11-20T19:29:32+08:00") @RequestParam String created_at,
            @Parameter(example = "4dcccfe6-fc76-4adc-84d0-067982c24805") @RequestParam String customer_id,
            @Parameter(example = "RedBox Logistics") @RequestParam String customer_name,
            @Parameter(example = "redbox-logistics") @RequestParam String customer_slug
    ) {
        TrackingRequest req = TrackingRequest.fromRequest(
                origin_country_id, destination_country_id, weight, created_at, customer_id, customer_name, customer_slug
        );
        return getNextTrackingNumberUseCase.generateTrackingNumber(req);
    }
}