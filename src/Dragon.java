import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class Dragon {
    private String name;
    private Long id;
    private Date born;
    private String race;
    private int numberOfHeads;
    private int weight;

    public Dragon() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBorn() {
        return born;
    }

    public void setBorn(Date born) {
        this.born = born;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public int getNumberOfHeads() {
        return numberOfHeads;
    }

    public void setNumberOfHeads(int numberOfHeads) {
        this.numberOfHeads = numberOfHeads;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}
