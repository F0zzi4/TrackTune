package app.tracktune.model.musicalInstrument;

public class MusicalInstrument implements Comparable<MusicalInstrument> {
    private final Integer id;
    private final String name;
    private final String description;

    public MusicalInstrument(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public MusicalInstrument(String name, String description) {
        this.id = null;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(MusicalInstrument o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}