/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.core.MtService;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.exception.PublicException;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.Random;


@RestController
@RequestMapping("/merchant")
public class MerchantRest {

    @Autowired
    MerchantJpaRepository merchantJpaRepository;
    @Autowired
    MtService mtService;


    @PostMapping
    public Merchant addMerchant(@RequestBody Merchant merchant) {
        //TODO: generic mobile format for save in system.
        if(merchant.getMobile().isEmpty())
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT,"Mobile Number is not valid");
        if(merchant.getCallback().isEmpty())
            throw new PublicException(ExceptionsDictionary.UNMATCHARGUMENT,"Callback URL is not valid");
        Optional<Merchant> merchant1;
        if (merchantJpaRepository.existsByMobile(merchant.getMobile())) {//Check if exist
            return getToken(merchant.getMobile());
        } else {
            Random random = new Random();
            int x = random.nextInt(90000) + 10000;
            merchant.setToken(x + "");
            merchant.setDatetime(new Date());
            merchant.setLastSendToken(new Date());
            String sha256hex = Hashing.sha256()
                    .hashString(merchant.toString()+random.nextInt(),StandardCharsets.UTF_8)
                    .toString();
            merchant.setApiKey(sha256hex);
            merchant1 = Optional.of(merchantJpaRepository.save(merchant));
            mtService.validation1(merchant.getMobile(), merchant.getToken());
        }
        return  merchant1.get();
    }


    @GetMapping
    public Merchant getMerchantInfo(@RequestParam(value = "mob", required = true) String mobileNum,
                                                    @RequestParam(value = "token", required = true) String token) {
        if (token == null || token.isEmpty()) {
            throw new PublicException(ExceptionsDictionary.PARAMETERNOTFOUND,"token not exist call this api for get token.  https://<addres>:<port>/merchant/token?mob= " + mobileNum);
        }
        if (mobileNum.isEmpty() || mobileNum == null) {
            throw new PublicException(ExceptionsDictionary.PARAMETERISNOTVALID,"mobile number is not valid");
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
    public Merchant getToken(@RequestParam(value = "mob", required = true) String mobileNum) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (merchant.isPresent()) {
            long lastSend = (new Date()).getTime() - merchant.get().getLastSendToken().getTime();
            if (lastSend > 1000 * 60 * 2) {
                mtService.validation1(merchant.get().getMobile(), merchant.get().getToken());
                merchant.get().setLastSendToken(new Date());
                Merchant merchant1 = merchantJpaRepository.save(merchant.get());
                HttpHeaders headers = new HttpHeaders();
                return  merchant1 ;
            } else {
                long remind = (1000 * 60 * 1 - lastSend) / 1000;
                throw new PublicException(ExceptionsDictionary.FREQUENTLYREQUEST,"Remind " + remind + " second to new request to get token");
            }
        } else {
            throw new PublicException(ExceptionsDictionary.CONTENTNOTFOUND,"this merchant not exist plz register user by this api  https://<addres>:<port>/merchant/reg");
        }
    }
}