package acari;

import acari.ui.*;
import arc.*;
import mindustry.game.*;
import mindustry.mod.*;

public class Acari extends Mod {

	public Acari() {
		Events.run(EventType.Trigger.update, () -> {
			AcariTest.load();
			MainPanel.scale();
		});
	}
	
	@Override
	public void init() {
		Core.scene.add(MainPanel.load());
	}
}
