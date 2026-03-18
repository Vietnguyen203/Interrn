package com.manager.account.interfaces.rest.dto.response;

public class BillPreviewDTO {
    private Double subtotal;
    private Double discount;
    private Double total;

    public BillPreviewDTO() {}
    public BillPreviewDTO(Double subtotal, Double discount, Double total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
    }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}




