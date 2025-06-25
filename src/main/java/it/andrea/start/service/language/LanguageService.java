package it.andrea.start.service.language;

import java.util.Collection;

import it.andrea.start.dto.LanguageDTO;

public interface LanguageService {

    Collection<LanguageDTO> getAvailableLanguages();

}
