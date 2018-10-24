package com.b2mark.invoice.entity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
public interface VuMerchantdebtRepository extends CrudRepository<VuMerchantdebt ,Long> {

    List<VuMerchantdebt> findVuMerchantdebtByBalanceIsGreaterThan(Pageable pageable,long balance);
}
