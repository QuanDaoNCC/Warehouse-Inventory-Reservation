package com.warehouse.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory")
public class InventoryEntity {

    @Id
    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
