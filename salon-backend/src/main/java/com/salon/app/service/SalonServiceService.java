package com.salon.app.service;

import com.salon.app.dto.CurrencyInfo;
import com.salon.app.dto.ServiceResponse;
import com.salon.app.model.SalonService;
import com.salon.app.repository.SalonServiceRepository;
import com.salon.app.util.CurrencyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalonServiceService {

    private final SalonServiceRepository serviceRepository;
    private final CurrencyUtil currencyUtil;

    public SalonServiceService(SalonServiceRepository serviceRepository, CurrencyUtil currencyUtil) {
        this.serviceRepository = serviceRepository;
        this.currencyUtil = currencyUtil;
    }

    public List<ServiceResponse> getAllActiveServices(String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        return serviceRepository.findByActiveTrue().stream()
                .map(service -> toResponse(service, currencyInfo))
                .collect(Collectors.toList());
    }

    public List<ServiceResponse> getServicesByCategory(String category, String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        return serviceRepository.findByCategoryAndActiveTrue(category).stream()
                .map(service -> toResponse(service, currencyInfo))
                .collect(Collectors.toList());
    }

    public ServiceResponse getServiceById(Long id, String countryCode) {
        CurrencyInfo currencyInfo = currencyUtil.getCurrencyByCountryCode(countryCode);
        SalonService service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return toResponse(service, currencyInfo);
    }

    @Transactional
    public ServiceResponse createService(SalonService service) {
        SalonService saved = serviceRepository.save(service);
        CurrencyInfo defaultCurrency = currencyUtil.getDefaultCurrency();
        return toResponse(saved, defaultCurrency);
    }

    @Transactional
    public ServiceResponse updateService(Long id, SalonService updatedService) {
        SalonService existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        existing.setName(updatedService.getName());
        existing.setDescription(updatedService.getDescription());
        existing.setPriceUsd(updatedService.getPriceUsd());
        existing.setDurationMinutes(updatedService.getDurationMinutes());
        existing.setCategory(updatedService.getCategory());
        existing.setImageUrl(updatedService.getImageUrl());
        existing.setActive(updatedService.isActive());

        SalonService saved = serviceRepository.save(existing);
        CurrencyInfo defaultCurrency = currencyUtil.getDefaultCurrency();
        return toResponse(saved, defaultCurrency);
    }

    @Transactional
    public void deleteService(Long id) {
        SalonService service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        service.setActive(false);
        serviceRepository.save(service);
    }

    private ServiceResponse toResponse(SalonService service, CurrencyInfo currencyInfo) {
        BigDecimal localPrice = currencyUtil.convertFromUsd(service.getPriceUsd(), currencyInfo.getCountryCode());
        return ServiceResponse.fromEntity(service, localPrice, currencyInfo.getCurrencyCode(), currencyInfo.getCurrencySymbol());
    }
}
