package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class UrubusWarrior implements Role {
	
	private HashMap<UUID, Short> alreadyChosenStuff = new HashMap<UUID, Short>();
	
	public HashMap<UUID, Short> getAlreadyChosenStuff(){
		return this.alreadyChosenStuff;
	}
	private double urubusWarriorResistance = 0.9;
	private double urubusWarriorStrenght = 1.1;
	private int urubusWarriorGoldenAppleOnUrubusDeath = 3;
	private Main main;
	
	public UrubusWarrior(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.URUBUS_WARRIOR;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, un choix vous sera proposé, via la commande §2\"/mco equipement\"§r, vous pourrez choisir entre, un livre §bFlame I§b accompagné de §b10% de Force§r ou un livre §bFire Aspect§r et §b10% de Résistance§r\n"
	   + "Lorsqu'un §aGuerrier Urubus§r meurt, vous gagnez §b5 pommes dorées.§r\n"
	   + "Voici l'identité de tous les §aGuerriers Urubus§r : %coalition-urubus_warrior%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		urubusWarriorGoldenAppleOnUrubusDeath = manager.getMain().getConfig().getInt("timers.urubus-warrior-golden-apple-on-urubus-death");

		List<Player> urubuses = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) urubuses.add(offlinePlayer.getPlayer());
		}
		
		for(Player urubus : urubuses) {
			
			BaseComponent[] total = 
					   new ComponentBuilder("§e----------------------------------------------------§r\n")
					   .append("Vous êtes un guerrier urubus, vous pouvez choisir votre équipement de départ : \n Votre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[1]")
					   		.color(ChatColor.AQUA)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement urubuswarrior 1"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Flame I + 10% de dégâts suppl.").create()))
					   .append(" - ")
					   		.color(ChatColor.WHITE)
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .append("[2]")
					   		.color(ChatColor.BLUE)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement urubuswarrior 2"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Fire Aspect + 10% de résistance").create()))
					   .append("\n§e----------------------------------------------------\n")
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .create();
			
			urubus.spigot().sendMessage(total);
			
		}

		urubusWarriorResistance = main.getConfig().getDouble("timers.urubus-warrior-resistance");
		urubusWarriorStrenght = main.getConfig().getDouble("timers.urubus-warrior-strenght");

	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.URUBUS_WARRIOR)){
			if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.URUBUS_WARRIOR).contains(event.getDamager().getUniqueId())) {
				
				if(this.alreadyChosenStuff.containsKey(event.getDamager().getUniqueId())) {
					if(this.alreadyChosenStuff.get(event.getDamager().getUniqueId()) == (short)1) {
						event.setDamage(event.getDamage() * urubusWarriorStrenght);
					} 
				} 
				
			} else if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.URUBUS_WARRIOR).contains(event.getEntity().getUniqueId())) {
				if(this.alreadyChosenStuff.containsKey(event.getEntity().getUniqueId())) {
					if(this.alreadyChosenStuff.get(event.getEntity().getUniqueId()) == (short)2) {
						event.setDamage(event.getDamage() * urubusWarriorResistance);
					} 
				}
			}
		}
	}
	
	@EventHandler
	public void onUrubusDeath(EntityDeathEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.URUBUS_WARRIOR) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.URUBUS_WARRIOR).contains(event.getEntity().getUniqueId())) {
		   
		   for(UUID seiban : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.URUBUS_WARRIOR)) {
			   if(main.getCurrentGameManager().isPlaying(seiban)) {
				   Bukkit.getPlayer(seiban).getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, urubusWarriorGoldenAppleOnUrubusDeath));
			   }
		   }
		}
	}

}
