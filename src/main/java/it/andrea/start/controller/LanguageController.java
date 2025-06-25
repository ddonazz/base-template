package it.andrea.start.controller;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.andrea.start.dto.LanguageDTO;
import it.andrea.start.service.language.LanguageService;

@Tag(name = "Language API", description = "API for languages")
@RestController
@RequestMapping("/api/language")
@PreAuthorize("hasRole('GUEST')")
public class LanguageController {
    
    private LanguageService languageService;
    
    public LanguageController(LanguageService languageService) {
        super();
        this.languageService = languageService;
    }

    // @formatter:off
    @Operation(
        description = "Lista delle lingue disponibili",
        summary = "Lista delle lingue disponibili"
    )
    // @formatter:on
    @GetMapping("/available")
    public ResponseEntity<Collection<LanguageDTO>> listAvailableLanguages() {
        return ResponseEntity.ok(languageService.getAvailableLanguages());
    }
    
}
