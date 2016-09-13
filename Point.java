package kMeansClustering;

import java.util.Map;
import general.Datum;
import general.Date;
import general.Tools;
import kMeansClustering.Kmeans.*;

public class Point extends Datum{
	Centroid center;
	Kmeans top;

	public Point(Map<String, Double> values, String name, Date date){
		super(name, date, Tools.copyMap(values));
	}
}
