package com.moamoa.backend.fee;

import com.moamoa.backend.config.RestAccessDeniedHandler;
import com.moamoa.backend.config.RestAuthenticationEntryPoint;
import com.moamoa.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeesController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class FeesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeeChannelRepository feeChannelRepository;

    @MockitoBean
    private FeeTierRepository feeTierRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // 신한은행 인터넷 채널 하나: 500불 이하 5000원, 500불 초과 10000원 (전신료 8000원 별도)
    private void mockSingleChannel(long wireFee, int etaHours) {
        FeeChannel channel = new FeeChannel("신한은행", "인터넷", wireFee, etaHours);
        UUID channelId = UUID.randomUUID();
        ReflectionTestUtils.setField(channel, "id", channelId);
        List<FeeTier> tiers = List.of(
                new FeeTier(channelId, 500, 5000),
                new FeeTier(channelId, null, 10000)
        );
        when(feeChannelRepository.findAllByOrderByBankNameAsc()).thenReturn(List.of(channel));
        when(feeTierRepository.findByFeeChannelIdInOrderByMaxUsdAmountAscNullsLast(List.of(channelId))).thenReturn(tiers);
    }

    @Test
    void calculatesFeeWithinBoundedTierPlusWireFee() throws Exception {
        // amount=700000원 / 1400원 = 미화 500불 상당 -> 500불 이하 구간
        mockSingleChannel(8000, 2);

        mockMvc.perform(get("/api/fees?amount=700000").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channels[0].name").value("신한은행 인터넷"))
                .andExpect(jsonPath("$.data.channels[0].fee").value(13000))
                .andExpect(jsonPath("$.data.channels[0].eta_hours").value(2));
    }

    @Test
    void calculatesFeeUsingUnboundedTierWhenOverAllThresholds() throws Exception {
        // amount=1000000원 / 1400원 ≈ 미화 714불 상당 -> 500불 이하 구간을 넘어서 상한 없는 구간 적용
        mockSingleChannel(8000, 2);

        mockMvc.perform(get("/api/fees?amount=1000000").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channels[0].fee").value(18000));
    }

    @Test
    void excludesChannelWhenFeeWouldExceedTheAmountBeingSent() throws Exception {
        // 전신료(8000)+구간수수료(5000)=13000 >= 보내는 금액(1000) -> 배보다 배꼽이라 목록에서 제외
        mockSingleChannel(8000, 2);

        mockMvc.perform(get("/api/fees?amount=1000").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channels").isEmpty());
    }

    @Test
    void sortsMultipleChannelsByFeeNotByAlphabeticalBankNameOrder() throws Exception {
        // 알파벳 순서로는 가나은행이 먼저지만, 수수료는 다라은행이 더 저렴함 -
        // 응답이 알파벳 순서(가나,다라)가 아니라 수수료 순서(다라,가나)로 나와야 정렬 로직이 실제로 검증됨
        FeeChannel ganaBank = new FeeChannel("가나은행", "인터넷", 0, 2);
        FeeChannel daraBank = new FeeChannel("다라은행", "인터넷", 0, 2);
        UUID ganaBankId = UUID.randomUUID();
        UUID daraBankId = UUID.randomUUID();
        ReflectionTestUtils.setField(ganaBank, "id", ganaBankId);
        ReflectionTestUtils.setField(daraBank, "id", daraBankId);

        when(feeChannelRepository.findAllByOrderByBankNameAsc()).thenReturn(List.of(ganaBank, daraBank));
        when(feeTierRepository.findByFeeChannelIdInOrderByMaxUsdAmountAscNullsLast(List.of(ganaBankId, daraBankId)))
                .thenReturn(List.of(
                        new FeeTier(ganaBankId, null, 20000),
                        new FeeTier(daraBankId, null, 5000)));

        mockMvc.perform(get("/api/fees?amount=1000000").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channels[0].name").value("다라은행 인터넷"))
                .andExpect(jsonPath("$.data.channels[0].fee").value(5000))
                .andExpect(jsonPath("$.data.channels[1].name").value("가나은행 인터넷"))
                .andExpect(jsonPath("$.data.channels[1].fee").value(20000));
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        mockMvc.perform(get("/api/fees?amount=0").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/fees?amount=1000000"))
                .andExpect(status().isUnauthorized());
    }
}
