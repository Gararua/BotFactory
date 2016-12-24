package botFarm;

import org.osbot.rs07.script.Script;

public abstract class Node {
	public Script s;
	public Environment e;
	
	public Node(Script s, Environment e){
		this.s = s;
		this.e = e;
	}
	
	public abstract boolean validate() throws InterruptedException;
	public abstract boolean execute() throws InterruptedException;
}
