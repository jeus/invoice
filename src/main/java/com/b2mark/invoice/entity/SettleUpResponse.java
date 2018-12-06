package com.b2mark.invoice.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class SettleUpResponse {

    private long id;
    private Date dateTime;
    private long amount;
    private String originCard;
    private String destCard;
    private String mobile;
    private String apikey;
    private String merchantId;
    private String txId;
    private String merchantName;
    private List<InvoiceResponse> invoices;

    //TODO: show system as a Invoices.


}