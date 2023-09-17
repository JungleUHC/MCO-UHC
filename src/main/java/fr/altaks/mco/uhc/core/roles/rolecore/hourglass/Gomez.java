package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Gomez implements Role {
	
	private HashMap<UUID, UUID> trackedPlayers = new HashMap<UUID, UUID>();

	@Override
	public RoleType getRole() {
		return RoleType.GOMEZ;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez octroyer l’effet §cForce 1§r de façon permanente.\n"
	   + "À partir de §b40 minutes§r de jeux, et cela, toutes les §b20 minutes§r, vous aurez accès à la commande §2\"/mco mission\"§r, "
	   + "qui vous permettra de recevoir le pseudonyme aléatoire d'un membre de la §aCoalition§r toujours en vie à abattre, "
	   + "si vous y arrivez, vous gagnez alors §b2 coeurs permanents§r supplémentaires pour la première élimination, ensuite"
	   + " vous ne gagnerez plus qu’§bun coeur§r permanent par élimination. Afin de vous aider dans votre tâche, vous disposez"
	   + " d'une flèche au-dessus de votre hotbar vous permettant de suivre votre cible. §oVous disposez seulement de trois "
	   + "utilisations pour ce pouvoir.§r";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		List<Player> gomezs = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				gomezs.add(offlinePlayer.getPlayer());
			}
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player gomez : gomezs) {
					if(gomez.isOnline() && !manager.hasDied(gomez.getUniqueId())) {
						gomez.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1_000_000, 0), true);
					}
				}
			}
		}.runTaskTimer(manager.getMain(), 0, 20);

	}
	
	@EventHandler
	public void onTargetedDeath(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		if(!(event.getDamager() instanceof Player)) return;
		if(event.getFinalDamage() < ((Player) event.getEntity()).getHealth()) return; // the damage has to be lethal

		Player tracked = (Player) event.getEntity();

		if(event.getEntity() instanceof Player) {
			UUID rewarded = null;
			for(Entry<UUID, UUID> entry : this.trackedPlayers.entrySet()) {
				if(entry.getKey().equals(tracked.getUniqueId())) {
					rewarded = entry.getValue();
					break;
				}
			}
			if(rewarded == null) return;
			if(Bukkit.getOfflinePlayer(rewarded).isOnline()) {
				Player gomez = Bukkit.getPlayer(rewarded);
				if(gomez.equals(event.getDamager())) {
					if(this.trackedPlayers.size() == 1) {
						gomez.setMaxHealth(gomez.getMaxHealth() + 4);
					} else {
						gomez.setMaxHealth(gomez.getMaxHealth() + 2);
					}
				}
			}
		}
	}

	public HashMap<UUID, UUID> getTrackedPlayers() {
		return trackedPlayers;
	}

}
