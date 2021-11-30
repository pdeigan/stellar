package com.deigan.stellar;

import java.util.List;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.*;

public class AWSConnectionManager {
    
    // for the record I would never store credentials in plaintext like this 
    // These would typically be set as env variables on my machine but 
    // for the sake of an interview, this should do the trick
    private AWSCredentials credentials;
    public AmazonS3 s3Client;

    /*
     * Constructor creates our s3 client for future use 
     */
    public AWSConnectionManager () {
        this.credentials = new ProfileCredentialsProvider().getCredentials();

        this.s3Client = AmazonS3ClientBuilder
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.US_EAST_1)
        .build();
    }

    public List<Bucket> listBuckets(){
        return s3Client.listBuckets();
    }

}
