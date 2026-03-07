package com.salon.app.service;

import com.salon.app.model.Staff;
import com.salon.app.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public List<Staff> getAllAvailableStaff() {
        return staffRepository.findByAvailableTrue();
    }

    public List<Staff> getStaffBySpecialization(String specialization) {
        return staffRepository.findBySpecialization(specialization);
    }

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    public Staff getStaffByUserId(Long userId) {
        return staffRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Staff profile not found for user: " + userId));
    }
}
