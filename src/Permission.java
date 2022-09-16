import java.util.SortedMap;
import java.util.TreeMap;

public class Permission {
    private final String name;
    private final SortedMap<String, Integer> locations;

    public Permission(String name) {
        this.name = name;
        this.locations = new TreeMap<>();
    }

    public String getName() {
        return this.name;
    }

    public SortedMap<String, Integer> getLocations() {
        return this.locations;
    }

    public void addLocation(String location) {
        if (!this.locations.containsKey(location)) {
            this.locations.put(location, 1);
        }
        else {
            this.locations.put(location, this.locations.get(location) + 1);
        }
    }
}
