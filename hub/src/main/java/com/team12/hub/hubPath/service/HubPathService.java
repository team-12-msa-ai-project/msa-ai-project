package com.team12.hub.hubPath.service;

import com.team12.common.dto.hub.HubPathDetailsResponseDto;
import com.team12.common.exception.BusinessLogicException;
import com.team12.common.exception.ExceptionCode;
import com.team12.hub.hub.domain.Hub;
import com.team12.hub.hub.dto.HubResponseDto;
import com.team12.hub.hub.repository.HubRepository;
import com.team12.hub.hubPath.domain.HubNode;
import com.team12.hub.hubPath.domain.HubPath;
import com.team12.hub.hubPath.domain.HubPathSpecification;
import com.team12.hub.hubPath.dto.HubPathCreateRequestDto;
import com.team12.hub.hubPath.dto.HubPathResponseDto;
import com.team12.hub.hubPath.dto.HubPathSearchRequestDto;
import com.team12.hub.hubPath.dto.HubPathUpdateRequestDto;
import com.team12.hub.hubPath.repository.HubPathRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class HubPathService {

    private final HubPathRepository hubPathRepository;
    private final HubRepository hubRepository;
    private final KakaoNaviService kakaoNaviService;


    @Transactional
    @CachePut(value = "hubPath", key = "#result.id")
    @CacheEvict(value = "hubPathAll", allEntries = true)
    public HubPathResponseDto createHubPath(HubPathCreateRequestDto hubPathRequestDto, Long loginUserId) {
        Hub fromHub = hubRepository.findByIdAndIsDeleted(hubPathRequestDto.getFromHubId(), false)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.FROM_HUB_NOT_FOUND));
        Hub toHub = hubRepository.findByIdAndIsDeleted(hubPathRequestDto.getToHubId(), false)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.TO_HUB_NOT_FOUND));

        List<Integer> distanceAndDuration = kakaoNaviService.getDistanceAndDuration(fromHub.getLatitude(), fromHub.getLongitude(), toHub.getLatitude(), toHub.getLongitude());
        HubPath hubPath = new HubPath(UUID.randomUUID(), fromHub, toHub, distanceAndDuration.get(0), distanceAndDuration.get(1), false);
        hubPath.setCreatedBy(loginUserId);
        hubPathRepository.save(hubPath);
        HubPathResponseDto hubPathResponseDto = new HubPathResponseDto(hubPath);
        return hubPathResponseDto;
    }
    @Transactional
    @CacheEvict(value = {"hubPath", "hubPathAll"}, allEntries = true)
    @CachePut(value = "hubPath", key = "#hubPathId")
    public HubPathResponseDto updateHubPath(UUID hubPathId, HubPathUpdateRequestDto hubPathRequestDto, Long loginUserId) {
        HubPath hubPath = hubPathRepository.findById(hubPathId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HUB_PATH_NOT_FOUND));
        if (hubPathRequestDto.getDistance() != null){
            hubPath.setDistance(hubPathRequestDto.getDistance());
        }
        if (hubPathRequestDto.getDuration() != null){
            hubPath.setDuration(hubPathRequestDto.getDuration());
        }
        hubPath.setUpdatedBy(loginUserId);
        hubPathRepository.save(hubPath);
        return new HubPathResponseDto(hubPath);
    }


    @Transactional
    @CacheEvict(value = {"hubPath", "hubPathAll"}, allEntries = true)
    public UUID deleteHubPath(UUID hubPathId, Long loginUserId) {

        HubPath hubPath = hubPathRepository.findByIdAndIsDeleted(hubPathId, false)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HUB_PATH_NOT_FOUND));
        hubPath.setIsDeleted(true);
        hubPath.setDeletedAt(LocalDateTime.now());
        hubPath.setDeletedBy(loginUserId);
        hubPathRepository.save(hubPath);
        return hubPathId;
    }

    @Transactional
    @Cacheable(value = "hubPath", key = "#hubPathId")
    public HubPathResponseDto getHubPath(UUID hubPathId) {
        HubPath hubPath = hubPathRepository.findByIdAndIsDeleted(hubPathId, false)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HUB_PATH_NOT_FOUND));
        return new HubPathResponseDto(hubPath);
    }

    @Cacheable(value = "hubPathAll", key = "#searchRequestDto")
    public List<HubPathResponseDto> getHubPaths(HubPathSearchRequestDto searchRequestDto, Pageable pageable) {
        Page<HubPath> hubPathPage = hubPathRepository.findAll(HubPathSpecification.searchWith(searchRequestDto), pageable);
        List<HubPathResponseDto> hubPathResponseDtoList = hubPathPage.map(hubPath -> new HubPathResponseDto(hubPath)).getContent();
        return hubPathResponseDtoList;
    }
    // 캐시된 List<HubResponseDto>를 다시 Page로 변환하는 메서드 (totalElements 사용하지 않음)
    public Page<HubPathResponseDto> convertListToPage(List<HubPathResponseDto> hubPathResponseDtoList, Pageable pageable) {
        return new PageImpl<>(hubPathResponseDtoList, pageable, hubPathResponseDtoList.size());
    }

    @Transactional
    @CacheEvict(value = {"hubPath", "hubPathAll"}, allEntries = true)
    public List<UUID> deleteHubPathsByHubId(UUID hubId) {
        List<UUID> hubPathIds = hubPathRepository.findHubPathsByHubId(hubId);

        for (UUID hubPathId : hubPathIds) {
            HubPath hubPath = hubPathRepository.findByIdAndIsDeleted(hubPathId, false)
                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HUB_PATH_NOT_FOUND));
            hubPath.setIsDeleted(true);
            hubPath.setDeletedAt(LocalDateTime.now());
            hubPath.setDeletedBy(0L);
        }
        return hubPathIds;
    }

    @Transactional
    @Cacheable(value = "optimalPaths", key = "{#departureHubID, #arrivalHubID}")
    public List<HubPathDetailsResponseDto> findOptimalPath(UUID departureHubID, UUID arrivalHubID) {
        List<HubPath> hubPathList = hubPathRepository.findByIsDeleted(false);

        // HubPath 리스트를 HubPathResponseDto 리스트로 변환
        List<HubPathResponseDto> hubPathDtoList = hubPathList.stream()
                .map(HubPathResponseDto::new)
                .toList();

        // 그래프 초기화
        Map<UUID, Integer> distances = new HashMap<>();
        Map<UUID, UUID> previous = new HashMap<>();
        PriorityQueue<HubNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(HubNode::getDuration));

        // 모든 허브에 대해 초기화
        Set<UUID> hubs = hubPathDtoList.stream()
                .flatMap(hubPath -> Stream.of(hubPath.getFromHubId(), hubPath.getToHubId()))
                .collect(Collectors.toSet());

        for (UUID hubId : hubs) {
            distances.put(hubId, Integer.MAX_VALUE);
            previous.put(hubId, null);
        }
        distances.put(departureHubID, 0);
        priorityQueue.add(new HubNode(departureHubID, 0));

        // Dijkstra 알고리즘 실행
        while (!priorityQueue.isEmpty()) {
            HubNode currentNode = priorityQueue.poll();
            UUID currentHubId = currentNode.getHubId();
            int currentDuration = currentNode.getDuration();

            if (currentHubId.equals(arrivalHubID)) {
                break; // 도착 허브에 도달하면 종료
            }

            if (currentDuration > distances.get(currentHubId)) {
                continue;
            }

            // 현재 노드에서 연결된 모든 노드에 대해 업데이트
            hubPathDtoList.stream()
                    .filter(hubPath -> hubPath.getFromHubId().equals(currentHubId))
                    .forEach(hubPath -> {
                        UUID neighborHubId = hubPath.getToHubId();
                        int newDuration = currentDuration + hubPath.getDuration();

                        if (newDuration < distances.get(neighborHubId)) {
                            distances.put(neighborHubId, newDuration);
                            previous.put(neighborHubId, currentHubId);
                            priorityQueue.add(new HubNode(neighborHubId, newDuration));
                        }
                    });
        }

        // 최적 경로 추적 및 간선 정보 수집
        List<HubPathDetailsResponseDto> optimalPathDetails = new ArrayList<>();
        for (UUID at = arrivalHubID; at != null; at = previous.get(at)) {
            UUID fromHubId = previous.get(at);
            if (fromHubId != null) {
                UUID finalAt = at;
                Optional<HubPathResponseDto> hubPathOpt = hubPathDtoList.stream()
                        .filter(hubPath -> hubPath.getFromHubId().equals(fromHubId) && hubPath.getToHubId().equals(finalAt))
                        .findFirst();

                if (hubPathOpt.isPresent()) {
                    HubPathResponseDto hubPath = hubPathOpt.get();
                    HubPathDetailsResponseDto dto = new HubPathDetailsResponseDto(
                            hubPath.getId(),
                            hubPath.getFromHubId(),
                            hubPath.getToHubId(),
                            hubPath.getDistance(),
                            hubPath.getDuration()
                    );
                    optimalPathDetails.add(dto);
                }
            }
        }
        Collections.reverse(optimalPathDetails); // 출발 허브부터 도착 허브까지 순서로 정렬
        return optimalPathDetails;

    }

}
