package com.teleport.tracking.presentation;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackingResponse {
    private final String tracking_number;
    private final String created_at;
}