// MIT License
// Copyright (c) Microsoft Corporation. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE

package blobQuickstart.blobAzureApp;


import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Month;


/* *************************************************************************************************************************
* Summary: This application demonstrates how to use the Blob Storage service.
* It does so by creating a container, creating a file, then uploading that file, listing all files in a container, 
* and downloading the file. Then it deletes all the resources it created
* 
* Documentation References:
* https://azure.github.io/azure-sdk-for-java/
* Associated Article - https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java
* What is a Storage Account - http://azure.microsoft.com/en-us/documentation/articles/storage-whatis-account/
* Getting Started with Blobs - http://azure.microsoft.com/en-us/documentation/articles/storage-dotnet-how-to-use-blobs/
* Blob Service Concepts - http://msdn.microsoft.com/en-us/library/dd179376.aspx 
* Blob Service REST API - http://msdn.microsoft.com/en-us/library/dd135733.aspx
* 
java.lang.Object
	com.microsoft.azure.storage.blob.CloudBlob -------> public abstract class CloudBlob extends Object implements ListBlobItem (Represents a Microsoft Azure blob.)
		com.microsoft.azure.storage.blob.CloudBlockBlob --> public final class CloudBlockBlob extends CloudBlob (Represents a blob that is uploaded as a set of blocks.)
		com.microsoft.azure.storage.blob.CloudPageBlob ---> public final class CloudPageBlob extends CloudBlob (Represents a Microsoft Azure page blob.)
* *************************************************************************************************************************
*/
public class AzureStorageApp 
{
	/* *************************************************************************************************************************
	* Instructions: Update the storageConnectionString variable with your AccountName and Key and then run the sample.
	* *************************************************************************************************************************
	*/
	public static final String storageConnectionString =
	"DefaultEndpointsProtocol=https;" +
	"AccountName=<your_account_name_here>;" +
	"AccountKey=<your_storageaccount_key_here>;";


	public static void main( String[] args )
	{
		File sourceFile1 =null;
		File sourceFile2 =null;
		File downloadedFile = null;
		
		LocalDateTime dateTime = LocalDateTime.now();
		int year = dateTime.getYear();
		Month month = dateTime.getMonth();
		int day = dateTime.getDayOfMonth();
		int hour = dateTime.getHour();
		int minute = dateTime.getMinute();
		
		CloudStorageAccount storageAccount;
		CloudBlobClient blobClient = null;
		CloudBlobContainer container = null;
		CloudBlobDirectory dir = null;
		
		try {    
			// Parse the connection string and create a Storage Account Object
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
           
			// Create a Blob service client to interact with Blob storage
			blobClient = storageAccount.createCloudBlobClient();
			
			// Create a local container object. It does not make a network call to the Azure Storage Service. The local container reference  
			// that this object represents may or may not exist in the Storage Service at this point. If it does exist, the properties will not yet 
			// have been populated on this object.
			
			container = blobClient.getContainerReference("quickstartcontainer");

			// Create the container if it does not exist with public access.
			System.out.println("Creating container: " + container.getName());
			
			/* First param defines Container-level public access. Anonymous clients can read container and blob data. For more details
			 * https://docs.microsoft.com/en-us/dotnet/api/microsoft.windowsazure.storage.blob.blobcontainerpublicaccesstype?view=azure-dotnet
			  
			 * The second param is used to define a set of timeout and retry policy options that may be specified for a request against the Blob service. More details at 
			 * https://docs.microsoft.com/en-us/dotnet/api/microsoft.windowsazure.storage.blob.blobcontainerpublicaccesstype?view=azure-dotnet
			 
			 * The third parameter represents the context for a request operation against the storage service, and provides additional runtime information about its execution.
			 */
			
			container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());		
			dir = container.getDirectoryReference(year+"/"+month+"/"+day+"/"+hour+"/"+minute);
			
			//Create an empty temporary sample file and write to it.
			
			sourceFile1 = File.createTempFile("sampleFileA", ".txt");
			sourceFile2= File.createTempFile("sampleFileB", ".txt");
			
			System.out.println("Creating sample file1 at: " + sourceFile1.toString());
			Writer output1 = new BufferedWriter(new FileWriter(sourceFile1));
			output1.write("Hello Azure!");
			output1.close();

			System.out.println("Creating sample file2 at: " + sourceFile2.toString());
			Writer output2 = new BufferedWriter(new FileWriter(sourceFile2));
			output2.write("Hello Azure Again!");
			output2.close();

			//Get a Block Blob reference in this container. This also does not make a service call, it only creates a local object. 
			// For Example, If we try to acquire a lease on this blob here it will throw the Error - Http code: 404 and error code: BlobNotFound
     		//	CloudBlockBlob blob = container.getBlockBlobReference(sourceFile1.getName());

	        // Similar to creating a Block blob reference from a Container reference, we can create a Block Blob reference from a Directory Reference.
			CloudBlockBlob blob = dir.getBlockBlobReference(sourceFile1.getName());

			System.out.println("Uploading the sample file \"SampleFilieA\"");
			// Upload a file to the Blob service. If the blob already exists, it will be overwritten.
			blob.uploadFromFile(sourceFile1.getAbsolutePath());
			
     		System.out.println("\n\tOverwrite the blob by uploading the second sample file \"SampleFileB\".");
	        
	        blob.uploadFromFile(sourceFile2.getAbsolutePath());
	        
	        System.out.println("\t\tSuccessfully overwrote the blob.");
	        
			//Listing contents of container. Interface ListBlobItem represents an item that may be returned by a blob listing operation.
			for (ListBlobItem blobItem : container.listBlobs()) {
				if (blobItem instanceof CloudBlob) {
                    System.out.println(String.format("\t\t%s\t: %s", ((CloudBlob) blobItem).getProperties().getBlobType(), blobItem.getUri().toString()));
					}
			}
		// Download blob. In most cases, you would have to retrieve the reference to cloudBlockBlob here. However, we created that reference earlier, and 
		// haven't changed the blob we're interested in, so we can reuse it. Here we are creating a new file to download to. Alternatively you can also pass in the path as a string into downloadToFile method: blob.downloadToFile("/path/to/new/file").
		// sourceFile.getParentFile() gives the absolute folder path where the sourceFile is (Ex - C:\Users\PRASAR~1\AppData\Local\Temp\6).
			
		 downloadedFile = new File(sourceFile1.getParentFile(), "downloadedFile.txt");
		
		// The downloadToFile() takes a String argument which represents the path to the file that will be created with the contents of the blob. (Ex :- C:\Users\PRASAR~1\AppData\Local\Temp\6\downloadedFile.txt)
		//blob.downloadToFile(downloadedFile.getAbsolutePath());
		
		} 
		catch (StorageException ex)
		{
			System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
		}
		catch (Exception ex) 
		{
			System.out.println(ex.getMessage());
		}
		finally 
		{
			System.out.println("The program has completed successfully.");
			System.out.println("Press the 'Enter' key while in the console to delete the sample files, example container, and exit the application.");

		//	Pausing for input. Using a simple text scanner which can parse primitive types and strings using regular expressions.
			Scanner sc = new Scanner(System.in);
			sc.nextLine();

		    System.out.println("Deleting the container");
			try {
				if(container != null)
					container.deleteIfExists();
		} 
			catch (StorageException ex) {
				System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
			}

 	System.out.println("Deleting the source, and downloaded files");

			if(downloadedFile != null)
			// Requests that the file or directory denoted by this abstract pathname be deleted when the virtual machine terminates. 
			// Once deletion has been requested, it is not possible to cancel the request. This method should therefore be used with care.
				downloadedFile.deleteOnExit();

            if(sourceFile1 != null)
				sourceFile1.deleteOnExit();
            
            if(sourceFile2 != null)
				sourceFile2.deleteOnExit();
            
			//Closing scanner
	          sc.close();
		}
	}
}