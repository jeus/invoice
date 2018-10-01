/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.enums;

public enum InvoiceCategory {

    POS("POS"),
    PGW("PGW");

    private String invoiceCategory;

    InvoiceCategory(String InvoiceCategory) {
        this.invoiceCategory = InvoiceCategory;
    }

    public String getInvoiceCategory() {
        return this.invoiceCategory;
    }

    public static InvoiceCategory fromString(String invoiceCategory) {
        for (InvoiceCategory b : InvoiceCategory.values()) {
            if (b.invoiceCategory.equalsIgnoreCase(invoiceCategory)) {
                return b;
            }
        }
        return null;
    }
}
