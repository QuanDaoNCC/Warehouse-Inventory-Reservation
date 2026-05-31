package com.warehouse.domain.model;

public class Inventory {

    private String sku;
    private int totalStock;
    private int availableStock;
    private int reservedStock;
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

    public void reserve(int quantity) {
        if (availableStock < quantity) {
            throw new IllegalStateException("Cannot reserve %d units; only %d available".formatted(quantity, availableStock));
        }
        availableStock -= quantity;
        reservedStock += quantity;
    }

    public void release(int quantity) {
        availableStock += quantity;
        reservedStock -= quantity;
    }
}
