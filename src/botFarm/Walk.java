package botFarm;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.Condition;

public class Walk extends Node {

	private Area area;
	private Position pos;

	public Walk(Script s, Environment e, Area area) {
		super(s, e);
		this.area = area;
	}

	public Walk(Script s, Environment e, Position pos) {
		super(s, e);
		this.pos = pos;
	}

	@Override
	public boolean validate() throws InterruptedException {
		if(area != null){
			return !area.contains(s.myPlayer());
		} else {
			return pos.distance(s.myPosition()) > 4;
		}
	}

	@Override
	public boolean execute() throws InterruptedException {
		WebWalkEvent event;
		if (area != null) {
			event = new WebWalkEvent(area);
		} else {
			event = new WebWalkEvent(pos);
			event.setBreakCondition(new Condition() {
				@Override
				public boolean evaluate() {
					return pos.distance(s.myPosition()) < 4;
				}
			});
		}
		s.execute(event);
		return true;
	}

}
