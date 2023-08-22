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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Ketcha implements Role {

	private Main main;
	private HashMap<Integer, Integer> timeSpentInvisibleByEpisode = new HashMap<Integer, Integer>();
	private long lastHitMade = 0;
	private long gameStart = 0;
	private long fiveMinAfterPvPStart;
	
	public Ketcha(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.KETCHA;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez octroyer l’effet §9Vitesse 1§r de façon permanente, cinq minutes après l'activation du PvP, lorsque vous retirerez votre armure, vous vous verrez devenir invisible et vous serez invulnérable aux dégâts de chutes, vous êtes cependant limité à dix minutes d'invisibilités par épisode, si jamais vous recevez un coup, vous resterez invisible en revanche, si vous mettez ce coup, vous redeviendrez visible. \n";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		gameStart = System.currentTimeMillis();
		fiveMinAfterPvPStart = gameStart;
		if(manager.getMain().getConfig().isSet("timers.time-before-pvp")) {
			fiveMinAfterPvPStart += manager.getMain().getConfig().getInt("timers.ketcha-time-before-invis-able") * 60 * 1000;
		}
		
		List<Player> ketchas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) ketchas.add(offlinePlayer.getPlayer());
		}
		
		for(Player ketcha : ketchas) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!ketcha.hasPotionEffect(PotionEffectType.SPEED)) {
						ketcha.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1_000_000, 0));
					}
					
					if(fiveMinAfterPvPStart <= System.currentTimeMillis()) {
						// check armor status
						boolean isWithoutArmor = 
								ketcha.getInventory().getHelmet()     == null && 
								ketcha.getInventory().getChestplate() == null && 
								ketcha.getInventory().getLeggings()   == null && 
								ketcha.getInventory().getBoots()      == null;
						
						int episode = (int)((System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000));
						
						if(isWithoutArmor && (!timeSpentInvisibleByEpisode.containsKey(episode) || timeSpentInvisibleByEpisode.get(episode) < (10 * 60)) && (lastHitMade + 10 * 1000 < System.currentTimeMillis())) {
							ketcha.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 50, 0), true);
						}
						
						if(ketcha.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
							timeSpentInvisibleByEpisode.putIfAbsent(episode, 0);
							timeSpentInvisibleByEpisode.put(episode, timeSpentInvisibleByEpisode.get(episode) + 1);
						}
					}
					
				}
			}.runTaskTimer(manager.getMain(), 0, 20l);
		}
	}
	
	@EventHandler
	public void onKetchaTakeFallDamageWithoutArmor(EntityDamageEvent event) {
		if(fiveMinAfterPvPStart <= System.currentTimeMillis()) {
			if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
			if(event.getCause() != DamageCause.FALL) return;
			if(!(event.getEntity() instanceof Player)) return;
			Player player = (Player)event.getEntity();
			
			if(!main.getCurrentGameManager().isRoleAttributed(RoleType.KETCHA)) return;
			if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.KETCHA).contains(player.getUniqueId())) {
				
				boolean isWithoutArmor = 
						player.getInventory().getHelmet()     == null && 
						player.getInventory().getChestplate() == null && 
						player.getInventory().getLeggings()   == null && 
						player.getInventory().getBoots()      == null ;
				
				if(isWithoutArmor) {
					event.setCancelled(true);
				}
				
			}
		}
	}
	
	@EventHandler
	public void onKetchaHitsPpl(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player)) return;
		if(main.getCurrentGameManager().isPlaying(event.getDamager().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getDamager().getUniqueId()) == RoleType.KETCHA) {
				this.lastHitMade = System.currentTimeMillis();
			}
		}
	
	}
	


}
