package com.b2mark.invoice.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
   public class SettleUpRequest{

        private String mobile;
        private String apikey;
        private String merchantId;
        private long amount;
        private Date setDateTime;
        private String destinationCard;
        private String OriginatedCard;
        private List<String> invoices;
   }