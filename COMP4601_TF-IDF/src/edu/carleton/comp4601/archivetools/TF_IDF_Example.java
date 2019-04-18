package edu.carleton.comp4601.archivetools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.lang.Math;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import edu.carleton.comp4601.databasetools.MyMongoClient;

public class TF_IDF_Example {
     
    public static void main(String args[]) throws IOException{

    	//STORAGE SETUP
    	MyMongoClient mc = new MyMongoClient();
    	DB database = mc.getDB();
    	database.getCollection("articles").drop();
    	DBCollection articles = database.getCollection("articles");
    	
    	//DATA INPUT - Construct archive access path to "articles"
    	String archivePath = "/Users/" + System.getProperty("user.name") + "/Desktop/articles/";
    	List<Path> articlesList = Files.walk(Paths.get(archivePath)).filter(Files::isRegularFile).collect(Collectors.toList());
    	ArrayList<String> stopwords = getStopWords();
    	
    	/*STORE AND PARSE Article DATA -> MONGO DB */ int idcount = 0;
    	for (Path article : articlesList) {
    		
    		//Read in text
			Scanner s = new Scanner(new File(article.toString()));
			ArrayList<String> fileTxt = new ArrayList<String>();
			while(s.hasNext()) {
				String word = s.next();
				if(!(stopwords.contains(word))) {
					fileTxt.add(word.replaceAll("[^a-zA-Z ]", "").toLowerCase());
				}
			}
			s.close();	
			
			//Clean text
			String plainTxt = fileTxt.toString();
			ArrayList<String> cleanTxt = fileTxt;	
			for(int i = 0; i < cleanTxt.size(); i++) {
				if(stopwords.contains((cleanTxt.get(i)))) {
					cleanTxt.set(i, "");
				}
			}
			
			//Term Frequency
			HashMap<String, Integer> termFrequency = new HashMap<>();
			
			for(int i = 0; i < cleanTxt.size(); i++) {
				
				if(!(termFrequency.containsKey(cleanTxt.get(i)))) {
					termFrequency.put(cleanTxt.get(i), 1);
				}
				else {
					int currVal = termFrequency.get(cleanTxt.get(i));
					termFrequency.put(cleanTxt.get(i), currVal + 1);
				}
				
			}
			
			for(int i = 0; i < cleanTxt.size(); i++) {
				
				if(!(termFrequency.containsKey(cleanTxt.get(i)))) {
					termFrequency.put(cleanTxt.get(i), 1);
				}
				else {
					int currVal = termFrequency.get(cleanTxt.get(i));
					termFrequency.put(cleanTxt.get(i), currVal + 1);
				}
				
			}

			idcount++;
						
			System.out.println("Document ID: " + idcount);
			System.out.println("Stopwords List:   " + stopwords.toString());
			System.out.println("Word List Print:  " + plainTxt);
			System.out.println("Clean List Print: " + cleanTxt);
			System.out.println("Term Frequency Print: " + termFrequency);
			System.out.println("\n");

    		//DATABASE INSERTIONS - newDataField.put new document information
			BasicDBObject newDataField = new BasicDBObject();
			newDataField.put("DOC_ID", idcount);
			newDataField.put("WordList", plainTxt);
			newDataField.put("CleanWords", cleanTxt);
			newDataField.put("TermFrequency", termFrequency);
			articles.insert(newDataField);	
    	}
    	
    	for(DBObject article: articles.find()) {

    		String[] wordsToCheck = article.get("CleanWords").toString().split(" ");
    		String[] cleanTxtSet = article.get("CleanWords").toString().split(" ");
    		HashMap<String, Integer> termDocFrequency = new HashMap<>();
    		ArrayList<String> cleanTxt = new ArrayList<>();
    		
    		for(String word : cleanTxtSet) {
    			cleanTxt.add(word);
    		}
    		
    		//Term Frequency
			HashMap<String, Integer> termFrequency = new HashMap<>();
			
			for(int i = 0; i < cleanTxt.size(); i++) {
				
				if(!(termFrequency.containsKey(cleanTxt.get(i)))) {
					termFrequency.put(cleanTxt.get(i), 1);
				}
				else {
					int currVal = termFrequency.get(cleanTxt.get(i));
					termFrequency.put(cleanTxt.get(i), currVal + 1);
				}
				
			}
			
			for(int i = 0; i < cleanTxt.size(); i++) {
				
				if(!(termFrequency.containsKey(cleanTxt.get(i)))) {
					termFrequency.put(cleanTxt.get(i), 1);
				}
				else {
					int currVal = termFrequency.get(cleanTxt.get(i));
					termFrequency.put(cleanTxt.get(i), currVal + 1);
				}
				
			}
	
    		for(String wordToCheck : wordsToCheck) {
    			
    			//Hasnt been found in any documents yet
    			int timesFound =  0;
    			
    			//check all documents for the word
    			for(DBObject articlesToCheck : articles.find()) {
    	    		
    				
    	    		String[] otherArticleWords = articlesToCheck.get("CleanWords").toString().split(" ");
    	    		
    	    		for(String currWord : otherArticleWords) {
    	    			if (currWord.equals(wordToCheck)) {
    	    				timesFound++;
    	    				break; //Found once is enough!
    	    			}
    	    		}
    	    	}
    			termDocFrequency.put(wordToCheck,timesFound);
    		}
    		
    		HashMap<String, Double> termFrequencyInverseDocFrequency = new HashMap<String, Double>();
    		
    		for(String term : termFrequency.keySet()) {
    			
    			Double tfidf = 0.0;
    			int tf = termFrequency.get(term);
    			int df = termDocFrequency.get(term);
    			int totalDocs = 5;
    			
    			//tf * (totaldocs / termDocFrequency
    			tfidf = Math.log((double) (tf*(totalDocs/df)));
    			
    			termFrequencyInverseDocFrequency.put(term, tfidf);
    			
    		}
    		
    		
			System.out.println("Document Frequency Print: " + termDocFrequency); 
			System.out.println("Current Term Frequency Print: " + termFrequency);
			System.out.println("TF-IDF: " + termFrequencyInverseDocFrequency);
			System.out.println("\n");
			
			BasicDBObject newDataField = new BasicDBObject();			
			newDataField.put("DOC_ID", article.get("DOC_ID"));
			newDataField.put("CleanWords", article.get("CleanWords"));
			newDataField.put("TermFrequency", article.get("TermFrequency"));
			newDataField.put("TermDocumentFrequency", termDocFrequency);
			newDataField.put("TF-IDF-Score", termFrequencyInverseDocFrequency);
			articles.update(article, newDataField);	
    	}
	
	}
		

    
    public static int getWordDocumentFrequencyInCorpus(String word) {
    	int count = 0;
    	//STORAGE SETUP
    	MyMongoClient mc = new MyMongoClient();
    	DB database = mc.getDB();
    	DBCollection articles = database.getCollection("articles");
    	
    	for(DBObject article : articles.find()) {
    		
    		String[] wordsToCheck = article.get("CleanWords").toString().split(" ");
    		for(String currWord : wordsToCheck) {
    			if (currWord.equals(word)) {
    				count++;
    				break;
    			}
    		}
    	}
    	
    	return count;
    }

    
    public static ArrayList<String> getStopWords(){
    	
    	//Declare list to be filled with stopwords vocabulary
		ArrayList<String> stopwords = new ArrayList<String>();
		BufferedReader br;
		String line;

		//Try to load stopwords from project file system
		try {
			br = new BufferedReader(new FileReader("./src/edu/carleton/comp4601/archivetools/stop.txt"));
			while((line = br.readLine()) != null) {
				for(String word: line.split(" "))
					stopwords.add(word);
			}
		//If stopwords cannot be loaded from project file system
		} catch (FileNotFoundException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();}
    			
		return stopwords;
    }
    
}

