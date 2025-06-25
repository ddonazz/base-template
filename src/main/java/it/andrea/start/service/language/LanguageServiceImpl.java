package it.andrea.start.service.language;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.andrea.start.constants.Language;
import it.andrea.start.dto.LanguageDTO;

@Service
@Transactional
public class LanguageServiceImpl implements LanguageService {

    @Override
    @Transactional(readOnly = true)
    public Collection<LanguageDTO> getAvailableLanguages() {
        // @formatter:off
        return Arrays.stream(Language.values())
                .map(lang -> {
                    LanguageDTO dto = new LanguageDTO();
                    dto.setLanguageCode(lang.getLanguageCode());
                    dto.setCountryCode(lang.getCountryCode());
                    dto.setBcp47Tag(lang.getBcp47Tag());
                    return dto;
                })
                .toList();
        // @formatter:on
    }

}
