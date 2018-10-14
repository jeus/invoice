/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Formatter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice {

    @Transient
    public static final int TIMEOUT = 40;//min
    @Transient
    public static final int EXTRME_TIMEOUT = 50;//min
    @Transient
    public static final String ACCEPTEDPAYMENT = "Verified";//when txVerified return true.

    @NotNull
    @Id
    @JsonIgnoreProperties
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true)
    private long id;


    @NotNull
    @ReadOnlyProperty
    @ApiModelProperty(readOnly = true)
    @Column(name = "regdatetime", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd hh:mm", timezone = "UTC")
    private Date regdatetime;

    @NotNull
    private long amount;

    @NotNull
    private Date userdatetime;

    private String description;
    @NotNull
    private String orderid;

    @ManyToOne
    @JoinColumn(name = "merchant", referencedColumnName = "id")
    @ApiModelProperty(readOnly = true, hidden = true)
    private Merchant merchant;


    @NotNull
    private String currency;

    @NotNull
    private String status;

    @NotNull
    private String qr;

    @NotNull
    private String category;


    /**
     * create format "CAT_MERCHANTID_INVOICELONGID" "POS_23443_1eqd34f233323347dhsj
     *
     * @return
     */
    @JsonGetter("id")
    @Transient
    public String getInvoiceId() {
        InvoiceCategory invoiceCategory = InvoiceCategory.fromString(category);
        if (invoiceCategory != null && merchant != null) {
            String invoiceId = "%s_%s_%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);
            String strId = Long.toString(id, 12);
            fmt.format(invoiceId, invoiceCategory.getInvoiceCategory(), merchant.getId(), strId);
            return sbuf.toString();
        } else {
            return null;
        }
    }


    public int getTimeout() {
        return TIMEOUT;
    }

    public String toString() {
        Gson json = new Gson();
        return json.toJson(this);
    }


    @Transient
    private static boolean checkExpire(long expminute, Date startDate) {
        long min = expminute - (((new Date()).getTime() - startDate.getTime()) / 1000 / 60);
        if (min > 0)
            return false;
        else
            return true;
    }

    public boolean checkAcceptPayment(String status) {

        if (status.equals(ACCEPTEDPAYMENT)) {
            return true;
        } else {
            return false;
        }
    }


    @Transient
    public boolean isSuccess() {
        return status.equals("success");
    }


    @Transient
    public boolean isWaiting() {
        return status.equals("waiting");
    }

    @Transient
    public boolean isFailed() {
        return status.equals("failed");
    }


    @Transient
    public Coin getBlockchainCoin() {
        if (qr.isEmpty())
            return null;
        else {
            Coin coin = Coin.fromName(qr.substring(0, qr.indexOf(":")));
            return coin;
        }
    }


    @Transient
    @JsonIgnoreProperties
    public boolean timeExpired() {
        return checkExpire(TIMEOUT, regdatetime);
    }

    @Transient
    @JsonIgnoreProperties
    public boolean timeExtremeExpired() {
        return checkExpire(EXTRME_TIMEOUT, regdatetime);
    }

    @Transient
    public long remaining() {
        return (TIMEOUT - (((new Date()).getTime() - regdatetime.getTime()) / 1000 / 60));
    }

}