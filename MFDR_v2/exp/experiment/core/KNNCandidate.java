package experiment.core;

public class KNNCandidate {

	final private int cluster_id;
	final private double dist;
	final private int count;
	
	public KNNCandidate(int cluster_id , double dist, int count){
		this.cluster_id = cluster_id;
		this.dist = dist;
		this.count = count;
	}
	
	public int id(){
		return this.cluster_id;
	}
	
	public double dist(){
		return this.dist;
	}
	
	public int count(){
		return this.count;
	}
	
	
}
