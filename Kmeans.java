package kMeansClustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Kmeans {

	Random rand = new Random();
	List<String> fields;
	List<Point> points;
	List<Centroid> centroids;
	int k;
	double score;
	public Kmeans(int k, List<String> fields, List<Map<String, Double>> data){
		this.fields = fields;
		points = new ArrayList<Point>();
		for(Map<String,Double> datum: data)
			points.add(new Point(datum));
		centroids = new ArrayList<Centroid>();
		this.k= k;
	}
	public static void main(String[] args) {
		List<String> fields = Arrays.asList("x", "y", "z");

		List<Map<String, Double>> data = generatePoints();
		
		Kmeans best = batchRun(4, 20, 5, fields, data);
		
		System.out.printf("Score: %.3f\n", best.score);
		for(Centroid c: best.centroids)
			System.out.println(c.export());
	}
	public static Kmeans batchRun(int k, int limit, int amount, List<String> fields, List<Map<String, Double>> data){
		
		Kmeans best=null;
		
		for (int i = 0; i<amount; i++){
			Kmeans foo = new Kmeans(k, fields, data);
			
			foo.run(limit);
			System.out.printf("Run %d, score: %.3f\n", i, foo.score);
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
					sum+= p.getVal(field);
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
			Point copy = points.get(rand.nextInt(points.size()));

			Map<String, Double> x = copyMap(copy.vals);
			
			centroids.add(new Centroid(x,i));
		}
	}
 	public static List<Map<String, Double>> generatePoints(){
		int amt = 10000;
 		List<Map<String, Double>> result = new ArrayList<Map<String, Double>>();
		for(int i = 0; i<amt; i++){
			Map<String, Double> x = new HashMap<String, Double>();
			x.put("x", .5 + Math.random());
			x.put("y", .5 + Math.random());
			x.put("z", -.5 + Math.random());
			result.add(x);
		}
		for(int i = 0; i<amt; i++){
			Map<String, Double> x = new HashMap<String, Double>();
			x.put("x", .5 + Math.random());
			x.put("y", -.5 + Math.random());
			x.put("z", .5 + Math.random());
			result.add(x);
		}
		for(int i = 0; i<amt; i++){
			Map<String, Double> x = new HashMap<String, Double>();
			x.put("x", -.5 + Math.random());
			x.put("y", .5 + Math.random());
			x.put("z", .5 + Math.random());
			result.add(x);
		}
		for(int i = 0; i<amt; i++){
			Map<String, Double> x = new HashMap<String, Double>();
			x.put("x", -.5 + Math.random());
			x.put("y", -.5 + Math.random());
			x.put("z", -.5 + Math.random());
			result.add(x);
		}
		return result;
	}
	private class Point{
		Map<String, Double> vals;
		Centroid center;
		
		public Point(Map<String, Double> values){
			this.vals = copyMap(values);
		}
		public double getVal(String s){
			return (double)vals.get(s);
		}
		public String export(){
			String s = "";
			for(String f: fields)
				s = String.format("%s%s: %.3f ", s, f, getVal(f));
			
			return s;
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
			String s = String.format("#%d \tsize: %d\t", id, elements.size());
			for(String f: fields)
				s = String.format("%s%s: %.3f ", s, f, getVal(f));
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
	}
	private double distance(Point p1, Point p2){
		double foo= 0;
		for(String field: fields)
			foo += Math.pow(p1.getVal(field) - p2.getVal(field), 2.0);
		
		return Math.sqrt(foo);
	}
	private double distance(Point p, Centroid c){
		double foo= 0;
		for(String field: fields)
			foo += Math.pow(p.getVal(field) - c.getVal(field), 2.0);
		
		return Math.sqrt(foo);
	}
	private static Map<String, Double> copyMap(Map<String, Double> m){
		Map<String, Double> foo = new HashMap<String, Double>();
		for(String s: m.keySet())
			foo.put(s, m.get(s));
		return foo;
	}
}