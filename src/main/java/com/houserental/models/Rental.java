package main.java.com.houserental.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Rental {
    private int rentalId;
    private int propertyId;
    private int tenantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double monthlyRent;
    private double securityDeposit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Rental() { }

    public Rental(int rentalId, int propertyId, int tenantId, LocalDate startDate, LocalDate endDate,
                  double monthlyRent, double securityDeposit, String status,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.rentalId = rentalId;
        this.propertyId = propertyId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.securityDeposit = securityDeposit;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public int getRentalId() {
        return rentalId;
    }
    public void setRentalId(int rentalId) {
        this.rentalId = rentalId;
    }
    public int getPropertyId() {
        return propertyId;
    }
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }
    public int getTenantId() {
        return tenantId;
    }
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public double getMonthlyRent() {
        return monthlyRent;
    }
    public void setMonthlyRent(double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    public double getSecurityDeposit() {
        return securityDeposit;
    }
    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    @Override
    public String toString() {
        return "Rental{" +
                "rentalId=" + rentalId +
                ", propertyId=" + propertyId +
                ", tenantId=" + tenantId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", monthlyRent=" + monthlyRent +
                ", securityDeposit=" + securityDeposit +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
