import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;

enum ClassSet{
	Electronics, Medical, Politics, Religion, Sports
}

public class NaiveBayesTrainer{
	static int wordCountKeeper[];
	static double classPriorProb[]; 
	static int vocabularySize;
	static boolean trainingSetCreated;
	static HashMap<String,List<Integer>> wordCountMap = new HashMap<String,List<Integer>>();
	List<String> pathList;
	static HashSet<String> avoidWords = new HashSet<String>();
	static boolean avoidWordsInitialized;
	int totalClasses;
	
	NaiveBayesTrainer(List<String> pathList, int totalClasses){
		this.totalClasses = totalClasses;
		this.pathList = pathList;
		wordCountKeeper = new int[totalClasses];
		classPriorProb = new double[totalClasses];
	}
	
	int getDocumentClass(String docPath){
		if(!avoidWordsInitialized){
			DataInputReader awr = new AvoidWordReaderConcrete();
			String toAvoid[] = awr.getDataFrom("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/wordsToAvoid.txt");
			for(int i=0; i<toAvoid.length; i++)
				avoidWords.add(toAvoid[i]);
			avoidWordsInitialized = true;
		}
		if(!trainingSetCreated){	
			wordCountMap = createWordCountMap(pathList);
			trainingSetCreated = true;
			vocabularySize = wordCountMap.size();
		}
		//System.out.println(wordCountKeeper[0]+" "+wordCountKeeper[1]+" "+vocabularySize);
		return getClass(docPath);
	}
	
	int getClass(String path){
		HashMap<String,Integer> docCountMap = new HashMap<String,Integer>();
		DataInputReader d = new InputReaderConcrete();
		String words[] = d.getDataFrom(path);
		for(int j=0; j<words.length; j++){
			//System.out.println(words[j]);
			String curr = wordCleaner(words[j]);
			if(curr.length()>0 && !avoidWords.contains(curr)){
				if(!docCountMap.containsKey(curr))
					docCountMap.put(curr,1);
				else{
					int val = docCountMap.get(curr);
					docCountMap.put(curr,val+1);
				}
			}
		}
		//System.out.println("Size "+docCountMap.size());
		return calcProbabilities(totalClasses,docCountMap);
	}
	
	int calcProbabilities(int totalClasses, HashMap<String,Integer> docCountMap){
		BigDecimal max = new BigDecimal(-1.0);
		int maxIdx = -1;
		for(int i=0; i<totalClasses; i++){
			int totalWordsInClass = wordCountKeeper[i];
			int denominator = totalWordsInClass+vocabularySize;
			//System.out.println("Denom "+i+" "+denominator);
			Set<String> s = docCountMap.keySet();
			Iterator<String> itr = s.iterator();
			//double docClassProb = 1.0;
			BigDecimal bg1 = new BigDecimal(1.0);
			while(itr.hasNext()){
				String key = itr.next();
				List<Integer> tempL = wordCountMap.get(key);
				int currDocWordCount = 0;
				if(tempL==null)
					currDocWordCount=1;
				else
					currDocWordCount = tempL.get(i)+1;
				//System.out.println("class "+i+" word "+key+" count "+currDocWordCount);
				double probWord = (currDocWordCount)/(1.0 * denominator);
				//System.out.print("num "+(currDocWordCount));
				BigDecimal bg2 = new BigDecimal(probWord);
				//docClassProb *= probWord;
				bg1 = bg1.multiply(bg2);
			}
			//System.out.println();
			//System.out.println("class " + i + " " + bg1);
			//docClassProb *= classPriorProb[i];
			bg1 = bg1.multiply(new BigDecimal(classPriorProb[i]));
			if(bg1.compareTo(max) > 0){
				max = bg1;
				maxIdx	= i;	
			}
		}
		return maxIdx;
	}
	
	HashMap<String,List<Integer>> createWordCountMap(List<String> pathList){
		HashMap<String,List<Integer>> wordCountMap = new HashMap<String,List<Integer>>();
		try{
			if(pathList==null)
				throw new Exception("Path list cannot be null.");
			if(pathList.size()>totalClasses)
				throw new Exception("Number of paths can't exceed totalClasses.");
			DataInputReader fileReader = new InputReaderConcrete();
			int docCountKeeper[] = new int[totalClasses];
			for(int i=0; i<pathList.size(); i++){
				int totalFiles = new File(pathList.get(i)).list().length;
				docCountKeeper[i] = totalFiles;
				File folder = new File(pathList.get(i));
				File[] listOfFiles = folder.listFiles();
				for (File file : listOfFiles) {
				    if (file.isFile()) {
				    	String currFilePath = pathList.get(i)+"/"+file.getName();
				    	String words[] = fileReader.getDataFrom(currFilePath);
				    	processWords(words,i,wordCountMap,totalClasses);
				    }
				}
			}
			getPriorProbability(docCountKeeper);
			//printMap(wordCountMap);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return wordCountMap;
	}
	
	void processWords(String words[], int classVal, HashMap<String,List<Integer>> wordCountMap, int totalClasses){
		for(int i=0; i<words.length; i++){
			words[i] = wordCleaner(words[i]);
			if(words[i].length() > 0 && !avoidWords.contains(words[i])){
				wordCountKeeper[classVal]++;
				if(!wordCountMap.containsKey(words[i])){
					List<Integer> countListForClasses = new ArrayList<Integer>();
					for(int j=0; j<totalClasses; j++)
						countListForClasses.add(0);
					countListForClasses.set(classVal,countListForClasses.get(classVal)+1);
					wordCountMap.put(words[i],countListForClasses);
				}
				else{
					List<Integer> countListForClasses = wordCountMap.get(words[i]);
					countListForClasses.set(classVal,countListForClasses.get(classVal)+1);
				}
			}
		}
	}
	
	String wordCleaner(String word){
		word = word.toLowerCase();
		StringBuilder newWord = new StringBuilder();
		for(int i=0; i<word.length(); i++){
			if(word.charAt(i) >= 'a' && word.charAt(i) <= 'z')
				newWord.append(word.charAt(i));
		}
		return newWord.toString();
	}
	
	void getPriorProbability(int docNumberKeeper[]){
		int totalDocuments = 0;
		for(int i=0; i<totalClasses; i++){
			totalDocuments += docNumberKeeper[i];
			classPriorProb[i] = 1;
		}
		if(totalDocuments > 0){
			for(int i=0; i<totalClasses; i++){
				classPriorProb[i] =  docNumberKeeper[i]/(1.0*totalDocuments);
			}
		}
	}
	
	void printMap(HashMap<String,List<Integer>> wordCountMap){
		Set<String> s = wordCountMap.keySet();
		Iterator<String> itr = s.iterator();
		while(itr.hasNext()){
			String key = itr.next();
			List<Integer> l = wordCountMap.get(key);
			//System.out.println(key + " " + l);
		}
	}
	
	void checkFortestData(List<String> pathList){
		int correctCount = 0;
		int totalCount = 0;
		for(int i=0; i<pathList.size(); i++){
			File folder = new File(pathList.get(i));
			File[] listOfFiles = folder.listFiles();
			totalCount += listOfFiles.length;
			for (File file : listOfFiles) {
			    if (file.isFile()) {
			    	String currFilePath = pathList.get(i)+"/"+file.getName();
			    	int classPredict = getDocumentClass(currFilePath);
			    	if(classPredict == i)
			    		correctCount++;
			    }
			}
		}
		double accuracy = (correctCount/(1.0*totalCount)) * 100;
		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println("Accuracy is "+Double.valueOf(df.format(accuracy))+"%");
	}
	
	String getClassName(int classNum)
	{
		switch(classNum){
		case 0: return "Electronics";
		case 1: return "Medical";
		case 2: return "Politics";
		case 3: return "Religion";
		case 4: return "Sports";
		}
		return "Invalid class input";
	}
	
	public static void  main(String args[]){
		List<String> pathList = new ArrayList<String>();
		pathList.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-train/Electronics");
		pathList.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-train/Medical");
		pathList.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-train/Politics");
		pathList.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-train/Religion");
		pathList.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-train/Sports");
		//For test data set
		List<String> pathListTest = new ArrayList<String>();
		pathListTest.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/Electronics");
		pathListTest.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/Medical");
		pathListTest.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/Politics");
		pathListTest.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/Religion");
		pathListTest.add("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/Sports");
		System.out.println("Using training data to create the Naive Bayes' Model. Please wait for a moment...");
		NaiveBayesTrainer obj = new NaiveBayesTrainer(pathList,5);
		System.out.println("Model created!");
		System.out.println("Checking the accuracy of model using test data set. Please wait again...");
		obj.checkFortestData(pathListTest);
		//System.out.println(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dataset/20news-bydate-test/talk.politics.guns/54464"));
		System.out.println("Categorization of sample text");
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Electronics.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Medical.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Politics.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Religion.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Sports.txt")));
		System.out.println("Today's NY times news articles");
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Sports2.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Politics1.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Medical1.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Politics2.txt")));
		System.out.println(obj.getClassName(obj.getDocumentClass("C:/Users/saviour/Documents/karanStudy/MSStuff/Sem4/NLP/Project/Dummy/Religion1.txt")));
		
	}
}
