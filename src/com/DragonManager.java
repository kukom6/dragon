package com;

import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface DragonManager {
    void createDragon(Dragon dragon);

    Dragon getDragonByID(Long ID);

    Collection<Dragon> getAllDragon();

    Collection<Dragon> getDragonsByName(String name);

    Collection<Dragon> getDragonsByRace(String race);

    Collection<Dragon> getDragonsByNumberOfHeads(int number);

    void updateDragon(Dragon dragon);

    void deleteCustomer(Dragon dragon);
}
