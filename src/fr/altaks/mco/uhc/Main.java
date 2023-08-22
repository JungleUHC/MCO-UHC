package fr.altaks.mco.uhc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import fr.altaks.mco.uhc.commands.debug.CheckItemsCommand;
import fr.altaks.mco.uhc.commands.debug.DisplayRolesCommand;
import fr.altaks.mco.uhc.commands.debug.DisplayTeamsCommand;
import fr.altaks.mco.uhc.commands.debug.GameStatusCommand;
import fr.altaks.mco.uhc.commands.debug.StartGameCommand;
import fr.altaks.mco.uhc.commands.debug.TestCommand;
import fr.altaks.mco.uhc.commands.game.MCOCommand;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.ArtifactListener;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Main extends JavaPlugin implements Listener {
	
	public static final String PREFIX = "§7[§bMCO UHC§7] \u00BB §r",
								DEBUG = "§7[§4MCO Dbg§7] \u00BB §c";
	
	private static final Logger LOGGER = Bukkit.getLogger();
	public static boolean debugMode = false;
	private WorldEditPlugin worldEditInstance;
	
	private GameManager game;
	
	@Override
	public void onEnable() {
		
		saveDefaultConfig();
		if(getConfig().isSet("debug-mode")) debugMode = getConfig().getBoolean("debug-mode");
		
		getCommand("checkitems").setExecutor(new CheckItemsCommand());
		getCommand("test").setExecutor(new TestCommand());
		getCommand("gamestatus").setExecutor(new GameStatusCommand(this));
		
		getCommand("start").setExecutor(new StartGameCommand(this));
		getCommand("displayroles").setExecutor(new DisplayRolesCommand(this));
		getCommand("displayteams").setExecutor(new DisplayTeamsCommand(this));
		
		getCommand("mco").setExecutor(new MCOCommand(this));
		
		for(RoleType role : RoleType.values()) {
			if(getConfig().isSet("roles-default-amount." + role.getRoleId())) role.setRoleAmountOfPlayers(getConfig().getInt("roles-default-amount." + role.getRoleId()));
		}
		
		game = new GameManager(this);
		Listener[] listeners = {
			game, this, new ArtifactListener(this)
		};
		for(Listener listener : listeners) Bukkit.getPluginManager().registerEvents(listener, this);
		
		this.worldEditInstance = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	}
	
	@EventHandler
	public void onPlayerDropsSensitiveItem(PlayerDropItemEvent event) {
		if(event.getItemDrop().getItemStack().hasItemMeta() && event.getItemDrop().getItemStack().getItemMeta().hasDisplayName() && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().startsWith("§")) {
			event.getPlayer().sendMessage(Main.PREFIX + "§cVous ne souhaitez pas perdre vos précieux objets, je me trompe ?");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDropsItemsOnDeath(PlayerDeathEvent event) {
		List<ItemStack> itemsToRemove = new ArrayList<>();
		if(event.getDrops() == null || event.getDrops().size() == 0) return;
		for(ItemStack item : event.getDrops()) {
			if(item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().startsWith("§")) {
				itemsToRemove.add(item);
			}
		}
		for(ItemStack item : itemsToRemove) event.getDrops().remove(item);
	}
	
	public static void logIfDebug(String message) {
		if(Main.debugMode) LOGGER.warning(DEBUG + message);
	}
	
	public static void log(String message) {
		LOGGER.info(PREFIX + message);
	}
	
	public GameManager getCurrentGameManager() {
		return this.game;
	}

	public WorldEditPlugin getWorldEditInstance() {
		return worldEditInstance;
	}

}
