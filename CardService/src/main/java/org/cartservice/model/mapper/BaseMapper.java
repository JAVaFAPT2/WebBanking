package org.cartservice.model.mapper;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseMapper<E, D> {

    /**
     * Converts the given DTO to an entity object.
     *
     * @param  dto   the DTO to be converted
     * @param  args  additional arguments (optional)
     * @return       the converted entity object
     */
    public abstract E convertToEntity(D dto, Object... args);

    /**
     * Converts the given entity to a DTO (Data Transfer Object) using the provided arguments.
     *
     * @param  entity  the entity to be converted
     * @param  args    additional arguments for the conversion process
     * @return         the DTO representing the converted entity
     */
    public abstract D convertToDto(E entity, Object... args);

    /**
     * Converts a collection of DTOs to a collection of entities.
     *
     * @param  dto   the collection of DTOs to convert
     * @param  args  additional arguments
     * @return       the collection of entities
     */
    public Collection<E> convertToEntity(Collection<D> dto, Object... args) {
        return dto.stream().map(d -> convertToEntity(d, args)).collect(Collectors.toList());
    }

    /**
     * Converts a collection of entities to a collection of DTOs.
     *
     * @param  entities  the collection of entities to be converted
     * @param  args      additional arguments for the conversion
     * @return           the collection of DTOs
     */
    public Collection<D> convertToDto(Collection<E> entities, Object... args) {
        return entities.stream().map(entity ->convertToDto(entity, args)).collect(Collectors.toList());
    }

    /**
     * Converts a collection of DTOs to a list of entities.
     *
     * @param  dtos  the collection of DTOs to convert
     * @param  args  additional arguments for the conversion
     * @return       the list of converted entities
     */
    public List<E> covertToEntityList(Collection<D> dtos, Object... args) {
        return new ArrayList<>(convertToEntity(dtos, args));
    }

    /**
     * Converts a collection of entities to a list of DTOs.
     *
     * @param  entities  the collection of entities to be converted
     * @param  args      additional arguments for the conversion
     * @return           the list of DTOs obtained from the conversion
     */
    public List<D> convertToDtoList(Collection<E> entities, Object... args) {
        return new ArrayList<>(convertToDto(entities, args));
    }

    /**
     * Converts a collection of DTOs to a set of entities.
     *
     * @param  dtos  the collection of DTOs to convert
     * @param  args  additional arguments (optional)
     * @return       the set of entities converted from the DTOs
     */
    public Set<E> convertToEntitySet(Collection<D> dtos, Object... args) {
        return new HashSet<>(convertToEntity(dtos, args));
    }

}
