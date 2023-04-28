import java.io.*;
import java.util.*;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import javax.lang.model.type.NullType;
import java.io.FileOutputStream;
import java.io.IOException;
public class Main {
    private static LinkedHashMap<String,Integer> statistics = new LinkedHashMap<>();
    private static Integer numOfRetweets = 0;
    private static Integer numOfEng = 0;
    private static Integer numOTweets = 0;
    private static int getNumOfLines(ArrayList<String> files) throws IOException {
        System.out.println("Calculating the number of tweets...");
        int lines = 0;
        for (String file:files){
            if (file.endsWith(".txt")){
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                lineNumberReader.skip(Long.MAX_VALUE);
                lines += lineNumberReader.getLineNumber();
                lineNumberReader.close();
            }
        }
        return lines;
    }
    public static void createExcel() throws IOException {
        System.out.println("Writing the Excel file...");
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell Cell_0 = row.createCell(0);
        Cell Cell_1 = row.createCell(1);
        Cell_0.setCellValue("Day");
        Cell_1.setCellValue("#of tweets");
        int rowCount = 0;
        for(HashMap.Entry entry:statistics.entrySet()) {
            Row row_n = sheet.createRow(++rowCount);
            row_n.createCell(0).setCellValue((String)entry.getKey());
            row_n.createCell(1).setCellValue((Integer)entry.getValue());
        };
        try(FileOutputStream fileOut = new FileOutputStream("histogram.xlsx")){
            workbook.write(fileOut);
        }
    }
    public static void main(String[] args) throws IOException {
        String pathTweets = "data/brexit/"; // input tweets file
        PrintWriter outputRunning = new PrintWriter("Running_information.txt");
//////////////////////////////////////////////////////////////////////////////////// loading tweets
        File dir = new File(pathTweets);
        String[] folders = dir.list(); // list of folders
        ArrayList<String> files = new ArrayList<>(); // list of files within folders
        for (String folder:folders){
            if (folder.startsWith("disc")){
                File subDir = new File(pathTweets + "/" +folder);
                for (String file : subDir.list()){
                    files.add(pathTweets + folder + "/" + file);
                }
            }
        }
        int lines;
        int iter = 0;
        float percentage;
        lines = getNumOfLines(files); // getting the number of all tweets
        for (String file : files) {
            if (file.endsWith(".txt")){
                //System.out.println("Reading file: " + file);
                //outputRunning.print("\n Reading file: " + file);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                Object obj;
///////////////////////////////////////////////////////////////////////////////////
                while ((line = reader.readLine()) != null) {
                    try {
                        obj = new JSONParser().parse(line);
                    } catch (ParseException e) {
                        continue;
                    } finally {
                        //////////////////////////////////////////////////////////////////////
                        percentage = (float) 100* iter / lines;
                        //long startTime = System.nanoTime();
                        if (percentage % 5 == 0) {
                            System.out.println("Creating the statistics: " + percentage + " %");
                            //outputRunning.print("\nCreating the statistics: " + percentage + " %");
                        }
                        iter = iter + 1;
                        ///////////////////////////////////////////////////////////////////////
                    }
                    // typecasting obj to JSONObject
                    JSONObject jo = (JSONObject) obj;
                    String createData = (String) jo.get("created_at");
///////////////////////////////////////////////////////////////////////////////////////////////
                    if (createData != null) {
                        try{
                            String[] date = createData.split("\\s+");
                            if (!statistics.containsKey(date[1] + " " + date[2])) {
                                statistics.put(date[1] + " " + date[2], 1);
                            } else {
                                int value = statistics.get(date[1] + " " + date[2]);
                                value = value + 1;
                                statistics.put(date[1] + " " + date[2], value);
                            }
                            numOTweets = numOTweets + 1;
                            if (jo.get("lang").equals("en")) {
                                numOfEng = numOfEng +1;
                            }
                            if (jo.get("retweeted_status") != null) {
                                numOfRetweets = numOfRetweets + 1;
                            }
                        }catch (NullPointerException e){
                            continue; //skip
                        }
                    }
                }
            }
        }
        //System.out.println(statistics);
        System.out.println("\n\n Statistics: \n");
        System.out.println("# of all tweets: " + lines);
        System.out.println("# of retweets: " + numOfRetweets);
        System.out.println("# of tweets with language (EN): " + numOfEng);
        outputRunning.print("\n\n  Statistics: \n");
        outputRunning.print("\n# of all tweets: " + lines);
        outputRunning.print("\n# of retweets: " + numOfRetweets);
        outputRunning.print("\n# of tweets with language (EN): " + numOfEng);
        outputRunning.close();
        createExcel();
    }
}