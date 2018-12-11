package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.common.entity.Pagination;
import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.entity.RequestSettle;
import com.b2mark.invoice.entity.SettleUpResponse;
import com.b2mark.invoice.entity.tables.*;
import com.b2mark.invoice.exception.PublicException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <h1></h1>
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@ApiResponses(value = {@ApiResponse(code = 204, message = "service and uri is ok but content not found"),
        @ApiResponse(code = 401, message = "Unauthorized to access to this service"), @ApiResponse(code = 400, message = "Bad request")})
@RestController
@RequestMapping("/settleup")
@CrossOrigin
public class SettleupRest {
    private static final Logger LOG = LoggerFactory.getLogger(SettleupRest.class);
    private final static String unauthorized = "Merchant id or apikey is not valid";
    private final SettleupJpsRepository settleupJpsRepository;
    private final MerchantJpaRepository merchantJpaRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;

    @Autowired
    public SettleupRest(SettleupJpsRepository settleupJpsRepository, MerchantJpaRepository merchantJpaRepository, InvoiceJpaRepository invoiceJpaRepository) {
        this.settleupJpsRepository = settleupJpsRepository;
        this.merchantJpaRepository = merchantJpaRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
    }


    @PostMapping("/add")
    public Settleup settleUp1(@RequestBody RequestSettle requestSettle) {

        if (!requestSettle.getMob().equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        Optional<Merchant> adminUser = merchantJpaRepository.findByMobile(requestSettle.getMob());
        if (!adminUser.isPresent()) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        } else if (!adminUser.get().getApiKey().equals(requestSettle.getApikey())) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }

        Coin settleupCurrency = Coin.fromSymbol(requestSettle.getCurrency());
        if( settleupCurrency != Coin.IRANRIAL)
        {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this currency for settleup is not valid");
        }

        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(requestSettle.getMerMobile());
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this merchant id (mobile) is not valid");

        Date nowDate = new Date();
        if(requestSettle.getDatetime().compareTo(nowDate) > 0)
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this date time is not valid");

        Set<Long> invoiceId = requestSettle.getInvoiceIds().parallelStream().map(s -> Invoice.dSerializeInvoice(s).getId()).collect(Collectors.toSet());
        List<Invoice> invoices = invoiceJpaRepository.findInvoicesByIdIn(invoiceId);
        if (!merchant.get().getCardNumber().equals(requestSettle.getDestCard())) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "this card number is not valid for this merchant");
        }

        List<String> notSuccess = invoices.parallelStream().filter(s -> !s.getStatus().equals("success")).map(Invoice::getInvoiceId).collect(Collectors.toList());
        if (!notSuccess.isEmpty()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "This invoices is not success." + StringUtils.join(notSuccess, ','));
        }


        List<String> notTrueMerchants = invoices.parallelStream().filter(s -> s.getMerchant().getId() != merchant.get().getId()).map(Invoice::getInvoiceId).collect(Collectors.toList());
        if (!notTrueMerchants.isEmpty()) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "this invoices is not match for merchant merchant:" + merchant.get().getShopName() + " Invoices:" + notTrueMerchants);
        }

        Function<Invoice ,BigDecimal> totalMapper = invoice -> invoice.getMerchantAmount();
        BigDecimal sumLong  = invoices.stream().map(totalMapper).reduce(BigDecimal.ZERO,BigDecimal::add);
        if (sumLong.compareTo( requestSettle.getAmount())  != 0) {
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT, "SUM amount of invoices is[" + sumLong + "] you'r amount is " + requestSettle.getAmount());
        }

        Set<Invoice> setInvoice = new HashSet<>(invoices);
        Settleup settleup = new Settleup();
        settleup.setMerchant(merchant.get());
        settleup.setTxid(requestSettle.getTxid());
        settleup.setAmount(requestSettle.getAmount());
        settleup.setDatetime(requestSettle.getDatetime());
        settleup.setDestCard(requestSettle.getDestCard());
        settleup.setOriginCard(requestSettle.getOriginCard());
        settleup.setInvoices(setInvoice);

        Optional<Settleup> settleup1 = Optional.of(settleupJpsRepository.save(settleup));

        for(Invoice invoice :settleup1.get().getInvoices())
        {
            invoice.setStatus("settled");
            invoiceJpaRepository.save(invoice);
        }
        LOG.info("action:presettleAdd,amount:{},dest_card:{},date_time:{},origin_card:{},merchant_mobile:{},invoices:{},mobile:{}apikey:*****,",
               requestSettle.getAmount(),requestSettle.getDestCard(),requestSettle.getDatetime(),requestSettle.getOriginCard(),
                requestSettle.getMerMobile(),requestSettle.getInvoiceIds(), requestSettle.getMob());
        return settleup1.get();

    }

    @GetMapping
    public Pagination<SettleUpResponse> getAll(@RequestParam(value = "mob") String mob,
                                               @RequestParam(value = "apikey") String apikey,
                                               @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                               @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                               @RequestParam(value = "dir", defaultValue = "asc", required = false) String dir,
                                               HttpServletRequest request) {

        long count;
        Sort.Direction direction = Sort.Direction.fromString(dir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, new Sort(direction, "id"));
        Preconditions.checkArgument(size <= 200);
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        List<SettleUpResponse> settleUpResponses = new ArrayList<>();
        List<Settleup> settleups;
        if (mob.equals("09120453931")) {
            settleups  = settleupJpsRepository.findSettleupByIdNotNull(pageable);
            count = settleupJpsRepository.countSettleupsByIdNotNull();
        }else{
            settleups = settleupJpsRepository.findSettleupByMerchantMobile(pageable, mob);
            count = settleupJpsRepository.countSettleupsByMerchantMobile(mob);
        }

        Pagination<SettleUpResponse> pagination = new Pagination<>();
        pagination.setName("Settleup");
        pagination.setCount(count);
        pagination.setSize(size);
        pagination.setPage(page);
        pagination.setStatus(size);
        pagination.setApiAddress(request.getRequestURL().toString() + "?" + request.getQueryString());

        for(Settleup settleup : settleups)
        {
            SettleUpResponse settleUpResponse = new SettleUpResponse(settleup);
            settleUpResponses.add(settleUpResponse);
            pagination.add(settleUpResponse);
        }

        return pagination;
    }






    /**
     * send several invoice for get price and check validation that.
     * if invoice empty return all invoices for this merchant that doesn't pay.
     *
     * @param invoiceIds invoice id
     * @param merMob     merchant mobile
     * @param mob        user mobile
     * @param apikey     user api key
     * @return all debt
     */

    @GetMapping("/presettle")
    public Debt getPreSettle(@RequestParam(value = "invoices", defaultValue = "", required = false) String invoiceIds,
                             @RequestParam(value = "mermob", defaultValue = "") String merMob,
                             @RequestParam(value = "mob", defaultValue = "") String mob,
                             @RequestParam(value = "apikey", defaultValue = "") String apikey) {

        if (!mob.equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        if (merMob.isEmpty())
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "merchant argument is not valid");

        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        merchant = merchantJpaRepository.findByMobile(merMob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "mobile number for merchant is not valid");

        List<Invoice> invoices;
        if (invoiceIds.isEmpty()) {
            invoices = invoiceJpaRepository.getInvoiceDebtByMerchantId(merchant.get().getId());
        } else {
            List<Long> invs = Arrays.stream(invoiceIds.split(",")).map(Invoice::dSerializeInvoice).map(Invoice.InvoiceId::getId).collect(Collectors.toList());
            invoices = invoiceJpaRepository.getInvoiceDebtByMerchantId(merchant.get().getId(), invs);
        }
        Debt debt = new Debt();
        debt.setCardNumber(merchant.get().getCardNumber());
        debt.setShopName(merchant.get().getShopName());
        debt.setMobile(merchant.get().getMobile());
        for (Invoice s :invoices) {
            debt.addNewSettleUpInvoice(s.getInvoiceId(), s.getMerchantAmount(),s.getMerchantCur(), s.getRegdatetime().getTime());
        }
        LOG.info("action:presettle,merchant_mobile:{},shop_name:{},mobile:{},apikey:*****,",
               merMob, merchant.get().getShopName(),mob);
        return debt;
    }

    /**
     * reset all invoices that use for QA for settleups.
     * @param mob admin mobile
     * @param apikey amind api
     * @return ok
     */
    @Transactional
    @GetMapping("/testreset")
    public String resetTesthop(@RequestParam(value = "mob", defaultValue = "") String mob,
                               @RequestParam(value = "apikey", defaultValue = "") String apikey) {
        LOG.info("");
        if (!mob.equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }

        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        settleupJpsRepository.deleteSettleupByMerchant_Mobile("09120779807");
        List<String> status = new ArrayList<>();
        status.add("settled");
        Sort.Direction direction = Sort.Direction.fromString("asc");
        Pageable pageable = PageRequest.of(0, 200, new Sort(direction, "id"));
        List<Invoice> invoices = invoiceJpaRepository.findAllByMerchantMobileAndStatusIn(pageable,"09120779807",status);
        for (Invoice invoice : invoices) {
            invoice.setStatus("success");
            invoiceJpaRepository.save(invoice);
        }
        LOG.info("action:testreset,mobile:{},apikey:*****,",
                merchant.get().getShopName(),mob);
        return "OK";
    }


    @GetMapping("/testsuccess")
    public String successAllwaitong(@RequestParam(value = "mob", defaultValue = "") String mob,
                                    @RequestParam(value = "apikey", defaultValue = "") String apikey,
                                    @RequestParam(value = "mermob", defaultValue = "09120779807") String mermob) {
        if (!mob.equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        if(!mermob.equals("09120779807") && !mermob.equals("09120453931"))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        Sort.Direction direction = Sort.Direction.fromString("asc");
        Pageable pageable = PageRequest.of(0, 200, new Sort(direction, "id"));
        List<String> status = new ArrayList<>();
        status.add("waiting");
        List<Invoice> invoices = invoiceJpaRepository.findAllByMerchantMobileAndStatusIn(pageable, mermob, status);

        StringBuilder stringBuilder = new StringBuilder();
        for (Invoice invoice : invoices) {
            invoice.setStatus("success");
            invoiceJpaRepository.save(invoice);
            stringBuilder.append(invoice.getInvoiceId()).append(",");
        }
        LOG.info("action:testsuccess,invoice_id:{},mobile:{},apikey:*****,",
               stringBuilder.toString(),mob);
        return stringBuilder.toString();
    }


    @Setter
    @Getter
    class Debt {
        private BigDecimal sum = new BigDecimal("0");
        private String mobile;
        private String shopName;
        private String cardNumber;
        private List<settleUpInvoices> settleUpInvoices = new ArrayList<>();

        void addNewSettleUpInvoice(String invId, BigDecimal merchantAmount,String merchantCur, long date) {
            sum = sum.add(merchantAmount);
            settleUpInvoices.add(new settleUpInvoices(invId, merchantAmount, merchantCur,date));
        }

        public int getCount() {
            return settleUpInvoices.size();
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    class settleUpInvoices {
        String id;
        BigDecimal merchantAmount;
        String merchantCur;
        long date;
    }

}

