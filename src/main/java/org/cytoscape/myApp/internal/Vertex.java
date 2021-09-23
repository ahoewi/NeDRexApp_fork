package org.cytoscape.myApp.internal;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class Vertex {
	
	// SUID is not unique in each loading of the same network in cytoscape
	private String name;
	//private Long suid;
	
	public Vertex (String name) {
		this.name = name;
		//this.suid = suid;
	}
	
	
	@Override
    public String toString()
    {
        return this.name;
    }
	
	@Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(!(o instanceof Vertex)) return false;

        Vertex n = (Vertex) o;
        return name.equals(n.toString());
    }
	
	@Override
	public int hashCode()
    {
        return toString().hashCode();
    }
	
/*	public Long getSuid()
    {
        return this.suid;
    }*/

}
