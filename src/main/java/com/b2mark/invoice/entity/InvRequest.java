/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity;

import com.b2mark.invoice.enums.InvoiceCategory;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InvRequest {
    String mobile;
    String apiKey;
    String price;
    long orderId;
    String description;
}
