package com.blooddonor.controller;

import com.blooddonor.dto.SearchDtos;
import com.blooddonor.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/donors")
    public ResponseEntity<List<SearchDtos.DonorSearchResult>> searchDonors(
            @Valid @RequestBody SearchDtos.DonorSearchRequest request) {
        return ResponseEntity.ok(searchService.searchNearby(request));
    }
}
