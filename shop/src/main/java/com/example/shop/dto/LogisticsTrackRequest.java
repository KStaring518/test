package com.example.shop.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LogisticsTrackRequest {
    private String location;
    private String description;
    private LocalDateTime trackTime;
}


