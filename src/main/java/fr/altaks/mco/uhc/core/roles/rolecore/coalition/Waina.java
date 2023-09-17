package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Waina implements Role {

	private Main main;
	private int traceDuFelinDuration = 60;
	private int traceDuFelinEffectsDuration = 60;

	private int speedAmplifier = 1;

	public Waina(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.WAINA;
	}

	@Override
	public String getRelativeExplications() {
		return
		  "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
		+ "\u30FBParticularités de votre rôle: \n"
		+ "Dès l'annonce des rôles, vous vous verrez octroyer l’effet §9Vitesse 1§r de façon permanente, vous recevez également l'item §2\"Repérage\"§r, celui-ci vous permet d'obtenir les §bcoordonnées§r ainsi que les §bpseudonymes§r des joueurs étant dans un rayon de §b100 blocs§r autour de vous. §oVous disposez d'une seule utilisation par épisode pour ce pouvoir.§r\n"
		+ "Vous possédez un item de dernier recours nommé §bTrace du Félin§r vous permettant de vous transformer en §bFélin§r, vous vous verrez octroyer l'effet §bVitesse 3§r ainsi que §4Weakness 1§r et ce pendant §bune minute§r, néanmoins, le contre-coup lui enlèvera §c4§r ceurs permanents. ";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		List<Player> wainas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) wainas.add(offlinePlayer.getPlayer());
		}

		traceDuFelinDuration = main.getConfig().getInt("timers.waina-trace-felin-duration");
		traceDuFelinEffectsDuration = main.getConfig().getInt("timers.waina-trace-felin-effects-duration");
		speedAmplifier = main.getConfig().getInt("timers.waina-speed-amplifier");

		for(Player waina : wainas) {
			waina.getInventory().addItem(Artifacts.RoleItems.REPERAGE);
			waina.getInventory().addItem(Artifacts.RoleItems.TRACE_DU_FELIN);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!waina.hasPotionEffect(PotionEffectType.SPEED)) {
						waina.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1_000_000, speedAmplifier-1));
					}
				}
			}.runTaskTimer(manager.getMain(), 0, 20l);
		}
		gameStart = System.currentTimeMillis();
	}
	
	private long lastUsedEpisodeReperage = -1, gameStart = 0;
	
	
	@EventHandler
	public void onWainaUseReperage(PlayerInteractEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem() == null) return;
		if(ItemManager.lightCompare(event.getItem(), Artifacts.RoleItems.REPERAGE)) {
			Player waina = event.getPlayer();
			if((System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000) == lastUsedEpisodeReperage) {
				waina.sendMessage(Main.PREFIX + "§bWaina \u00BB §cJe ne vois pas bien pour le moment...");
				return;
			}
			List<Player> nearbyPlayers = new ArrayList<Player>();
			for(Entity entity : waina.getNearbyEntities(100, 100, 100)) {
				if(entity instanceof Player) {
					if(entity.equals(event.getPlayer())) continue;
					nearbyPlayers.add((Player)entity);
				}
			}
			
			for(Player player : nearbyPlayers) {
				waina.sendMessage(Main.PREFIX + "§bWaina \u00BB §r" + player.getName() + " : " + player.getLocation().getBlockX() + " | " + player.getLocation().getBlockZ());
			}
			lastUsedEpisodeReperage = (System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
		}
	}
	
	private HashMap<UUID, UUID> felinToWaina = new HashMap<>();
	
	@EventHandler
	public void onWainaUseTraceDuFelin(PlayerInteractEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem() == null) return;
		if(ItemManager.lightCompare(event.getItem(), Artifacts.RoleItems.TRACE_DU_FELIN)) {
			Player waina = event.getPlayer();
			waina.getInventory().setItemInHand(null);
			
			// waina.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, traceDuFelinEffectsDuration * 20, 0, false, true), true);
			for(Player player : Bukkit.getOnlinePlayers()){
				if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) player.hidePlayer(waina);
			}


			waina.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, traceDuFelinEffectsDuration * 20, 0, false, true), true);
			waina.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, traceDuFelinEffectsDuration * 20, 2, false, true), true);
			
			Ocelot felin = (Ocelot) waina.getWorld().spawnEntity(waina.getLocation(), EntityType.OCELOT);
			felin.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, traceDuFelinDuration * 20, 4, false, true), true);
			felinToWaina.put(felin.getUniqueId(), waina.getUniqueId());
			
			new BukkitRunnable() {
				
				int timer = traceDuFelinDuration * 20;
				Location lastLocation = null;
				
				@Override
				public void run() {
					if(timer >= 0) {
						if(!(lastLocation != null && lastLocation.equals(waina.getLocation()))) {
							felin.teleport(waina.getLocation());
							lastLocation = waina.getLocation();
						}
						timer--;
					} else {
						felin.remove();
						waina.setMaxHealth(waina.getMaxHealth() - 4*2);
						felinToWaina.remove(felin.getUniqueId());
						for(Player player : Bukkit.getOnlinePlayers()){
							if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) player.showPlayer(waina);
						}
						cancel();
					}
				}
			}.runTaskTimer(main, 0, 1);
			
		}
	}
	
	@EventHandler
	public void onFelinGetsHit(EntityDamageEvent event) {
		if(this.felinToWaina.keySet().contains(event.getEntity().getUniqueId())) {
			Player waina = Bukkit.getPlayer(this.felinToWaina.get(event.getEntity().getUniqueId()));
			
			waina.damage(event.getDamage());
			waina.setLastDamageCause(event);
			
			if(event.getFinalDamage() >= ((LivingEntity)event.getEntity()).getHealth()) {
				waina.setHealth(0.0d);
			}
		}
	}

}
