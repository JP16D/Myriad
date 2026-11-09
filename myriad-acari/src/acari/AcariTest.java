package acari;

import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;

import static acari.tool.Debugger.*;

public class AcariTest {
	
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