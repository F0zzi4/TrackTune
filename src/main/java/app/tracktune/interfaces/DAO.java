package app.tracktune.interfaces;

import app.tracktune.model.user.User;

import java.util.SortedSet;

public interface DAO<T> {
    /**
     * Inserts a new record into the data source
     *
     * @param data the object to be inserted into the data source
     */
    public void insert(T data);

    public void update(T data);

    public void delete(T data);

    public T getByKey(Object key);

    public User getByKey(String username);

    public SortedSet<T> getAll();
}
