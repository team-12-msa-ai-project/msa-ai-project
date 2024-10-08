package com.team12.hub.hub.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class KakaoMapService {
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private static final String GEOCODING_API_URL = "https://dapi.kakao.com/v2/local/search/address.json?query=%s";

    public List<String> getLatLongFromAddress(String address) {
        String url = String.format(GEOCODING_API_URL, address);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JSONObject json = new JSONObject(response.getBody());
        JSONArray documents = json.getJSONArray("documents");

        if (documents.length() > 0) {
            JSONObject location = documents.getJSONObject(0).getJSONObject("address");
            String latitude = location.getString("y");
            String longitude = location.getString("x");
            return Arrays.asList(latitude, longitude);
        } else {
            throw new RuntimeException("No results found for the address: " + address);
        }
    }
}
