package app.tracktune.interfaces;

import app.tracktune.model.user.User;

import java.util.SortedSet;

public interface DAO<T> {
    /**
     * Refresh the cache related to the entity T from the database
     */
    void refreshCache();

    /**
     * Inserts a new record into the database
     *
     * @param data the object to be inserted into the data source
     */
    void insert(T data);

    /**
     * Updates a record on database
     *
     * @param data the object to update on database
     */
    void update(T data);

    /**
     * Deletes a record on database
     *
     * @param data the object to delete on database
     */
    void delete(T data);

    /**
     * Get the related object from database with corresponding given key
     * @param key usually is the primary key or logic key in the database
     * @return the related object if key exists, null otherwise
     */
    T getByKey(Object key);

    /**
     * Get all the elements stored in the cache
     * @return sorted list of stored elements
     */
    SortedSet<T> getAll();

    boolean alreadyExists(T data);
}
