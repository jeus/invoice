/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Setter
@Getter
public class InvoiceResponse {
    private String shopName;
    private String id;
    private long price;
    private String symbol;
    @JsonIgnoreProperties
    private String orderId;
    private String callback;
    private String desc;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd hh:mm", timezone = "UTC")
    private Date date;
    private String qr;
    private int timeout;

    /**
     * return miniute of invoices.
     *
     * @return
     */
    public int getRemaining() {
        int minute = (int) (timeout - (((new Date()).getTime() - date.getTime()) / 1000 / 60));
        if (minute < 0)
            minute = 0;
        return minute;
    }


    public String getStatus() {
        if (this.getRemaining() <= 0) {
            status = "failed";
            return status;
        } else
            return status;
    }

    /**
     * after susccess return empty callback.
     * @return
     */
    public String getCallback()
    {
        if(status.equals("success"))
        {
            callback="";
            return callback;
        }
        return callback+"?orderid="+orderId;
    }

}
