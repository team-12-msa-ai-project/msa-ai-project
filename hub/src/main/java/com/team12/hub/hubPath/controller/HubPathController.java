package com.team12.hub.hubPath.controller;


import com.team12.common.dto.hub.HubPathDetailsResponseDto;
import com.team12.common.dto.hub.HubPathOptimalRequestDto;
import com.team12.common.exception.response.SuccessResponse;
import com.team12.hub.hubPath.dto.HubPathCreateRequestDto;
import com.team12.hub.hubPath.dto.HubPathResponseDto;
import com.team12.hub.hubPath.dto.HubPathSearchRequestDto;
import com.team12.hub.hubPath.dto.HubPathUpdateRequestDto;
import com.team12.hub.hubPath.service.HubPathService;
import com.team12.hub.util.SuccessMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/hub-paths")
@RestController
public class HubPathController {
    private final HubPathService hubPathService;

    @PostMapping
    public SuccessResponse<?> createHubPath(@RequestBody HubPathCreateRequestDto hubPathCreateRequestDto, @RequestHeader("X-User-Id") Long loginUserId) {
        HubPathResponseDto createdHubPath = hubPathService.createHubPath(hubPathCreateRequestDto, loginUserId);
        return SuccessResponse.success(SuccessMessage.CREATE_HUB_PATH.getHttpStatus().value(), SuccessMessage.CREATE_HUB_PATH.getMessage(), createdHubPath);
    }

    @PutMapping("/{hubPathId}")
    public SuccessResponse<?> updateHubPath(@PathVariable UUID hubPathId, @RequestBody HubPathUpdateRequestDto hubPathUpdateRequestDto, @RequestHeader("X-User-Id") Long loginUserId) {
        HubPathResponseDto updatedHubPath = hubPathService.updateHubPath(hubPathId, hubPathUpdateRequestDto, loginUserId);
        return SuccessResponse.success(SuccessMessage.UPDATE_HUB_PATH.getHttpStatus().value(), SuccessMessage.UPDATE_HUB_PATH.getMessage(), updatedHubPath);
    }

    @DeleteMapping("/{hubPathId}")
    public SuccessResponse<?> deleteHubPath(@PathVariable UUID hubPathId, @RequestHeader("X-User-Id") Long loginUserId) {
        UUID deletedHubPathId = hubPathService.deleteHubPath(hubPathId, loginUserId);
        return SuccessResponse.success(SuccessMessage.DELETE_HUB_PATH.getHttpStatus().value(), SuccessMessage.DELETE_HUB_PATH.getMessage(), deletedHubPathId);
    }

    @GetMapping("/{hubPathId}")
    public SuccessResponse<?> getHubPath(@PathVariable UUID hubPathId) {
        HubPathResponseDto hubPathResponseDto = hubPathService.getHubPath(hubPathId);
        return SuccessResponse.success(SuccessMessage.GET_HUB_PATH.getHttpStatus().value(), SuccessMessage.GET_HUB_PATH.getMessage(), hubPathResponseDto);
    }

    @GetMapping
    public SuccessResponse<?> getHubPaths(@RequestParam(required = false) UUID fromHubID,
                                                @RequestParam(required = false) UUID toHubID,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "fromHubId") String sort,
                                                @RequestParam(defaultValue = "asc") String direction
                                                ) {
        HubPathSearchRequestDto searchRequestDto = new HubPathSearchRequestDto(fromHubID, toHubID);
        // 정렬 순서 및 방향 설정
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Sort sortOption = Sort.by(sortDirection, sort);
        // size가 10, 20, 30이 아닌 경우 10으로 조정
        if (size != 10 && size != 20 && size != 30) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        List<HubPathResponseDto> cachedHubPaths = hubPathService.getHubPaths(searchRequestDto, pageable);
        // List를 다시 Page로 변환 (totalElements 사용하지 않음)
        Page<HubPathResponseDto> hubPathResponsDtoPage = hubPathService.convertListToPage(cachedHubPaths, pageable);

        return SuccessResponse.success(SuccessMessage.GET_HUB_PATHS.getHttpStatus().value(), SuccessMessage.GET_HUB_PATHS.getMessage(), hubPathResponsDtoPage);
    }

    @PostMapping("/findOptimalPath")
    public List<HubPathDetailsResponseDto> findOptimalPath(@RequestBody HubPathOptimalRequestDto hubPathOptimalRequestDto) {
        return hubPathService.findOptimalPath(hubPathOptimalRequestDto.getDepartureHubID(), hubPathOptimalRequestDto.getArrivalHubID());
    }
}
