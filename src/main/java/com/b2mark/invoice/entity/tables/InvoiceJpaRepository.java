package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
public interface InvoiceJpaRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findById(long aLong);

    List<Invoice> findInvoicesByMerchantMobileAndMerchantToken(String mobileNum, String Token);

    List<Invoice> findInvoicesByMerchantMobile(Pageable pg, String mobileNum);

    List<Invoice> findInvoicesByStatusIn(Pageable pg,List<String> status);

    List<Invoice> findInvoicesByIdIn(Set<Long> id);

    List<Invoice> findAllByMerchantMobile(Pageable pg, String mobileNum);

    List<Invoice> findAllByMerchantMobileAndStatusIn(Pageable pg, String mobileNum,List<String> status);

    long countAllByMerchantMobile(String mobileNum);

    long countAllByMerchantMobileAndStatusIn(String mobileNum,List<String> status);

    Optional<Invoice> findInvoiceByQr(String qr);

    long countInvoiceByStatusIn(List<String> status);

    Optional<Invoice> findByIdAndMerchant_IdAndCategory(long id,long merchant,String category);


    Optional<Invoice> findByOrderidAndMerchant_Id(String orderId,long merchant);


    @Query(value = "SELECT i FROM  Invoice i LEFT JOIN  i.merchant as m LEFT JOIN i.settleup as s WHERE i.status = 'success' AND s.id is null AND  m.id =?1")
    List<Invoice> getInvoiceDebtByMerchantId(long merchant);


    @Query(value = "SELECT i FROM  Invoice i LEFT JOIN  i.merchant as m LEFT JOIN i.settleup as s WHERE i.status = 'success' AND s.id is null AND  m.id =?1 AND i.id IN (?2)")
    List<Invoice> getInvoiceDebtByMerchantId(long merchant,Collection<Long> InvIds);
}
