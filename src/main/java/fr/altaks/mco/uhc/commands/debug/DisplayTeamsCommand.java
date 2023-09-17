package fr.altaks.mco.uhc.commands.debug;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;

public class DisplayTeamsCommand implements CommandExecutor {

	private Main main;
	
	public DisplayTeamsCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(cmd.getName().equalsIgnoreCase("displayteams") && main.getCurrentGameManager().getCurrentGameState() == GameState.PLAYING) {
			
			GameManager manager = main.getCurrentGameManager();
			
			for(UUID id : manager.roleOfPlayer().keySet()) {
				if(manager.isPlaying(id)) {
					Role role = manager.getRoleFromRoleType().get(manager.roleOfPlayer().get(id));
					sender.sendMessage("["+Bukkit.getOfflinePlayer(id).getName() + "] \u00BB " + role.getTeamOfPlayer().getTeamName());
				}
			}
			
			return true;
		}
		return false;
	}

}
