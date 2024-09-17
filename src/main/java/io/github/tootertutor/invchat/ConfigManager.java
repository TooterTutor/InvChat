package io.github.tootertutor.invchat;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Config(name = "InvChat")
@Config.Gui.Background("minecraft:textures/block/gray_stained_glass.png")
@Environment(EnvType.CLIENT)
public class ConfigManager implements ConfigData {
    public boolean enabled = true;
    public boolean anchorBelowInventory = false;
    public float xOffset = 0;
    public float yOffset = 0;
}
