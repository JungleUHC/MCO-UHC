package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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

public class OlmequeSoldier implements Role {
	
	private HashMap<UUID, Short> alreadyChosenStuff = new HashMap<UUID, Short>();
	
	public HashMap<UUID, Short> getAlreadyChosenStuff(){
		return this.alreadyChosenStuff;
	}

	private double soldierResistance = 0.8;
	
	private Main main;
	
	public OlmequeSoldier(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.OLMEQUE_SOLDIER;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, un choix vous sera proposé, via la commande §2\"/mco equipement\"§r, vous pourrez choisir entre un livre §bProtection III§r accompagné de §b20%§r de §bRésistance§r ou un livre §bUnbreaking III§r et §bNoFall§r\n"
	   + "Lorsqu'un §aSoldat Olmèque§r meurt, vous êtes pris d'une humeur de vengeance, vous obtenez un effet de §bRésistance I§r durant 2 minutes.\n"
	   + "Voici l'identité de tous les §aSoldats Olmèques§r : %coalition-olmeque_soldier%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> olmeques = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) olmeques.add(offlinePlayer.getPlayer());
		}

		soldierResistance = main.getConfig().getDouble("timers.olmeque-soldier-resistence");
		
		for(Player olmeque : olmeques) {
			
			BaseComponent[] total = 
					   new ComponentBuilder("§e----------------------------------------------------§r\n")
					   .append("Vous êtes un soldat olmèque, vous pouvez choisir votre équipement de départ : \n Votre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[1]")
					   		.color(ChatColor.AQUA)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement olmequesoldier 1"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Protection III + 20% de résistance").create()))
					   .append(" - ")
					   		.color(ChatColor.WHITE)
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .append("[2]")
					   		.color(ChatColor.BLUE)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement olmequesoldier 2"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Unbreaking III + NoFall").create()))
					   .append("\n§e----------------------------------------------------\n")
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .create();
			
			olmeque.spigot().sendMessage(total);
			
		}

	}
	
	@EventHandler
	public void onOlmequeDeathEvent(EntityDeathEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.OLMEQUE_SOLDIER) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.OLMEQUE_SOLDIER).contains(event.getEntity().getUniqueId())) {
		   
		   for(UUID olmeque : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.OLMEQUE_SOLDIER)) {
			   if(!main.getCurrentGameManager().hasDied(olmeque)) {
				   Bukkit.getPlayer(olmeque).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2*60*20, 0), true);
			   }
		   }
		}
	}
	
	@EventHandler
	public void onOlmequeTakeDamage(EntityDamageEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.OLMEQUE_SOLDIER) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.OLMEQUE_SOLDIER).contains(event.getEntity().getUniqueId())) {
							
			if(this.alreadyChosenStuff.containsKey(event.getEntity().getUniqueId())) {
				if(this.alreadyChosenStuff.get(event.getEntity().getUniqueId()) == (short)1) {
					Main.logIfDebug("Damage before reduction " + event.getDamage());
					event.setDamage(event.getDamage() * soldierResistance);
					Main.logIfDebug("Damage after reduction " + event.getDamage());
				} else if(this.alreadyChosenStuff.get(event.getEntity().getUniqueId()) == (short)2 && event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
				}
			}
			
		}
	}

}
