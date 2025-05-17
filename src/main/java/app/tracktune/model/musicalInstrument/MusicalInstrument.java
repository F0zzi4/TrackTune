package app.tracktune.model.musicalInstrument;

public class MusicalInstrument implements Comparable<MusicalInstrument> {
    private final String name;
    private final String description;

    public MusicalInstrument(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int compareTo(MusicalInstrument o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
