package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.swing.table.AbstractTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 30.4.2015.
 */
public class DragonsTableModel extends AbstractTableModel{

    DragonManager dragonManager;
    List<Dragon> allDragons;


    public DragonsTableModel(DragonManager dragonManager){
        this.dragonManager = dragonManager;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon;
        try {
            dragon = new Dragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "trhac", 5, 150);
        } catch (ParseException ex){
            throw new ServiceFailureException("asdsda",ex);
        }
        dragonManager.createDragon(dragon);
        allDragons = new ArrayList<>(dragonManager.getAllDragons());
    }

    @Override
    public int getRowCount() {
        return allDragons.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Dragon dragon = allDragons.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return dragon.getId();
            case 1:
                return dragon.getName();
            case 2:
                return dragon.getBorn();
            case 3:
                return dragon.getRace();
            case 4:
                return dragon.getNumberOfHeads();
            case 5:
                return dragon.getWeight();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Dragon dragon = getDragonAt(rowIndex);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        switch (columnIndex) {
            case 1:
                dragon.setName(aValue.toString());
                break;
            case 2:
                try {
                    dragon.setBorn(sdf.parse(aValue.toString()));
                } catch (ParseException ex){
                    throw new ServiceFailureException("Cannot parse date while updating dragon from JTable");
                }
                break;
            case 3:
                dragon.setRace(aValue.toString());
                break;
            case 4:
                try {
                    dragon.setNumberOfHeads(Integer.parseInt(aValue.toString()));
                } catch (NumberFormatException ex){
                    throw new ServiceFailureException("Cannot parse number of heads while updating dragon from JTable");
                }
                break;
            case 5:
                try {
                    dragon.setWeight(Integer.parseInt(aValue.toString()));
                } catch (NumberFormatException ex){
                    throw new ServiceFailureException("Cannot parse weight while updating dragon from JTable");
                }
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        dragonManager.updateDragon(getDragonAt(rowIndex));
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if(columnIndex != 0){
            return true;
        }
        return false;
    }

    @Override
    public void fireTableDataChanged() {
        super.fireTableDataChanged();
        allDragons = new ArrayList<>(dragonManager.getAllDragons());
    }

    public Dragon getDragonAt(int row){
        return allDragons.get(row);
    }
}
