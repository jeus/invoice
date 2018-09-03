/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.core.PriceDiscovery;
import com.b2mark.invoice.entity.FactorGuy;
import com.b2mark.invoice.entity.tables.Invoice;
import com.b2mark.invoice.entity.tables.InvoiceJpaRepository;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.exception.BadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/inv")
public class InvoiceRest {
    @Autowired
    InvoiceJpaRepository invoiceJpaRepository;
    @Autowired
    MerchantJpaRepository merchantJpaRepository;
    @Autowired
    PriceDiscovery priceDiscovery;

    //add invoive to merchant.
    @PostMapping("/{mob}")
    public Invoice addInvoice(@PathVariable(value = "mob") String mob, @RequestBody Invoice inv) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        Invoice invoice = inv;
        if (merchant.isPresent()) {
            invoice.setMerchant(merchant.get());
            invoice.setRegdatetime(new Date());
            invoice.setStatus("waiting");
            invoice.setQr("");
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            String qrCode = priceDiscovery.qrCode(invoice1.getAmount(), invoice1.getId());
            invoice1.setQr(qrCode);
            invoice1 = invoiceJpaRepository.save(invoice);
            return invoice1;
        } else {
            throw new BadRequest("Merchant mob number not register");
        }
    }

    @GetMapping
    public Invoice addInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                              @RequestParam(value = "price", required = true) String amount) {
        Invoice invoice = new Invoice();
        invoice.setAmount(Long.parseLong(amount));
        invoice.setCallback("TESTCALLBACK");
        invoice.setCurrency("IRR");
        invoice.setDescription("NEW PAYMANET");
        Random random = new Random();
        int x = random.nextInt(90000) + 10000;
        invoice.setOrderid(x);
        invoice.setUserdatetime(new Date());
        return addInvoice(mobileNum, invoice);
    }


    @GetMapping("/rialbtc")
    public String rialToBtc(@RequestParam(value = "IRR", required = true) long rial) {
        return priceDiscovery.getRialToSatoshi(rial) + "";
    }


    @GetMapping(value = "/all", produces = "application/json")
    public List<FactorGuy> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                         @RequestParam(value = "token", required = true) String token) {
        List<Invoice> invoices = invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantToken(mobileNum, token);
        List<FactorGuy> factorGuys = new ArrayList<>();
        for (Invoice inv : invoices) {
            FactorGuy factorGuy = new FactorGuy();
            factorGuy.setDesc(inv.getDescription());
            factorGuy.setId(inv.getId());
            factorGuy.setPrice(inv.getAmount());
            factorGuy.setShopName(inv.getMerchant().getShopName());
            factorGuy.setStatus(inv.getStatus());
            factorGuy.setSymbol("$");
        }


        return factorGuys;
    }


    @GetMapping(value = "/id", produces = "application/json")
    public FactorGuy getById(@RequestParam(value = "invoice", required = true) long invid) {
        Optional<Invoice> invoices = invoiceJpaRepository.findById(invid);
        List<FactorGuy> factorGuys = new ArrayList<>();
        if (!invoices.isPresent()) {
            return null;
        }
            FactorGuy factorGuy = new FactorGuy();
            factorGuy.setDesc(invoices.get().getDescription());
            factorGuy.setId(invoices.get().getId());
            factorGuy.setPrice(invoices.get().getAmount());
            factorGuy.setShopName(invoices.get().getMerchant().getShopName());
            factorGuy.setStatus(invoices.get().getStatus());
            factorGuy.setSymbol("$");



        return factorGuy;
    }


    @GetMapping(value = "/qrcode", produces = "application/json")
    public String getAllInvoice(@RequestParam(value = "amount", required = true) long amount,
                                @RequestParam(value = "id", required = true) long id) {
        return priceDiscovery.qrCode(amount, id);
    }


    private String createQr(long rial, String wallet) {
        return "nadare alan ";//TODO: have to create this.
    }

}