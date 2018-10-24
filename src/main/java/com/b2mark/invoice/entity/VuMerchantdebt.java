package com.b2mark.invoice.entity;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Table(name = "vu_merchantdebt")
@Setter
@Getter
public class VuMerchantdebt {

    @Id
    private long id;
    private int count;
    private String merMobile;
    private long balance;
    private String symbol;
    private String name;
}