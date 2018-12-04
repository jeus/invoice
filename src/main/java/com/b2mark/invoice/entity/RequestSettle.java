package com.b2mark.invoice.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
public class RequestSettle {
    private String mob;
    private String apikey;
    private BigDecimal amount;
    private String merMobile;
    private Date datetime;
    private String originCard;
    private String destCard;
    private String txid;
    private Set<String> invoiceIds;
}