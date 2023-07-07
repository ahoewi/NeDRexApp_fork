package org.cytoscape.nedrex.internal.exceptions;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class NodeTypeException extends Exception{
	
	private String errorMessage = "";

	public NodeTypeException() {};
	public NodeTypeException(String errorMessage) {
		super(errorMessage);
	}
	
	@Override
	public String getMessage() {
		return this.errorMessage;
	}

}
