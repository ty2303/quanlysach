package vn.hutech.trandinhty_2280618597.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import vn.hutech.trandinhty_2280618597.dto.UserRegistrationDTO;
import vn.hutech.trandinhty_2280618597.services.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO dto,
            BindingResult result, Model model) {

        // Check if passwords match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
        }

        // Check if username exists
        if (userService.existsByUsername(dto.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
        }

        // Check if email exists
        if (userService.existsByEmail(dto.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã được sử dụng");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        userService.registerNewUser(dto);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/register-admin")
    public String showAdminRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "auth/register-admin";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@Valid @ModelAttribute("user") UserRegistrationDTO dto,
            BindingResult result, Model model) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
        }

        if (userService.existsByUsername(dto.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
        }

        if (userService.existsByEmail(dto.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã được sử dụng");
        }

        if (result.hasErrors()) {
            return "auth/register-admin";
        }

        userService.registerAdmin(dto);
        return "redirect:/login?registered=true";
    }
}
