package com.team12.hub.hub.dto;

import com.team12.hub.hub.domain.Hub;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HubResponseDto {
    private UUID id;
    private String name;
    private String address;
    private String latitude;
    private String longitude;

    public HubResponseDto(Hub hub) {
        this.id = hub.getId();
        this.name = hub.getName();
        this.address = hub.getAddress();
        this.latitude = hub.getLatitude();
        this.longitude = hub.getLongitude();
    }

    @Override
    public String toString() {
        return "HubResponseDto [id=" + id + ", name=" + name + ", address=" + address + ", latitude=" + latitude;
    }
}
