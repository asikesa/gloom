package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.DiscordPresence;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * @author S-B99
 */
@Module.Info(name = "DiscordRPC", category = Module.Category.MISC, description = "Discord Rich Presence")
public class BlueDiscordRPC extends Module {

    public Setting<Boolean> startupGlobal = register(Settings.b("Enable Automatically", true));
    public Setting<Boolean> versionGlobal = register(Settings.b("Version", false));
    public Setting<Boolean> usernameGlobal = register(Settings.b("Username", false));
    public Setting<Boolean> hpGlobal = register(Settings.b("Health", false));
    public Setting<Boolean> ipGlobal = register(Settings.b("Server IP", false));

    @Override
    public void onEnable() {
        DiscordPresence.start();
    }

    public void onDisable() { DiscordPresence.disable(); }
}
