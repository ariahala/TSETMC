import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

public class Main extends Application {

    ArrayList<Company> EveryCompany = new ArrayList<>();
    clientType clientData;

    public void start(Stage primaryStage) throws Exception {
//        InfluxDB influxDB = InfluxDBFactory.connect("http://127.0.0.1:8086","ariahala","13761367");
//        String DATABASE = new String("IRANSTOCK");
//        influxDB.createDatabase(DATABASE);
//        influxDB.setDatabase(DATABASE);
//        Methods.influxDB = influxDB;
        Methods m = new Methods();
        Group root = new Group();
        Group secondRoot = new Group();

//        //Redirecting System output
//        TextArea ta = TextAreaBuilder.create().prefWidth(500).prefHeight(700).wrapText(true).build();
//        ta.setLayoutY(0);
//        ta.setLayoutX(0);
//        Console console = new Console(ta);
//        PrintStream ps = new PrintStream(console,false);
//        System.setOut(ps);
//        System.setErr(ps);
//        secondRoot.getChildren().add(ta);
//        ta.textProperty().addListener(new ChangeListener<Object>() {
//            @Override
//            public void changed(ObservableValue<?> observable, Object oldValue,
//                                Object newValue) {
//                ta.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
//                //use Double.MIN_VALUE to scroll to the top
//            }
//        });
//        ta.setEditable(false);

        //Setting the initalize button
        Button initalize = new Button("Initalize");
        initalize.setLayoutX(20);
        initalize.setLayoutY(70);
        initalize.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isInitingNow = true;
                        try{
                            String everyLink = Methods.getHTML("http://www.tsetmc.com/Loader.aspx?ParTree=111C1417");
                            Methods.writeToFile(everyLink,"." + File.separatorChar + "Tokens.txt");
                            ArrayList<String> AllCompanies = Methods.readFile("." + File.separatorChar + "Tokens.txt");
                            System.out.println("." + File.separatorChar + "Tokens.txt");
                            try{
                                PrintWriter writer = new PrintWriter("." + File.separatorChar + "Tokens.txt", "UTF-8");
                                String checkLink = new String("o");
                                for ( int i = 0 ; i < AllCompanies.size() ; i++ ){
                                    String temp = AllCompanies.get(i);
                                    if ( temp.contains("href")){
                                        int ind = temp.indexOf("inscode=");
                                        temp = temp.substring(ind+8,temp.length()-1);
                                        ind = temp.indexOf("target");
                                        temp = temp.substring(0,ind-2);
                                        if ( temp.equals(checkLink) ) {
                                        }else {
                                            writer.println(temp);
                                        }
                                        checkLink = temp;
                                    }
                                }
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ArrayList<String> Tokens = Methods.readFile("." + File.separatorChar + "Tokens.txt");
                            for (int i = 0 ; i < Tokens.size() ; i++ ) {
                                if (!Methods.isInitingNow){
                                    break;
                                }
                                EveryCompany.add(new Company(Tokens.get(i)));
                                System.out.println((double)100*i/Tokens.size());
                            }
                            ArrayList<String> FaraTokens = Methods.readFile("." + File.separatorChar + "Fara.txt");
                            for ( int i = 0 ; i < FaraTokens.size() ; i++ ){
                                System.out.println("FaraBoors");
                                if (!Methods.isInitingNow){
                                    break;
                                }
                                EveryCompany.add(new Company(FaraTokens.get(i)));
                                System.out.println((double)100*i/FaraTokens.size());
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        System.out.println("Done initalizing");
                    }
                }).start();
            }

        });
        root.getChildren().add(initalize);
        Button haltInitalizing = new Button("Halt");
        haltInitalizing.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isInitingNow = false;
            }
        });
        haltInitalizing.setLayoutY(70);
        haltInitalizing.setLayoutX(150);
        root.getChildren().add(haltInitalizing);

        //getting address textfield and button handling
        TextField motherAdd = new TextField("/Users/mac/Desktop/Data");
        motherAdd.setLayoutY(20);
        motherAdd.setLayoutX(130);
        root.getChildren().add(motherAdd);
        Button getAddress = new Button("Set Address");
        getAddress.setLayoutY(20);
        getAddress.setLayoutX(20);
        getAddress.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.motherAddress = motherAdd.getText();
                System.out.println();
            }
        });
        root.getChildren().add(getAddress);


        //update button handling
        Button updateButton = new Button("Update");
        updateButton.setLayoutX(20);
        updateButton.setLayoutY(120);
        root.getChildren().add(updateButton);
        updateButton.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isUpdatingNow = true;
                        for (int i = 0 ; i < EveryCompany.size() ; i++ ){
                            EveryCompany.get(i).update();
                            System.out.println("Done updating " + EveryCompany.get(i).getToken());
                        }
                        System.out.println("DONE UPDATING OVERALL!!!!");
                    }
                }).start();
            }
        });
        Button haltUpdate = new Button("Halt");
        haltUpdate.setLayoutX(150);
        haltUpdate.setLayoutY(120);
        haltUpdate.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isUpdatingNow = false;
                System.out.println("Successfully halted updating");
            }
        });
        root.getChildren().add(haltUpdate);

        //DayCollectButton handling, that start to collect the stuff
        DatePicker fromDate = new DatePicker();
        javafx.scene.text.Text fromDateTxt = new javafx.scene.text.Text("From this Date");
        fromDateTxt.setLayoutX(250);
        fromDateTxt.setLayoutY(185);
        root.getChildren().add(fromDateTxt);
        fromDate.setLayoutY(170);
        fromDate.setLayoutX(20);
        DatePicker toDate = new DatePicker();
        javafx.scene.text.Text toDateTxt = new javafx.scene.text.Text("To this Date");
        toDateTxt.setLayoutX(250);
        toDateTxt.setLayoutY(235);
        root.getChildren().add(toDateTxt);
        toDate.setLayoutY(220);
        toDate.setLayoutX(20);
        root.getChildren().add(toDate);
        root.getChildren().add(fromDate);
        Button getDays = new Button("Get Days");
        getDays.setLayoutX(20);
        getDays.setLayoutY(270);
        getDays.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isPlayingNow = true;
                        LocalDate fromTemp = fromDate.getValue();
                        int temp = 10000*fromTemp.getYear() + 100*fromTemp.getMonthValue() + fromTemp.getDayOfMonth();
                        String from = "" + temp;
                        LocalDate toTemp = toDate.getValue();
                        temp = 10000*toTemp.getYear() + 100*toTemp.getMonthValue() + toTemp.getDayOfMonth();
                        String to = "" + temp;
                        for (Company comp: EveryCompany ){
                            comp.downloadData(from,to);
                            System.out.println(comp.getToken() + " Done!");
                        }
                        System.out.println("Getting days DONE!!!!!!");
                        Methods.isPlayingNow = false;
                    }
                }).start();
            }
        });
        root.getChildren().add(getDays);
        Button haltGettingDays = new Button("Halt");
        haltGettingDays.setLayoutY(270);
        haltGettingDays.setLayoutX(150);
        haltGettingDays.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isPlayingNow = false;

            }
        });
        root.getChildren().add(haltGettingDays);

        //Beautifying Button handling
        Button beautify = new Button("Beautify");
        beautify.setLayoutX(20);
        beautify.setLayoutY(320);
        beautify.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Started Beautifying");
                        Beautifier.isBeautifyingNow = true;
                        Beautifier.Beautify();
                        System.out.println("Successfully....");
                    }
                }).start();

            }
        });
        root.getChildren().add(beautify);
        Button haltBeautify = new Button("Halt");
        haltBeautify.setLayoutY(320);
        haltBeautify.setLayoutX(150);
        haltBeautify.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Beautifier.isBeautifyingNow = false;
                        System.out.println("Halted beautifying");
                    }
                }).start();
            }
        });
        root.getChildren().add(haltBeautify);



        //failhandling button
        Button failHandler = new Button("Handle Fails");
        failHandler.setLayoutX(20);
        failHandler.setLayoutY(370);
        failHandler.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if ( !new File(Methods.motherAddress + File.separatorChar + "Fails.txt").exists()){
                            Methods.writeToFile(new String(""),Methods.motherAddress + File.separatorChar + "Fails.txt");
                        }
                        Methods.isFailhandling = true;
                        Methods.startFailHandler();
                        System.out.println("Fail handling DONE!!!!");
                    }
                }).start();
            }
        });
        root.getChildren().add(failHandler);
        Button haltFailHandling = new Button("Halt");
        haltFailHandling.setLayoutX(150);
        haltFailHandling.setLayoutY(370);
        root.getChildren().add(haltFailHandling);
        haltFailHandling.setOnMouseClicked(new javafx.event.EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isFailhandling = false;
                System.out.println("Halted Fail handling!!!!!!!!!!!");
            }
        });

        //Adding the Insertion into DB button
        Button insertToDB = new Button("Insert to DB");
        insertToDB.setLayoutX(20);
        insertToDB.setLayoutY(420);
        insertToDB.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isInsertingRightNow = true;
                        Beautifier.StartInsertingInDB();
                        System.out.println("FUCKING DONE WITH DB!");
                    }
                }).start();
            }
        });
        Button cancelDB = new Button("Halt");
        cancelDB.setLayoutX(150);
        cancelDB.setLayoutY(420);
        cancelDB.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isInsertingRightNow = false;
            }
        });
        root.getChildren().add(insertToDB);
        root.getChildren().add(cancelDB);

        Group thirdRoot = new Group();


        clientData = new clientType(EveryCompany, Methods.influxDB);

        Button collectClientData = new Button("Collect Client Data");
        collectClientData.setLayoutX(20);
        collectClientData.setLayoutY(470);
        collectClientData.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isGettingClientData = true;
                        clientData.InsertInDB();
                        System.out.println("DONE!!!!");
                    }
                }).start();
            }
        });
        root.getChildren().add(collectClientData);
        Button cancelClientData = new Button("Halt");
        cancelClientData.setLayoutX(150);
        cancelClientData.setLayoutY(470);
        cancelClientData.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isGettingClientData = false;
            }
        });
        root.getChildren().add(cancelClientData);


        //setting the trade day info getter and halter buttons
        Button getDayTradeInfo = new Button("Collect Days Info");
        getDayTradeInfo.setLayoutY(520);
        getDayTradeInfo.setLayoutX(20);
        getDayTradeInfo.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ExecutorService executorService = Executors.newFixedThreadPool(10);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Methods.isGettingTradeDayInfo = true;
                        for ( Company comp: EveryCompany){
                            if (!Methods.isGettingTradeDayInfo)
                                break;
                            ArrayList<DBDataDay> data = comp.collectTradeDaysInfo();
                            System.out.println("Done getting from net " + comp.getToken());
                            executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println(data.size());
                                    for (DBDataDay db : data){
                                        db.writeToDB(Methods.influxDB,comp.getToken());
                                    }
                                    System.out.println("Done Inserting " + comp.getToken());
                                }
                            });
                        }

                        System.out.println("Done");
                    }
                }).start();

            }
        });
        Button haltDaysInfoGetter = new Button("Halt");
        haltDaysInfoGetter.setLayoutY(520);
        haltDaysInfoGetter.setLayoutX(150);
        haltDaysInfoGetter.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isGettingTradeDayInfo = false;
            }
        });
        root.getChildren().add(getDayTradeInfo);
        root.getChildren().add(haltDaysInfoGetter);

        //setting the scene and adding the Group root to Run the Experimental UI
        Scene firstScene = new Scene(root,500,700);
        Scene secondScene = new Scene(secondRoot,500,700);
        Stage secondaryStage = new Stage();
        secondaryStage.setScene(secondScene);
        primaryStage.setScene(firstScene);
        secondaryStage.show();
        primaryStage.show();




        //handling the exit button
        Button exitButton = new Button("Quit");
        exitButton.setLayoutX(20);
        exitButton.setLayoutY(570);
        exitButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Methods.isFailhandling = false;
                System.out.println("Fail handling DONE!!!!");
                Beautifier.isBeautifyingNow = false;
                System.out.println("Halted beautifying");
                Methods.isPlayingNow = false;
                Methods.isUpdatingNow = false;
                primaryStage.close();
                secondaryStage.close();
            }
        });
        root.getChildren().add(exitButton);


    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Console extends OutputStream {

        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }

        public void write(int b) throws IOException {
            Platform.runLater(() -> appendText(String.valueOf((char)b)));
        }
    }


}
