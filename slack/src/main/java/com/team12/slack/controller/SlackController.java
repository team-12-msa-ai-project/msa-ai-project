package com.team12.slack.controller;

import com.team12.common.exception.response.SuccessResponse;
import com.team12.slack.dto.SlackRequestDto;
import com.team12.slack.service.SlackService;
import com.team12.slack.util.SuccessMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@AllArgsConstructor
@RequestMapping("/slack")
@Slf4j
public class SlackController {
    private final SlackService slackService;

    // todo : response entity 형식 변경
    @PostMapping("")
    public SuccessResponse<?> sendMessage(@RequestBody SlackRequestDto request) {
        slackService.sendMessageToUser(request);
        return SuccessResponse.success(SuccessMessage.SEND_MESSAGE.getHttpStatus().value(), SuccessMessage.SEND_MESSAGE.getMessage());
    }

    @GetMapping("/all")
    public SuccessResponse<?> getSlackAll(@RequestParam(value = "email", required = true) String email, Pageable pageable) {
        return SuccessResponse.success(SuccessMessage.SEND_MESSAGE.getHttpStatus().value(), SuccessMessage.GET_MESSAGE.getMessage(), slackService.getSlackAll(email, pageable));
    }

    @GetMapping("")
    public SuccessResponse<?> getSlack(@RequestParam(value= "messageId", required = true) UUID messageId) {
        return SuccessResponse.success(SuccessMessage.GET_MESSAGE.getHttpStatus().value(), SuccessMessage.GET_MESSAGE.getMessage(), slackService.getSlack(messageId));

    }


}
