package bot.dto.MarvelDTO;

public class Variants {
    private String resourceURI;
    private String name;

    public Variants(String resourceURI, String name){
        this.resourceURI = resourceURI;
        this.name = name;
    }

    public Variants(){
        super();
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public String getName() {
        return name;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public void setName(String name) {
        this.name = name;
    }
}