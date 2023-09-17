package fr.altaks.mco.uhc.core.roles.rolecore.independant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Calmeque implements Role {

	private Main main;
	private int bombsBlindnessDuration = 10;
	
	public Calmeque(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.CALMEQUE;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous êtes en duo avec §dMenator§r, votre objectif commun est donc de tuer tous les joueurs de la partie.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez attribuer les effets §cForce I§r de nuit ainsi que §4Weakness I§r de jour, vous recevrez également §bcinq bombes aveuglantes§r, qui lors de l'impact aveuglent tous les joueurs dans un rayon de §btrois blocs§r durant dix secondes. \n"
	   + "Dès l'apparition des §6Cités d'Or§r, vous aurez la possibilité de vous y téléporter quand vous le voudrez, dans celle-ci, vous perdez votre effet de §4Weakness§r.\n"
	   + "La commande §2\"/mco piege\"§r disponible toutes les §bvingt minutes§r vous est également attribuée, celle-ci permettant d'empêcher le joueur ciblé de sauter pendant trente secondes.\n"
	   + "Si vous frappez un joueur possédant moins de deux coeurs, vous l'achevez de façon instantanée.\n"
	   + "Voici l’identité de §dMenator§r: %independant-menator%";
		
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> calmeques = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				calmeques.add(offlinePlayer.getPlayer());
			}
		}

		for(Player calmeque : calmeques) {
			calmeque.getInventory().addItem(Artifacts.RoleItems.BOMBES_AVEUGL);
		}

		new BukkitRunnable() {
			
			final long SUNSET = 12542, SUNRISE = 23460;
			
			@Override
			public void run() {
				
				for(Player calmeque : calmeques) {
						PotionEffectType type = PotionEffectType.WEAKNESS;
						
						long worldTicks = calmeque.getWorld().getTime(); // obtenir le temps du jour
						if((SUNSET < worldTicks && worldTicks < SUNRISE) || calmeque.getLocation().getWorld().getName().equalsIgnoreCase("cities")) type = PotionEffectType.INCREASE_DAMAGE; // si la nuit alors force
						
						calmeque.addPotionEffect(new PotionEffect(type, 50, 0), true);
				}
				
			}
		}.runTaskTimer(manager.getMain(), 0, 20);


		bombsBlindnessDuration = manager.getMain().getConfig().getInt("timers.calmeque-bombs-smoke-duration");
	}
	
	@EventHandler
	public void onCalmequeHitLowPlayer(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId()) && main.getCurrentGameManager().isPlaying(event.getDamager().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getDamager().getUniqueId()) == RoleType.CALMEQUE) {
				if(((Player)event.getEntity()).getHealth() < 4) {
					event.setDamage(DamageModifier.BASE, event.getDamage() + 4);
				}
			}
		}
	}
	
	public List<Entity> thrownBombs = new ArrayList<>();
	
	@EventHandler
	public void onCalmequeThrowBombAveugl(ProjectileLaunchEvent event) {
		if(event.getEntity().getShooter() instanceof Player) {
			Player thrower = (Player) event.getEntity().getShooter();
			if(event.getEntity().getType() == EntityType.SNOWBALL) {
				if(main.getCurrentGameManager().isPlaying(thrower.getUniqueId())) {
					if(ItemManager.lightCompare(thrower.getItemInHand(), Artifacts.RoleItems.BOMBES_AVEUGL)) {
						thrownBombs.add(event.getEntity());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onThownBombHitTheGround(ProjectileHitEvent event) {
		if(thrownBombs.contains(event.getEntity())) {
			List<Entity> entities = event.getEntity().getNearbyEntities(3, 3, 3);
			for(Entity nearbyEntity : entities) {
				if(event.getEntity().getShooter().equals(nearbyEntity)) continue;
 				if(nearbyEntity instanceof LivingEntity) {
					((LivingEntity)nearbyEntity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, bombsBlindnessDuration*20, 1), true);
				}
			}
		}
	}

}
