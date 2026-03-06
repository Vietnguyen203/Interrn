package com.manager.account.interfaces.rest.dto.response;

public class ReceiptDTO {
    private String orderId;
    private String tableId;
    private Double subtotal;
    private Double discount;
    private Double total;
    private String paymentMethod;
    private Double amountReceived;
    private Double change;
    private Long paidAtEpochMs;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Double getAmountReceived() { return amountReceived; }
    public void setAmountReceived(Double amountReceived) { this.amountReceived = amountReceived; }
    public Double getChange() { return change; }
    public void setChange(Double change) { this.change = change; }
    public Long getPaidAtEpochMs() { return paidAtEpochMs; }
    public void setPaidAtEpochMs(Long paidAtEpochMs) { this.paidAtEpochMs = paidAtEpochMs; }
}




