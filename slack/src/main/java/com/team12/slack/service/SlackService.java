package com.team12.slack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.common.exception.BusinessLogicException;
import com.team12.common.exception.ExceptionCode;
import com.team12.slack.domain.Message;
import com.team12.slack.dto.SlackRequestDto;
import com.team12.slack.dto.SlackResDto;
import com.team12.slack.repository.SlackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackService {

    private final SlackRepository slackRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${SLACK_TOKEN}")
    private String slackToken;

    private static final String SLACK_API_URL = "https://slack.com/api/chat.postMessage";

    @KafkaListener(topics = "delivery-status-update", groupId = "slack-group")
    public void kafkaListener(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SlackRequestDto request = objectMapper.readValue(message, SlackRequestDto.class);
            sendMessageToUser(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessLogicException(ExceptionCode.INVALID_PARAMETER);
        }

    }


    public void sendMessageToUser(SlackRequestDto request) {
        try {
            String slackUserId = getSlackUserId(request.getEmail());
            if (slackUserId.isEmpty()) {
                log.error("User not found");
                throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
            }
            Message message = new Message(request.getEmail(), request.getContent());
            slackRepository.save(message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(slackToken);

            String requestBody = String.format("{\"channel\":\"%s\", \"text\":\"%s\"}", slackUserId, request.getContent());
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(SLACK_API_URL, entity, String.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessLogicException(ExceptionCode.INVALID_PARAMETER);
        }
    }

    public String getSlackUserId(String email) {
        String url = "https://slack.com/api/users.lookupByEmail";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            String apiUrlWithParams = url + "?email=" + email;
            ResponseEntity<String> response = restTemplate.exchange(apiUrlWithParams, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.path("ok").asBoolean()) {
                    return root.path("user").path("id").asText();
                }
            }
            log.error("User not found");
            throw new BusinessLogicException(ExceptionCode.USER_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessLogicException(ExceptionCode.INVALID_PARAMETER);
        }
    }

    public Page<SlackResDto> getSlackAll(String email, Pageable pageable) {
        try {
            return slackRepository.findByEmail(email, pageable)
                    .map(SlackResDto::new);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessLogicException(ExceptionCode.INVALID_PARAMETER);
        }
    }

    public Object getSlack(UUID messageId) {
        try {
            return slackRepository.findById(messageId)
                    .map(SlackResDto::new);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessLogicException(ExceptionCode.INVALID_PARAMETER);
        }
    }
}
