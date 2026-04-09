package com.domus.server.packages.entity;

import com.domus.server.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "packages")
public class PackageEntity {

    @Id
    private UUID id;

    @Column(name = "description", nullable = false, length = 180)
    private String description;

    @Column(name = "sender_name", length = 150)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 40)
    private PackageType packageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id")
    private UserEntity residentUser;

    @Column(name = "resident_name", nullable = false, length = 150)
    private String residentName;

    @Column(name = "unit_label", length = 80)
    private String unitLabel;

    @Column(name = "block_label", length = 80)
    private String blockLabel;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PackageStatus status;

    @Column(name = "observations", length = 500)
    private String observations;

    @Column(name = "received_by_name", length = 150)
    private String receivedByName;

    @Column(name = "delivered_to_name", length = 150)
    private String deliveredToName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private UserEntity recordedByUser;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public UserEntity getResidentUser() {
        return residentUser;
    }

    public void setResidentUser(UserEntity residentUser) {
        this.residentUser = residentUser;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    public String getBlockLabel() {
        return blockLabel;
    }

    public void setBlockLabel(String blockLabel) {
        this.blockLabel = blockLabel;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getReceivedByName() {
        return receivedByName;
    }

    public void setReceivedByName(String receivedByName) {
        this.receivedByName = receivedByName;
    }

    public String getDeliveredToName() {
        return deliveredToName;
    }

    public void setDeliveredToName(String deliveredToName) {
        this.deliveredToName = deliveredToName;
    }

    public UserEntity getRecordedByUser() {
        return recordedByUser;
    }

    public void setRecordedByUser(UserEntity recordedByUser) {
        this.recordedByUser = recordedByUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
