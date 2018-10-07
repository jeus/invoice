/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayerLogJpaRepository extends JpaRepository<PayerLog, Long> {

    Optional<PayerLog> findById(long id);

    Optional<PayerLog> findByEmail(String email);

    Optional<PayerLog> findByInvoice(long invoiceId);

}
