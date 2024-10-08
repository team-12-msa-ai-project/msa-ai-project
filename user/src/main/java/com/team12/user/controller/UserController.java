package com.team12.user.controller;

import com.team12.common.customPage.CustomPageResponse;
import com.team12.common.dto.user.UserDetailsDto;
import com.team12.common.exception.response.CommonResponse;
import com.team12.user.dto.*;
import com.team12.user.service.ClientService;
import com.team12.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ClientService clientService;

    //회원가입
    @PostMapping("/sign-up")
    public ResponseEntity signUp(@RequestBody UserSignUpRequestDto userRequestDto) throws Exception {
        UserResponseDto userResponseDto = userService.signUp(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    //관리자 : 유저 가입 승인
    @PatchMapping("/reg/{userId}/approve")
    public ResponseEntity approve(
            @PathVariable(name = "userId") Long userId,
                                  @RequestParam(name = "isConfirmed") boolean isConfirmed) {
        UserResponseForRegisterDto userResponseForRegisterDto = userService.approve(userId, isConfirmed);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userResponseForRegisterDto);
    }
    //유저 : 개인 상세 정보 조회
    @GetMapping("/usr")
    public ResponseEntity getUserDetail(@RequestHeader("X-User-Id") Long tokenUserId) {
        //Todo : header 토큰 받아서 처리하기
        Long userId = tokenUserId;
        UserResponseDto userResponseDto = userService.getUserDetail(userId);
        return ResponseEntity.ok(userResponseDto);
    }

    //관리자 : 유저 상세 정보 조회
    @GetMapping("/reg/{userId}")
    public ResponseEntity getUserDetailForRegister(@PathVariable("userId") Long userId) {
        UserResponseForRegisterDto userResponseForRegisterDto = userService.getUserDetailForRegister(userId);
        return ResponseEntity.ok(userResponseForRegisterDto);
    }
    //관리자 : 유저 리스트 조회
    @GetMapping("/reg/list")
    public ResponseEntity getUsers(@RequestParam("page") int page,
                                   @RequestParam("size") int size,
                                   @RequestParam("sort") String sort) {
        CustomPageResponse<UserDataForRegisterDto> userList = userService.getUsers(page-1, size, sort);
        return ResponseEntity.ok(userList);
    }

    //관리자 : 유저 검색
    @GetMapping("/reg/search")
    public ResponseEntity searchUser(@RequestParam(name="text") String searchText,
                                     @RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size,
                                     @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
        CustomPageResponse<UserDataForRegisterDto> userList = userService.searchUsers(searchText, page-1, size, sort);
        return ResponseEntity.ok(userList);
    }

    //관리자 : 유저 정보 수정
    @PatchMapping("/reg/{userId}")
    public ResponseEntity patchUser(@PathVariable("userId") Long userId,
                                    @RequestBody UserPatchRequestForRegisterDto patchDto) {
        UserResponseForRegisterDto<UserDataForRegisterDto> patchUser =
                userService.patchUser(userId, patchDto);
        return ResponseEntity.ok(patchUser);
    }
    //관리자 : 유저 삭제
    @DeleteMapping("/reg/{userId}")
    public ResponseEntity deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


}
