package com.b2mark.invoice.entity.tables;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.controller.rest.InvoiceRest;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.b2mark.invoice.exception.PublicException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice {

    private static Pattern pattern = Pattern.compile("^(?<category>.{3})_(?<merchant>\\d*)_(?<id>[a-zA-Z0-9]*)$");
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceRest.class);
    private static final int invRedix = 12;

    @Transient
    public static final int TIMEOUT = 40;//min
    @Transient
    public static final int EXTRME_TIMEOUT = 50;//min
    @Transient
    public static final String ACCEPTEDPAYMENT = "Verified";//when txVerified return true.

    @Id
    @NotNull
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant", referencedColumnName = "id")
    @ApiModelProperty(readOnly = true, hidden = true)
    private Merchant merchant;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name="invoicesettle", joinColumns={@JoinColumn(name="invoice_id")},
            inverseJoinColumns={@JoinColumn(name="settleup")})
    @JsonBackReference
    private Settleup settleup;


    @NotNull
    private String currency;

    @NotNull
    private String status;

    @NotNull
    private String qr;

    @NotNull
    private String category;



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
        return min <= 0;
    }

    public boolean checkAcceptPayment(String status) {

        return status.equals(ACCEPTEDPAYMENT);
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
            return Coin.fromName(qr.substring(0, qr.indexOf(":")));
        }
    }

    @Transient
    @JsonIgnoreProperties
    public String getCryptoAmount() {
        String cryptoAmount =null;
        if (getBlockchainCoin() != null) {
            //TODO: have to change this to database for save.
            cryptoAmount = qr.substring(qr.indexOf("amount=") + 7);
        }
        return cryptoAmount;
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



    /**
     * deserialize String invoice to invoiceId.
     *
     * @param strInvoiceId getString invoice format
     * @return return InvoiceId
     */
    @Transient
    public static InvoiceId dSerializeInvoice(String strInvoiceId) {
        Matcher matcher = pattern.matcher(strInvoiceId);
        if (!matcher.matches()) {
            LOG.error("action:DSER,invoice_id:{}", strInvoiceId);
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This invoice id is not valid");
        }
        String strCat = matcher.group("category");
        String strId = matcher.group("id");
        String strMerchId = matcher.group("merchant");

        long setId = Long.parseLong(strId, invRedix);
        long merchant = Long.parseLong(strMerchId);
        InvoiceId invoiceId = new InvoiceId();
        invoiceId.setCategory(InvoiceCategory.fromString(strCat));
        invoiceId.setId(setId);
        invoiceId.setMerchantId(merchant);
        return invoiceId;
    }


    /**
     * create format "CAT_MERCHANTID_INVOICELONGID" "POS_23443_1eqd34f233323347dhsj
     *
     * @return invoiceId
     */
    @JsonGetter("id")
    @Transient
    public String getInvoiceId() {
        InvoiceCategory invoiceCategory = InvoiceCategory.fromString(category);
        if (invoiceCategory != null && merchant != null) {
            String invoiceId = "%s_%s_%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);
            String strId = Long.toString(id, invRedix);
            fmt.format(invoiceId, invoiceCategory.getInvoiceCategory(), merchant.getId(), strId);
            return sbuf.toString();
        } else {
            return null;
        }
    }


    @Setter
    @Getter
    public static class InvoiceId {
        private long id;
        private long merchantId;
        private InvoiceCategory category;
    }

}