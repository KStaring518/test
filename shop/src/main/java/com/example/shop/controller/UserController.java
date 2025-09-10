package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(RuntimeException::new);
        return Result.success(UserProfileVO.from(user));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(Authentication auth, @Validated @RequestBody UpdateProfileDTO dto) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(RuntimeException::new);
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        userRepository.save(user);
        return Result.success(UserProfileVO.from(user));
    }

    /** 修改密码：校验旧密码并使用BCrypt加密 */
    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth, @Validated @RequestBody ChangePasswordDTO dto) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(RuntimeException::new);
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            return Result.error(400, "新密码至少6位");
        }
        if (dto.getOldPassword() == null || dto.getOldPassword().isEmpty()) {
            return Result.error(400, "请输入旧密码");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            return Result.error(400, "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return Result.success();
    }

    @Data
    public static class UpdateProfileDTO {
        private String nickname;
        private String phone;
        private String email;
        private String avatarUrl;
    }

    @Data
    public static class UserProfileVO {
        private Long id;
        private String username;
        private String nickname;
        private String phone;
        private String email;
        private String avatarUrl;
        private String role;
        public static UserProfileVO from(User u) {
            UserProfileVO v = new UserProfileVO();
            v.id = u.getId();
            v.username = u.getUsername();
            v.nickname = u.getNickname();
            v.phone = u.getPhone();
            v.email = u.getEmail();
            v.avatarUrl = u.getAvatarUrl();
            v.role = u.getRole().name();
            return v;
        }
    }

    @Data
    public static class ChangePasswordDTO {
        private String oldPassword;
        private String newPassword;
    }
}


