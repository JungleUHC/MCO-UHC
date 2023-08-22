package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.Potion.Tier;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@SuppressWarnings("deprecation")
public class Fernando implements Role {
	
	private int healUsed = 3;
	private List<UUID> inDanger = new ArrayList<UUID>();
	private Main main;

	private int timeBeforeUnableToTpToEndangeredMember = 60;
	
	public Fernando(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.FERNANDO_LAGUERRA;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous recevez trois potions d'§bInstant Heal II§r ainsi qu'un moyen de communication avec §cMarinché§r, pour se faire exécuter la commande §2\"/mco chat\"§r\n"
	   + "Lorsqu'un joueur faisant partie de l'Ordre du Sablier tombe en dessous de 5 cœurs, et qu'il est dans un rayon de 30 blocs autour de vous. Vous recevez un message dans le chat vous permettant de choisir de le soigner ou non. §oVous disposez seulement de trois utilisations pour ce pouvoir.§r\n"
	   + "Voici l’identité d'§cAmbrosius§r: %hourglass-ambrosius%\n"
	   + "Voici l’identité d'§cAthanaos§r: %hourglass-athanaos%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> fernandos = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				fernandos.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player fernando : fernandos) {
			
			Potion pot = new Potion(1); 
			pot.setType(PotionType.INSTANT_HEAL);
			pot.setTier(Tier.TWO);
			pot.setSplash(true);
			fernando.getInventory().addItem(pot.toItemStack(3));	
		}

		timeBeforeUnableToTpToEndangeredMember = main.getConfig().getInt("timers.fernando-time-before-unable-to-tp-to-endangered-member");
	}
	
	@EventHandler
	public void onHourglassMemberGetLow(EntityDamageEvent event) {
		if(healUsed == 0) return;
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		if(player.getHealth() - event.getDamage() < (5 * 2) && (player.getHealth() >= 5 * 2)) {
			if(main.getCurrentGameManager().isRoleAttributed(RoleType.FERNANDO_LAGUERRA)) {

			   if(!main.getCurrentGameManager().roleOfPlayer().keySet().contains(player.getUniqueId())) return;
 			   if(main.getCurrentGameManager().getTeamOfPlayer(player.getUniqueId()) != GameTeam.HOURGLASS) return;
 			   
			   List<Player> fernandos = new ArrayList<Player>(); 
			   for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.FERNANDO_LAGUERRA)) {
				   OfflinePlayer fernando = Bukkit.getOfflinePlayer(id);
				   if(fernando.isOnline() && player.getLocation().distance(fernando.getPlayer().getLocation()) <= 30) {
					   fernandos.add(fernando.getPlayer());
				   }
			   }
			   									
			   
			   BaseComponent[] total = 
					   new ComponentBuilder("§c----------------------------------------------------§r\n")
					   .append("Un membre de l'ordre est en danger ? Voulez vous le soigner ?\nVotre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[\u2714]").color(ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco fernandoheal " + player.getName()))
					   .append(" - ").color(ChatColor.WHITE).bold(false)
					   .append("[\u2716]").color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco fernandoignore " + player.getName()))
					   .append("§c----------------------------------------------------\n").bold(false)
					   .create();
			   
			   inDanger.add(player.getUniqueId());
			   new BukkitRunnable() {
					@Override
					public void run() {
						inDanger.remove(player.getUniqueId());
					}
					
			   }.runTaskLater(main, timeBeforeUnableToTpToEndangeredMember * 20l);
			   
			   for(Player fernando : fernandos) {
				   fernando.spigot().sendMessage(total);
			   }
			   
			}
		}
	}
	
	public int getHealUsed() {
		return this.healUsed;
	}
	
	public List<UUID> getInDanger(){
		return this.inDanger;
	}
	
	public void reduceHealUsed() {
		this.healUsed--;
	}
	

}
