package app.tracktune.interfaces;

import java.util.List;

public interface DAO<T> {
    /**
     * Inserts a new record into the database
     *
     * @param data the object to be inserted into the data source
     */
    Integer insert(T data);

    /**
     * Updates a record on database
     *
     * @param id ID of the entity to update
     */
    void updateById(T data, int id);

    /**
     * Deletes a record on database
     *
     * @param id ID of the entity to delete
     */
    void deleteById(int id);

    /**
     * Get the related object from database with corresponding given key
     * @param id usually is the primary key or logic key in the database
     * @return the related object if key exists, null otherwise
     */
    T getById(int id);

    /**
     * Get all the elements stored in the cache
     * @return sorted list of stored elements
     */
    List<T> getAll();
}
