package com.salon.app.config;

import com.salon.app.model.SalonService;
import com.salon.app.model.Staff;
import com.salon.app.model.User;
import com.salon.app.repository.SalonServiceRepository;
import com.salon.app.repository.StaffRepository;
import com.salon.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(UserRepository userRepo, SalonServiceRepository serviceRepo,
                               StaffRepository staffRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                log.info("Initializing sample data...");

                // Create admin user
                User admin = User.builder()
                        .fullName("Salon Admin")
                        .email("admin@salon.com")
                        .password(encoder.encode("admin123"))
                        .phone("+1234567890")
                        .role(User.Role.ADMIN)
                        .countryCode("US")
                        .currencyCode("USD")
                        .build();
                userRepo.save(admin);

                // Create staff users
                User staffUser1 = User.builder()
                        .fullName("Emma Johnson")
                        .email("emma@salon.com")
                        .password(encoder.encode("staff123"))
                        .phone("+1234567891")
                        .role(User.Role.STAFF)
                        .countryCode("US")
                        .currencyCode("USD")
                        .build();
                userRepo.save(staffUser1);

                User staffUser2 = User.builder()
                        .fullName("Priya Sharma")
                        .email("priya@salon.com")
                        .password(encoder.encode("staff123"))
                        .phone("+919876543210")
                        .role(User.Role.STAFF)
                        .countryCode("IN")
                        .currencyCode("INR")
                        .build();
                userRepo.save(staffUser2);

                User staffUser3 = User.builder()
                        .fullName("Sakura Tanaka")
                        .email("sakura@salon.com")
                        .password(encoder.encode("staff123"))
                        .phone("+81901234567")
                        .role(User.Role.STAFF)
                        .countryCode("JP")
                        .currencyCode("JPY")
                        .build();
                userRepo.save(staffUser3);

                // Create staff profiles
                staffRepo.save(Staff.builder().user(staffUser1).specialization("Hair Styling, Coloring").bio("10+ years experience in modern hair styling").build());
                staffRepo.save(Staff.builder().user(staffUser2).specialization("Bridal Makeup, Skincare").bio("Expert in traditional and modern bridal looks").build());
                staffRepo.save(Staff.builder().user(staffUser3).specialization("Nail Art, Manicure").bio("Certified nail technician with Japanese precision").build());

                // Create sample customer
                User customer = User.builder()
                        .fullName("Test Customer")
                        .email("customer@test.com")
                        .password(encoder.encode("customer123"))
                        .phone("+911234567890")
                        .role(User.Role.CUSTOMER)
                        .countryCode("IN")
                        .currencyCode("INR")
                        .build();
                userRepo.save(customer);

                // Create salon services
                serviceRepo.save(SalonService.builder().name("Haircut - Women").description("Professional women's haircut with wash and blow dry").priceUsd(new BigDecimal("35.00")).durationMinutes(45).category("Hair").build());
                serviceRepo.save(SalonService.builder().name("Haircut - Men").description("Classic men's haircut with styling").priceUsd(new BigDecimal("20.00")).durationMinutes(30).category("Hair").build());
                serviceRepo.save(SalonService.builder().name("Hair Coloring").description("Full hair coloring with premium products").priceUsd(new BigDecimal("80.00")).durationMinutes(120).category("Hair").build());
                serviceRepo.save(SalonService.builder().name("Highlights").description("Partial or full highlights").priceUsd(new BigDecimal("100.00")).durationMinutes(150).category("Hair").build());
                serviceRepo.save(SalonService.builder().name("Keratin Treatment").description("Smoothing keratin treatment for frizz-free hair").priceUsd(new BigDecimal("150.00")).durationMinutes(180).category("Hair").build());
                serviceRepo.save(SalonService.builder().name("Bridal Makeup").description("Complete bridal makeup with trial").priceUsd(new BigDecimal("200.00")).durationMinutes(120).category("Makeup").build());
                serviceRepo.save(SalonService.builder().name("Party Makeup").description("Glamorous party makeup look").priceUsd(new BigDecimal("60.00")).durationMinutes(60).category("Makeup").build());
                serviceRepo.save(SalonService.builder().name("Facial - Classic").description("Deep cleansing facial with massage").priceUsd(new BigDecimal("45.00")).durationMinutes(60).category("Skincare").build());
                serviceRepo.save(SalonService.builder().name("Facial - Gold").description("Premium gold facial for glowing skin").priceUsd(new BigDecimal("75.00")).durationMinutes(90).category("Skincare").build());
                serviceRepo.save(SalonService.builder().name("Manicure").description("Classic manicure with nail polish").priceUsd(new BigDecimal("25.00")).durationMinutes(30).category("Nails").build());
                serviceRepo.save(SalonService.builder().name("Pedicure").description("Relaxing pedicure with foot massage").priceUsd(new BigDecimal("35.00")).durationMinutes(45).category("Nails").build());
                serviceRepo.save(SalonService.builder().name("Gel Nails").description("Long-lasting gel nail application").priceUsd(new BigDecimal("50.00")).durationMinutes(60).category("Nails").build());
                serviceRepo.save(SalonService.builder().name("Full Body Massage").description("Relaxing full body massage").priceUsd(new BigDecimal("70.00")).durationMinutes(60).category("Spa").build());
                serviceRepo.save(SalonService.builder().name("Hot Stone Massage").description("Therapeutic hot stone massage").priceUsd(new BigDecimal("90.00")).durationMinutes(75).category("Spa").build());
                serviceRepo.save(SalonService.builder().name("Threading - Eyebrows").description("Precision eyebrow threading").priceUsd(new BigDecimal("10.00")).durationMinutes(15).category("Threading").build());
                serviceRepo.save(SalonService.builder().name("Full Face Threading").description("Complete face threading").priceUsd(new BigDecimal("25.00")).durationMinutes(30).category("Threading").build());

                log.info("Sample data initialized successfully!");
            }
        };
    }
}
