package app.tracktune.model.genre;

import app.tracktune.model.author.Author;

public class Genre {
    private final Integer id;
    private final String name;
    private final String description;

    public Genre(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Genre(String name, String description) {
        this.id = null;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if(o instanceof Genre g){
            result = g.getName().equalsIgnoreCase(this.name);
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}