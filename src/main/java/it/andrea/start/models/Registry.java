package it.andrea.start.models;

import java.time.LocalDate;

import it.andrea.start.constants.DocumentType;
import it.andrea.start.constants.Gender;
import it.andrea.start.constants.TypeRegistry;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
@Embeddable
public class Registry {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeRegistry typeRegistry;

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String businessName;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String email;

    @Column
    private String telephone;

    @Column
    private String pec;

    @Column
    private String vatCode;

    @Column
    private String fiscalCode;

    @Column
    private String birthCity;

    @Column
    private String birthState;

    @Column
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column
    private DocumentType documentType;

    @Column
    private String identityCardNumber;

    @Column
    private String companyRegistrationNumber;

}
