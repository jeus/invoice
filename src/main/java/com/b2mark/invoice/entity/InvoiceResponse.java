/**
 * <h1>response JSON when calling</h1>
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
import org.apache.commons.text.StrSubstitutor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({
        "id",
        "shopName",
        "status",
        "remaining",
        "payerAmount",
        "payerCoin",
        "merchantAmount",
        "merchantCoin",
        "date",
        "timestamp",
        "orderid",
        "timeout",
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
    @JsonIgnore
    private boolean presentCryptoAmount = true;
    @JsonIgnore
    private boolean presentMerchantCoin = true;
    @JsonIgnore
    private boolean presentMerchantCoinAmount = true;



    public InvoiceResponse(Role role) {
        switch (role) {
            case user:
                presentQrCode = true;
                presentCryptoAmount = true;
                presentCallback = false;
                presentMerchantCoin = false;
                presentMerchantCoinAmount = false;
                break;
            case merchant:
                presentQrCode = false;
                presentCryptoAmount = false;
                presentCallback = true;
                presentMerchantCoin = true;
                presentMerchantCoinAmount = true;
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

    public BigDecimal getPayerAmount() {
        return invoice.getAmount();
    }

    public String getPayerCoin() {
        return invoice.getPayerCoin();
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


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public BigDecimal getMerchantAmount(){return presentMerchantCoinAmount ? invoice.getMerchantAmount() : null;}


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMerchantCoin(){return presentMerchantCoin ? invoice.getMerchantCoin() : null;}

    public String getGatewayUrl() {
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

//TODO: have migrate to server.
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
        if (!presentCallback) {
            if (getStatus().equals("success") || getStatus().equals("failed")) {
                return "";
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("orderId", invoice.getOrderid());
        String callback = StrSubstitutor.replace(invoice.getMerchant().getCallback(), map);
        return callback;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCryptoAmount(){
        return invoice.getCryptoAmount();
    }


    public int getTimeout() {
        return invoice.getTimeout();
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
