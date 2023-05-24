package ro.pub.cs.systems.eim.practicaltest02.model;

public class ServerInformation {

    private String abilities;
    private String types;
    private String image_url;

    public ServerInformation(String abilities, String types, String image_url) {
        this.abilities = abilities;
        this.types = types;
        this.image_url = image_url;
    }

    public String getAbilities() {
        return abilities;
    }

    public String getTypes() {
        return types;
    }

    public String getImage_url() {
        return image_url;
    }

    @Override
    public String toString() {
        return "ServerInformation{" +
                "abilities='" + abilities + '\'' +
                ", types='" + types + '\'' +
                '}';
    }
}
