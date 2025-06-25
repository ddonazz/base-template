package it.andrea.start.models.support;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Location {

    @Column
    private String street;

    @Column
    private String number;

    @Column
    private String city;

    @Column
    private String zipCode;

    @Column
    private String country;

    @Column
    private String stateOrProvince;

    @Column
    private String additionalInfo;

}
