package com.b2mark.invoice.entity.tables;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

public interface SettleupJpsRepository extends JpaRepository<Settleup, Long> {
    void deleteSettleupByMerchant_Mobile(String mobile);
    boolean existsInvoicesById(long invoiceId);
    List<Settleup> findSettleupByMerchantMobile(Pageable pg, String mobileNum);
    long countSettleupsByMerchantMobile(String mobileNum);

    List<Settleup> findSettleupByIdNotNull(Pageable pg);
    long countSettleupsByIdNotNull();
}
