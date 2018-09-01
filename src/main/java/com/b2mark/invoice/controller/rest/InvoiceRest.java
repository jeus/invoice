/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.entity.tables.Invoice;
import com.b2mark.invoice.entity.tables.InvoiceJpaRepository;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.exception.BadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/invoice")
public class InvoiceRest {
    @Autowired
    InvoiceJpaRepository invoiceJpaRepository;
    @Autowired
    MerchantJpaRepository merchantJpaRepository;

    @PostMapping("/{mob}")
    public Invoice addInvoice(@PathVariable(value = "mob") String mob, @RequestBody Invoice inv, @ApiIgnore Authentication authentication) {
        Invoice invoice = new Invoice();
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);

        if (merchant.isPresent()) {
            invoice.setMerchant(merchant.get());
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            return invoice1;
        } else {
            throw new BadRequest("Merchant mob number not register");
        }
    }

    @GetMapping(produces = "application/json")
    public List<Invoice> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                       @RequestParam(value = "token", required = true) String token) {
        return invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantToken(mobileNum, token);
    }
}