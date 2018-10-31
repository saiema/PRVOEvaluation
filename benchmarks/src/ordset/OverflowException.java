package ordset;

/*
 * Created on Oct 20, 2005
 * 
 */

public class OverflowException extends Exception {
	
	private static final long serialVersionUID = -2517734776838847257L;

	public OverflowException() {
		super();
	}
	
	public OverflowException(String s) {
		super(s);
	}
	
	public OverflowException(String s, Throwable ex) {
		super(s, ex);
	}
}
