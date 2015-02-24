package com;

import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface DragonManager {
    void createDragon(Dragon dragon);

    Dragon getDragonByID(Long ID);


    Collection<Dragon> getAllDragon();

    Collection<Dragon> getAllDragonByName(String name);

    Collection<Dragon> getAllDragonByRace(String race);

    void updateDragon(Dragon dragon);

    void deleteCustomer(Dragon dragon);
}
