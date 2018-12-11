package com.b2mark.invoice.entity.tables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.*;

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


    /**Use this query for limitation per day Invoicing is schema at this native query*/
    @Query(value = "SELECT sum(merchant_amount) FROM invoicing.invoice i WHERE merchant = ?1 and regdatetime <= ?2  and regdatetime <= ?3 and status in('settled','success','waiting') GROUP BY merchant" , nativeQuery = true)
    BigDecimal sumAmountPerMerchantPerDay(long merchantId,Date startDate,Date endDate);

    //TODO: have to implement sumAmountPerMerchantPerHour
    //TODO: have to implement sumAmountPerMerchantPerMonth

}
