/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentSuccess {
    private String shopName;
    private long amount;
}
