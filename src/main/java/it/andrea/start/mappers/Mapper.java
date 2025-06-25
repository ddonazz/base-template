package it.andrea.start.mappers;

import java.util.Collection;

public interface Mapper<T, E> {

    T toDto(E entity);

    void toEntity(T dto, E entity);

    Collection<T> toDtos(Collection<E> entities);

}
