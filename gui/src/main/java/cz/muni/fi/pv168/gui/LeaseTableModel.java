package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.Lease;
import cz.muni.fi.pv168.dragon.LeaseManager;
import cz.muni.fi.pv168.dragon.ServiceFailureException;

import javax.swing.table.AbstractTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michal on 4.5.2015.
 */
public class LeaseTableModel extends AbstractTableModel{
    LeaseManager leaseManager;
    List<Lease> allLeases;


    public LeaseTableModel(LeaseManager leaseManager){
        this.leaseManager = leaseManager;
        allLeases = new ArrayList<>(leaseManager.getAllLeases());
    }

    @Override
    public int getRowCount() {
        return allLeases.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Lease lease = allLeases.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return lease.getId();
            case 1:
                return lease.getPrice();
            case 2:
                return lease.getStartDate();
            case 3:
                return lease.getEndDate();
            case 4:
                return lease.getDragon().getName();
            case 5:
                return lease.getCustomer().getName();
            case 6:
                return lease.getReturnDate();
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
                return lang.getString("mainwindow_price");
            case 2:
                return lang.getString("mainwindow_startDate");
            case 3:
                return lang.getString("mainwindow_endDate");
            case 4:
                return lang.getString("mainwindow_dragonName");
            case 5:
                return lang.getString("mainwindow_customerName");
            case 6:
                return lang.getString("mainwindow_returnDate");
            default:
                throw new IllegalArgumentException("column");
        }
    }

    @Override
    public void fireTableDataChanged() {
        super.fireTableDataChanged();
        allLeases = new ArrayList<>(leaseManager.getAllLeases());
    }

    public Lease getLeaseAt(int row){
        return allLeases.get(row);
    }
}
