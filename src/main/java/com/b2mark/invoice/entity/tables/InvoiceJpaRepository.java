/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findById(long aLong);

    List<Invoice> findInvoicesByMerchantMobileAndMerchantToken(String mobileNum, String Token);

    List<Invoice> findInvoicesByMerchantMobileAndMerchantApiKeyOrderById(String mobileNum, String Token);

    Optional<Invoice> findInvoiceByQr(String qr);

    Optional<Invoice> findByIdAndMerchant_IdAndCategory(long id,long merchant,String category);


}
