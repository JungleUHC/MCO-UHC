package fr.altaks.mco.uhc.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.altaks.mco.uhc.Main;

public class GameStatusCommand implements CommandExecutor {
	
	private Main main;
	
	public GameStatusCommand(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("gamestatus")) {
			sender.sendMessage(Main.DEBUG + "Statut de la partie : " + main.getCurrentGameManager().getCurrentGameState().desc);
			return true;
		}
		return false;
	}

}
