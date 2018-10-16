/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.common.exceptions.ExceptionResponse;
import com.b2mark.invoice.core.Blockchain;
import com.b2mark.invoice.core.EmailService;
import com.b2mark.invoice.core.PriceDiscovery;
import com.b2mark.invoice.entity.ChangeCoinRequest;
import com.b2mark.invoice.entity.InvRequest;
import com.b2mark.invoice.entity.InvoiceResponse;
import com.b2mark.invoice.entity.PaymentSuccess;
import com.b2mark.invoice.entity.tables.*;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.b2mark.invoice.exception.*;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
        @ApiResponse(code = 401, message = "Unauthorized to access to this service"), @ApiResponse(code = 400, message = "Bad request")})
@RestController
@RequestMapping("/invoice")
@CrossOrigin
public class InvoiceRest {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceRest.class);
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
    @Autowired
    EmailService emailService;

    private final String unauthorized = "Merchant id or apiKey is not valid";

    static Pattern pattern;

    static {
        pattern = Pattern.compile("^(?<category>.{3})_(?<merchant>\\d*)_(?<id>[a-zA-Z0-9]*)$");
    }

    //add invoive to merchant.
    @PostMapping
    public InvoiceResponse addInvoice(@RequestBody InvRequest inv) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(inv.getMobile());
        if (inv.getOrderId() == null || inv.getOrderId().isEmpty())
            throw new PublicException(ExceptionsDictionary.PARAMETERNOTFOUND, "Order id is empty");
        if (!merchant.isPresent()) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        } else if (!merchant.get().getApiKey().equals(inv.getApiKey())) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        if (inv.getDescription().length() > 1000) {
            throw new PublicException(ExceptionsDictionary.ARGUMENTTOOLONG, "description is too long.");
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
        try {
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            InvoiceResponse invoiceResponse = invoiceResponseFactory(invoice1, InvoiceResponse.Role.merchant);
            return invoiceResponse;
        } catch (Exception ex) {
            if (ex.getMessage().startsWith("could not execute statement; SQL [n/a]; constraint [orderIdPerMerchant]"))
                throw new PublicException(ExceptionsDictionary.IDISNOTUNIQUE, "Order id is not unique");
            else
                throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "undefined error");
        }

    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public final ResponseEntity<ExceptionResponse> handleArgumentFailed(Exception ex, WebRequest request) {
        ExceptionsDictionary exceptionsDictionary = ExceptionsDictionary.PARAMETERNOTFOUND;
        ExceptionResponse exceptionResponse = new ExceptionResponse(exceptionsDictionary);
        return new ResponseEntity<>(exceptionResponse, exceptionsDictionary.getHttpStatus());
    }

    /**
     * Change coin in specific invoice after check that.
     *
     * @return InvoiceResponse
     */

    @PutMapping(value = "/coinselection", produces = "application/json")
    @ApiOperation(value = "change coin specific invoice")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Bad request")})
    public InvoiceResponse changeCoin(@RequestBody ChangeCoinRequest changeCode) {
        InvoiceId invoiceid1 = dserInvoiceId(changeCode.getInvoiceId());
        Optional<Invoice> optionalInvoice = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceid1.getId(), invoiceid1.getMerchantId(), invoiceid1.getCategory().getInvoiceCategory());

        InvoiceResponse invoiceResponse;
        if (!optionalInvoice.isPresent()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "invalid invoice id");
        }
        Invoice invoice = optionalInvoice.get();
        if (invoice.timeExtremeExpired()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this invoice number is not active");
        }

        String qrCode = blockchain.qrCode(Coin.fromSymbol(changeCode.getCoinSymbol()), Coin.IRANRIAL, invoice.getAmount() + "", invoice.getId());
        invoice.setQr(qrCode);
        invoice = invoiceJpaRepository.save(invoice);
        PayerLog payerLog = new PayerLog();
        payerLog.setMobile(changeCode.getMobileNum());
        payerLog.setEmail(changeCode.getEmail());
        payerLog.setInform(changeCode.isInform());
        payerLog.setInvoice(invoice.getId());
        payerLog.setDatetime(new Date());
        payerLog.setQrcode(qrCode);
        payerLogJpaRepository.save(payerLog);
        invoiceResponse = invoiceResponseFactory(invoice, InvoiceResponse.Role.user);
        return invoiceResponse;
    }


    @GetMapping(value = "/all", produces = "application/json")
    @ApiOperation(value = "return invoices pagination if not found 204 content not found")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
            @ApiResponse(code = 401, message = "Unauthorized to access to this service"), @ApiResponse(code = 400, message = "Bad request")})
    public List<InvoiceResponse> getAllInvoice(@RequestParam(value = "mob", required = true) String mobileNum,
                                               @RequestParam(value = "apiKey", required = true) String apikey,
                                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                               @RequestParam(value = "dir", defaultValue = "asc", required = false) String dir,
                                               @RequestParam(value = "status", defaultValue = "all", required = false) String st) {
        Preconditions.checkArgument(size < 200);
        Sort.Direction direction = Sort.Direction.fromString(dir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, new Sort(direction, new String[]{"regdatetime"}));
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        List<Invoice> invoices = null;
                if(merchant.get().getMobile().equals("09120453931"))//TODO: this is hardcode have to remove this.
                    invoices = invoiceJpaRepository.findAll();
                else
                    invoices = invoiceJpaRepository.findInvoicesByMerchantMobileAndMerchantApiKey(pageable, mobileNum, apikey);
        if (invoices.size() <= 0) {
            throw new PublicException(ExceptionsDictionary.CONTENTNOTFOUND, "content not found");
        }
        List<InvoiceResponse> invoiceResponses = new ArrayList<>();
        for (Invoice invoice : invoices) {
            InvoiceResponse invoiceResponse = null;
            if ((invoiceResponse = invoiceResponseFactory(invoice, InvoiceResponse.Role.merchant)) != null)
                invoiceResponses.add(invoiceResponse);
        }
        return invoiceResponses;
    }


    @GetMapping(produces = "application/json")
    public InvoiceResponse getById(@RequestParam(value = "id", required = true) String invid,
                                   @RequestParam(value = "mob", required = false) String mobileNum,
                                   @RequestParam(value = "apiKey", required = false) String apikey) {
        InvoiceId invoiceId = dserInvoiceId(invid);
        InvoiceResponse invoiceResponse;
        InvoiceResponse.Role role = InvoiceResponse.Role.user;
        if (mobileNum != null) {
            Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
            if (!merchant.isPresent()) {
                throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
            } else if (!merchant.get().getApiKey().equals(apikey)) {
                throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
            }
            role = InvoiceResponse.Role.merchant;
        }
        Optional<Invoice> invoices = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceId.getId(), invoiceId.getMerchantId(), invoiceId.category.getInvoiceCategory());
        if (!invoices.isPresent()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This invoice is not exist");
        }
        invoiceResponse = invoiceResponseFactory(invoices.get(), role);
        return invoiceResponse;
    }


    /**
     * this method implement for MVP test shoping user check anywherepay is work or not?
     *
     * @param qrCode
     * @return
     */
    @CrossOrigin
    //@GetMapping(value = "/anywherepay", produces = "application/json")
    private PaymentSuccess rialToBtc(@RequestParam(value = "qrcode", required = true) String qrCode) {
        Optional<Invoice> invoice = invoiceJpaRepository.findInvoiceByQr(qrCode);
        if (invoice.isPresent()) {
            PaymentSuccess paymentSuccess = new PaymentSuccess();
            paymentSuccess.setAmount(invoice.get().getAmount());
            paymentSuccess.setShopName(invoice.get().getMerchant().getShopName());
            return paymentSuccess;
        } else
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "invalid invoice id");
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
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This invoice id is not valid");
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


    private InvoiceResponse invoiceResponseFactory(Invoice invoice, InvoiceResponse.Role role) {
        InvoiceResponse invoiceResponse = new InvoiceResponse(role);
        Coin coin = null;
        if (invoice.timeExtremeExpired() && role != InvoiceResponse.Role.merchant) {
            return null;
        }
        if (invoice.isSuccess()) {
            invoiceResponse.setInvoice(invoice);
            return invoiceResponse;
        } else if (invoice.isWaiting()) {
            if ((coin = invoice.getBlockchainCoin()) != null) {
                if (invoice.checkAcceptPayment(blockchain.getStatus(invoice.getId(), coin))) {
                    invoice.setStatus("success");
                    invoiceJpaRepository.save(invoice);
                    invoiceResponse.setInvoice(invoice);
                    sendInform(invoiceResponse);
                    return invoiceResponse;
                }
            }
            if (invoice.timeExpired()) {
                invoice.setStatus("failed");
                invoiceJpaRepository.save(invoice);

                invoiceResponse.setInvoice(invoice);
                return invoiceResponse;
            } else {
                invoiceResponse.setInvoice(invoice);
                return invoiceResponse;
            }
        } else if (invoice.isFailed()) {
            invoiceResponse.setInvoice(invoice);
            return invoiceResponse;
        }
        return null;
    }

    @Setter
    @Getter
    private class InvoiceId {
        private long id;
        private long merchantId;
        private InvoiceCategory category;
    }

    @GetMapping("/logtest")
    public String ok(){
        System.out.println("THIS IS WORK");
        LOG.debug("THIS IS WORK [LOGGER.DEBUG]");
        LOG.info("THIS IS WORK [LOGGER.INFO]");
        LOG.warn("THIS IS WORK [LOGGER.FATAL]");
        LOG.error("THIS IS WORK [LOGGER.ERROR]");
        return "OK";
    }

    public void sendInform(InvoiceResponse invoiceResponse) {
        Optional<PayerLog> payerLog = payerLogJpaRepository.findTopByInvoiceOrderByIdDesc(invoiceResponse.getInvoice().getId());
        if (!payerLog.isPresent())
            return;
        if (!payerLog.get().isInform())
            return;
        String email = payerLog.get().getEmail();
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            Map<String, Object> map = new HashMap<>();
            map.put("message", "پرداخت شما با موفقیت انجام شد");
            map.put("invoiceid", invoiceResponse.getId());
            map.put("amount", invoiceResponse.getPrice() + "");
            map.put("orderid", invoiceResponse.getOrderId());
            map.put("shopname", invoiceResponse.getShopName());
            map.put("callbackUrl",invoiceResponse.getCallback());
            emailService.sendMail(email, "mailTemplate", map);
        } catch (AddressException ex) {
            System.out.println("email address is not valid");
        } catch (MessagingException e) {
            System.err.println("error exception send mail");
        }
    }


}

