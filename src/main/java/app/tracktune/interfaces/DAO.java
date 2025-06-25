package app.tracktune.interfaces;

import java.util.List;

/**
 * Generic Data Access Object (DAO) interface defining standard CRUD operations.
 *
 * @param <T> the type of the entity managed by this DAO
 */
public interface DAO<T> {

    /**
     * Inserts a new entity record into the data source.
     *
     * @param data the entity to be inserted
     * @return the generated ID of the inserted entity, or null if insertion failed
     */
    Integer insert(T data);

    /**
     * Updates an existing entity record identified by the given ID.
     *
     * @param data the updated entity data
     * @param id the ID of the entity to update
     */
    void updateById(T data, int id);

    /**
     * Deletes an entity record identified by the given ID.
     *
     * @param id the ID of the entity to delete
     */
    void deleteById(int id);

    /**
     * Retrieves an entity record by its unique identifier.
     *
     * @param id the ID of the entity to retrieve
     * @return the entity if found, or null if no matching record exists
     */
    T getById(int id);

    /**
     * Retrieves all entity records stored in the data source.
     *
     * @return a sorted list of all entities
     */
    List<T> getAll();
}
