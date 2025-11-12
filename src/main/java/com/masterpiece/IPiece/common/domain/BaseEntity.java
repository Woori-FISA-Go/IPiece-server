package com.masterpiece.IPiece.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamp", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamp")
    private LocalDateTime updatedAt;

}
