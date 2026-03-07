package com.salon.app.repository;

import com.salon.app.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserId(Long userId);
    List<Staff> findByAvailableTrue();
    List<Staff> findBySpecialization(String specialization);
}
