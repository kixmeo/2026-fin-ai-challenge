package com.moamoa.backend.fee;

import com.moamoa.backend.common.ApiException;
import com.moamoa.backend.common.ApiResponse;
import com.moamoa.backend.common.ErrorCode;
import com.moamoa.backend.fee.dto.FeeChannelResponse;
import com.moamoa.backend.fee.dto.FeesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fees")
public class FeesController {

    // 실시간 환율(F5)은 아직 안 붙어있어서, 수수료 구간(미화 환산액 기준) 판단에만 쓰는 고정 환율 추정치
    private static final double USD_TO_KRW_RATE = 1400.0;

    private final FeeChannelRepository feeChannelRepository;
    private final FeeTierRepository feeTierRepository;

    public FeesController(FeeChannelRepository feeChannelRepository, FeeTierRepository feeTierRepository) {
        this.feeChannelRepository = feeChannelRepository;
        this.feeTierRepository = feeTierRepository;
    }

    @GetMapping
    public ApiResponse<FeesResponse> getFees(@RequestParam long amount) {
        if (amount <= 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "amount는 0보다 커야 합니다.");
        }

        double usdEquivalent = amount / USD_TO_KRW_RATE;

        List<FeeChannel> channels = feeChannelRepository.findAllByOrderByBankNameAsc();
        Map<UUID, List<FeeTier>> tiersByChannelId = feeTierRepository
                .findByFeeChannelIdInOrderByMaxUsdAmountAscNullsLast(channels.stream().map(FeeChannel::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(FeeTier::getFeeChannelId));

        List<FeeChannelResponse> responses = channels.stream()
                .map(channel -> toResponse(channel, tiersByChannelId.getOrDefault(channel.getId(), List.of()), usdEquivalent, amount))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingLong(FeeChannelResponse::fee))
                .toList();

        return ApiResponse.success(new FeesResponse(responses));
    }

    private Optional<FeeChannelResponse> toResponse(FeeChannel channel, List<FeeTier> tiers, double usdEquivalent, long amount) {
        // 불변식: 시드 데이터의 모든 fee_channel은 max_usd_amount가 NULL인 "초과" 구간을 정확히 하나 가짐 (상한 없는 catch-all).
        // 이게 깨지면(구간 누락) 데이터 입력 실수이므로 여기서 바로 실패시켜 조용히 잘못된 값이 나가지 않게 함.
        long tierFee = tiers.stream()
                .filter(tier -> tier.getMaxUsdAmount() == null || usdEquivalent <= tier.getMaxUsdAmount())
                .findFirst()
                .map(FeeTier::getFee)
                .orElseThrow(() -> new IllegalStateException("fee_channel " + channel.getId() + "에 매칭되는 구간이 없습니다."));

        long totalFee = tierFee + channel.getWireFee();
        // 수수료가 보내는 금액과 같거나 더 크면(전신료만으로도 배보다 배꼽) 비교 목록에서 제외
        if (totalFee >= amount) {
            return Optional.empty();
        }

        String name = channel.getBankName() + " " + channel.getChannelType();
        return Optional.of(new FeeChannelResponse(name, totalFee, channel.getEtaHours()));
    }
}
