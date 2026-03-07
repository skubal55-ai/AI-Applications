package com.salon.app.repository;

import com.salon.app.model.Appointment;
import com.salon.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByCustomerOrderByAppointmentDateTimeDesc(User customer);
    List<Appointment> findByStaffOrderByAppointmentDateTimeDesc(User staff);

    @Query("SELECT a FROM Appointment a WHERE a.staff.id = :staffId " +
           "AND a.appointmentDateTime BETWEEN :start AND :end " +
           "AND a.status NOT IN ('CANCELLED')")
    List<Appointment> findStaffAppointmentsBetween(
        @Param("staffId") Long staffId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT a FROM Appointment a WHERE a.customer.id = :customerId " +
           "AND a.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS') " +
           "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingByCustomer(@Param("customerId") Long customerId);
}
