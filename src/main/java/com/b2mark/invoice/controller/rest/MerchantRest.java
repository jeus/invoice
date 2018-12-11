package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.common.entity.Pagination;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.core.MtService;
import com.b2mark.invoice.entity.MerchantRequest;
import com.b2mark.invoice.entity.VuMerchantdebt;
import com.b2mark.invoice.entity.VuMerchantdebtRepository;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.exception.PublicException;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;


/**
 * <h1></h1>

 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@RestController
@RequestMapping("/merchant")
public class MerchantRest {
    private final static String unauthorized = "Merchant id or apikey is not valid";
    private final MerchantJpaRepository merchantJpaRepository;
    private final VuMerchantdebtRepository vuMerchantdebtRepository;
    private final MtService mtService;

    @Autowired
    public MerchantRest(MerchantJpaRepository merchantJpaRepository, VuMerchantdebtRepository vuMerchantdebtRepository, MtService mtService) {
        this.merchantJpaRepository = merchantJpaRepository;
        this.vuMerchantdebtRepository = vuMerchantdebtRepository;
        this.mtService = mtService;
    }


    @PostMapping
    public Merchant addMerchant(@RequestBody MerchantRequest merchantReq) {
        //TODO: generic mobile format for save in system.

        if (!merchantReq.getAdminMob().equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        Optional<Merchant> adminMerchant = merchantJpaRepository.findByMobile(merchantReq.getAdminMob());
        if (!adminMerchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!adminMerchant.get().getApiKey().equals(merchantReq.getAdminApikey()))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        if(merchantReq.getMobile().isEmpty())
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT,"Mobile Number is not valid");
        if(merchantReq.getCallback().isEmpty())
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT,"Callback URL is not valid");
        if(merchantReq.getShopName().isEmpty())
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT,"ShopName is not valid");
        if(merchantReq.getPushToken().length() > 200)
            throw new PublicException(ExceptionsDictionary.ARGUMENTTOOLONG,"Push token is too long");
        Optional<Merchant> optionMerchant;
        if (merchantJpaRepository.existsByMobile(merchantReq.getMobile())) {//Check if exist
            return getToken(merchantReq.getMobile());
        } else {
            Merchant merchant = new Merchant();
            merchant.setToken(merchantReq.getToken());
            merchant.setCallback(merchantReq.getCallback());
            merchant.setPushToken(merchantReq.getPushToken());
            merchant.setMobile(merchantReq.getMobile());
            merchant.setShopName(merchantReq.getShopName());
            merchant.setCardNumber(merchantReq.getCardNumber());

            Random random = new Random();
            int x = random.nextInt(90000) + 10000;
            merchant.setToken(x + "");
            merchant.setDatetime(new Date());
            merchant.setLastSendToken(new Date());
            String sha256hex = Hashing.sha256()
                    .hashString(merchant.toString()+random.nextInt(),StandardCharsets.UTF_8)
                    .toString();
            merchant.setApiKey(sha256hex);
            optionMerchant = Optional.of(merchantJpaRepository.save(merchant));
            //TODO: have to check this sysetm. when call system.
            String parameter = "ثبت نام";
            String template = "registerd";
            mtService.validation1(merchant.getMobile(),parameter,template);
        }
        return  optionMerchant.get();
    }


    @GetMapping
    public Merchant getMerchantInfo(@RequestParam(value = "mob") String mobileNum,
                                                    @RequestParam(value = "token") String token) {
        if (token == null || token.isEmpty()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERNOTFOUND,"token not exist call this api for get token.  https://<addres>:<port>/merchant/token?mob= " + mobileNum);
        }
        if (mobileNum.isEmpty()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID, "mobile number is not valid");
        }
        Optional<Merchant> merchant = merchantJpaRepository.findByMobileAndToken(mobileNum, token);
        if (merchant.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{uid}")
                    .buildAndExpand(merchant.get().getMobile()).toUri();
            headers.setLocation(location);
            return merchant.get();
        } else {
            throw new PublicException(ExceptionsDictionary.CONTENTNOTFOUND,"this merchant is not exist. plz register user by this api  https://<addres>:<port>/merchant/reg");
        }
    }



    @GetMapping("/token")
    public Merchant getToken(@RequestParam(value = "mob") String mobileNum) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (merchant.isPresent()) {
            long lastSend = (new Date()).getTime() - merchant.get().getLastSendToken().getTime();
            if (lastSend > 1000 * 60 * 2) {
                mtService.validation1(merchant.get().getMobile(), "ثبت نام","registerd");
                merchant.get().setLastSendToken(new Date());
                Merchant merchant1 = merchantJpaRepository.save(merchant.get());
                HttpHeaders headers = new HttpHeaders();
                return  merchant1 ;
            } else {
                long remind = (1000 * 60 - lastSend) / 1000;
                throw new PublicException(ExceptionsDictionary.FREQUENTLYREQUEST,"Remind " + remind + " second to new request to get token");
            }
        } else {
            throw new PublicException(ExceptionsDictionary.CONTENTNOTFOUND,"this merchant not exist plz register user by this api  https://<addres>:<port>/merchant/reg");
        }
    }


    @GetMapping("/debt")
    public Pagination<VuMerchantdebt> getMerchantDebt(@RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                      @RequestParam(value = "size", defaultValue = "20", required = false) int size,
                                                      @RequestParam(value = "dir", defaultValue = "asc", required = false) String dir,
                                                      @RequestParam(value = "mob", defaultValue = "") String mob,
                                                      @RequestParam(value = "apikey", defaultValue = "") String apikey,
                                                       HttpServletRequest request){

        if (!mob.equals("09120453931")) {
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        }
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mob);
        if (!merchant.isPresent())
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);
        else if (!merchant.get().getApiKey().equals(apikey))
            throw new PublicException(ExceptionsDictionary.UNAUTHORIZED, unauthorized);

        Sort.Direction direction = Sort.Direction.fromString(dir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, new Sort(direction, "id"));
        List<VuMerchantdebt> debtToMerchants = vuMerchantdebtRepository.findVuMerchantdebtByBalanceIsGreaterThan(pageable,0);
        long count = vuMerchantdebtRepository.count();

        Pagination<VuMerchantdebt> debtToMerchantPagination = new Pagination<>();
        debtToMerchantPagination.setName("DebtToMerchant");
        debtToMerchantPagination.setCount(count);
        debtToMerchantPagination.setPage(page);
        debtToMerchantPagination.setSize(size);
        debtToMerchantPagination.setStatus(200);
        debtToMerchantPagination.setApiAddress(request.getRequestURL().toString() + "?" + request.getQueryString());

        for (VuMerchantdebt vuMerchantdebt : debtToMerchants) {
                debtToMerchantPagination.add(vuMerchantdebt);
        }


        return debtToMerchantPagination;
    }

}