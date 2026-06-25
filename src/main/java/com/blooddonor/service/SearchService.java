package com.blooddonor.service;

import com.blooddonor.dto.SearchDtos;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.util.GeoUtils;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final DonorProfileRepository donorRepository;

    public SearchService(DonorProfileRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchDtos.DonorSearchResult> searchNearby(SearchDtos.DonorSearchRequest request) {
        List<DonorProfile> candidates = donorRepository.findEligibleDonors(request.bloodGroup());
        return candidates.stream()
                .map(d -> {
                    double distance = GeoUtils.distanceKm(
                            request.latitude(), request.longitude(),
                            d.getLatitude(), d.getLongitude());
                    return new ResultWithDistance(d, distance);
                })
                .filter(r -> r.distanceKm() <= request.radiusKm())
                .sorted(Comparator.comparingDouble(ResultWithDistance::distanceKm))
                .map(r -> new SearchDtos.DonorSearchResult(
                        r.donor().getId(),
                        r.donor().getFullName(),
                        r.donor().getAge(),
                        r.donor().getBloodGroup().getLabel(),
                        r.donor().getDiseases(),
                        r.donor().getPhone(),
                        r.donor().getAddress(),
                        r.donor().getLatitude(),
                        r.donor().getLongitude(),
                        Math.round(r.distanceKm() * 100.0) / 100.0,
                        r.donor().isAvailable()))
                .toList();
    }

    private record ResultWithDistance(DonorProfile donor, double distanceKm) {}
}
