package com.ihome24.ihome24.controller.publicapi.company;

import com.ihome24.ihome24.dto.response.company.CompanyPartySuggestion;
import com.ihome24.ihome24.dto.response.company.CompanySuggestResponse;
import com.ihome24.ihome24.service.company.DadataPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publicapi/company")
@RequiredArgsConstructor
public class CompanySuggestRestController {

    private final DadataPartyService dadataPartyService;

    @GetMapping("/suggest")
    public ResponseEntity<CompanySuggestResponse> suggest(
            @RequestParam("q") String query,
            @RequestParam(value = "count", defaultValue = "8") int count) {
        return ResponseEntity.ok(dadataPartyService.suggest(query, count));
    }

    @GetMapping("/party")
    public ResponseEntity<CompanyPartySuggestion> party(@RequestParam("inn") String inn) {
        CompanyPartySuggestion party = dadataPartyService.findParty(inn);
        if (party == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(party);
    }
}
