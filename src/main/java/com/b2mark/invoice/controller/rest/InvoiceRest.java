/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.core.Blockchain;
import com.b2mark.invoice.core.PriceDiscovery;
import com.b2mark.invoice.entity.ChangeCoinRequest;
import com.b2mark.invoice.entity.InvRequest;
import com.b2mark.invoice.entity.InvoiceResponse;
import com.b2mark.invoice.entity.PaymentSuccess;
import com.b2mark.invoice.entity.tables.Invoice;
import com.b2mark.invoice.entity.tables.InvoiceJpaRepository;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.b2mark.invoice.exception.BadRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/invoice")
@CrossOrigin
public class InvoiceRest {
    @Autowired
    InvoiceJpaRepository invoiceJpaRepository;
    @Autowired
    MerchantJpaRepository merchantJpaRepository;
    @Autowired
    PriceDiscovery priceDiscovery;
    @Autowired
    Blockchain blockchain;
    static Pattern pattern;

    static {
        pattern = Pattern.compile("^(?<category>.{3})_(?<merchant>\\d*)_(?<id>[a-zA-Z0-9]*)$");
    }

    //add invoive to merchant.
    @PostMapping
    public Invoice addInvoice(@RequestBody InvRequest inv) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(inv.getMobile());
        if (merchant.isPresent()) {
            if (!merchant.get().getApiKey().equals(inv.getApiKey())) {
                throw new BadRequest("Your Api_Key is not valid");
            }
            Invoice invoice = new Invoice();
            invoice.setMerchant(merchant.get());
            invoice.setRegdatetime(new Date());
            invoice.setStatus("waiting");
            invoice.setAmount(Long.parseLong(inv.getPrice()));
            invoice.setCallback(inv.getCallBackUri());
            invoice.setCurrency("IRR");//TODO: get this from merchant information.
            invoice.setDescription(inv.getDescription());
            invoice.setCategory(InvoiceCategory.POS.getInvoiceCategory());
            invoice.setOrderid(inv.getOrderId());
            invoice.setUserdatetime(new Date());
            invoice.setQr("");
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            return invoice1;
        } else {
            throw new BadRequest("Merchant mobile number is not registered");
        }
    }


    /**
     * Change coin in specific invoice after check that.
     *
     * @return InvoiceResponse
     */
    @PutMapping("/changecoin")
    public InvoiceResponse changeCoin(@RequestBody ChangeCoinRequest changeCode) {
        InvoiceId invoiceid1 = dserInvoiceId(changeCode.getInvoiceId());
        Optional<Invoice> optionalInvoice = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceid1.getId(), invoiceid1.getMerchantId(), invoiceid1.getCategory().getInvoiceCategory());

        if (!optionalInvoice.isPresent()) {
            throw new BadRequest("this invoice number is invalid");
        }
        Invoice invoice = optionalInvoice.get();
        if (!optionalInvoice.get().getStatus().equals("waiting")) {
            throw new BadRequest("this invoice is not active");
        }
        String qrCode = blockchain.qrCode(changeCode.getCoinSymbol(), invoice.getAmount(), invoice.getId());
        invoice.setQr(qrCode);
        invoice = invoiceJpaRepository.save(invoice);
        InvoiceResponse invoiceResponse = convertInvoice(invoice);
        return invoiceResponse;
    }

    @CrossOrigin
    @GetMapping("/rialSat")
    public String rialToBtc(@RequestParam(value = "IRR", required = true) long rial) {
        return (new Date()).toString();
        // return priceDiscovery.getRialToSatoshi(rial) + "";
    }


    @GetMapping(value = "/all", produces = "application/json")
    public List<InvoiceResponse> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                               @RequestParam(value = "apiKey", required = true) String apikey) {
        List<Invoice> invoices = invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantApiKeyOrderById(mobileNum, apikey);
        List<InvoiceResponse> invoiceResponses = new ArrayList<>();
        for (Invoice inv : invoices) {
            InvoiceResponse invoiceResponse = convertInvoice(inv);
            if (inv.getStatus().equals("success") || invoiceResponse.getRemaining() == 0) {
                System.out.println("JEUSDEBUG:INVOICE ID:" + invoiceResponse.getId() + " is " + inv.getStatus() + " remaining:" + invoiceResponse.getRemaining());
            } else {
                String status = blockchain.getStatus(inv.getId());
                if (status.equals("Verified")) {
                    inv.setStatus("success");
                    System.out.println("JEUSDEBUG:INVOICE ID" + invoiceResponse.getId() + "  getstatus:" + status);
                    invoiceJpaRepository.save(inv);
                    invoiceResponse.setStatus("success");
                }
            }
            invoiceResponses.add(invoiceResponse);
        }
        return invoiceResponses;
    }


    @GetMapping(produces = "application/json")
    public InvoiceResponse getById(@RequestParam(value = "id", required = true) String invid) {
        InvoiceId invoiceId = dserInvoiceId(invid);
        Optional<Invoice> invoices = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceId.getId(), invoiceId.getMerchantId(), invoiceId.category.getInvoiceCategory());
        if (!invoices.isPresent()) {
            return null;
        }
        Invoice invoice = invoices.get();
        InvoiceResponse invoiceResponse = convertInvoice(invoice);
        if (invoice.getStatus().equals("success") || invoiceResponse.getRemaining() == 0) {
            return invoiceResponse;
        }
        String status = blockchain.getStatus(invoice.getId());
        if (status.equals("Verified")) {
            System.out.println("JEUSDEBUG:++++++change status to success");
            invoice.setStatus("success");
            invoiceJpaRepository.save(invoice);
            invoiceResponse.setStatus("success");
        }
        return invoiceResponse;
    }


    /**
     * this method implement for MVP test shoping user check anywherepay is work or not?
     *
     * @param qrCode
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/anywherepay", produces = "application/json")
    public PaymentSuccess rialToBtc(@RequestParam(value = "qrcode", required = true) String qrCode) {
        Optional<Invoice> invoice = invoiceJpaRepository.findInvoiceByQr(qrCode);
        if (invoice.isPresent()) {
            PaymentSuccess paymentSuccess = new PaymentSuccess();
            paymentSuccess.setAmount(invoice.get().getAmount());
            paymentSuccess.setShopName(invoice.get().getMerchant().getShopName());
            return paymentSuccess;
        } else
            throw new BadRequest("THIS INVOICE NOT FOUND");
    }


    /**
     * deserialize String invoice to invoiceId.
     *
     * @param strInvId
     * @return
     */
    private InvoiceId dserInvoiceId(String strInvId) {
        System.out.println("JEUSDEBUG: id is :" + strInvId);
        Matcher matcher = pattern.matcher(strInvId);
        if (!matcher.matches()) {
            throw new BadRequest("This id is not valid");
        }

        System.out.println("JEUSDEBUG: THIS IS MATCH:------");
        String strCat = matcher.group("category");
        String strId = matcher.group("id");
        String strMerchId = matcher.group("merchant");

        long setId = Long.parseLong(strId, 12);
        long merchant = Long.parseLong(strMerchId);

        InvoiceId invoiceId = new InvoiceId();
        invoiceId.setCategory(InvoiceCategory.fromString(strCat));
        invoiceId.setId(setId);
        invoiceId.setMerchantId(merchant);
        return invoiceId;
    }


    /**
     * convert invoice for show to the user.
     * @param inv
     * @return
     */
    private InvoiceResponse convertInvoice(Invoice inv) {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setDesc(inv.getDescription());
        invoiceResponse.setId(inv.getId());
        invoiceResponse.setPrice(inv.getAmount());
        invoiceResponse.setShopName(inv.getMerchant().getShopName());
        invoiceResponse.setStatus(inv.getStatus());
        invoiceResponse.setDate(inv.getRegdatetime());
        invoiceResponse.setQr(inv.getQr());
        invoiceResponse.setTimeout(15);
        invoiceResponse.setSymbol("IRR");
        return invoiceResponse;
    }

    @Setter
    @Getter
    private class InvoiceId {
        private long id;
        private long merchantId;
        private InvoiceCategory category;
    }


}