package it.andrea.start.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LanguageDTO {
    
    private String languageCode; // ISO 639-1
    private String countryCode; // ISO 3166-1
    private String bcp47Tag; // e.g., "en-US", "it-IT"

}
