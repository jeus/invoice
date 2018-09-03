package com.b2mark.invoice.entity;

        import lombok.Getter;
        import lombok.Setter;

        import java.util.Date;
@Setter
@Getter
public class Product {
    private String name;
    private String price;
    private Date date;
    private String vendor;
}
