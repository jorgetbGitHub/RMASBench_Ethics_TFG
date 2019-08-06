/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analyticsprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 *
 * @author jorgetb
 */
public class AnalyticsProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        
        //String s = "./report0.txt";
        
        args = new String[14];
        args[0] = "./report0.txt";
        args[1] = "./report1.txt";
        args[2] = "./report2.txt";
        args[3] = "./report3.txt";
        args[4] = "./report4.txt";
        args[5] = "./report5.txt";
        args[6] = "./report6.txt";
        args[7] = "./report7.txt";
        args[8] = "./report8.txt";
        args[9] = "./report9.txt";
        args[10] = "./report10.txt";
        args[11] = "./report11.txt";
        args[12] = "./report12.txt";
        args[13] = "./report13.txt";
        /*args[0] = "./report1_ATRules.txt";
        args[1] = "./report2_ATRules.txt";
        args[2] = "./report3_ATRules.txt";
        args[3] = "./report4_ATRules.txt";
        args[4] = "./report5_ATRules.txt";*/
        /*args[0] = "./report0_ATNoRules.txt";
        args[1] = "./report1_ATNoRules.txt";
        args[2] = "./report2_ATNoRules.txt";
        args[3] = "./report3_ATNoRules.txt";
        args[4] = "./report4_ATNoRules.txt";*/
        /*args[2] = "./report2.txt";
        args[3] = "./report3.txt";
        args[4] = "./report4.txt";*/
        
        File file;
        String strProperty;
        String st;
        
        int lastCount = -1;
        int totalNumericProperties = 0;
        int order;
        
        PropertyLine propertyLine;
        ArrayList<ArrayList<PropertyLine>> propertyLines = new ArrayList<>();
        ArrayList<Integer> variableOffset = new ArrayList<>();
        Map<Integer, String> subtitles = new HashMap<>();
        
        for (int i = 0; i < args.length; i++) {
        
            String s = args[i];
            
            System.out.print(s);
            System.out.println(s.equals("./report0.txt"));
            
            file = new File("../RSLB2/boot/" + s.substring(2));
            if (!file.exists()) {
                file = new File("RSLB2/boot/" + s.substring(2));
            }

            FileReader fr = new FileReader(file);
            
            BufferedReader br = new BufferedReader(fr);
            
            propertyLines.add(new ArrayList<PropertyLine>());
            PropertyLine lastPropertyLine = null;
            order = 0;
            
            while ((st = br.readLine()) != null) {
                
                if (isPropertyLine(st)) {
                    
                    String value = getValueAsString(st);
                    
                    if (isNumeric(value)) { //Check if is numeric value
                        propertyLine = buildNumericPropertyLine(st, order);
                                            
                    }else if (isBoolean(value)) { //Check if is boolean value
                        propertyLine = buildBooleanPropertyLine(st, order);
                    
                    }else { // Is string value
                        propertyLine = buildStringPropertyLine(st, order);
                    }

                    if (propertyLine != null) {
                        propertyLines.get(propertyLines.size()-1).add(propertyLine);

                        if (lastPropertyLine == null && (propertyLine.propertyName.equals("Total extinguished:")
                                || propertyLine.propertyName.equals("Total cleared:"))) {
                           variableOffset.add(propertyLines.get(propertyLines.size()-1).size() - 4);
                        }
                    }
                    
                    lastPropertyLine = propertyLine;
                
                }else {
                    //Is it substitle?
                    if (st.contains("***")) {
                        subtitles.put(totalNumericProperties, st);
                    }
                    
                    lastPropertyLine = null;
                }
                
                order++;
            }
            
            /*if (lastCount == -1) {
                lastCount = totalNumericProperties;
            }else {
                if (lastCount != totalNumericProperties) {
                    System.out.println("Worng number of numeric properties: lastCount->" + lastCount + "  totalNumericProperties->" + totalNumericProperties);
                    throw new Exception("Wrong number of numeric properties");
                }
            }
            
            totalNumericProperties = 0;*/
        }
        
        for (PropertyLine pl :propertyLines.get(0)) {
            if (pl instanceof NumericPropertyLine) {
                NumericPropertyLine npl = (NumericPropertyLine)pl;
                System.out.println(npl.propertyName + " " + npl.value + " " + npl.percentage + "% " + npl.postExplanation);
            }
        }
        
        //1. Check which is the maximum property lines
        int max = -1;
        int maxIdx = 0;
        for (int i = 0; i < propertyLines.size(); i++) {
            if (max < propertyLines.get(i).size()) {
                max = propertyLines.get(i).size();
                maxIdx = i;
            }
        }
        
        //2. Fix offset
        ArrayList<ArrayList<PropertyLine>> newPropertyLines = new ArrayList<>();
        for (int i = 0; i < propertyLines.size(); i++) {
            ArrayList<PropertyLine> currentList = propertyLines.get(i);
            if (currentList.size() < max) {
                //This list needs to be normalized
                int offset = (max - currentList.size())/9;
                ArrayList<PropertyLine> newArray = new ArrayList<>();
                for (int j = 0; j < currentList.size(); j++) {
                    if (j == variableOffset.get(i*2) + 3) {
                        for (int k = 0; k < offset; k++) {
                            
                            //last total rescued value
                            double newTotalRescued = ((NumericPropertyLine)currentList.get(j - 3)).value; 
                            double newTotalTime = ((NumericPropertyLine)currentList.get(j+2 - 3)).value + k + 1;
                            double newRescuedVelocity = newTotalRescued/newTotalTime;
                            
                            newArray.add(new NumericPropertyLine("Total rescued:", PropertyLine.NO_PERCENTAGE,
                            "", -1, newTotalRescued));
                            
                            newArray.add(new NumericPropertyLine("Rescue velocity:", PropertyLine.NO_PERCENTAGE,
                            "", -1, newRescuedVelocity));
                            
                            newArray.add(new NumericPropertyLine("TIME:", PropertyLine.NO_PERCENTAGE, "", -1, newTotalTime));
                        }
                    }
                    
                    if (j == variableOffset.get(i*2 + 1) + 3) {
                        for (int k = 0; k < offset; k++) {
                            
                            //last total rescued value
                            double newTotalRescued = ((NumericPropertyLine)currentList.get(j-3)).value; 
                            double newTotalTime = ((NumericPropertyLine)currentList.get(j-1)).value + k + 1;
                            double newRescuedVelocity = newTotalRescued/newTotalTime;
                            
                            newArray.add(new NumericPropertyLine("Total extinguished:", PropertyLine.NO_PERCENTAGE,
                            "", -1, newTotalRescued));
                            
                            newArray.add(new NumericPropertyLine("Extintion velocity:", PropertyLine.NO_PERCENTAGE,
                            "", -1, newRescuedVelocity));
                            
                            newArray.add(new NumericPropertyLine("TIME:", PropertyLine.NO_PERCENTAGE, "", -1, newTotalTime));
                        }
                    }
                    
                    newArray.add(currentList.get(j));
                }
                
                for (int k = 0; k < offset; k++) {
                            
                    //last total rescued value
                    double newTotalRescued = ((NumericPropertyLine)currentList.get(currentList.size()-3)).value; 
                    double newTotalTime = ((NumericPropertyLine)currentList.get(currentList.size()-1)).value + k + 1;
                    double newRescuedVelocity = newTotalRescued/newTotalTime;

                    newArray.add(new NumericPropertyLine("Total cleared:", PropertyLine.NO_PERCENTAGE,
                    "", -1, newTotalRescued));

                    newArray.add(new NumericPropertyLine("Clear velocity:", PropertyLine.NO_PERCENTAGE,
                    "", -1, newRescuedVelocity));

                    newArray.add(new NumericPropertyLine("TIME:", PropertyLine.NO_PERCENTAGE, "", -1, newTotalTime));
                }
                
                newPropertyLines.add(newArray);
            }else {
                newPropertyLines.add(propertyLines.get(i));
            }
        }
        
        ArrayList<PropertyLine> means = (ArrayList<PropertyLine>) computeMeans(newPropertyLines);
        
        //PropertyLine firstElement = means.remove(0);
        //PropertyLine secondElement = means.get(1);        
        
        ArrayList<String> lines = new ArrayList<>();
        int meansSize = means.size();
        for (int i = 0; i < meansSize + subtitles.size(); i++) {
            if (subtitles.containsKey(i)) {
                lines.add(subtitles.get(i));
            }else{
                lines.add(means.remove(0).toString());
            }
        }
        
        
        
        /*System.out.println("propertyNames = " + propertyNames);
        System.out.println("matrixNumericProperties = " + matrixNumericProperties);
        System.out.println("subtitles = " + subtitles);*/
        
       
       
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	Date date = new Date();
        Path finalReport = Paths.get("Report" + dateFormat.format(date) + ".txt");
        Files.write(finalReport, lines, Charset.forName("UTF-8"));
    }
    
    private static Collection<PropertyLine> computeMeans(ArrayList<ArrayList<PropertyLine>> propertyLines) {
        //ArrayList<Double> numericValues = new ArrayList<>();
        //ArrayList<Pair<Double, Double>>  numericValues = new ArrayList<>();
        ArrayList<Triple> numericValues = new ArrayList<>();
        boolean firstItr = true;
        int currentIdx;
        for (ArrayList<PropertyLine> plList :propertyLines) {
            currentIdx = 0;
            for (PropertyLine pl :plList) {
                if (pl instanceof NumericPropertyLine) {
                    NumericPropertyLine npl = (NumericPropertyLine)pl;
                    
                    if (firstItr) {
                        //numericValues.add(new Pair(npl.value, npl.percentage));
                        numericValues.add(new Triple(npl.value, npl.percentage, npl.propertyName));
                    }else {
                        if ( ((String)numericValues.get(currentIdx).third).equals(npl.propertyName) ) {
                            numericValues.set(currentIdx, new Triple((double)numericValues.get(currentIdx).first + npl.value,
                        (double)numericValues.get(currentIdx).second + npl.percentage, npl.propertyName));
                        }else {
                        }
                    }
                    
                    currentIdx++;
                }
            }
            
            firstItr = false;
        }
        
        for (int i = 0; i < numericValues.size(); i++) {
            numericValues.set(i, new Triple((double)numericValues.get(i).first/(double)propertyLines.size(),
            (double)numericValues.get(i).second/(double)propertyLines.size(), numericValues.get(i).third));
        }
        
        ArrayList<PropertyLine> returnedLines = new ArrayList<>(propertyLines.get(0));
        for (PropertyLine pl :returnedLines) {
            if (pl instanceof NumericPropertyLine) {
                Triple r = numericValues.remove(0);
                ((NumericPropertyLine) pl).value = (double)r.first;
                ((NumericPropertyLine) pl).percentage = (double)r.second;
            }
        }    
        
        return returnedLines;
    }
    
    private static boolean isPropertyLine(String line) {
        return line.contains(":");
    }
    
    private static String getValueAsString(String line) {
        return line.substring(line.indexOf(":") + 1).split(" ")[1];
    }
    
    private static boolean isNumeric(String str) { 
        try {  
          Double.parseDouble(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
    }
    
    private static boolean isBoolean(String str) {
        try {
            Boolean.parseBoolean(str);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    
    private static NumericPropertyLine buildNumericPropertyLine(String strProperty, int order) {
        int div = strProperty.indexOf(":");
        String propertyName = strProperty.substring(0, div + 1);
        String propertyContent = strProperty.substring(div + 1, strProperty.length());
        String[] splitContent = propertyContent.split(" ");
        
        String value = splitContent[1];
        double percentage = PropertyLine.NO_PERCENTAGE;
        String postExplanation = "";
        
        String percentageData = "";
        
        if (splitContent.length > 2 && propertyContent.contains("%")) {
            //Then there's a percentage in format ([0-100]%)
            percentageData = splitContent[2];
            try {
            percentage = Double.valueOf(percentageData.substring(1, percentageData.length() - 2));
            }catch (Exception ex) {
                percentage = -1;
            }
        }
        
        return new NumericPropertyLine(propertyName, percentage, postExplanation, order, Double.valueOf(value));
    }
    
    private static BooleanPropertyLine buildBooleanPropertyLine(String strProperty, int order) {
        return null;
    }
    
    private static StringPropertyLine buildStringPropertyLine(String strProperty, int order) {
        return null;
    }
    
    
    public static class PropertyLine {
        
        public static final double NO_PERCENTAGE = -1.0;
        
        public String propertyName;
        public double percentage;
        public String postExplanation;
        public int order;
        
        public PropertyLine(String propertyName, Double percentage, String postExplanation, int order) {
            this.propertyName = propertyName;
            this.percentage = percentage;
            this.postExplanation = postExplanation;
            this.order = order;
        }
    }
    
    public static class NumericPropertyLine extends PropertyLine {
        public double value;
        
        public NumericPropertyLine(String propertyName, Double percentage, String postExplanation, int order, double value) {
            super(propertyName, percentage, postExplanation, order);
            this.value = value;
        }
        
        @Override
        public String toString() {
            if (this.percentage != NO_PERCENTAGE) {
                return this.propertyName + " " + this.value + " (" + this.percentage + "%) " + this.postExplanation;
            }else {
                return this.propertyName + " " + this.value + " " + this.postExplanation;
            }
        }
    }
    
    public static class BooleanPropertyLine extends PropertyLine {
        public boolean value;
        
        public BooleanPropertyLine(String propertyName, Double percentage, String postExplanation, int order, boolean value) {
            super(propertyName, percentage, postExplanation, order);
            this.value = value;
        }
    }
    
    public static class StringPropertyLine extends PropertyLine {
        public String value;
        
        public StringPropertyLine(String propertyName, Double percentage, String postExplanation, int order, String value) {
            super(propertyName, percentage, postExplanation, order);
            this.value = value;
        }
    }
}
