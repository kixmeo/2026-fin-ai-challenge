package com.moamoa.backend.fee;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FeeTierRepository extends JpaRepository<FeeTier, UUID> {

    // FeesController가 채널마다 따로 조회하지 않고 한 번에 가져와서 그룹핑하도록 (N+1 방지)
    @Query("SELECT t FROM FeeTier t WHERE t.feeChannelId IN :feeChannelIds ORDER BY t.maxUsdAmount ASC NULLS LAST")
    List<FeeTier> findByFeeChannelIdInOrderByMaxUsdAmountAscNullsLast(@Param("feeChannelIds") Collection<UUID> feeChannelIds);
}
