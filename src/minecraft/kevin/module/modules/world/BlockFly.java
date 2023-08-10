package kevin.module.modules.world;

import kevin.module.Module;
import kevin.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

class BlockFly extends Module {
  public BlockFly() {
      super("BlockFly", "Auto place blocks under your feet.", Keyboard.KEY_NONE, ModuleCategory.WORLD);
  }
}
