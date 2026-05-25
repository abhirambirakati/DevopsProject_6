package com.voltstore;

import com.voltstore.model.*;
import com.voltstore.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class VoltStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoltStoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataInitializer(UserRepository userRepository,
                                             OrderRepository orderRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("demo").isEmpty()) {
                // Create demo user
                User demoUser = new User("demo", passwordEncoder.encode("demo123"), "demo@voltstore.com", "Demo Customer");
                userRepository.save(demoUser);

                // Add a past order
                Order order1 = new Order(demoUser, LocalDateTime.now().minusDays(3), 168890.0, "Delivered",
                        "123 Tech Avenue, Silicon Valley", "Bangalore", "560001", "+91 98765 43210", "Credit Card");
                // iPhone 15 Pro Max (id 1, price 149900)
                OrderItem item1 = new OrderItem(1, 1, 149900.0);
                order1.addOrderItem(item1);
                // Keychron Q1 Pro (id 16, price 18990)
                OrderItem item2 = new OrderItem(16, 1, 18990.0);
                order1.addOrderItem(item2);
                orderRepository.save(order1);

                // Add another past order
                Order order2 = new Order(demoUser, LocalDateTime.now().minusDays(10), 9990.0, "Delivered",
                        "123 Tech Avenue, Silicon Valley", "Bangalore", "560001", "+91 98765 43210", "Cash on Delivery");
                // Logitech MX Master 3S (id 18, price 9990)
                OrderItem item3 = new OrderItem(18, 1, 9990.0);
                order2.addOrderItem(item3);
                orderRepository.save(order2);
            }
        };
    }
}