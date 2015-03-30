/**
 * Created by Matej on 30. 3. 2015.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeServiceImplTest implements TimeService{
    private Date currentDate;

    public TimeServiceImplTest() throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        currentDate=sdf.parse("15-03-2015 12:00:00");
    }

    public void setCurrentDate(Date date){
        currentDate=date;
    }
    public Date getCurrentDate(){
        return currentDate;
    }

}