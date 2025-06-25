package it.andrea.start.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class SoftDeleteBaseEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE;

}
