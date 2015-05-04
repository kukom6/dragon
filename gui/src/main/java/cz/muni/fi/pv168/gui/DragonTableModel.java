package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.table.AbstractTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michal on 30.4.2015.
 */
public class DragonTableModel extends AbstractTableModel{

    DragonManager dragonManager;
    List<Dragon> allDragons;


    public DragonTableModel(DragonManager dragonManager){
        this.dragonManager = dragonManager;
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
    public String getColumnName(int column) {
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        switch (column) {
            case 0:
                return lang.getString("mainwindow_id");
            case 1:
                return lang.getString("mainwindow_name");
            case 2:
                return lang.getString("mainwindow_born");
            case 3:
                return lang.getString("mainwindow_race");
            case 4:
                return lang.getString("mainwindow_heads");
            case 5:
                return lang.getString("mainwindow_weight");
            default:
                throw new IllegalArgumentException("column");
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
        return columnIndex != 0;
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
