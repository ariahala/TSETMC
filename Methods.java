import org.influxdb.InfluxDB;
import org.jsoup.Jsoup;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by mac on 8/26/17.
 */
public class Methods {
    public static String motherAddress = "/Users/mac/Desktop/Data";
    public static boolean isInitingNow;
    public static boolean isPlayingNow;
    public static boolean isFailhandling;
    public static boolean isUpdatingNow;
    public static boolean isInsertingRightNow;
    public static InfluxDB influxDB;
    public static boolean isGettingClientData;
    public static boolean isGettingTradeDayInfo;


    public static void startFailHandler(){

        ArrayList<String> fails = new ArrayList<>();
        fails = readFile(motherAddress + File.separatorChar +"Fails.txt");
        ArrayList<String> failToWrite = new ArrayList<>();
        for ( String fail : fails ){
            if ( !isFailhandling ){
                break;
            }
            if ( fail.contains("http://cdn.tsetmc.com/Loader.aspx?ParTree=15131P&i=")){
                //handling fails related to day fetching
                String token = fail.substring(51,fail.length());
                int index = token.indexOf('&');
                String date = token.substring(index+3,token.length());
                token = token.substring(0,index);
                String RawData = Methods.getCompanyDayHTML(token, date);
                Methods.writeToFile(RawData, motherAddress + File.separatorChar + token + File.separatorChar +"Days" + File.separatorChar + date + File.separatorChar +"RawHTML.txt");
                ArrayList<String> toCheckRawHTML = readFile(motherAddress + File.separatorChar +"" + token + File.separatorChar +"Days" + File.separatorChar + date + File.separatorChar +"RawHTML.txt");
                if (toCheckRawHTML.get(0).equals("nullnull")){
                    failToWrite.add(fail);
                }else{
                    System.out.println("Successfully handled fail at token " + token + " at the date " + date);
                }
            }else{
                failToWrite.add(fail);
                //TODO handle other type of fails
            }
        }
        writeToFile(failToWrite,motherAddress + File.separatorChar +"Fails.txt");
    }
    public static String getHTML (String Link){
        try {
            return Jsoup.connect(Link).maxBodySize(0).timeout(60000000).get().html();
        }catch (Exception e){
            e.printStackTrace();
            if (new File(Methods.motherAddress + File.separatorChar +"Fails.txt").exists()) {
                System.out.println("Something went wrong at token " + Link + " HTML fetching");
                appendToFile(Link,Methods.motherAddress + File.separatorChar +"Fails.txt");
            }else{

                System.out.println("Something went wrong at token " + Link + " HTML fetching");
                writeToFile(Link,Methods.motherAddress + File.separatorChar +"Fails.txt");

            }
            return null;
        }
    }
    public static ArrayList<String> readFile(String filename) {
        ArrayList<String> records = new ArrayList<String>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();
            return records;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }
    public static String readFileToString(String filename) {
        String record = new String();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separatorChar");

            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            reader.close();
            return stringBuilder.toString();
        }

        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }

    }
    public static boolean createDirectory ( String dirLocation ){
        File dir = new File(dirLocation);

        // attempt to create the directory here
        boolean successful = dir.mkdir();
        if (successful)
        {
            return true;
        }
        else
        {
            // creating the directory failed
            System.out.println("failed trying to create the directory " + dirLocation);
            return false;
        }
    }
    public static void writeToFile ( ArrayList<String> toWrite , String location ){
        try{
            PrintWriter writer = new PrintWriter(location, "UTF-8");

            for ( int i = 0 ; i < toWrite.size() ; i++ ){
                writer.println(toWrite.get(i));
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeToFile ( String toWrite , String location ){
        try{
            PrintWriter writer = new PrintWriter(location, "UTF-8");


                writer.println(toWrite);


            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void appendToFile ( ArrayList<String> toWrite , String location ){
        try(FileWriter fw = new FileWriter(location, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            for ( int i = 0 ; i < toWrite.size() ; i++ ){
                out.println(toWrite.get(i));
            }
        } catch (IOException e) {
            System.out.println("Something went wrong with the append at " + location);
        }
    }
    public static void appendToFile ( String toWrite , String location ){
        try(FileWriter fw = new FileWriter(location, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(toWrite);
        } catch (IOException e) {
            System.out.println("Something went wrong with the append at " + location);
        }
    }
    private static String getCompInfoLink ( String companyToken ){
        return "http://www.tsetmc.com/Loader.aspx?Partree=15131M&i=" + companyToken;
    }
    private static String getCompanyOpenTradeDatesLink ( String companyToken , int numOfDays){
        return "http://members.tsetmc.com/tsev2/data/InstTradeHistory.aspx?i=" + companyToken + "&Top="+numOfDays+"&A=0";
    }
    private static String getCompanyDayLink (String companyToken , String date){
        return "http://cdn.tsetmc.com/Loader.aspx?ParTree=15131P&i=" + companyToken + "&d=" + date;
    }
    public static String getCompanyInfoHTML ( String companyToken){
        return getHTML(getCompInfoLink(companyToken));
    }
    public static String getCompanyOpenTradeDaysHTML ( String companyToken , int numOfDays){
        return getHTML(getCompanyOpenTradeDatesLink(companyToken,numOfDays));
    }
    public static String getCompanyDayHTML ( String companyToken , String date ){
        return getHTML(getCompanyDayLink(companyToken,date));
    }
    public static String getLiveString( String companyToken ){
        return getHTML("http://www.tsetmc.com/tsev2/data/instinfofast.aspx?i=" + companyToken +"&c=");
    }
    public static String getClientTypeDataString ( String companyToken ){
        return getHTML("http://www.tsetmc.com/tsev2/data/clienttype.aspx?i=" + companyToken);
    }

    public static void main(String[] args){
        System.out.println(getCompanyOpenTradeDaysHTML("46348559193224090",3).replaceAll(";","\n").replaceAll("@","\t"));
        String temp = "1234.00";
        System.out.println(temp.substring(0,temp.indexOf('.')));
    }
}

