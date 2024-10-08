package com.team12.order_delivery.deliveryRoute.client;

import com.team12.common.dto.hub.HubPathDetailsResponseDto;
import com.team12.common.dto.hub.HubPathOptimalRequestDto;
import com.team12.common.dto.hub.HubResponseDto;
import com.team12.common.dto.hub.ManagerResponseDto;
import com.team12.common.exception.response.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub", url = "http://localhost:19093/api")
public interface HubClient {
    @GetMapping("/hub-paths/findOptimalPath")
    List<HubPathDetailsResponseDto> findOptimalPath(@RequestBody HubPathOptimalRequestDto hubPathOptimalRequestDto);

    @GetMapping("/hubs/{hubId}")
    SuccessResponse<HubResponseDto> getHub(@PathVariable UUID hubId);
    @GetMapping("/managers/hub-to-hub")
    List<ManagerResponseDto> getHubToHubManagers();

    @GetMapping("/managers/hub-to-company")
    List<ManagerResponseDto> getHubToCompanyManagers(@RequestParam UUID hubId);

}
