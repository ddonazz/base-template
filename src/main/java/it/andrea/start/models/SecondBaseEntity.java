package it.andrea.start.models;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SecondBaseEntity extends FirstBaseEntity {

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE;

}
