package kMeansClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import general.Read;
import general.Tools;

public class Kmeans {

	Random rand = new Random();
	List<String> fields;
	List<Point> points;
	List<Centroid> centroids;
	int k;
	double score;
	static String returnField = "Forward3m";
	static String period = "_all";
	public Kmeans(int k, List<String> fields, List<Point> data){
		this.fields = fields;
		points = new ArrayList<Point>();
		for(Point datum: data){
			datum.top = this;
			points.add(datum);
		}
		centroids = new ArrayList<Centroid>();
		this.k= k;
	}
	public static void main(String[] args) throws IOException {
		List<String> fields = Arrays.asList("EV/EBITDA", "P/Book", "Analyst Rating", "EBIT Growth", "Fin Lvg", "ROIC", "Interest Coverage", "Div Yield", "Market Cap", "Rev Growth", "Current Ratio");
		
		for (int i = 0; i<fields.size(); i++)
			fields.set(i,fields.get(i).concat(Kmeans.period));
		
		List<Point> data = Read.readDataToPoints(new File("normedData.csv"));
		System.out.println("done reading");
		
		Kmeans best = batchRun(10, 20, 3, fields, data);
		
		System.out.printf("Score: %.3f\n", best.score);
		for(Centroid c: best.centroids)
			System.out.printf("%7.3f%% %s\n",c.average(Kmeans.returnField)*100,c.export() );
		
		best.printData(new File("clusteredData.csv"));
	}
	public static Kmeans batchRun(int k, int limit, int amount, List<String> fields, List<Point> data){
		Kmeans best=null;
		for (int i = 0; i<amount; i++){
			Kmeans foo = new Kmeans(k, fields, data);
			
			foo.run(limit);
			System.out.printf("Run %d, score: %.3f\n", i+1, foo.score);
			if(best==null || best.score >foo.score)
				best = foo;
		}
		return best;
	}
	public void run(int limit){
		generateCentroids();

		for(int i = 0; i<limit; i++){
			if(i!= 0)
				recomputeCentroids();
			int changes = assignPoints();
			evaluate();
			//System.out.printf("Run %d had %d changes, scoring %.4f\n", i, changes, score);
			if(changes==0)
				break;
		}
	}
	private int assignPoints(){
		int changeCount = 0;
		for(Point p: points){
			double closestDist=-1;
			Centroid closestCent = null;
			for(Centroid c: centroids){
				double dist = distance(p,c);
				if(closestDist<0 || dist<closestDist){
					closestDist = dist;
					closestCent = c;
				}
			}
			if(closestCent.addPoint(p))
				changeCount++;
		}
		return changeCount;
	}
	private void recomputeCentroids(){
		for(Centroid c: centroids){
			for(String field: fields){
				double sum = 0;
				int count = 0;
				for(Point p: c.elements){
					sum+= p.getValue(field);
					count++;
				}
				c.vals.put(field, sum / (double) count);
			}
			c.elements = new ArrayList<Point>();
		}
	}
	private void evaluate(){
		double foo = 0;
		int count = 0;
		
		for(Centroid c: centroids){
			for(Point p: c.elements){
				foo+= Math.pow(distance(p,c),2);
				count++;
			}
		}
		score = foo/(double) count;
	}
	private void generateCentroids(){
		for(int i = 0; i<k; i++){
			Point foo = points.get(rand.nextInt(points.size()));
			centroids.add(new Centroid(Tools.copyMap(foo.data),i));
		}
	}
	public class Centroid{
		Map<String, Double> vals;
		int id;
		List<Point> elements;
		
		private Centroid(Map<String, Double> values, int id){
			this.vals = values;
			this.id = id;
			this.elements = new ArrayList<Point>();
		}
		public double getVal(String s){
			return (double)vals.get(s);
		}
		public String export(){
			String s = String.format("#%d \tsize: %4d\t", id, elements.size());
			for(String f: fields)
				s = String.format("%s%s: %6.3f ", s, f.replaceFirst(Kmeans.period,""), getVal(f));
			return s;
		}
		private boolean addPoint(Point p){
			elements.add(p);
			
			boolean change = false;
			if(p.center != this)
				change = true;
			p.center = this;
			
			return change;
		}
		private double average(String s){
			double foo = 0;
			int count = 0;
			for(Point p: elements){
				foo+=p.getValue(s);
				count++;
			}
			return foo / (double) count;
		}
	}
	private double distance(Point p, Centroid c){
		double foo= 0;
		for(String field: fields)
			foo += Math.pow(p.getValue(field) - c.getVal(field), 2.0);
		
		return Math.sqrt(foo);
	}
	public void printData(File f){
		try{
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(f));
			fileWriter.write("Security,Date,Group,Return," + String.join(",", fields) + "\n");
			for(Centroid c: centroids)
				for(Point p : c.elements){
					String s = String.format("%s,%s,%d,%f,%s",p.name,p.date.toString(), c.id, p.getValue(returnField),p.export(fields));
					fileWriter.write(s + "\n");
				}
			fileWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}