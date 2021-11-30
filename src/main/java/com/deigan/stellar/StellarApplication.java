package com.deigan.stellar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

//import java.io.*;
//import java.util.*;

import com.amazonaws.services.s3.model.*;

public class StellarApplication {

    protected static AWSConnectionManager aws = new AWSConnectionManager();
    private static final String tempFile = "./src/main/resources/tempfile.txt";

	public static void main(String[] args) throws IOException {
		// get file from s3
        // cycle through list of objects:
        //      alter data as needed
        //      write to s3, overwriting old file
        // 

        // first, lets pull the file down to manipulate it
        String inputFile = "patients.log";
        String bucketName = "stellar.health.test.patrick.deigan";
        S3Object object = aws.s3Client.getObject(bucketName, inputFile);
        FileCleaner cleaner = new FileCleaner(object, tempFile);
        
        File file = new File(tempFile);
        if(file.exists()){
            file.delete();
        }

        //perform the cleaning
        String newFile = cleaner.cleanFile();
        File newData = new File(newFile);

        //push to our bucket
        aws.s3Client.putObject(bucketName, inputFile, newData);

        //remove our temp file
        newData.delete();
	}


}
