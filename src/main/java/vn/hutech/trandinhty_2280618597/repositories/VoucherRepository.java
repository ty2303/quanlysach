package vn.hutech.trandinhty_2280618597.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.hutech.trandinhty_2280618597.entities.Voucher;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {

    Optional<Voucher> findByCode(String code);

    Optional<Voucher> findByCodeIgnoreCase(String code);

    List<Voucher> findByActiveTrue();

    List<Voucher> findAllByOrderByCreatedAtDesc();

    boolean existsByCode(String code);

    boolean existsByCodeIgnoreCase(String code);
}
