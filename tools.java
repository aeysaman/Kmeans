package kMeansClustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class tools {

	public static List<Map<String, Double>> readNormedData(File f) throws IOException{
 		List<Map<String, Double>> result = new ArrayList<Map<String, Double>>();
 		BufferedReader read = new BufferedReader(new FileReader(f));
 		List<String> topLine = readTopLine(read.readLine());
 		String line;
 		while((line =read.readLine())!=null){
 			Map<String, Double> foo = new HashMap<String, Double>();
 			String[] split = line.split(",");
 			boolean containsNull = false;
 			for(int i = 3; i<split.length; i++){
 				if(split[i].equals("null")){
 					containsNull = true;
 					break;
 				}
 				else
 					foo.put(topLine.get(i), Double.parseDouble(split[i]));
 			}
 			if(!containsNull)
 				result.add(foo);
 		}
 		read.close();
 		return result;
	}
	public static List<String> readTopLine(String s){
		String[] strArray = s.split(",");
		List<String> result = new ArrayList<String>();
		for(int i = 0; i<strArray.length;i++)
			result.add(strArray[i]);
		return result;
	}
}
