package fr.altaks.mco.uhc.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.items.Artifacts;

public class CheckItemsCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if(cmd.getName().equalsIgnoreCase("checkitems") && sender instanceof Player && Main.debugMode) {
			
			Player player = (Player)sender;
			for(ItemStack item : Artifacts.everyItems()) {
				player.getInventory().addItem(item);
			}
			
		}
		
		return false;
	}

}
