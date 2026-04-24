package backend.dto;

import java.util.List;

public class LookupDTO {
    private String name;
    private List<String> values;

    public LookupDTO(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}