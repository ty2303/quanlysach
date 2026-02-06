package vn.hutech.trandinhty_2280618597.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.hutech.trandinhty_2280618597.entities.Voucher;
import vn.hutech.trandinhty_2280618597.services.VoucherService;

@Controller
@RequestMapping("/admin/vouchers")
@PreAuthorize("hasRole('ADMIN')")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    /**
     * Danh sách voucher
     */
    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "admin/vouchers";
    }

    /**
     * Form tạo voucher mới
     */
    @GetMapping("/create")
    public String createVoucherForm(Model model) {
        Voucher voucher = new Voucher();
        voucher.setDiscountType(Voucher.TYPE_PERCENT);
        voucher.setActive(true);
        model.addAttribute("voucher", voucher);
        return "admin/voucher-create";
    }

    /**
     * Xử lý tạo voucher mới
     */
    @PostMapping("/create")
    public String createVoucher(@ModelAttribute Voucher voucher, RedirectAttributes redirectAttributes) {
        try {
            // Set default values
            if (voucher.getStartDate() == null) {
                voucher.setStartDate(LocalDateTime.now());
            }

            voucherService.createVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã giảm giá thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/vouchers/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/vouchers/create";
        }
        return "redirect:/admin/vouchers";
    }

    /**
     * Bật/tắt voucher
     */
    @PostMapping("/{id}/toggle")
    public String toggleVoucher(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        if (voucherService.toggleVoucher(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái voucher!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher!");
        }
        return "redirect:/admin/vouchers";
    }

    /**
     * Xóa voucher
     */
    @PostMapping("/{id}/delete")
    public String deleteVoucher(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        if (voucherService.deleteVoucher(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher!");
        }
        return "redirect:/admin/vouchers";
    }
}
