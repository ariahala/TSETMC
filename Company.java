import java.io.File;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;




public class Company {

    private String token;
    private String address;
    private ArrayList<String> openDays = new ArrayList<>();
    private ArrayList<Log> logs = new ArrayList<>();
    private int notCollectedDays;

    public Company (String token){
        this.token = token;
        this.address = Methods.motherAddress + File.separatorChar + this.token;

        if (new File(Methods.motherAddress + File.separatorChar + token).exists()){
            this.openDays = Methods.readFile(this.address+File.separatorChar + "OpenDays.txt");
        }else{
            Methods.createDirectory(address);
            this.openDays = readOpenDates(999999);
            writeInfo();
            writeOpenDays();
        }
        if (new File(Methods.motherAddress + File.separatorChar + token + File.separatorChar + "Days").exists()) {

        }else {
            Methods.createDirectory(address + File.separatorChar + "Days");
        }
        if (new File(Methods.motherAddress + File.separatorChar + token + File.separatorChar + "TradeDayLog.txt").exists() ){
            this.notCollectedDays = Integer.parseInt(Methods.readFile(Methods.motherAddress + File.separatorChar + token + File.separatorChar + "TradeDayLog.txt").get(0));
        }else{
            Methods.writeToFile("999999",Methods.motherAddress + File.separatorChar + token + File.separatorChar + "TradeDayLog.txt");
            this.notCollectedDays = 999999;
        }
    }
    private synchronized ArrayList<String> readOpenDates(int numOfDays){
        String rawHTML = Methods.getCompanyOpenTradeDaysHTML(this.token,numOfDays);
        ArrayList<String>  ans = new ArrayList<>();
        int index = 0;

        while(rawHTML.indexOf("@" , index) != -1) {
            index = rawHTML.indexOf("@",index);
//            System.out.println(index);
            ans.add(rawHTML.substring(index-8,index));
//            System.out.println(ans.get(ans.size()-1));
            index = rawHTML.indexOf(";", index);
        }
        ArrayList<String> finale = new ArrayList<>();
        for ( int i = 0 ; i < ans.size() ; i++ ){
            finale.add(ans.get(ans.size()-1-i));
        }
        return finale;
    }
    private synchronized void writeInfo (){
        String info = Methods.getCompanyInfoHTML(this.token);
        info = info.substring(info.indexOf("content"));
        info = info.substring(info.indexOf("<tbody>")+7);
        info = info.substring(0,info.indexOf("</tbody>"));
        info = info.replaceAll("</tr>" , "");
        info = info.replaceAll("<tr>" , "");
        info = info.replaceAll("</td>" , "");
        info = info.replaceAll("<td>" , "");
        info = info.replaceAll("( )+"," ");
        info = info.replaceAll("\n \n" , "\n");
        info = info.replaceAll(" \n" , "\n");
        info = info.replaceAll("\n " , "\n");
        String[] INFO = info.split("[\r\n]+");
//        for ( int i = 0 ; i < INFO.length ; i++) {
//            System.out.println(INFO[i]);
//        }
        ArrayList inf = new ArrayList();
        for ( int i = 1 ; i < INFO.length ; i++ ){

            inf.add(INFO[i]);
            if ( i%2 == 0 ) {
                inf.add(new String(""));
            }
        }
        Methods.writeToFile(inf,Methods.motherAddress +File.separatorChar+token+File.separatorChar + "info.txt");
    }
    private synchronized void writeOpenDays (){
        Methods.writeToFile(this.openDays,address+File.separatorChar + "OpenDays.txt");
        ArrayList<String> log = new ArrayList<>();
            for (int i = 0; i < this.openDays.size(); i++) {
                log.add(new String(this.openDays.get(i) + "\t0"));
            }
            Methods.writeToFile(log, address + File.separatorChar + "Log.txt");
    }
    public synchronized void update (){
        if( Methods.isUpdatingNow) {
            if (this.openDays.size() != 0) {
                String lastOpenDate = this.openDays.get(this.openDays.size() - 1);
                int year = Integer.parseInt(lastOpenDate.substring(0, 4));
                int month = Integer.parseInt(lastOpenDate.substring(4,6));
                int day = Integer.parseInt(lastOpenDate.substring(6,8));
                String realLastOpenDate = readOpenDates(1).get(0);
                int newYear = Integer.parseInt(realLastOpenDate.substring(0, 4));
                int newMonth = Integer.parseInt(realLastOpenDate.substring(4,6));
                int newDay = Integer.parseInt(realLastOpenDate.substring(6,8));
                int numOfDaysToFetch = ( newYear - year ) * 370;
                numOfDaysToFetch += Math.abs(newMonth - month) * 40;
                numOfDaysToFetch += Math.abs(newDay - day);
                ArrayList<String> newOpenDays = readOpenDates(numOfDaysToFetch);
                int THEIndex = -1;
                for ( int i = 0 ; i < newOpenDays.size() ; i++){
                    if ( newOpenDays.get(i).equals(lastOpenDate) ){
                        if ( i != newOpenDays.size() ) {
                            THEIndex = i + 1;
                        }
                        break;
                    }
                }
                if ( THEIndex != -1 ){
                    ArrayList<String> toAddToOpenDays = new ArrayList<>();
                    ArrayList<String> toAddToLog = new ArrayList<>();
                    for ( int i = THEIndex ; i < newOpenDays.size() ; i++ ){
                        openDays.add(newOpenDays.get(i));
                        toAddToOpenDays.add(newOpenDays.get(i));
                        toAddToLog.add(newOpenDays.get(i) + "\t0");
                    }
                    this.notCollectedDays++;
                    Methods.appendToFile(toAddToOpenDays,this.address+File.separatorChar + "OpenDays.txt");
                    Methods.appendToFile(toAddToLog,this.address+File.separatorChar + "Log.txt");
                }
            }else{
                this.openDays = readOpenDates(999999);
                writeOpenDays();
            }
            this.notCollectedDays = this.notCollectedDays + 2;
            if (this.notCollectedDays <= 999999 ) {
                Methods.writeToFile("" + this.notCollectedDays, Methods.motherAddress + File.separatorChar + token + File.separatorChar + "TradeDayLog.txt");
            } else{
                Methods.writeToFile("999999", Methods.motherAddress + File.separatorChar + token + File.separatorChar + "TradeDayLog.txt");

            }
        }
    }
    private synchronized void readLog (){
        this.logs = new ArrayList<>();
        ArrayList<String> tempLog = Methods.readFile(this.address + File.separatorChar + "Log.txt");
        for ( int i = 0 ; i < tempLog.size() ; i++ ){
            String[] tempString = tempLog.get(i).split("\t");
            if (tempString[1].equals("0")){
                logs.add(new Log(tempString[0],false));
            }else{
                logs.add(new Log(tempString[0],true));
            }
        }
    }
    public synchronized void downloadData (String FromDate , String ToDate){
        this.readLog();
        for ( int i = 0 ; i < this.logs.size() ; i++ ){
            if ( Integer.parseInt(logs.get(i).date) >= Integer.parseInt(FromDate) && Integer.parseInt(logs.get(i).date) <= Integer.parseInt(ToDate)  ){
                logs.get(i).hasToBeFetched = true;
            }
        }

            for (int i = 0; i < logs.size(); i++) {
                if (logs.get(i).hasToBeFetched) {
                    String dateToFetch = logs.get(i).date;
                    if ( !new File(address + File.separatorChar + "Days" + File.separatorChar+dateToFetch).exists()) {
                        Methods.createDirectory(address + File.separatorChar + "Days" + File.separatorChar + dateToFetch);
                        String RawData = Methods.getCompanyDayHTML(token, dateToFetch);
                        Methods.writeToFile(RawData, address + File.separatorChar + "Days" + File.separatorChar + dateToFetch + File.separatorChar + "RawHTML.txt");
                    }else{
                        System.out.println("Company with " + this.token + " token at day " + dateToFetch + " has been already fetched");
                    }
                    logs.remove(i);
//                    System.out.println(i + " " + logs.size());
                    i--;
                }
                if (!Methods.isPlayingNow){
                    break;
                }
            }

//        System.out.println("33333333333");
        ArrayList<String> toWriteLogs = new ArrayList<>();
        String temp;
        for ( int i = 0 ; i < logs.size() ; i++ ){
            if ( logs.get(i).hasToBeFetched) {
                temp = new String(logs.get(i).date + "\t1");
            }else{
                temp = new String(logs.get(i).date + "\t0");
            }
            toWriteLogs.add(temp);
        }
        Methods.writeToFile(toWriteLogs,address+File.separatorChar + "Log.txt");
    }
    public synchronized String getToken() {
        return token;
    }
    public synchronized ArrayList<DBDataDay> collectTradeDaysInfo(){
        update();
        String input = null;
        while(input==null){
            input = Methods.getCompanyOpenTradeDaysHTML(token,notCollectedDays);
        }
        ArrayList<DBDataDay> data = new ArrayList<>();
        input = input.replaceAll(";","\n").replaceAll("@","\t");
        if ( input.indexOf("<body>") + 7 > input.indexOf("</body>")) {

        }else {
            input = input.substring(input.indexOf("<body>") + 7, input.indexOf("</body>"));

            String[] splittedInput = input.split("\\n");
            for (int i = 0; i < splittedInput.length; i++) {
                String tempString = splittedInput[i];
                String[] tempArrString = tempString.split("\\s+");
                if (tempArrString.length != 10) {
                    continue;
                }
                data.add(new DBDataDay(tempArrString[0],Integer.parseInt(tempArrString[1].substring(0,tempArrString[1].indexOf('.'))),Integer.parseInt(tempArrString[2].substring(0,tempArrString[2].indexOf('.'))),Integer.parseInt(tempArrString[3].substring(0,tempArrString[3].indexOf('.'))),Integer.parseInt(tempArrString[4].substring(0,tempArrString[4].indexOf('.'))),Integer.parseInt(tempArrString[5].substring(0,tempArrString[5].indexOf('.'))),Integer.parseInt(tempArrString[6].substring(0,tempArrString[6].indexOf('.'))),Long.parseLong(tempArrString[7].substring(0,tempArrString[7].indexOf('.'))),Long.parseLong(tempArrString[8]),Integer.parseInt(tempArrString[9])));
            }
        }
        Methods.writeToFile("0",Methods.motherAddress + File.separatorChar + token + File.separatorChar +"TradeDayLog.txt");
        return data;
    }
}
