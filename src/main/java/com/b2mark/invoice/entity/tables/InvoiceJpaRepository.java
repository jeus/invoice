/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findById(long aLong);

    List<Invoice> findInvoicesByMerchantMobileAndMerchantToken(String mobileNum, String Token);

    List<Invoice> findInvoicesByMerchantMobileAndMerchantApiKey(Pageable pg, String mobileNum, String Token);

    List<Invoice> findAllOrderByRegdatetime (Pageable pg);

    Optional<Invoice> findInvoiceByQr(String qr);

    Optional<Invoice> findByIdAndMerchant_IdAndCategory(long id,long merchant,String category);
}
