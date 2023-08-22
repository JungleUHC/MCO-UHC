package fr.altaks.mco.uhc.commands.debug;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.altaks.mco.uhc.Main;

public class StartGameCommand implements TabExecutor {

	private Main main;
	
	public StartGameCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("start") && sender instanceof Player) {
			main.getCurrentGameManager().start((Player)sender, args);
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length <= 1) {
			return main.getCurrentGameManager()
					   .getRemainingRoles().keySet()
					   .stream().map(role -> role.getRole().getRoleId())
					   .filter(id -> id.startsWith(args[0]))
					   .collect(Collectors.toList());
		} else {
			String lastArg = args[args.length - 1];
			if(!lastArg.contains(":")) {
				return Bukkit.getOnlinePlayers()
						.stream()
						.map(player -> player.getName())
						.filter(player -> !player.equalsIgnoreCase(sender.getName()))
						.collect(Collectors.toList());
			} else {
				return main.getCurrentGameManager()
						   .getRemainingRoles().keySet()
						   .stream()
						   		.map(role -> lastArg.split(":")[0] + ":" + role.getRole().getRoleId())
						   		.filter(text -> text.startsWith(lastArg))
						   .collect(Collectors.toList());
			}
		}
	}

}
