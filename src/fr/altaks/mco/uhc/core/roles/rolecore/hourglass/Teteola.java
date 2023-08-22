package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Teteola implements Role {
	
	private List<UUID> hasGotEffects = new ArrayList<UUID>();

	@Override
	public RoleType getRole() {
		return RoleType.TETEOLA;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous vous voyez octroyer §cdeux coeurs supplémentaires§r.\n"
	   + "Vous êtes le disciple de §cMarinché§r et de §cFernando Laguerra§r, par conséquent selon lequel des deux vous croisez en premier, un effet vous sera octroyé, si vous croisez §cMarinché§r, vous obtiendrez §bSpeed I§r de façon permanente, mais si en revanche vous croiser §cFernando Laguerra§r, vous vous verrez octroyer l'effet §bForce I§r également de manière permanente. \n"
	   + "Lorsque vous êtes dans un rayon §b20 blocs§r de §cMarinché§r et de §cFernando Laguerra§r, vous obtenez §bRésistance I§r.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> teteolas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				teteolas.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player teteola : teteolas) {
			teteola.setMaxHealth(24);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!teteola.isOnline()) return;
					List<Entity> entities = teteola.getNearbyEntities(20, 20, 20);
					for(Entity entity : entities) {
						if(!(entity instanceof Player)) continue;
						if(!(manager.roleOfPlayer().containsKey(entity.getUniqueId()))) return;
						RoleType role = manager.roleOfPlayer().get(entity.getUniqueId());
						if(role == RoleType.FERNANDO_LAGUERRA || role == RoleType.MARINCHE) {
							teteola.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 0, false), true);

							if(!hasGotEffects.contains(teteola.getUniqueId())) {
								new BukkitRunnable() {

									PotionEffectType effect = (role == RoleType.MARINCHE) ? PotionEffectType.SPEED : PotionEffectType.INCREASE_DAMAGE;

									@Override
									public void run() {
										teteola.addPotionEffect(new PotionEffect(effect, 50, 0, false), true);
									}
								}.runTaskTimer(manager.getMain(), 0, 20);
								hasGotEffects.add(teteola.getUniqueId());
							}
						} 

					}
					
				}
			}.runTaskTimer(manager.getMain(), 0, 40);
		}

	}

}
