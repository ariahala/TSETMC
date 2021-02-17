import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class clientType {
    private ArrayList<Company> everyCompany = new ArrayList<>();
    private InfluxDB DB;
    private Thread inserterThread;

    private ArrayList<DBDataClient> beautifyData( String input ){
        ArrayList<DBDataClient> data = new ArrayList<>();
        input = input.replaceAll(";","\n").replaceAll(",","\t");
        if ( input.indexOf("<body>") + 7 > input.indexOf("</body>")) {
            return data;
        }
        input = input.substring(input.indexOf("<body>") + 7,input.indexOf("</body>"));

        String[] splittedInput = input.split("\\n");
        for ( int i = 0 ; i < splittedInput.length ; i++ ){
            String tempString = splittedInput[i];
            String[] tempArrString = tempString.split("\\s+");
            if ( tempArrString.length != 13 ){
                continue;
            }
            data.add(new DBDataClient(Integer.parseInt(tempArrString[0]),Long.parseLong(tempArrString[1]),Long.parseLong(tempArrString[2]),Long.parseLong(tempArrString[3]), Long.parseLong(tempArrString[4]), Long.parseLong(tempArrString[5]), Long.parseLong(tempArrString[6]),Long.parseLong(tempArrString[7]),Long.parseLong(tempArrString[8]),Long.parseLong(tempArrString[9]), Long.parseLong(tempArrString[10]),Long.parseLong(tempArrString[11]),Long.parseLong(tempArrString[12])));
        }

        return data;
    }
    public void InsertInDB (){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for ( Company comp : this.everyCompany ){
            if ( !Methods.isGettingClientData )
                break;
            String input = null;
            while (input==null){
                input = Methods.getClientTypeDataString(comp.getToken());
            }
            ArrayList<DBDataClient> data = beautifyData(input);
            System.out.println(comp.getToken());
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for ( DBDataClient db : data ){
                        db.writeToDB(DB,comp.getToken());
                    }
                    System.out.println("Done inserting " + comp.getToken());
                }
            });
        }
        executorService.shutdown();
    }
    public clientType(ArrayList<Company> everyCompany, InfluxDB DB) {
        this.everyCompany = everyCompany;
        this.DB = DB;
    }
}
