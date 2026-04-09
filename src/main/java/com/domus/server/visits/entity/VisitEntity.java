package com.domus.server.visits.entity;

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
@Table(name = "visits")
public class VisitEntity {

    @Id
    private UUID id;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(name = "visitor_document", nullable = false, length = 50)
    private String visitorDocument;

    @Column(name = "visitor_phone", length = 50)
    private String visitorPhone;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id")
    private UserEntity residentUser;

    @Column(name = "resident_name", nullable = false, length = 150)
    private String residentName;

    @Column(name = "unit_label", length = 80)
    private String unitLabel;

    @Column(name = "block_label", length = 80)
    private String blockLabel;

    @Column(name = "entry_at")
    private Instant entryAt;

    @Column(name = "exit_at")
    private Instant exitAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VisitStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false, length = 40)
    private VisitRegistrationType registrationType;

    @Column(name = "observations", length = 500)
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorDocument() {
        return visitorDocument;
    }

    public void setVisitorDocument(String visitorDocument) {
        this.visitorDocument = visitorDocument;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public void setVisitorPhone(String visitorPhone) {
        this.visitorPhone = visitorPhone;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
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

    public Instant getEntryAt() {
        return entryAt;
    }

    public void setEntryAt(Instant entryAt) {
        this.entryAt = entryAt;
    }

    public Instant getExitAt() {
        return exitAt;
    }

    public void setExitAt(Instant exitAt) {
        this.exitAt = exitAt;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public VisitRegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(VisitRegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
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
