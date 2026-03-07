package com.salon.app.repository;

import com.salon.app.model.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
    List<SalonService> findByActiveTrue();
    List<SalonService> findByCategory(String category);
    List<SalonService> findByCategoryAndActiveTrue(String category);
}
