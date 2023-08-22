package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class MayaSoldier implements Role {
	
	private HashMap<UUID, Short> alreadyChosenStuff = new HashMap<UUID, Short>();
	
	public HashMap<UUID, Short> getAlreadyChosenStuff(){
		return this.alreadyChosenStuff;
	}

	private double mayaSoldierNonResistance = 1.15;
	private double mayaSoldierStrength = 1.2;
	private int soldierStrenghtOnSimilarDeathDuration = 2;
	
	private Main main;
	
	public MayaSoldier(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.MAYA_SOLDIER;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, un choix vous sera proposé, via la commande §2\"/mco equipement\"§r, vous pourrez choisir entre un livre §bTranchant III§r accompagné de §b20%§r de §bForce§r ou un livre §bProtection III§r et §b15%§r de Résistance. \n"
	   + "Lorsqu'un §aSoldat Maya§r meurt, vous êtes pris d'une humeur de vengeance, vous obtenez un effet de §cForce I§r durant 2 minutes.\n"
	   + "Voici l'identité de tous les §aSoldats Mayas§r : %coalition-maya_soldier%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> mayas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) mayas.add(offlinePlayer.getPlayer());
		}

		soldierStrenghtOnSimilarDeathDuration = main.getConfig().getInt("timers.maya-soldier-strenght-on-similar-death-duration");
		mayaSoldierStrength = main.getConfig().getDouble("timers.maya-soldier-strength");
		
		for(Player maya : mayas) {
			
			BaseComponent[] total = 
					   new ComponentBuilder("§e----------------------------------------------------§r\n")
					   .append("Vous êtes un soldat maya, vous pouvez choisir votre équipement de départ : \n Votre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[1]")
					   		.color(ChatColor.AQUA)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement mayasoldier 1"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Tranchant III + 20% de dégâts").create()))
					   .append(" - ")
					   		.color(ChatColor.WHITE)
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .append("[2]")
					   		.color(ChatColor.BLUE)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement mayasoldier 2"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Protection III + 15% de résistance").create()))
					   .append("\n§e----------------------------------------------------\n")
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .create();
			
			maya.spigot().sendMessage(total);
			
		}

		mayaSoldierNonResistance = main.getConfig().getDouble("timers.maya-soldier-resistance");
	}
	
	@EventHandler
	public void onMayaDeath(EntityDeathEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.MAYA_SOLDIER) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MAYA_SOLDIER).contains(event.getEntity().getUniqueId())) {
			
		   for(UUID maya : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MAYA_SOLDIER)) {
			   if(!main.getCurrentGameManager().hasDied(maya)) {
				   Bukkit.getPlayer(maya).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, soldierStrenghtOnSimilarDeathDuration*60*20, 0), true);
			   }
		   }
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.MAYA_SOLDIER)){
			if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MAYA_SOLDIER).contains(event.getDamager().getUniqueId())) {
				
				if(this.alreadyChosenStuff.containsKey(event.getDamager().getUniqueId())) {
					if(this.alreadyChosenStuff.get(event.getDamager().getUniqueId()) == (short)1) {
						event.setDamage(event.getDamage() * mayaSoldierStrength);
					} 

				} 
			} else if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MAYA_SOLDIER).contains(event.getEntity().getUniqueId())) {
				if(this.alreadyChosenStuff.containsKey(event.getEntity().getUniqueId())) {
					if(this.alreadyChosenStuff.get(event.getEntity().getUniqueId()) == (short)2) {
						event.setDamage(event.getDamage() * (2 - mayaSoldierNonResistance));
					} 
				}
			}
		}
	}

}
