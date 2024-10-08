package com.team12.hub.hubPath.domain;

import com.team12.common.audit.AuditingEntity;
import com.team12.hub.hub.domain.Hub;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_hub_path", schema = "s_hub")
public class HubPath extends AuditingEntity {
    @Id
    @GeneratedValue
    @Column(name = "hub_path_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "from_hub_id", nullable = false)
    private Hub fromHub;  // 출발 허브 ID

    @ManyToOne
    @JoinColumn(name = "to_hub_id", nullable = false)
    private Hub toHub;  // 도착 허브 ID

    @Column(name = "distance", nullable = false)
    private int distance; // 허브 간 거리 (미터)

    @Column(name = "duration", nullable = false)
    private int duration;  // 소요시간 (초)

    public HubPath(UUID id, Hub fromHub, Hub toHub, int distance, int duration, boolean isDeleted) {
        this.id = id;
        this.fromHub = fromHub;
        this.toHub = toHub;
        this.distance = distance;
        this.duration = duration;
        this.setIsDeleted(isDeleted);
    }
}
