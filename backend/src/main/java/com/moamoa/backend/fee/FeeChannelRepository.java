package com.moamoa.backend.fee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeeChannelRepository extends JpaRepository<FeeChannel, UUID> {

    List<FeeChannel> findAllByOrderByBankNameAsc();
}
