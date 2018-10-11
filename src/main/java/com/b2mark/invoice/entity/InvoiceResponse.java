/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;


import com.b2mark.invoice.entity.tables.Invoice;
import com.fasterxml.jackson.annotation.*;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@JsonPropertyOrder({
        "id",
        "shopName",
        "status",
        "remaining",
        "symbol",
        "price",
        "date",
        "timestamp",
        "qr",
        "description",
        "callback"
})

@Setter
@Getter
public class InvoiceResponse {

    @JsonIgnoreProperties
    @JsonIgnore
    private Invoice invoice;

    public InvoiceResponse(Invoice invoice) {
        this.invoice = invoice;
    }


    public String getShopName() {
        return invoice.getMerchant().getShopName();
    }

    public String getId() {
        return invoice.getInvoiceId();
    }

    public long getPrice() {
        return invoice.getAmount();
    }

    public String getSymbol() {
        return invoice.getCurrency();
    }


    public String getDescription() {
        return invoice.getDescription();
    }


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "UTC")
    public Date getDate() {
        return invoice.getRegdatetime();
    }

    public String getQr() {
        return invoice.getQr();
    }


    /**
     * return miniute of invoices.
     *
     * @return
     */
    public int getRemaining() {
        int minute = (int) invoice.remaining();
        if (invoice.remaining() < 0)
            minute = 0;
        return minute;
    }


    public String getStatus() {
        if (invoice.remaining() <= 0 && !invoice.getStatus().equals("success")) {
            return "failed";
        } else
            return invoice.getStatus();
    }


    public long getTimestamp() {
        return invoice.getRegdatetime().getTime();
    }

    /**
     * after susccess return empty callback.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCallback() {
        if (getStatus().equals("success") || getStatus().equals("failed")) {
            return null;
        }
        return invoice.getMerchant().getCallback() + "?orderid=" + invoice.getOrderid();
    }

    @Override
    public String toString() {
        Gson json = new Gson();
        return json.toJson(this);
    }
}
