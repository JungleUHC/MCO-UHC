package fr.altaks.mco.uhc.core.roles.rolecore.independant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Takashi implements Role {

	private Main main;
	
	
	private HashMap<UUID, UUID> canTakeDamageFrom = new HashMap<>();
	private long lastEpisodeThatUsedDuel = -1;
	private long gameStart = 0;
	private int killRegenerationDuration = 1, killStrenghtDuration = 15;
	
	public Takashi(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.TAKASHI;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous êtes §dSolitaire§r, votre objectif est donc de tuer tous les joueurs de la partie.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez attribuer les effets §cForce I§r et §bVitesse I§r de manière permanente, vous recevez également une épée de diamant §bTranchant IV§r\n"
	   + "Lorsque §dTakashi§r effectue un meurtre, une régénération automatique d'§bune minute§r lui est attribué ainsi que §b15 secondes§r de §cForce II§r. \n"
	   + "Vous possédez la commande §2\"/mco duel\"§r qui vous permettra de défier un joueur pendant §btrente secondes§r, pendant ce laps de temps, il sera impossible aux autres joueurs de vous affronter. "
	   + "§oVous ne disposez que d'une seule utilisation par épisode pour ce pouvoir.§r";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		this.gameStart = System.currentTimeMillis();
		
		List<Player> takashis = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				takashis.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player takashi : takashis) {
			takashi.getInventory().addItem(Artifacts.RoleStuffs.EPEE_TAKASHI);	
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player takashi : takashis) {
					takashi.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 50, 0), false);
					takashi.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0), true);
				}
			}
		}.runTaskTimer(manager.getMain(), 0, 10l);

		killRegenerationDuration = manager.getMain().getConfig().getInt("timers.takashi-kill-regeneration-duration");
		killStrenghtDuration = manager.getMain().getConfig().getInt("timers.takashi-kill-strenght-duration");

	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onTakashiKillsPlayer(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId()) && main.getCurrentGameManager().isPlaying(event.getDamager().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getDamager().getUniqueId()) == RoleType.TAKASHI) {
				if(((Player)event.getEntity()).getHealth() <= event.getFinalDamage()) {
					((Player)event.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, killRegenerationDuration * 60 * 20, 0), true);
					((Player)event.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, killStrenghtDuration * 20, 1), true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityTakesDamage(EntityDamageByEntityEvent event) {
		if(this.canTakeDamageFrom.containsKey(event.getEntity().getUniqueId())) {
			if(!this.canTakeDamageFrom.get(event.getEntity().getUniqueId()).equals(event.getDamager().getUniqueId())) {
				event.setCancelled(true);
			}
		} else {
			if(this.canTakeDamageFrom.containsKey(event.getDamager().getUniqueId()) && !this.canTakeDamageFrom.containsKey(event.getEntity().getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

	public HashMap<UUID, UUID> getCanTakeDamageFrom() {
		return canTakeDamageFrom;
	}

	public void markThisEpisodeDuelAsUsed() {
		long episode = (System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
		this.lastEpisodeThatUsedDuel = episode;
	}
	
	public boolean canUseDuel() {
		return (System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000) != this.lastEpisodeThatUsedDuel;
	}


}
