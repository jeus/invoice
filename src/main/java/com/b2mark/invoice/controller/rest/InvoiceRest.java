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
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;


@RestController
@RequestMapping("/inv")
public class InvoiceRest {
    @Autowired
    InvoiceJpaRepository invoiceJpaRepository;
    @Autowired
    MerchantJpaRepository merchantJpaRepository;

    //add invoive to merchant.
    @PostMapping("/{mob}")
    public Invoice addInvoice(@PathVariable(value = "mob") String mob, @RequestBody Invoice inv) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        Invoice invoice = inv;
        if (merchant.isPresent()) {
            System.out.println(invoice.getOrderid()+"-------------------------------************--");
            invoice.setMerchant(merchant.get());
            invoice.setRegdatetime(new Date());
            System.out.println("---------asdasdasd---------asd-------sdsd"+invoice.getRegdatetime());
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            return invoice1;
        } else {
            throw new BadRequest("Merchant mob number not register");
        }
    }

    @GetMapping
    public Invoice addInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                              @RequestParam(value = "price", required = true) String amount){

        Invoice invoice = new Invoice();
        invoice.setAmount(Long.parseLong(amount));
        invoice.setCallback("TESTCALLBACK");
        invoice.setCurrency("IRR");
        invoice.setDescription("NEW PAYMANET");
        Random random = new Random();
        int x = random.nextInt(90000) + 10000;
        invoice.setOrderid(x);
        invoice.setUserdatetime(new Date());
        return addInvoice(mobileNum,invoice);

    }


    @GetMapping(produces = "application/json")
    public List<Invoice> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                       @RequestParam(value = "token", required = true) String token) {
        return invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantToken(mobileNum, token);
    }


}