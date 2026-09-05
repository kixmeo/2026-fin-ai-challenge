package com.moamoa.backend.fee.dto;

import java.util.List;

public record FeesResponse(List<FeeChannelResponse> channels) {
}
