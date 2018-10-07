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

public interface MerchantJpaRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findById(long id);

    Optional<Merchant> findByMobileAndToken(String mobileNum, String token);

    Optional<Merchant> findByMobileAndApiKey(String mobileNum, String token);

    Optional<Merchant> findByMobile(String mobileNum);

    boolean existsByMobile(String mobileNum);
}
