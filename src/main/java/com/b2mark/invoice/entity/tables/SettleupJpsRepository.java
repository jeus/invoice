package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;


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
}
