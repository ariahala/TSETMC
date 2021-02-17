import org.influxdb.InfluxDB;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DBDataClient {
    private int date;

    private long buyRealNum;
    private long buyCompNum;
    private long sellRealNum;
    private long sellCompNum;

    private long buyRealVolume;
    private long buyCompVolume;
    private long sellRealVolume;
    private long sellCompVolume;

    private long buyRealValue;
    private long buyCompValue;
    private long sellRealValue;
    private long sellCompValue;

    private long CompToRealChangeVolume;

    private float avgRealBuyPrice;
    private float avgCompBuyPrice;
    private float avgRealSellPrice;
    private float avgCompSellPrice;

    private float realBuyPercentage;
    private float compBuyPercentage;
    private float realSellPercentage;
    private float compSellPercentage;

    private long totalVolume;
    private long totalValue;

    public DBDataClient(int date, long buyRealNum, long buyCompNum, long sellRealNum, long sellCompNum, long buyRealVolume, long buyCompVolume, long sellRealVolume, long sellCompVolume, long buyRealValue, long buyCompValue, long sellRealValue, long sellCompValue) {
        this.date = date;
        this.buyRealNum = buyRealNum;
        this.buyCompNum = buyCompNum;
        this.sellRealNum = sellRealNum;
        this.sellCompNum = sellCompNum;
        this.buyRealVolume = buyRealVolume;
        this.buyCompVolume = buyCompVolume;
        this.sellRealVolume = sellRealVolume;
        this.sellCompVolume = sellCompVolume;
        this.buyRealValue = buyRealValue;
        this.buyCompValue = buyCompValue;
        this.sellRealValue = sellRealValue;
        this.sellCompValue = sellCompValue;

        CompToRealChangeVolume = this.buyRealVolume - this.sellRealVolume;
        if ( this.buyCompVolume != 0 ) {
            this.avgCompBuyPrice = (float) this.buyCompValue / this.buyCompVolume;
        }else{
            this.avgCompBuyPrice = 0;
        }
        if ( this.sellCompVolume != 0) {
            this.avgCompSellPrice = (float) this.sellCompValue / this.sellCompVolume;
        }else{
            this.avgCompSellPrice = 0;
        }
        if ( this.buyRealVolume != 0 ) {
            this.avgRealBuyPrice = (float) this.buyRealValue / this.buyRealVolume;
        }else{
            this.avgRealBuyPrice = 0;
        }
        if ( this.sellRealVolume != 0 ) {
            this.avgRealSellPrice = (float) this.sellRealValue / this.sellRealVolume;
        }else{
            this.avgRealSellPrice = 0;
        }
        if ( this.buyRealVolume + this.buyCompVolume != 0 ) {
            this.realBuyPercentage = (float)this.buyRealVolume/(this.buyRealVolume + this.buyCompVolume);
            this.compBuyPercentage = (float)this.buyCompVolume/(this.buyRealVolume + this.buyCompVolume);
        }else{
            this.realBuyPercentage = 0;
            this.compBuyPercentage = 0;
        }
        if ( this.sellRealVolume + this.sellCompVolume != 0  ) {
            this.realSellPercentage = (float) this.sellRealVolume / (this.sellRealVolume + this.sellCompVolume);
            this.compSellPercentage = (float) this.sellCompVolume / (this.sellRealVolume + this.sellCompVolume);
        }else{
            this.realSellPercentage = 0;
            this.compSellPercentage = 0;
        }

        this.totalValue = this.buyCompValue + this.buyRealValue;
        this.totalVolume = this.buyCompVolume + this.buyRealVolume;
    }
    public void writeToDB (InfluxDB DB , String companyToken){
        String toInsert = "Client_Type_Data,Company_Number="+companyToken + " ";
        //Total values and volumes inserting
        toInsert = toInsert + "Total_Value=" + this.totalValue;
        toInsert = toInsert + ",Total_Volume=" + this.totalVolume;
        //Trade people Numbers inserting in that day
        toInsert = toInsert + ",Real_Sell_Num=" + this.sellRealNum;
        toInsert = toInsert + ",Comp_Sell_Num=" +this.sellCompNum;
        toInsert = toInsert + ",Real_Buy_Num=" + this.buyRealNum;
        toInsert = toInsert + ",Comp_Buy_Num=" + this.buyCompNum;
        //Trade Volume inserting
        toInsert = toInsert + ",Real_Buy_Volume=" + this.buyRealVolume;
        toInsert = toInsert + ",Comp_Buy_Volume=" + this.buyCompVolume;
        toInsert = toInsert + ",Real_Sell_Volume=" + this.sellRealVolume;
        toInsert = toInsert + ",Comp_Sell_Volume=" + this.sellCompVolume;
        //Trade value inserting in that particular day
        toInsert = toInsert + ",Real_Buy_Value=" + this.buyRealValue;
        toInsert = toInsert + ",Comp_Buy_Value=" + this.buyCompValue;
        toInsert = toInsert + ",Real_Sell_Value=" + this.sellRealValue;
        toInsert = toInsert + ",Comp_Sell_Value=" + this.sellCompValue;
        //Trade percentages inserting in that particular day
        toInsert = toInsert + ",Real_Buy_Percentage=" + this.realBuyPercentage;
        toInsert = toInsert + ",Comp_Buy_Percentage=" + this.compBuyPercentage;
        toInsert = toInsert + ",Real_Sell_Percentage=" + this.realSellPercentage;
        toInsert = toInsert + ",Comp_Sell_Percentage=" + this.compSellPercentage;
        //Trade average price inserting in that particular day
        toInsert = toInsert + ",Real_Buy_Avg=" + this.avgRealBuyPrice;
        toInsert = toInsert + ",Comp_Buy_Avg=" + this.avgCompBuyPrice;
        toInsert = toInsert + ",Real_Sell_Avg=" + this.avgRealSellPrice;
        toInsert = toInsert + ",Comp_Sell_Avg=" + this.avgCompSellPrice;
        //Inserting Comp To Real change in volume of stock
        toInsert = toInsert + ",Comp_To_Real_Volume_Change=" + this.CompToRealChangeVolume;
        //inserting the Time at the end of it
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
