package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Mendoza implements Role {

	private Main main;
	private long lastCourageUsage = -1;
	private int checkOnEstebanUsages = 2;
	private List<UUID> inDanger = new ArrayList<UUID>();
	private int timeBeforeBeingUnableToTpToEsteban = 60;
	private double courageStrenght = 1.1;
	
	public Mendoza(Main main) {
		this.main = main;
	}
	
	
	@Override
	public RoleType getRole() {
		return RoleType.MENDOZA;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle: Dès l’annonce des rôles  vous recevez une épée en diamants tranchant 4.\n"
	   + "De plus, étant donné que vous êtes un combattant expert, vous recevez l’effet §cForce 1§r de façon permanente.\n"
	   + "Mendoza est un très bon meneur par conséquent il possède la commande §2“/mco courage”§r qui confèrent 10% de dommage supplémentaires pendant 1 minute à tous les membres de la coalition.   vous disposez d’une seule utilisation pour ce pouvoir. \n"
	   + "A chaque fois qu'§aEsteban§r tombe en dessous de 5 coeurs vous pouvez choisir de connaître ou non les coordonnées de ce dernier. §oVous disposez seulement de deux utilisations pour ce pouvoir.§r.\n"
	   + "Voici l’identité de §aEsteban§r: %coalition-esteban%";
	}
	
	@EventHandler
	public void onEntityDamageSmth(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(lastCourageUsage != -1) {// Damager has to be a player
			if(!(event.getDamager() instanceof Player)) return;
			
			UUID damager = ((Player)event.getDamager()).getUniqueId();
			if((main.getCurrentGameManager().getTeamOfPlayer(damager) == GameTeam.COALITION) && (lastCourageUsage + (60 * 1000) >= System.currentTimeMillis())) {
				event.setDamage(event.getDamage() * courageStrenght);
			}
			
		}
	}
	
	@EventHandler
	public void onEstebanLow(EntityDamageEvent event) {
		if(checkOnEstebanUsages == 0) return;
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		if(player.getHealth() - event.getDamage() < (5 * 2) && (player.getHealth() >= 5 * 2)) {
			if(main.getCurrentGameManager().isRoleAttributed(RoleType.ESTEBAN) &&
			   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ESTEBAN).contains(player.getUniqueId())) {
			    
			   if(!main.getCurrentGameManager().isRoleAttributed(RoleType.MENDOZA)) return;
 			   ArrayList<UUID> mendozas = main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENDOZA);

			   BaseComponent[] total = 
					   new ComponentBuilder("§c----------------------------------------------------§r\n")
					   .append("Esteban est en danger ? Voulez vous connaître ses coordonnées ?\nVotre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[\u2714]").color(ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco mendozaseek " + player.getName()))
					   .append(" - ").color(ChatColor.WHITE).bold(false)
					   .append("[\u2716]").color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco mendozaignore " + player.getName()))
					   .append("§c----------------------------------------------------\n").bold(false)
					   .create();
			   
			   inDanger.add(player.getUniqueId());
			   new BukkitRunnable() {
					@Override
					public void run() {
						inDanger.remove(player.getUniqueId());
					}
					
			   }.runTaskLater(main, timeBeforeBeingUnableToTpToEsteban * 20l);
			   
			   for(UUID mendoza : mendozas) {
				   Bukkit.getPlayer(mendoza).spigot().sendMessage(total);
			   }
						
			}
		}
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> mendozas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) mendozas.add(offlinePlayer.getPlayer());
		}
		
		for(Player mendoza : mendozas) {
			
			mendoza.getInventory().addItem(Artifacts.RoleStuffs.EPEE_MENDOZA);
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!mendoza.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
						mendoza.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1_000_000, 0));
					}
				}
			}.runTaskTimer(manager.getMain(), 0, 20l);
			
		}

		timeBeforeBeingUnableToTpToEsteban = main.getConfig().getInt("timers.mendoza-time-before-unable-to-tp-to-esteban");
		courageStrenght = main.getConfig().getDouble("timers.mendoza-courage-strenght");
	}
	
	public void setLastUsageCourage(long timestamp) {
		this.lastCourageUsage = timestamp;
	}

	public List<UUID> getEstebansInDanger(){
		return this.inDanger;
	}
	
	public void reduceMendozaSeekUsages() {
		this.checkOnEstebanUsages--;
	}

}
