package source;

import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class Dragon {
    private Long id;
    private String name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dragon dragon = (Dragon) o;

        if (numberOfHeads != dragon.numberOfHeads) return false;
        if (weight != dragon.weight) return false;
        if (!born.equals(dragon.born)) return false;
        if (!id.equals(dragon.id)) return false;
        if (!name.equals(dragon.name)) return false;
        if (!race.equals(dragon.race)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + born.hashCode();
        result = 31 * result + race.hashCode();
        result = 31 * result + numberOfHeads;
        result = 31 * result + weight;
        return result;
    }

}
