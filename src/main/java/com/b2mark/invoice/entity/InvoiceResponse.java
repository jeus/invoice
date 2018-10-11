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
        "orderid",
        "qr",
        "description",
        "gatewayUrl",
        "callback"
})

@Setter
@Getter
public class InvoiceResponse {


    @JsonIgnore
    private Invoice invoice;
    @JsonIgnore
    private boolean presentQrCode = true;
    @JsonIgnore
    private boolean presentCallback = true;


    public InvoiceResponse(Role role) {
        switch (role) {
            case user:
                presentQrCode = true;
                presentCallback = false;
                break;
            case merchant:
                presentQrCode = false;
                presentCallback = true;

        }
    }


    public InvoiceResponse(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoiceResponse(Invoice invoice, boolean presentQrCode) {
        this.invoice = invoice;
        this.presentQrCode = presentQrCode;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getQr() {
        return presentQrCode ? invoice.getQr() : null;
    }

    public String getOrderId() {
        return invoice.getOrderid();
    }


    public String getGatewayUrl(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://gateway.becopay.com/invoice/").append(invoice.getInvoiceId());
        return stringBuilder.toString();
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
        if (presentCallback) {
            return invoice.getMerchant().getCallback() + "?orderid=" + invoice.getOrderid();
        } else {
            if (getStatus().equals("success") || getStatus().equals("failed")) {
                return "";
            } else {
                return invoice.getMerchant().getCallback() + "?orderid=" + invoice.getOrderid();
            }
        }

    }

    @Override
    public String toString() {
        Gson json = new Gson();
        return json.toJson(this);
    }


    public enum Role {
        user,
        merchant
    }
}
