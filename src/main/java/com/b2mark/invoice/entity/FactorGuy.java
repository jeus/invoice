/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Setter
@Getter
public class FactorGuy {
    private String shopName;
    private long id;
    private long price;
    private String symbol;
    private String desc;
    private String status;
    private List<Product> products;
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "MM-dd hh:mm" , timezone="UTC")
    private Date date;
    private String qr;
    private int timeout;

    public int getRemaining() {
        int minute = (int) (timeout - (((new Date()).getTime() - date.getTime()) / 1000 / 60));
        if (minute < 0)
            minute =  0;
        return minute;
    }




    public void addProduct(Product product) {
        if (products == null)
            products = new ArrayList<>();
        products.add(product);
    }


}
