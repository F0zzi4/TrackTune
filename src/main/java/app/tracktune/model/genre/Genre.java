package app.tracktune.model.genre;

public class Genre implements Comparable<Genre>{
    private final String name;
    private final String description;

    public Genre(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public int compareTo(Genre other) {
        return name.compareToIgnoreCase(other.name);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
