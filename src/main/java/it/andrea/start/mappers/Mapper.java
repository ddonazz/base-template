package it.andrea.start.mappers;

import java.util.Collection;

import it.andrea.start.error.exception.mapping.MappingToDtoException;
import it.andrea.start.error.exception.mapping.MappingToEntityException;

public interface Mapper<T, E> {

    T toDto(E entity) throws MappingToDtoException;

    void toEntity(T dto, E entity) throws MappingToEntityException;

    Collection<T> toDtos(Collection<E> entities) throws MappingToDtoException;

}
