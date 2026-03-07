package com.salon.app.service;

import com.salon.app.dto.AppointmentRequest;
import com.salon.app.dto.AppointmentResponse;
import com.salon.app.dto.CurrencyInfo;
import com.salon.app.model.Appointment;
import com.salon.app.model.SalonService;
import com.salon.app.model.User;
import com.salon.app.repository.AppointmentRepository;
import com.salon.app.repository.SalonServiceRepository;
import com.salon.app.repository.UserRepository;
import com.salon.app.util.CurrencyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SalonServiceRepository serviceRepository;
    private final CurrencyUtil currencyUtil;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository,
                               SalonServiceRepository serviceRepository, CurrencyUtil currencyUtil) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.currencyUtil = currencyUtil;
    }

    @Transactional
    public AppointmentResponse createAppointment(Long customerId, AppointmentRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        SalonService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        User staff = null;
        if (request.getStaffId() != null) {
            staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            // Check for scheduling conflicts
            LocalDateTime endTime = request.getAppointmentDateTime()
                    .plusMinutes(service.getDurationMinutes());
            List<Appointment> conflicts = appointmentRepository.findStaffAppointmentsBetween(
                    staff.getId(), request.getAppointmentDateTime(), endTime);
            if (!conflicts.isEmpty()) {
                throw new RuntimeException("Selected staff is not available at this time");
            }
        }

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .staff(staff)
                .service(service)
                .appointmentDateTime(request.getAppointmentDateTime())
                .status(Appointment.AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved, customer.getCountryCode());
    }

    public List<AppointmentResponse> getCustomerAppointments(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return appointmentRepository.findByCustomerOrderByAppointmentDateTimeDesc(customer).stream()
                .map(a -> toResponse(a, customer.getCountryCode()))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getUpcomingAppointments(Long customerId) {
        return appointmentRepository.findUpcomingByCustomer(customerId).stream()
                .map(a -> toResponse(a, a.getCustomer().getCountryCode()))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getStaffAppointments(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        return appointmentRepository.findByStaffOrderByAppointmentDateTimeDesc(staff).stream()
                .map(a -> toResponse(a, a.getCustomer().getCountryCode()))
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(Appointment.AppointmentStatus.valueOf(status.toUpperCase()));
        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved, saved.getCustomer().getCountryCode());
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getCustomer().getId().equals(userId) &&
            (appointment.getStaff() == null || !appointment.getStaff().getId().equals(userId))) {
            throw new RuntimeException("Not authorized to cancel this appointment");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved, saved.getCustomer().getCountryCode());
    }

    private AppointmentResponse toResponse(Appointment appointment, String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        BigDecimal localPrice = currencyUtil.convertFromUsd(appointment.getService().getPriceUsd(), countryCode);
        return AppointmentResponse.fromEntity(appointment, currencyInfo.getCurrencyCode(),
                currencyInfo.getCurrencySymbol(), localPrice);
    }
}
