/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InvRequest {
    private String mobile;
    private String apikey;
    private String price;
    private String orderId;
    /**
     * merchant coin, Destination coin or withdraw coin is coin that merchant get from the system. default is IRR
     */
    @JsonIgnore
    private Coin merCoin = Coin.IRANRIAL;
    /**
     * payer coin, Source coin or deposit coin is coin that show to payer for example Euro or USD. default is IRR
     */
    @JsonIgnore
    private Coin payCoin = Coin.IRANRIAL;
    private String description;

    public void setMerchantCur(String symbol) {
        merCoin = Coin.fromSymbol(symbol);
    }

    public void setPayerCur(String symbol) {
        payCoin = Coin.fromSymbol(symbol);
    }


    public String getMerchantCur() {
        return merCoin.getSymbol();
    }

    public String getPayerCur() {
        return payCoin.getSymbol();
    }


}
