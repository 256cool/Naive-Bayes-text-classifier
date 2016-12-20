# Naive-Bayes-text-classifier
Bag of words approach is used to represent the document...

enum ClassSet{
	Electronics, Medical, Politics, Religion, Sports
} 
The "enum ClassSet" in source code is just given for readers to understand that the document categories can be of these types. This enum type is not used in code instead a switch-case is used to convert int to string which maps number with document class name.

In main method create an arraylist as below
   < List\<String> pathList = new ArrayList\<String>();
		pathList.add("__Path__/20news-bydate-train/Electronics");
		pathList.add("__Path__/20news-bydate-train/Medical");
		pathList.add("__Path__/20news-bydate-train/Politics");
		pathList.add("__Path__/20news-bydate-train/Religion");
		pathList.add("__Path__/20news-bydate-train/Sports");>
Number of elements in List decides the number of classes so for above list there are five classes and Number 0 corresponds with Electronics same way 1 <--> Medical, 2 <--> Politics, 3 <--> Religion, 4 <--> Sports

After training model is created check accuracy of your model using method "checkFortestData" 
