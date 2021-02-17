import org.influxdb.InfluxDB;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DBDataDay {
    private String date;

    private int mostPrice;
    private int leastPrice;
    private int lastRealValue;
    private int lastTradedValue;
    private int firstPrice;
    private int yesterdayLastRealPrice;
    private long tradedValue;
    private long tradedVolume;
    private int tradeNumber;

    private float lastTradedValueChange;
    private float lastTradedValueChangePercentage;

    private float lastRealValueChange;
    private float lastRealValueChangePercentage;

    public DBDataDay(String date, int mostPrice, int leastPrice, int lastRealValue, int lastTradedValue, int firstPrice, int yesterdayLastRealPrice, long tradedValue, long tradedVolume, int tradeNumber) {
        this.date = date;
        this.mostPrice = mostPrice;
        this.leastPrice = leastPrice;
        this.lastRealValue = lastRealValue;
        this.lastTradedValue = lastTradedValue;
        this.firstPrice = firstPrice;
        this.yesterdayLastRealPrice = yesterdayLastRealPrice;
        this.tradedValue = tradedValue;
        this.tradedVolume = tradedVolume;
        this.tradeNumber = tradeNumber;

        this.lastRealValueChange = (float) this.lastRealValue - this.yesterdayLastRealPrice;
        this.lastTradedValueChange = (float) this.lastTradedValue - this.yesterdayLastRealPrice;
        if ( this.yesterdayLastRealPrice != 0 ) {
            this.lastTradedValueChangePercentage = this.lastTradedValueChange/this.yesterdayLastRealPrice;
            this.lastRealValueChangePercentage = this.lastRealValueChange/this.yesterdayLastRealPrice;
        }else{
            this.lastTradedValueChangePercentage = 0;
            this.lastRealValueChangePercentage = 0;
        }
    }

    public void writeToDB(InfluxDB DB , String companyToken){
        String toInsert = "Trade_Day_Data,Company_Token=" + companyToken + " ";

        toInsert = toInsert + "Most_Price=" + this.mostPrice;
        toInsert = toInsert + ",Least_Price=" + this.leastPrice;
        toInsert = toInsert + ",Last_Real_Value=" + this.lastRealValue;
        toInsert = toInsert + ",Last_Traded_Value=" + this.lastTradedValue;
        toInsert = toInsert + ",First_Price=" + this.firstPrice;
        toInsert = toInsert + ",Yesterday_Last_Real_Price=" + this.yesterdayLastRealPrice;
        toInsert = toInsert + ",Total_Traded_Value=" + this.tradedValue;
        toInsert = toInsert + ",Total_Traded_Volume=" + this.tradedVolume;
        toInsert = toInsert + ",Traded_Number=" + this.tradeNumber;

        toInsert = toInsert + ",Last_Traded_Value_Change=" + this.lastRealValueChange;
        toInsert = toInsert + ",Last_Traded_Value_Change_Percentage=" + this.lastTradedValueChangePercentage;

        toInsert = toInsert + ",Last_Real_Value_Change=" + this.lastRealValueChange;
        toInsert = toInsert + ",Last_Real_Value_Change_Percentage=" + this.lastRealValueChangePercentage;

        String strDate;
        String tempString = "230000";
        if ( tempString.length() == 6) {
            strDate = this.date + " " + tempString;
        }else{
            strDate = this.date + " 0" + tempString;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
        LocalDateTime zdt = LocalDateTime.parse(strDate, dtf);
        Long time = zdt.toInstant(ZoneOffset.ofHoursMinutes(0, 0)).toEpochMilli();
        time = time * 1000000;
        toInsert = toInsert + " " + time;

        DB.write(toInsert);
    }
}
