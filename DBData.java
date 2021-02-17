import java.util.Comparator;

/**
 * Created by mac on 12/6/17.
 */
public class DBData implements Comparable<DBData> {
    public int index;
    public long time;
    public long price;
    public long shareNum;
    public long peopleNum;
    public String type;
    public long stockValue;
    public long valueTradedTillNow;
    public long sharesTradedTillNow;
    public long lastTradedValue;
    public int fail;

    @Override
    public int compareTo(DBData o) {
        long timeDiff = this.time - o.time;
        if ( timeDiff < 0 ){
            return -1;
        }else if (timeDiff > 0){
            return 1;
        }else{
            return this.index - o.index;
        }
    }
    public DBData(int index, long sharesTradedTillNow, long time, long price, long shareNum, long peopleNum, String type, long stockValue, long valueTradedTillNow, long lastTradedValue, int fail) {
        this.sharesTradedTillNow = sharesTradedTillNow;
        this.time = time;
        this.price = price;
        this.shareNum = shareNum;
        this.peopleNum = peopleNum;
        this.type = type;
        this.stockValue = stockValue;
        this.valueTradedTillNow = valueTradedTillNow;
        this.lastTradedValue = lastTradedValue;
        this.index = index;
        this.fail = fail;
    }
}
