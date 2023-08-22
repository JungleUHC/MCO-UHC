package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Kraka implements Role {
	
	private Main main;
	
	private long lastCityIncaUsage = 0;
	private Location lastCityEntryPointSpawn = null;
	private boolean hasUsedKey = false;

	private int speedWhenWainaOrKetchaDie = 1;
	
	public Kraka(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.KRAKA;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous vous voyez octroyer §cdeux coeurs supplémentaires§r, ainsi qu'une §6Clé de Cité d'Or§r, celle-ci en échange de deux cœurs permanents vous permet de débloquer la commande §2\"/mco inca\"§r, celle-ci vous permettra d'obtenir une partie des coordonnées de la dernière cité ayant spawn. §oVous disposez d'une seule utilisation toutes les §b25 minutes§r§o pour ce pouvoir.§r\n"
	   + "Si §aWaina§r vient à mourir, il obtient l’identité de §aKetcha§r\n"
	   + "Si §aKetcha§r vient à mourir, il obtient l'identité de §aWaina§r\n"
	   + "Si §aWaina§r vient à mourir, vous obtenez §bSpeed I§r\n"
	   + "Si §aKetcha§r vient à mourir, vous obtenez §bSpeed I§r\n"
	   + "Si les deux sont morts vous obtenez §bSpeed II§r de façon permanente";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		List<Player> krakas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) krakas.add(offlinePlayer.getPlayer());
		}
		
		for(Player kraka : krakas) {
			kraka.setMaxHealth(24);
			kraka.getInventory().addItem(Artifacts.CLE_CITE_OR.clone());
		}

		speedWhenWainaOrKetchaDie = main.getConfig().getInt("timers.kraka-when-waina-or-ketcha-dies-set-speed");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(!main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId())) return;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.WAINA) && main.getCurrentGameManager().isRoleAttributed(RoleType.KETCHA)) {
			
			// get all krakas players
			ArrayList<Player> krakas = new ArrayList<>();
			for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.KRAKA)) {
				if(main.getCurrentGameManager().isPlaying(id)) krakas.add(Bukkit.getPlayer(id));
			}
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					
					// if dead role == ketcha
					if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.KETCHA)) {
						
						// find a random waina
						ArrayList<Player> wainas = new ArrayList<>();
						for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.WAINA)) {
							if(main.getCurrentGameManager().isPlaying(id)) wainas.add(Bukkit.getPlayer(id));
						}
						if(wainas.size() == 0) return;
						int pick = new Random().nextInt(wainas.size());
						
						for(Player kraka : krakas) kraka.sendMessage(Main.PREFIX + "§cKetcha est morte ! Vous avez cependant compris que " + wainas.get(pick).getDisplayName() + " était Waina !");
						
					} else if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.WAINA)) {
						
						ArrayList<Player> ketchas = new ArrayList<>();
						for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.KETCHA)) {
							if(main.getCurrentGameManager().isPlaying(id)) ketchas.add(Bukkit.getPlayer(id));
						}
						if(ketchas.size() == 0) return;
						int pick = new Random().nextInt(ketchas.size());
						
						for(Player kraka : krakas) kraka.sendMessage(Main.PREFIX + "§cWaina est morte ! Vous avez cependant compris que " + ketchas.get(pick).getDisplayName() + " était Ketcha !");
						
					}
				}
			}.runTaskLater(main, 20);
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					
					if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.WAINA) && main.getCurrentGameManager().getDeadRoles().contains(RoleType.KETCHA)) {
						for(Player kraka : krakas) {
							kraka.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, speedWhenWainaOrKetchaDie), true);
						}
					} else if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.WAINA) || main.getCurrentGameManager().getDeadRoles().contains(RoleType.KETCHA)) {
						for(Player kraka : krakas) {
							kraka.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, speedWhenWainaOrKetchaDie-1), true);
						}
					}
					
				}
			}.runTaskTimer(main, 20, 20);
			
			
		}
		
	}
	
	@EventHandler
	public void onKrakaUseGoldenKey(PlayerInteractEvent event) {
		if(event.hasItem() && ItemManager.lightCompare(event.getItem(), Artifacts.CLE_CITE_OR)) {
			this.hasUsedKey = true;
			event.getPlayer().setItemInHand(null);
			event.getPlayer().setMaxHealth(20);
			return;
		}
	}

	public void setLastCityEntryPointSpawn(Location lastCityEntryPointSpawn) {
		this.lastCityEntryPointSpawn = lastCityEntryPointSpawn;
	}
	
	public Location getLastCityEntryPointSpawn() {
		return lastCityEntryPointSpawn;
	}

	public long getLastCityIncaUsage() {
		return lastCityIncaUsage;
	}

	public void setLastCityIncaUsage(long lastCityIncaUsage) {
		this.lastCityIncaUsage = lastCityIncaUsage;
	}

	public boolean hasUsedKey() {
		return hasUsedKey;
	}

}
