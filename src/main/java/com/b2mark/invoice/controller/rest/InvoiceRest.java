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
@CrossOrigin
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

    @CrossOrigin
    @GetMapping("/rialSat")
    public String rialToBtc(@RequestParam(value = "IRR", required = true) long rial) {
        return (new Date()).toString();
        // return priceDiscovery.getRialToSatoshi(rial) + "";
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
            factorGuy.setDate(inv.getRegdatetime());
            factorGuy.setQr(inv.getQr());
            factorGuy.setTimeout(15);
            factorGuy.setSymbol("IRR");
            if (inv.getStatus().equals("success") || factorGuy.getRemaining() == 0) {
                System.out.println("-----------------------***********----------------------");
                System.out.println("-----------------------****SUC****----------------------");
                System.out.println("-----------------------***********----------------------");
            } else {
                if (priceDiscovery.getStatus(inv.getId()).equals("Verfied")) {
                    inv.setStatus("success");
                    invoiceJpaRepository.save(inv);
                    factorGuy.setStatus("success");
                }
            }
            factorGuys.add(factorGuy);
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
        Invoice invoice = invoices.get();
        FactorGuy factorGuy = new FactorGuy();
        factorGuy.setDesc(invoice.getDescription());
        factorGuy.setId(invoice.getId());
        factorGuy.setPrice(invoice.getAmount());
        factorGuy.setShopName(invoice.getMerchant().getShopName());
        factorGuy.setStatus(invoice.getStatus());
        factorGuy.setDate(invoice.getRegdatetime());
        factorGuy.setTimeout(15);
        factorGuy.setQr(invoice.getQr());
        factorGuy.setSymbol("IRR");
        if (invoice.getStatus().equals("success") || factorGuy.getRemaining() == 0) {
            return factorGuy;
        }
        String status = priceDiscovery.getStatus(invoice.getId());
        if (status.equals("Verfied")) {
            invoice.setStatus("success");
            invoiceJpaRepository.save(invoice);
            factorGuy.setStatus("success");
        }
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