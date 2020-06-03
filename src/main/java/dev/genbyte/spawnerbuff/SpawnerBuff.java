package dev.genbyte.spawnerbuff;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerBuff extends JavaPlugin implements Listener {
	public boolean allowLoad, allowUnload;

	@Override
	public void onEnable() {
		loadConfig();
		this.getServer().getPluginManager().registerEvents(new MinecartHandler(this), this);
	}

	private void loadConfig() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();

		allowLoad = config.getBoolean("allowLoadMinecart");
		allowUnload = config.getBoolean("allowUnloadMinecart");
	}
}