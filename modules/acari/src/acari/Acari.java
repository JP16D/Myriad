package acari;

import acari.ui.*;
import arc.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;

import static acari.tool.Debugger.*;

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

	class AcariTest {
		
		@Debug("(view)test-v")
		@Debug("(write)test-w")
		public static Vec2 test = new Vec2();
		//
		@Guarded
		@Debug("(writable):5")
		public static Table table;
		
		public static void load() {
			debug("test-vx", test.x);
			debug("test-vy", test.y);
			//
			test.x = debug("(w)test-wx", test.x);
			test.y = debug("(w)test-wy", test.y);
			//
			register(AcariTest.class);
			//
			for (var f : Icon.class.getFields()) try {
				if (f.get(new Icon()) instanceof Drawable d)
				debug("icon-" + f.getName(), d);
			} catch (Exception e) {}
		}
	}
}
