package org.cytoscape.myApp.internal;

import org.jgrapht.graph.DefaultWeightedEdge;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class Link extends DefaultWeightedEdge{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7624431279214855512L;
	//private String name;
	private final Vertex source, target;
	private Long suid;
	private Double weight;
	
	public Link (Vertex source, Vertex target, Long suid, Double weight) {
		super();
		//this.name = name;
		this.source = source;
        this.target = target;
		this.suid = suid;
		this.weight = weight;
	}
	
	
	public Long getSuid() {
		return this.suid;
	}
	
	public Vertex getSource() {
		//return (Vertex)super.getSource();
		return source;
	}
	
	public Vertex getTarget() {
		//return (Vertex)super.getTarget();
		return target;
	}
	
	public double getWeight() {
		return super.getWeight();
		//return weight;
	}
	
	/*@Override
    public String toString()
    {
        return "(" + getSource().toString() + ":" + getTarget().toString() + "_weight=" + weight+")";
    }*/
	
	@Override
    public String toString()
    {
        return "(" + getSource().toString() + ":" + getTarget().toString() + "_weight=" + super.getWeight()+"_SUID:"+ getSuid() +")";
    }
	
	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Link link = (Link) obj;
        //return this.suid == link.getSuid();
        return suid.equals(link.getSuid());
    }
	
	// not sure if it should be overridden for edge, but it should be overridden for node certainly
/*	@Override
    public int hashCode() {
		return suid.hashCode();
		//return Objects.hash(suid);
    }*/

	/*@Override
	public int compareTo(Link link) {
		//compare id
		//Long.toString(this.suid);
		//Long.toString(link.getSuid());
		
		//return Long.toString(this.suid).compareTo(Long.toString(link.getSuid()));
		return this.suid.compareTo(link.getSuid());
       // return this.id.compareTo(link.getId());
	}*/


}
