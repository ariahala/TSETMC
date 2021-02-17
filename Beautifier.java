import org.influxdb.InfluxDB;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;


public class Beautifier {
    public static boolean isBeautifyingNow = false;
    public static String getBetweenStrings (String text, String textFrom, String textTo) throws Exception {
        String result = new String();


            // Cut the beginning of the text to not occasionally meet a
            // 'textTo' value in it:
            result =
                    text.substring(
                            text.indexOf(textFrom) + textFrom.length(),
                            text.length());

            // Cut the excessive ending of the text:
            result =
                    result.substring(
                            0,
                            result.indexOf(textTo));

            return result;

    }
    private static void CreateDataFile(String dataName, String rawHTML, String location) {
        try {
            String mess = getBetweenStrings(rawHTML, dataName, ";");
            mess = mess.replaceAll("(=\\[\\[)|(\\]\\])|(')|(=\\[)", "");

            ArrayList<String> num = new ArrayList<String>();
            for (String data: mess.split("\\]+,+\\[")) {
                num.add(data.replaceAll("\\]", "").replaceAll("(,)", "\t"));
            }
            Methods.writeToFile(num, location + File.separatorChar + dataName + ".txt");

        }
        catch (Exception e) {
            try {
                String location1 = location + "indicator";
                String token = getBetweenStrings(location, Methods.motherAddress + File.separatorChar, File.separatorChar + "Days");
                String date = getBetweenStrings(location1, "Days" + File.separatorChar,"indicator");
                Methods.appendToFile("http://cdn.tsetmc.com/Loader.aspx?ParTree=15131P&i=" + token + "&d=" + date ,Methods.motherAddress + File.separatorChar + "Fails.txt");
                System.err.format("Exception occurred trying to get '%s in location: '%s'.", dataName, location);
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Shit is fuqed up man");
            }
        }
    }
    private static void ExtractFromRawHTML(String location){
        String html = Methods.readFileToString(location + File.separatorChar + "RawHTML.txt");
        if ( html.split("\\s+")[0].equals("nullnull") ){
            try {
                String location1 = location + "indicator";
                String token = getBetweenStrings(location, Methods.motherAddress + File.separatorChar, File.separatorChar + "Days");
                String date = getBetweenStrings(location1, "Days" + File.separatorChar,"indicator");
                Methods.appendToFile("http://cdn.tsetmc.com/Loader.aspx?ParTree=15131P&i=" + token + "&d=" + date ,Methods.motherAddress + File.separatorChar + "Fails.txt");
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Shit is fuqed up man");

            }
            return;
        }
        if ( !new File(location + File.separatorChar + "StaticTreshholdData.txt").exists()) {
            CreateDataFile("StaticTreshholdData", html, location);
        }
        if ( !new File(location + File.separatorChar + "ClosingPriceData.txt").exists()) {
            CreateDataFile("ClosingPriceData", html, location);
        }
        if ( !new File(location + File.separatorChar + "IntraDayPriceData.txt").exists()) {
            CreateDataFile("IntraDayPriceData", html, location);
        }
        if ( !new File(location + File.separatorChar + "InstrumentStateData.txt").exists()) {
            CreateDataFile("InstrumentStateData", html, location);
        }
        if ( !new File(location + File.separatorChar + "IntraTradeData.txt").exists()) {
            CreateDataFile("IntraTradeData", html, location);
        }
        if ( !new File(location + File.separatorChar + "ShareHolderData.txt").exists()) {
            CreateDataFile("ShareHolderData", html, location);
        }
        if ( !new File(location + File.separatorChar + "ShareHolderDataYesterday.txt").exists()) {
            CreateDataFile("ShareHolderDataYesterday", html, location);
        }
        if ( !new File(location + File.separatorChar + "ClientTypeData.txt").exists()) {
            CreateDataFile("ClientTypeData", html, location);
        }
        if ( !new File(location + File.separatorChar + "BestLimitData.txt").exists()) {
            CreateDataFile("BestLimitData", html, location);
            createType1Data(location);
        }else{
            if (!new File(location + File.separatorChar + "Table_1_buy.txt").exists() ){
                createType1Data(location);
            }
        }

    }
    public static void Beautify(){
        String address  = Methods.motherAddress;
        File dir = new File(Methods.motherAddress);
        recursiveBeautify(dir);
    }
    private static void recursiveInsertionDB (File dir){
        if ( Methods.isInsertingRightNow ) {
            try {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        recursiveInsertionDB(file);
                    } else {
                        String tempAddress = file.getCanonicalPath();
                        if (tempAddress.contains(File.separatorChar + "RawHTML.txt")) {
                            tempAddress = tempAddress.substring(0, tempAddress.indexOf(File.separatorChar + "RawHTML.txt"));
                            if ( new File(tempAddress + File.separatorChar + "Beautified").exists() ) {
                                if (!new File(tempAddress + File.separatorChar + "DONE").exists()) {
                                    boolean isSuccesfullyDone = insertInDB(tempAddress);
                                    if (isSuccesfullyDone) {
                                        Methods.createDirectory(tempAddress + File.separatorChar + "DONE");
                                    }
                                }
                                if( !new File(tempAddress + File.separatorChar + "BLDONE").exists()){
                                    boolean isSuccessfullyDone = insertInDBBestLimitData(tempAddress);
                                    if ( isSuccessfullyDone){
                                        Methods.createDirectory(tempAddress + File.separatorChar + "BLDONE");
                                    }
                                }
                            }
                        }else if (tempAddress.contains(File.separatorChar + "info.txt" )){
                            System.out.println(tempAddress);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void StartInsertingInDB(){
        String address  = Methods.motherAddress;
        File dir = new File(Methods.motherAddress);
        recursiveInsertionDB(dir);
    }
    private static boolean insertInDB (String location){
        try {
            String token = getBetweenStrings(location, Methods.motherAddress + File.separatorChar, File.separatorChar + "Days");
            String tempAddress = location;
            String date = tempAddress.substring(tempAddress.length() - 8);
            ArrayList<DBData> DayDeals = new ArrayList<>();
            ArrayList<DBData> DaySell = new ArrayList<>();
            ArrayList<DBData> DayBuy = new ArrayList<>();
            Map<Integer,Integer> tradeSuccess = new HashMap<>();
            if (new File(tempAddress + File.separatorChar + "IntraTradeData.txt").exists() ){
                ArrayList<String> tmpIntra = Methods.readFile(tempAddress + File.separatorChar + "IntraTradeData.txt");
                for ( int i = 0 ; i < tmpIntra.size() ; i++ ) {
                    String[] tempString = tmpIntra.get(i).split("\\s+");
                    if ( tempString.length == 5) {
                        tradeSuccess.put(Integer.parseInt(tempString[0]), Integer.parseInt(tempString[4]));
                    }
                }
            }
            if (new File(tempAddress + File.separatorChar + "ClosingPriceData.txt").exists()) {
                ArrayList<String> tmpCP = Methods.readFile(tempAddress + File.separatorChar + "ClosingPriceData.txt");
                for (int i = 0; i < tmpCP.size(); i++) {
                    String[] tempString = tmpCP.get(i).split("\\s+");
                    String strDate;
                    strDate = date + " " + tempString[1];
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
                    LocalDateTime zdt = LocalDateTime.parse(strDate, dtf);
                    Long time = zdt.toInstant(ZoneOffset.ofHoursMinutes(0, 0)).toEpochMilli();
                    time = time * 1000000;
                    int index = Integer.parseInt(tempString[9]);
                    long priceOfDeal = Long.parseLong(tempString[3]);
                    long stockValue = Long.parseLong(tempString[4]);
                    DBData newDBData;
                    if (index == 0) {
                        newDBData = new DBData(0, 0, time, priceOfDeal, 0, 0, "Open", stockValue, 0, priceOfDeal, 0);
                    } else {
                        long valueTradedTillNow = Long.parseLong(tempString[11]);
                        long sharesTradedTillNow = Long.parseLong(tempString[10]);
                        long lastTradedValue = Long.parseLong(tempString[3]);
                        int fail = 0;
                        if ( tradeSuccess.get(index) == null){
                            fail = 0;
                        }else{
                            fail = tradeSuccess.get(index);
                        }
                        newDBData = new DBData(index, sharesTradedTillNow, time, priceOfDeal, 0, 1, "Deal", stockValue, valueTradedTillNow, lastTradedValue,fail);
                    }
                    DayDeals.add(newDBData);
                }
                Collections.sort(DayDeals);
                for (int i = 1; i < DayDeals.size(); i++) {
                    DBData tmp = DayDeals.get(i);
                    tmp.shareNum = tmp.sharesTradedTillNow - DayDeals.get(i - 1).sharesTradedTillNow;
                    if (tmp.shareNum < 0){
                        tmp.fail = 1;
                    }

                }
            }

            if (new File(tempAddress + File.separatorChar + "Table_1_buy.txt").exists()) {
                ArrayList<String> tmpBuy = Methods.readFile(tempAddress + File.separatorChar + "Table_1_buy.txt");
                for (int i = 0; i < tmpBuy.size(); i++) {
                    String[] tempString = tmpBuy.get(i).split("\\s+");
                    String strDate;
                    if ( tempString[0].length() == 6) {
                        strDate = date + " " + tempString[0];
                    }else{
                        strDate = date + " 0" + tempString[0];
                    }
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
                    LocalDateTime zdt = LocalDateTime.parse(strDate, dtf);
                    Long time = zdt.toInstant(ZoneOffset.ofHoursMinutes(0, 0)).toEpochMilli();
                    time = time * 1000000;
                    long price = Long.parseLong(tempString[1]);
                    long shareNum = Long.parseLong(tempString[2]);
                    long peopleNum = Long.parseLong(tempString[3]);
                    DBData newDBData = new DBData(0, 0, time, price, shareNum, peopleNum, "Buy", 0, 0, 0, 0);
                    DayBuy.add(newDBData);
                }
            }
            if (new File(tempAddress + File.separatorChar + "Table_1_sell.txt").exists()) {
                ArrayList<String> tmpSell = Methods.readFile(tempAddress + File.separatorChar + "Table_1_sell.txt");
                for (int i = 0; i < tmpSell.size(); i++) {
                    String[] tempString = tmpSell.get(i).split("\\s+");
                    String strDate;
                    if ( tempString[0].length() == 6) {
                        strDate = date + " " + tempString[0];
                    }else{
                        strDate = date + " 0" + tempString[0];
                    }
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
                    LocalDateTime zdt = LocalDateTime.parse(strDate, dtf);
                    Long time = zdt.toInstant(ZoneOffset.ofHoursMinutes(0, 0)).toEpochMilli();
                    time = time * 1000000;
                    long price = Long.parseLong(tempString[1]);
                    long shareNum = Long.parseLong(tempString[2]);
                    long peopleNum = Long.parseLong(tempString[3]);
                    DBData newDBData = new DBData(0, 0, time, price, shareNum, peopleNum, "Sell", 0, 0, 0 ,0);
                    DaySell.add(newDBData);
                }
            }
            int buyInd = 0;
            int sellInd = 0;
            int closeInd = 0;
            int INDEX = 0;
            long stockValue1 = DayDeals.get(0).stockValue;
            long valueTradedTillNow1 = 0;
            long lastTradedValue1 = DayDeals.get(0).lastTradedValue;
            long sharesTradedTillNow1 = 0;
            long lastTime = 0;
            int timeIndex = 0;

            while (buyInd < DayBuy.size() || sellInd < DaySell.size() || closeInd < DayDeals.size()) {
                String toInsertToDB = new String("Trade_History,company_Number=" + token + ",");
                long tempTime = 0;
                int indicator = 0;
                if ( buyInd < DayBuy.size() ){
                    tempTime = DayBuy.get(buyInd).time;
                    indicator = 1;
                }
                if(sellInd < DaySell.size() ){
                    if ( indicator == 0 ){
                        tempTime = DaySell.get(sellInd).time;
                        indicator = 2;
                    }else{
                        if ( tempTime >= DaySell.get(sellInd).time){
                            tempTime = DaySell.get(sellInd).time;
                            indicator = 2;
                        }
                    }
                }
                if ( closeInd < DayDeals.size() ){
                    if ( indicator == 0 ){
                        tempTime = DayDeals.get(closeInd).time;
                        indicator = 3;
                    }else{
                        if ( tempTime >= DayDeals.get(closeInd).time){
                            tempTime = DayDeals.get(closeInd).time;
                            indicator = 3;
                        }
                    }
                }
                if ( lastTime == tempTime){
                    timeIndex++;
                }else{
                    timeIndex = 0;
                }
                lastTime = tempTime;
                tempTime += timeIndex*100;

                if ( indicator == 1){
                    toInsertToDB = toInsertToDB + "type=Buy,price_tag="+DayBuy.get(buyInd).price+" Share_Number=" + DayBuy.get(buyInd).shareNum + ",People_Number="+DayBuy.get(buyInd).peopleNum+",Value_Traded_Till_Now="+valueTradedTillNow1+",Shares_Traded_Till_Now="+sharesTradedTillNow1+",Last_Traded_Value="+lastTradedValue1+",Real_Value="+stockValue1 + ",price=" + DayBuy.get(buyInd).price +" "+ tempTime;
                    buyInd++;
                    INDEX++;
                }else if ( indicator == 2){
                    toInsertToDB = toInsertToDB + "type=Sell,price_tag="+DaySell.get(sellInd).price+" Share_Number=" + DaySell.get(sellInd).shareNum + ",People_Number="+DaySell.get(sellInd).peopleNum+",Value_Traded_Till_Now="+valueTradedTillNow1+",Shares_Traded_Till_Now="+sharesTradedTillNow1+",Last_Traded_Value="+lastTradedValue1+",Real_Value="+stockValue1 + ",price=" + DaySell.get(sellInd).price + " " + tempTime;
                    sellInd++;
                    INDEX++;
                }else if ( indicator == 3) {
                    stockValue1 = DayDeals.get(closeInd).stockValue;
                    lastTradedValue1 = DayDeals.get(closeInd).lastTradedValue;
                    sharesTradedTillNow1 = DayDeals.get(closeInd).sharesTradedTillNow;
                    valueTradedTillNow1 = DayDeals.get(closeInd).valueTradedTillNow;
                    toInsertToDB = toInsertToDB + "Deal_Index=" + DayDeals.get(closeInd).index + ",type="+DayDeals.get(closeInd).type+",price_tag="+DayDeals.get(closeInd).price+",fail="+DayDeals.get(closeInd).fail+" Share_Number=" + DayDeals.get(closeInd).shareNum + ",People_Number="+DayDeals.get(closeInd).peopleNum+",Value_Traded_Till_Now="+valueTradedTillNow1+",Shares_Traded_Till_Now="+sharesTradedTillNow1+",Last_Traded_Value="+lastTradedValue1+",Real_Value="+stockValue1 + ",price=" + DayDeals.get(closeInd).price+" " + tempTime;
                    closeInd++;
                    INDEX++;
                }
                Methods.influxDB.write(toInsertToDB);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(location);
            return false;
        }
    }
    private static boolean insertInDBBestLimitData(String location){
        try {
            String token = getBetweenStrings(location, Methods.motherAddress + File.separatorChar, File.separatorChar + "Days");
            String tempAddress = location;
            String date = tempAddress.substring(tempAddress.length() - 8);

            if ( new File(tempAddress+File.separatorChar+"BestLimitData.txt").exists() ){
                ArrayList<String> BLData = Methods.readFile(tempAddress+File.separatorChar+"BestLimitData.txt");
                for ( int i = 0 ; i < BLData.size() ; i++ ){
                    String[] tempString = BLData.get(i).split("\\s+");
                    if ( tempString.length!=8){
                        continue;
                    }
                    String strDate;
                    if ( tempString[0].length() == 6) {
                        strDate = date + " " + tempString[0];
                    }else{
                        strDate = date + " 0" + tempString[0];
                    }
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
                    LocalDateTime zdt = LocalDateTime.parse(strDate, dtf);
                    Long time = zdt.toInstant(ZoneOffset.ofHoursMinutes(0, 0)).toEpochMilli();
                    time = time * 1000000;

                    String toInsertToDB;
                    toInsertToDB = "Best_Limit_History,company_Number="+token+",Row_Number="+tempString[1]+" People_Number_Buying=" + tempString[2]+",Share_Number_Buying="+tempString[3]+",Buying_Price="+tempString[4]+",Selling_Price="+tempString[5]+",Share_Number_Selling="+tempString[6]+",People_Number_Selling="+tempString[7] + " " + time;
                    Methods.influxDB.write(toInsertToDB);
                }
            }
            return true;

        }catch(Exception e){
            e.printStackTrace();
            System.out.println(location);
            return false;
        }
    }
    private static void recursiveBeautify(File dir) {
        if ( isBeautifyingNow ) {
            try {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        recursiveBeautify(file);
                    } else {
                        String tempAddress = file.getCanonicalPath();
                        if (tempAddress.contains(File.separatorChar + "RawHTML.txt")) {
                            tempAddress = tempAddress.substring(0, tempAddress.indexOf(File.separatorChar + "RawHTML.txt"));
                            if ( !new File(tempAddress+File.separatorChar+"Beautified").exists() ) {
                                ExtractFromRawHTML(tempAddress);
                                Methods.createDirectory(tempAddress + File.separatorChar + "Beautified");
                            }
                        }else if (tempAddress.contains(File.separatorChar + "info.txt" )){
                            System.out.println(tempAddress);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void createType1Data (String location) {
        Map<Long , String> checkingSellData = new HashMap<>();
        Map<Long , String> checkingBuyData = new HashMap<>();

        ArrayList<String> bld = new ArrayList<String>();
        ArrayList<String> lastRow = new ArrayList<String>(6);
        for (int i = 0  ; i < 6; i++)
            lastRow.add(null);
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(location + File.separatorChar + "BestLimitData.txt"));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] sep = line.split("\t|\\s");
                int row = Integer.parseInt(sep[1]);
                if (lastRow.get(row) != null) {
                    String[] sepLastRow = lastRow.get(row).split("\t|\\s");
                    if (Long.parseLong(sep[2]) == Long.parseLong(sepLastRow[2]) && Long.parseLong(sep[3]) == Long.parseLong(sepLastRow[3]) && Long.parseLong(sep[4]) == Long.parseLong(sepLastRow[4])) {
                        // it's a sell
                        if ( checkingSellData.containsKey(Long.parseLong(sep[5]))){
                            if ( !checkingSellData.get(Long.parseLong(sep[5])).equals( sep[6] + "\t" + sep[7] ) ){
                                String tempString = checkingSellData.get(Long.parseLong(sep[5]));
                                String[] temp = tempString.split("\t|\\s");

                                String sell = new String(sep[0] + "\t" + sep[5] + "\t" + sep[6] + "\t" + sep[7] );
                                Methods.appendToFile(sell, location + File.separatorChar + "Table_1_sell.txt");
                                checkingSellData.put(Long.parseLong(sep[5]),sep[6] + "\t" + sep[7]);
                            }
                        }else {
                            String sell = new String(sep[0] + "\t" + sep[5] + "\t" + sep[6] + "\t" + sep[7]);
                            Methods.appendToFile(sell, location + File.separatorChar + "Table_1_sell.txt");
                            checkingSellData.put(Long.parseLong(sep[5]),sep[6] + "\t" + sep[7]);
                        }
                    }
                    else if(Long.parseLong(sep[5]) == Long.parseLong(sepLastRow[5]) && Long.parseLong(sep[6]) == Long.parseLong(sepLastRow[6]) && Long.parseLong(sep[7]) == Long.parseLong(sepLastRow[7])){
                        // it's a buy
                        if ( checkingBuyData.containsKey(Long.parseLong(sep[4]))){
                            if ( !checkingBuyData.get(Long.parseLong(sep[4])).equals(sep[3] + "\t" + sep[2])){
                                String tempString = checkingBuyData.get(Long.parseLong(sep[4]));
                                String[] temp = tempString.split("\t|\\s");
                                String buy = new String(sep[0] + "\t" + sep[4] + "\t" + sep[3] +  "\t" + sep[2]);
                                Methods.appendToFile(buy, location + File.separatorChar + "Table_1_buy.txt");
                                checkingBuyData.put(Long.parseLong(sep[4]),sep[3] + "\t" + sep[2]);
                            }
                        }else {
                            String buy = new String(sep[0] + "\t" + sep[4] + "\t" + sep[3] + "\t" + sep[2]);
                            Methods.appendToFile(buy, location + File.separatorChar + "Table_1_buy.txt");
                            checkingBuyData.put(Long.parseLong(sep[4]),sep[3] + "\t" + sep[2]);
                        }
                    }
                    lastRow.set(row,line);
                }
                else {
                    lastRow.set(row, line);
                }
                bld.add(line);
            }
            reader.close();
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", location + File.separatorChar + "BestLimitData.txt");
            e.printStackTrace();
        }

    }
}

