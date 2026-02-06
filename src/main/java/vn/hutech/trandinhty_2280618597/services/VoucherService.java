package vn.hutech.trandinhty_2280618597.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hutech.trandinhty_2280618597.entities.Voucher;
import vn.hutech.trandinhty_2280618597.repositories.VoucherRepository;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    /**
     * Tạo voucher mới
     */
    public Voucher createVoucher(Voucher voucher) {
        // Chuyển mã voucher thành chữ hoa
        voucher.setCode(voucher.getCode().toUpperCase().trim());
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUsedCount(0);

        if (voucherRepository.existsByCodeIgnoreCase(voucher.getCode())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại!");
        }

        return voucherRepository.save(voucher);
    }

    /**
     * Lấy tất cả voucher
     */
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lấy voucher theo ID
     */
    public Optional<Voucher> getVoucherById(String id) {
        return voucherRepository.findById(id);
    }

    /**
     * Tìm voucher theo mã
     */
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code.trim());
    }

    /**
     * Validate voucher
     * 
     * @return null nếu hợp lệ, message lỗi nếu không hợp lệ
     */
    public String validateVoucher(String code, double orderAmount) {
        Optional<Voucher> voucherOpt = findByCode(code);

        if (voucherOpt.isEmpty()) {
            return "Mã giảm giá không tồn tại!";
        }

        Voucher voucher = voucherOpt.get();

        if (!voucher.isActive()) {
            return "Mã giảm giá đã bị vô hiệu hóa!";
        }

        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return "Mã giảm giá chưa có hiệu lực!";
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return "Mã giảm giá đã hết hạn!";
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return "Mã giảm giá đã hết lượt sử dụng!";
        }

        if (orderAmount < voucher.getMinOrderAmount()) {
            return String.format("Đơn hàng tối thiểu %.0fđ để sử dụng mã này!", voucher.getMinOrderAmount());
        }

        return null; // Hợp lệ
    }

    /**
     * Tính số tiền giảm
     */
    public double calculateDiscount(String code, double orderAmount) {
        Optional<Voucher> voucherOpt = findByCode(code);
        if (voucherOpt.isEmpty()) {
            return 0;
        }
        return voucherOpt.get().calculateDiscount(orderAmount);
    }

    /**
     * Áp dụng voucher (tăng số lần sử dụng)
     */
    public void applyVoucher(String code) {
        Optional<Voucher> voucherOpt = findByCode(code);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
        }
    }

    /**
     * Bật/tắt trạng thái voucher
     */
    public boolean toggleVoucher(String id) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setActive(!voucher.isActive());
            voucherRepository.save(voucher);
            return true;
        }
        return false;
    }

    /**
     * Xóa voucher
     */
    public boolean deleteVoucher(String id) {
        if (voucherRepository.existsById(id)) {
            voucherRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật voucher
     */
    public Voucher updateVoucher(String id, Voucher updatedVoucher) {
        Optional<Voucher> existingOpt = voucherRepository.findById(id);
        if (existingOpt.isPresent()) {
            Voucher existing = existingOpt.get();
            existing.setDescription(updatedVoucher.getDescription());
            existing.setDiscountType(updatedVoucher.getDiscountType());
            existing.setDiscountValue(updatedVoucher.getDiscountValue());
            existing.setMinOrderAmount(updatedVoucher.getMinOrderAmount());
            existing.setMaxDiscount(updatedVoucher.getMaxDiscount());
            existing.setUsageLimit(updatedVoucher.getUsageLimit());
            existing.setStartDate(updatedVoucher.getStartDate());
            existing.setEndDate(updatedVoucher.getEndDate());
            existing.setActive(updatedVoucher.isActive());
            return voucherRepository.save(existing);
        }
        return null;
    }
}
