/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.core.PriceDiscovery;
import com.b2mark.invoice.entity.InvoiceUiModel;
import com.b2mark.invoice.entity.PaymentSuccess;
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
    public List<InvoiceUiModel> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                              @RequestParam(value = "token", required = true) String token) {
        List<Invoice> invoices = invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantTokenOrderById(mobileNum, token);
        List<InvoiceUiModel> invoiceUiModels = new ArrayList<>();
        for (Invoice inv : invoices) {
            InvoiceUiModel invoiceUiModel = new InvoiceUiModel();
            invoiceUiModel.setDesc(inv.getDescription());
            invoiceUiModel.setId(inv.getId());
            invoiceUiModel.setPrice(inv.getAmount());
            invoiceUiModel.setShopName(inv.getMerchant().getShopName());
            invoiceUiModel.setStatus(inv.getStatus());
            invoiceUiModel.setDate(inv.getRegdatetime());
            invoiceUiModel.setQr(inv.getQr());
            invoiceUiModel.setTimeout(15);
            invoiceUiModel.setSymbol("IRR");
            if (inv.getStatus().equals("success") || invoiceUiModel.getRemaining() == 0) {
                System.out.println("INVOICE ID:"+invoiceUiModel.getId() +" is "+inv.getStatus()+" remaining:"+invoiceUiModel.getRemaining() );
            } else {
                String status = priceDiscovery.getStatus(inv.getId());
                if (status.equals("Verified")) {
                    inv.setStatus("success");
                    System.out.println("INVOICE ID"+invoiceUiModel.getId()+"  getstatus:"+status);
                    invoiceJpaRepository.save(inv);
                    invoiceUiModel.setStatus("success");
                }
            }
            invoiceUiModels.add(invoiceUiModel);
        }
        return invoiceUiModels;
    }


    @GetMapping(value = "/id", produces = "application/json")
    public InvoiceUiModel getById(@RequestParam(value = "invoice", required = true) long invid) {
        Optional<Invoice> invoices = invoiceJpaRepository.findById(invid);
        List<InvoiceUiModel> invoiceUiModels = new ArrayList<>();
        if (!invoices.isPresent()) {
            return null;
        }
        Invoice invoice = invoices.get();
        InvoiceUiModel invoiceUiModel = new InvoiceUiModel();
        invoiceUiModel.setDesc(invoice.getDescription());
        invoiceUiModel.setId(invoice.getId());
        invoiceUiModel.setPrice(invoice.getAmount());
        invoiceUiModel.setShopName(invoice.getMerchant().getShopName());
        invoiceUiModel.setStatus(invoice.getStatus());
        invoiceUiModel.setDate(invoice.getRegdatetime());
        invoiceUiModel.setTimeout(15);
        invoiceUiModel.setQr(invoice.getQr());
        invoiceUiModel.setSymbol("IRR");
        if (invoice.getStatus().equals("success") || invoiceUiModel.getRemaining() == 0) {
            return invoiceUiModel;
        }
        String status = priceDiscovery.getStatus(invoice.getId());
        if (status.equals("Verfied")) {
            invoice.setStatus("success");
            invoiceJpaRepository.save(invoice);
            invoiceUiModel.setStatus("success");
        }
        return invoiceUiModel;
    }


    /**
     * this method implement for MVP test shoping user check anywherepay is work or not ?
     * @param qrCode
     * @return
     */
    @CrossOrigin
    @GetMapping(value="/anywherepay", produces = "application/json")
    public PaymentSuccess rialToBtc(@RequestParam(value = "qrcode", required = true) String qrCode){
       Optional<Invoice> invoice =   invoiceJpaRepository.findInvoiceByQr(qrCode);
       if(invoice.isPresent()) {
           PaymentSuccess paymentSuccess = new PaymentSuccess();
           paymentSuccess.setAmount(invoice.get().getAmount());
           paymentSuccess.setShopName(invoice.get().getMerchant().getShopName());
           return paymentSuccess;
       }else
           throw  new BadRequest("THIS INVOICE NOT FOUND");
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