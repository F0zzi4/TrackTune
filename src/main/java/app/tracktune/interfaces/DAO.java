package app.tracktune.interfaces;

import app.tracktune.model.user.User;

import java.util.SortedSet;

public interface DAO<T> {
    /**
     * Inserts a new record into the database
     *
     * @param data the object to be inserted into the data source
     */
    public void insert(T data);

    /**
     * Updates a record on database
     *
     * @param data the object to update on database
     */
    public void update(T data);

    /**
     * Deletes a record on database
     *
     * @param data the object to delete on database
     */
    public void delete(T data);

    /**
     * Get the related object from database with corresponding given key
     * @param key usually is the primary key or logic key in the database
     * @return the related object if key exists, null otherwise
     */
    public T getByKey(Object key);

    /**
     * Get all the elements stored in the cache
     * @return sorted list of stored elements
     */
    public SortedSet<T> getAll();
}
