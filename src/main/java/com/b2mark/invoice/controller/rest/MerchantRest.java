/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.controller.rest;

import com.b2mark.invoice.core.MtService;
import com.b2mark.invoice.entity.tables.Merchant;
import com.b2mark.invoice.entity.tables.MerchantJpaRepository;
import com.b2mark.invoice.exception.BadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<Merchant> addMerchant(@RequestBody Merchant merchant) {
        //TODO: generic mobile format for save in system.

        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        System.out.println("=================================================================================");
        Optional<Merchant> merchant1;
        if (merchantJpaRepository.existsByMobile(merchant.getMobile())) {//Check if exist
            throw new BadRequest("User By this mobile number is exist");
        } else {
            Random random = new Random();
            int x = random.nextInt(90000) + 10000;
            merchant.setToken(x + "");
            merchant.setDatetime(new Date());
            merchant.setLastSendToken(new Date());
            merchant1 = Optional.of(merchantJpaRepository.save(merchant));
            mtService.validation1(merchant.getMobile(), merchant.getToken());
        }
        HttpHeaders headers = new HttpHeaders();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{uid}")
                .buildAndExpand(merchant1.get().getMobile()).toUri();
        headers.setLocation(location);
        return new ResponseEntity<>( merchant1.get(), headers, HttpStatus.CREATED);
    }


    @GetMapping
    public Optional<Merchant> getMerchantInfo(@RequestParam(value = "mob", required = true) String mobileNum, @RequestParam(value = "token", required = true) String token) {
        if (token.isEmpty() || token == null) {
            throw new BadRequest("token not exist call this api for get token.  https://<addres>:<port>/merchant/token?mob= " + mobileNum);
        }
        if (mobileNum.isEmpty() || mobileNum == null) {
            throw new BadRequest("mobile number is not valid");
        }
        Optional<Merchant> merchant = merchantJpaRepository.findByMobileAndToken(mobileNum, token);
        if (merchant.isPresent()) {
            return merchant;
        } else {
            throw new BadRequest("this merchant is not exist.plz register user by this api  https://<addres>:<port>/merchant/reg");
        }
    }


    @GetMapping("/token")
    public String getToken(@RequestParam(value = "mob", required = true) String mobileNum) {
        Optional<Merchant> merchant = merchantJpaRepository.findByMobile(mobileNum);
        if (merchant.isPresent()) {
            long lastSend = (new Date()).getTime() - merchant.get().getLastSendToken().getTime();
            if (lastSend > 1000 * 60 * 2) {
                mtService.validation1(merchant.get().getMobile(), merchant.get().getToken());
                merchant.get().setLastSendToken(new Date());
                merchantJpaRepository.save(merchant.get());
                return "ok";
            } else {
                long remind = (1000 * 160 * 2 - lastSend) / 1000;
                throw new BadRequest("Remind " + remind + " second to new request to get token");
            }
        } else {
            throw new BadRequest("this merchant not exist plz register user by this api  https://<addres>:<port>/merchant/reg");
        }
    }

}