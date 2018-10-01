/**
 * <h1></h1>
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
import com.b2mark.invoice.entity.tables.*;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.b2mark.invoice.exception.BadRequest;
import com.b2mark.invoice.exception.ContentNotFound;
import com.b2mark.invoice.exception.Unauthorized;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    PayerLogJpaRepository payerLogJpaRepository;
    @Autowired
    Blockchain blockchain;
    static Pattern pattern;

    static {
        pattern = Pattern.compile("^(?<category>.{3})_(?<merchant>\\d*)_(?<id>[a-zA-Z0-9]*)$");
    }

    //add invoive to merchant.
    @PostMapping
    public InvoiceResponse addInvoice(@RequestBody InvRequest inv) {
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
            invoice.setCurrency("IRR");//TODO: get this from merchant information.
            invoice.setDescription(inv.getDescription());
            invoice.setCategory(InvoiceCategory.POS.getInvoiceCategory());
            invoice.setOrderid(inv.getOrderId());
            invoice.setUserdatetime(new Date());
            invoice.setQr("");
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            InvoiceResponse invoiceResponse = convertInvoice(invoice1);
            return invoiceResponse;
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
    @ApiOperation(value = "change coin specific invoice ")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Bad request")})
    public InvoiceResponse changeCoin(@RequestBody ChangeCoinRequest changeCode) {
        InvoiceId invoiceid1 = dserInvoiceId(changeCode.getInvoiceId());
        Optional<Invoice> optionalInvoice = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceid1.getId(), invoiceid1.getMerchantId(), invoiceid1.getCategory().getInvoiceCategory());

        InvoiceResponse invoiceResponse ;
        if (!optionalInvoice.isPresent()) {
            throw new BadRequest("this invoice number is invalid");
        }
        Invoice invoice = optionalInvoice.get();
        invoiceResponse = convertInvoice(invoice);
        if (invoiceResponse.getRemaining() < -20) {
            throw new ContentNotFound("this invoice number not found");
        }
        if (!invoiceResponse.getStatus().equals("waiting")) {
            throw new BadRequest("this invoice is not active");
        }
        String qrCode = blockchain.qrCode(changeCode.getCoinSymbol(), invoice.getAmount(), invoice.getId());
        invoice.setQr(qrCode);
        invoice = invoiceJpaRepository.save(invoice);
        PayerLog payerLog = new PayerLog();
        payerLog.setMobile(changeCode.getMobileNum());
        payerLog.setEmail(changeCode.getEmail());
        payerLog.setInform(changeCode.isInform());
        payerLog.setInvoice(invoice.getId());
        payerLog.setDatetime(new Date());
        payerLog.setQrcode(qrCode);
        PayerLog payerLog1 = payerLogJpaRepository.save(payerLog);
        invoiceResponse = convertInvoice(invoice);
        return invoiceResponse;
    }



    @CrossOrigin
    @GetMapping("/rialSat")
    public String rialToBtc(@RequestParam(value = "IRR", required = true) long rial) {
        return (new Date()).toString();
        // return priceDiscovery.getRialToSatoshi(rial) + "";
    }
    @GetMapping(value = "/all", produces = "application/json")
    @ApiOperation(value = "return invoices pagination if not found 204 content not found")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
            @ApiResponse(code = 401, message = "Unauthorized to access to this service")})
    public List<InvoiceResponse> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                               @RequestParam(value = "apiKey", required = true) String apikey,
                                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                               @RequestParam(value = "dir", defaultValue = "asc", required = false) String dir,
                                               @RequestParam(value = "status", defaultValue = "all", required = false) String st) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (dir.toLowerCase().equals("asc")) {
            direction = Sort.Direction.ASC;
        } else if (dir.toLowerCase().equals("desc")) {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, new Sort(direction, new String[]{"regdatetime"}));

        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (!merchant.isPresent()) {
            if (!merchant.get().getApiKey().equals(apikey))
                throw new Unauthorized("this apikey is not valid.");
        }

        List<Invoice> invoices = invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantApiKeyOrderById(pageable, mobileNum, apikey);
        if (invoices.size() <= 0) {
            throw new ContentNotFound("content not found.");
        }
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
            throw new BadRequest("This invoice is not exist");
        }
        Invoice invoice = invoices.get();
        InvoiceResponse invoiceResponse = convertInvoice(invoice);
        if (invoiceResponse.getRemaining() <= 0) {
            if (invoiceResponse.getRemaining() < -20) {
                throw new ContentNotFound("this invoice number not found");
            }
            invoiceResponse.setStatus("failed");
            return invoiceResponse;
        }
        if (invoice.getStatus().equals("success")) {
            return invoiceResponse;
        }
        if (invoice.getQr().isEmpty() || invoice.getQr() == null) {
            String status = blockchain.getStatus(invoice.getId());
            if (status.equals("Verified")) {
                System.out.println("JEUSDEBUG:++++++change status to success");
                invoice.setStatus("success");
                invoiceJpaRepository.save(invoice);
                invoiceResponse.setStatus("success");
            }
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
     *
     * @param inv
     * @return
     */
    private InvoiceResponse convertInvoice(Invoice inv) {
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setDesc(inv.getDescription());
        invoiceResponse.setId(inv.getInvoiceId());
        invoiceResponse.setOrderId(inv.getOrderid());
        invoiceResponse.setPrice(inv.getAmount());
        invoiceResponse.setShopName(inv.getMerchant().getShopName());
        invoiceResponse.setStatus(inv.getStatus());
        invoiceResponse.setDate(inv.getRegdatetime());
        invoiceResponse.setQr(inv.getQr());
        invoiceResponse.setTimeout(15);
        invoiceResponse.setSymbol("IRR");
        invoiceResponse.setCallback(inv.getMerchant().getCallback());
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