package com.deigan.stellar;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectId;

public class FileCleaner {

    private S3Object object;
    private String tempPath;
    private File tempFile;
    private static String newTempFile = "./src/main/resources/newtemp.log";

    public FileCleaner(S3Object object, String tempPath){
        this.tempPath = tempPath;
        this.object = object;

        this.tempFile = new File(tempPath);
    }

    public String cleanFile() throws IOException {
        //pull the data from s3
        pullDataFromObject();

        //perform the data cleanup
        List<String> goodData = dataCleanse();

        //write to our temp file
        FileWriter writer = new FileWriter(newTempFile);
        for(String line : goodData) {
            writer.write(line + System.lineSeparator());
        }
        writer.close();

        return newTempFile;
    }

    /*
     *  pullFromS3
     *      takes an S3Object and converts into an inputstream
     *      for ease of manipulation
     */
    private void pullDataFromObject() throws IOException {
        
        InputStream objectData = object.getObjectContent();
        try {
            Files.copy(objectData, tempFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            objectData.close();
        }
    }

    /*
     *  dataCleanse
     *      cycles through the data in the given file and 
     *      cleans up the identifying information
     */
    private List<String> dataCleanse() throws IOException {
        List<String> fileLines = Files.readAllLines(tempFile.toPath());
        List<String> newLines = new ArrayList<>();

        //cycle through all lines in the data file
        for(int i = 0; i < fileLines.size(); i++){
            //some bad data containing ip's at the start -- filtering based on [ at the beginning of the line
            if(!fileLines.get(i).isEmpty() && fileLines.get(i).charAt(0) == '[') {
                String[] currentLine = fileLines.get(i).split(" "); 
                if(currentLine.length >= 7) {
                    currentLine[6] = removeDOB(currentLine[6]);
                }
                newLines.add(String.join(" ",currentLine));
            }
            else {
                // no need to cleanse dates in HTTP lines
                // currently we are not discriminating BAD data, just obfuscating identifying info
                newLines.add(fileLines.get(i));
            }
        }

        return newLines;
    }

    /*
     *  removeDOB
     *      this function takes a date of birth string input
     *      removes the month and date, and returns in the same format
     */
    private String removeDOB(String dob){
        //need to take numerous data types into consideration here:
        // DOB='July 4th 1972'
        // DOB='12-24-1970'
        // DOB='7/26/1925'
        String obfuscated = new String();
        String[] tmp = new String[]{};

        if(dob.contains(" ")){
            //format is DOB='July 4th 1972'
            tmp = dob.split(" ");
            obfuscated = "DOB='X X " + tmp[2];

        } else if(dob.contains("/")) {
            //format is DOB='7/26/1925'
            tmp = dob.split("/");
            obfuscated = "DOB='X/X/" + tmp[2];

        } else if(dob.contains("-")) {
            //format is DOB='12-24-1970'
            tmp = dob.split("-");
            obfuscated = "DOB='X-X-" + tmp[2];
        }

        return obfuscated;
    }
}
