package ca.kanoa.slashlock;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SlashLock extends JavaPlugin {
	
	private EventListener eventListener;
	private CommandExecutor commandExecutor;
	
	private static SlashLock instance;
	
	@Override
	public void onEnable() {
		instance = this;
		eventListener = new EventListener();
		commandExecutor = new CommandExecutor();
		
		Bukkit.getPluginManager().registerEvents(eventListener, this);
		getCommand("lock").setExecutor(commandExecutor);
	}
	
	public static SlashLock getInstance() {
		return instance;
	}
	
}
