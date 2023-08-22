package fr.altaks.mco.uhc.commands.debug;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;

public class DisplayRolesCommand implements CommandExecutor {

	private Main main;
	
	public DisplayRolesCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("displayroles") && main.getCurrentGameManager().getCurrentGameState() == GameState.PLAYING) {
			
			GameManager manager = main.getCurrentGameManager();
			
			for(Entry<Role, ArrayList<UUID>> attributedRoles : manager.getPlayersFromRole().entrySet()) {
				StringJoiner joiner = new StringJoiner(", ");
				for(UUID id : attributedRoles.getValue()) {
					joiner.add(Bukkit.getOfflinePlayer(id).getName());
				}
				sender.sendMessage(attributedRoles.getKey().getRole().getRoleName() + " : " + joiner.toString() + " [" + (main.getCurrentGameManager().getDeadRoles().contains(attributedRoles.getKey().getRole()) ? "Rôle mort" : "Rôle encore en vie") + "]");
			}
			for(Role nonAttributed : manager.getRemainingRoles().keySet()) {
				sender.sendMessage(nonAttributed.getRole().getRoleName() + " : Non attribué");
			}
			
			return true;
		}
		return false;
	}

}
