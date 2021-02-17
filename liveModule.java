import org.influxdb.InfluxDB;

import java.util.ArrayList;

public class liveModule {
    private ArrayList<Company> everyCompany = new ArrayList<>();
    private InfluxDB DB;
    private String table = new String("InstantStockData");


    public liveModule(ArrayList<Company> everyCompany, InfluxDB DB, String table) {
        this.everyCompany = everyCompany;
        this.DB = DB;
        this.table = table;
    }

    public void start(int milliSecRefreshment){

        for ( Company comp : everyCompany ){
            String companyToken = comp.getToken();

        }
    }

    public static void main(String[] args) {
//        System.out.println(Methods.getLiveString("46348559193224090").replaceAll(";","\n").replaceAll("@","\t").replaceAll(",","\n"));
        System.out.println(Methods.getClientTypeDataString("46348559193224090").replaceAll(";","\n").replaceAll(",","\t"));
    }

}
