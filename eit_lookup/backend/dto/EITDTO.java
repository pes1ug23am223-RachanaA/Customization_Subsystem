package backend.dto;

public class EITDTO {
    private int id;
    private String name;
    private String type;

    public EITDTO(String name, String type) {
        this.id = 0;
        this.name = name;
        this.type = type;
    }

    public EITDTO(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}