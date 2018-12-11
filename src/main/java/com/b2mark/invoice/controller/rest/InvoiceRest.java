package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.common.entity.Pagination;
import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.common.exceptions.ExceptionResponse;
import com.b2mark.invoice.core.Blockchain;
import com.b2mark.invoice.core.EmailService;
import com.b2mark.invoice.core.PriceDiscovery;
import com.b2mark.invoice.entity.ChangeCoinRequest;
import com.b2mark.invoice.entity.InvRequest;
import com.b2mark.invoice.entity.InvoiceResponse;
import com.b2mark.invoice.entity.price.Price;
import com.b2mark.invoice.entity.tables.*;
import com.b2mark.invoice.enums.InvoiceCategory;
import com.b2mark.invoice.exception.*;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * <h1>Invoice Rest</h1>
 * Create invoices and get QR_Code from blockchain layer.
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

@ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
        @ApiResponse(code = 401, message = "Unauthorized to access to this service"), @ApiResponse(code = 400, message = "Bad request")})
@RestController
@RequestMapping("/invoice")
@CrossOrigin
public class InvoiceRest {
    private static final Logger LOG = LoggerFactory.getLogger(InvoiceRest.class);
    private final InvoiceJpaRepository invoiceJpaRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final PriceDiscovery priceDiscovery;
    private final PayerLogJpaRepository payerLogJpaRepository;
    private final SettleupJpsRepository settleupJpsRepository;
    private final Blockchain blockchain;
    private final EmailService emailService;

    private final static String unauthorized = "Merchant id or apikey is not valid";
    private static String dailyLimit = "50000000";

    @Autowired
    public InvoiceRest(InvoiceJpaRepository invoiceJpaRepository, MerchantJpaRepository merchantJpaRepository,
                       PriceDiscovery priceDiscovery, PayerLogJpaRepository payerLogJpaRepository,
                       Blockchain blockchain, EmailService emailService, SettleupJpsRepository settleupJpsRepository) {
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.merchantJpaRepository = merchantJpaRepository;
        this.priceDiscovery = priceDiscovery;
        this.payerLogJpaRepository = payerLogJpaRepository;
        this.blockchain = blockchain;
        this.emailService = emailService;
        this.settleupJpsRepository = settleupJpsRepository;
    }

    /**
     * create new invoice by merchant
     */
    @ApiOperation(value = "create new invoice.")
    @PostMapping
    public InvoiceResponse addInvoice(@RequestBody InvRequest invReq) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(invReq.getMobile());
        if (invReq.getOrderId() == null || invReq.getOrderId().isEmpty())
            throw new PublicException(ExceptionsDictionary.PARAMETERNOTFOUND, "Order id is empty");
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(invReq.getApikey()))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        if (invReq.getDescription().length() > 1000)
            throw new PublicException(ExceptionsDictionary.ARGUMENTTOOLONG, "description is too long.");

        if (invReq.getMerchantCur() == null)
            invReq.setMerCoin(Coin.IRANRIAL);

        if (invReq.getCurrency() == null)
            invReq.setPayCur(Coin.IRANRIAL);

        if (invReq.getMerCoin() != Coin.IRANRIAL)
            throw new PublicException(ExceptionsDictionary.UNSUPPORTEDCOIN, "Destination coin is not supported.");

        //TODO: have to change from hardcode to implementation full structure.
        BigDecimal merchantAmount = new BigDecimal(0);
        if (invReq.getMerchantCur().equals(invReq.getCurrency()))
            merchantAmount = new BigDecimal(invReq.getPrice());
        else {
            Coin baseCoin = Coin.fromSymbol(invReq.getCurrency());
            Coin toCoin = Coin.fromSymbol(invReq.getMerchantCur());
            Price price = priceDiscovery.getPrice(baseCoin, toCoin, "GENERAL");
             merchantAmount = new BigDecimal(price.getPrice()).multiply(new BigDecimal(invReq.getPrice()));

        }
        BigDecimal bigdecimal = invoiceJpaRepository.sumAmountPerMerchantPerDay(merchant.get().getId(), new Date(), new Date());
        bigdecimal = bigdecimal == null ? new BigDecimal(0): bigdecimal;
        bigdecimal = bigdecimal.add(merchantAmount);
        if (bigdecimal.compareTo(new BigDecimal(dailyLimit)) >= 0 && !(invReq.getMobile().equals("09120779807") || invReq.getMobile().equals("09120453931"))) {
            throw new PublicException(ExceptionsDictionary.ARGUMENTTOOLONG, "invoice amount exceeded your daily limit");
        }

        Invoice invoice = new Invoice();
        invoice.setMerchant(merchant.get());
        invoice.setRegdatetime(new Date());
        invoice.setStatus("waiting");
        invoice.setPayerAmount(new BigDecimal(invReq.getPrice()));
        invoice.setPayerCur(invReq.getCurrency());//TODO: get this from merchant information priorit request then propery then system
        invoice.setMerchantAmount(merchantAmount);
        invoice.setMerchantCur(invReq.getMerchantCur());
        invoice.setDescription(invReq.getDescription());
        invoice.setCategory(InvoiceCategory.POS.getInvoiceCategory());
        invoice.setOrderid(invReq.getOrderId());
        invoice.setUserdatetime(new Date());
        invoice.setQr("");
        try {
            Invoice invoice1 = invoiceJpaRepository.save(invoice);
            LOG.info("action:addinvoice,shop_name:{},merchant_coin:{},payer_coin:{},amount:{},mobile:{},order_id:{},description:{},apikey:*****,",
                    merchant.get().getShopName(), invoice.getMerchantCur(), invoice.getPayerCur(), invReq.getPrice(), invReq.getMobile(), invReq.getOrderId(), invReq.getDescription());
            return invoiceResponseFactory(invoice1, InvoiceResponse.Role.merchant);
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
        Invoice.InvoiceId invoiceid = Invoice.dSerializeInvoice(changeCode.getInvoiceId());
        Optional<Invoice> optionalInvoice = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceid.getId(), invoiceid.getMerchantId(), invoiceid.getCategory().getInvoiceCategory());

        if (!optionalInvoice.isPresent()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "invalid invoice id");
        }
        Invoice invoice = optionalInvoice.get();
        if (invoice.timeExtremeExpired()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this invoice number is not active");
        }

        String qrCode = blockchain.qrCode(Coin.fromSymbol(changeCode.getCoinSymbol()), Coin.fromSymbol(invoice.getPayerCur()), invoice.getPayerAmount() + "", invoice.getId());
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
        LOG.info("action:coinselection,payer_coin:{},invoice_id:{},email:{},mobile:{},inform:{}",
                changeCode.getCoinSymbol(), changeCode.getInvoiceId(), changeCode.getEmail(), changeCode.getMobileNum(), changeCode.isInform());
        return invoiceResponseFactory(invoice, InvoiceResponse.Role.user);
    }


    @GetMapping(value = "/all", produces = "application/json")
    @ApiOperation(value = "return invoices pagination if not found 204 content not found")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
            @ApiResponse(code = 401, message = "Unauthorized to access to this service"), @ApiResponse(code = 400, message = "Bad request")})
    public Pagination<InvoiceResponse> getAllInvoicev2(@RequestParam(value = "mob") String mobileNum,
                                                       @RequestParam(value = "apikey") String apikey,
                                                       @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                       @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                                       @RequestParam(value = "dir", defaultValue = "asc", required = false) String dir,
                                                       @RequestParam(value = "status", defaultValue = "success,waiting,failed,settled", required = false) String status,
                                                       HttpServletRequest request) {


        Preconditions.checkArgument(size <= 200);
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);


        Sort.Direction direction = Sort.Direction.fromString(dir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, new Sort(direction, "id"));

        long count;
        List<Invoice> invoices;
        String[] strStatus = status.split(",");
        List<String> statuses = Arrays.asList(strStatus);
        if (merchant.get().getMobile().equals("09120453931")) {//TODO: this is hardcode have to remove this.
            count = invoiceJpaRepository.countInvoiceByStatusIn(statuses);
            invoices = invoiceJpaRepository.findInvoicesByStatusIn(pageable, statuses);
        } else {
            count = invoiceJpaRepository.countAllByMerchantMobileAndStatusIn(mobileNum, statuses);
            invoices = invoiceJpaRepository.findAllByMerchantMobileAndStatusIn(pageable, mobileNum, statuses);
        }


        Pagination<InvoiceResponse> invoiceResponses = new Pagination<>();
        invoiceResponses.setName("Invoice");
        invoiceResponses.setCount(count);
        invoiceResponses.setPage(page);
        invoiceResponses.setSize(size);
        invoiceResponses.setStatus(200);
        invoiceResponses.setApiAddress(request.getRequestURL().toString() + "?" + request.getQueryString());

        for (Invoice invoice : invoices) {
            InvoiceResponse invoiceResponse;
            if ((invoiceResponse = invoiceResponseFactory(invoice, InvoiceResponse.Role.merchant)) != null)
                invoiceResponses.add(invoiceResponse);
        }

        return invoiceResponses;
    }


    @GetMapping(produces = "application/json")
    public InvoiceResponse getById(@RequestParam(value = "id") String invid,
                                   @RequestParam(value = "mob", required = false) String mobileNum,
                                   @RequestParam(value = "apikey", required = false) String apikey) {
        Invoice.InvoiceId invoiceId = Invoice.dSerializeInvoice(invid);
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
        Optional<Invoice> invoices = invoiceJpaRepository.findByIdAndMerchant_IdAndCategory(invoiceId.getId(), invoiceId.getMerchantId(), invoiceId.getCategory().getInvoiceCategory());
        if (!invoices.isPresent()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This invoice is not exist");
        }
        invoiceResponse = invoiceResponseFactory(invoices.get(), role);
        return invoiceResponse;
    }

    @GetMapping(value = "/byorderid", produces = "application/json")
    public InvoiceResponse getByOrderId(@RequestParam(value = "id") String orderId,
                                        @RequestParam(value = "mob") String mobileNum,
                                        @RequestParam(value = "apikey") String apikey) {
        InvoiceResponse invoiceResponse;
        InvoiceResponse.Role role;
        Merchant merchant;
        Optional<Merchant> OpMerchant = merchantJpaRepository.findByMobile(mobileNum);
        if (!OpMerchant.isPresent()) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        } else if (!OpMerchant.get().getApiKey().equals(apikey)) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        merchant = OpMerchant.get();
        role = InvoiceResponse.Role.merchant;

        Optional<Invoice> invoices = invoiceJpaRepository.findByOrderidAndMerchant_Id(orderId, merchant.getId());
        if (!invoices.isPresent()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This orderId does not exist.");
        }
        invoiceResponse = invoiceResponseFactory(invoices.get(), role);
        return invoiceResponse;
    }



    private InvoiceResponse invoiceResponseFactory(Invoice invoice, InvoiceResponse.Role role) {
        InvoiceResponse invoiceResponse = new InvoiceResponse(role);
        Coin coin;
        if (invoice.timeExtremeExpired() && role != InvoiceResponse.Role.merchant) {
            return null;
        }
        if (invoice.isSettled()) {
            invoiceResponse.setInvoice(invoice);
            return invoiceResponse;
        }
        if (invoice.isSuccess()) {
            boolean isSettle = settleupJpsRepository.existsInvoicesById(invoice.getId());
            if (isSettle) {
                invoice.setStatus("settled");
                invoiceJpaRepository.save(invoice);
            }
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


    private void sendInform(InvoiceResponse invoiceResponse) {
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
            map.put("payeramount", invoiceResponse.getPayerAmount().setScale(2, RoundingMode.UP));
            map.put("payercoin", invoiceResponse.getPayerCur());
            map.put("orderid", invoiceResponse.getOrderId());
            map.put("shopname", invoiceResponse.getShopName());
            map.put("callbackUrl", invoiceResponse.getCallback());
            emailService.sendMail(email, "mailTemplate", map);
        } catch (AddressException ex) {
            LOG.warn("action:EMail_Invalid,email_address:{},cause:{}", email, ex.getCause());
        } catch (MessagingException e) {
            LOG.error("action:Send_Mail,email_address:,cause:{}", email, e.getCause());
        }
    }


}

