package fr.altaks.mco.uhc.commands.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class TestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("test")) {

			sender.sendMessage(Main.DEBUG + "Liste des rôles et leur affichage (" + RoleType.values().length + ")");
			for(RoleType role : RoleType.values()) {
				sender.sendMessage("["+role.getRoleTeam().getTeamName() + "] ("+ role.getRoleAmountOfPlayers() +") : " + role.getRoleName() + "\n");
			}
			return true;
			
		}
		return false;
	}

}
