package com.b2mark.invoice.entity;

import com.b2mark.invoice.entity.tables.Invoice;
import com.b2mark.invoice.entity.tables.Settleup;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@JsonPropertyOrder({
        "id",
        "dateTime",
        "amount",
        "originCard",
        "destCard",
        "txId",
        "merchantId",
        "shopName",
        "invoices"
})



@Setter
@Getter
public class SettleUpResponse {

    private long id;
    private Date dateTime;
    private BigDecimal amount;
    private String originCard;
    private String destCard;
    private String txId;
    private long merchantId;
    private String shopName;
    private List<InvoiceResponse> invoices;

    //TODO: show system as a Invoices.
    public SettleUpResponse (Settleup settleup){

        Object[] invoiceObjs = settleup.getInvoices().toArray();

        this.id = settleup.getId();
        this.dateTime = settleup.getDatetime();
        this.amount = settleup.getAmount();
        this.originCard = settleup.getOriginCard();
        this.destCard = settleup.getDestCard();
        this.txId = settleup.getTxid();
        this.merchantId =((Invoice) invoiceObjs[0]).getMerchant().getId();
        this.shopName =((Invoice) invoiceObjs[0]).getMerchant().getShopName();
        invoices = new ArrayList();
        for(Object invoice : invoiceObjs)
        {
            InvoiceResponse invoiceResponse = new InvoiceResponse(InvoiceResponse.Role.merchant);
            invoiceResponse.setInvoice((Invoice)invoice);
            invoices.add(invoiceResponse);
        }
    }

}