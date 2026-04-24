package com.vietnl.usersservice.infrastructure.persistence.seeder;

import com.vietnl.usersservice.domain.entities.User;
import com.vietnl.usersservice.domain.enums.UserRole;
import com.vietnl.usersservice.domain.enums.UserStatus;
import com.vietnl.usersservice.infrastructure.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem tài khoản admin đã tồn tại chưa
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            
            // Xuyên thủng hệ thống validation, băm trực tiếp mật khẩu "admin"
            admin.setPassword(passwordEncoder.encode("admin"));
            
            // Set Role là 1 (ADMIN)
            admin.setRole(UserRole.ADMIN.getValue()); 
            admin.setStatus(UserStatus.ACTIVE.getValue());
            
            // Các trường bắt buộc nhồi data ảo để không bị Oracle chửi Not Null
            admin.setFullName("Tối Cao Pháp Sư Admin");
            admin.setEmail("admin@food.com");
            admin.setPhoneNumber("0999999999");
            admin.setCitizenPid("000000000000");
            admin.setBirthday(LocalDateTime.now().minusYears(20));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("====== ĐÃ KHỞI TẠO TÀI KHOẢN ADMIN THÀNH CÔNG ======");
            System.out.println("Tài khoản: admin");
            System.out.println("Mật khẩu: admin");
            System.out.println("Quyền: ADMIN (1)");
        }
    }
}
