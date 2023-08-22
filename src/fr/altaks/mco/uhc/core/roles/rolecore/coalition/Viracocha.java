package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Viracocha implements Role {
	
	private List<Entity> poisonousArrows = new ArrayList<Entity>();
	private long lastArrowHit = 0;
	private long cooldownUntilNextHit = 0;
	private int numberOfHits = 0;

	private int cooldownBetweenSpecialArrows = 30;
	private int fallDamageCancellation = 4;
	private int sixHeartsSpeedAmplifier = 2, fourHeartsSpeedAmplifier = 3, heightHeartsSpeedAmplifier = 1;
	private int arrowCooldownReductionOnHit = 2;

	private Main main;
	
	public Viracocha(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.VIRACOCHA;
	}

	@Override
	public String getRelativeExplications() {
		return 
		"\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
		+ "\u30FBParticularités de votre rôle:\n"
		+ "Dès l'annonce des rôles, vous recevez un §bArc Puissance 4§r accompagné de 64 flèches.\n"
		+ "Vos flèches infligent un effet de §4Poison I§r pendant 5 secondes avec un cooldown de 30 secondes, cependant à chaque Hit, ce même cooldown se voit diminué de deux secondes mais il est ramené à 30 secondes tous les 10 Hits.\n"
		+ "Vous disposez également de la commande §2\"/mco scan\"§r, celle-ci a un rayon d'utilisation de 15 blocs, celui-ci fera apparaître des particules visible unique par vous, chaque couleur à son information: \n"
		+ " - §2Vert foncé§r : 2+ §2Guerriers§r / §2Soldats§r présents dans le rayon§r\n - §aVert Clair§r: Au moins deux membres de §aLa Coalition§r sont présents dans le rayon§r\n - §cRouge§r: Au moins 3 Membres de l'§cOrdre du Sablier§r sont présents dans le rayon.\nLes solitaires sont considérés comme membres de la Coalition.\n"
		+ "Vos effets diffère selon votre barre de vie,\n"
		+ " - 10 à 8 coeurs : Aucun effet \n - 8 à 6 coeurs : §bVitesse I§r\n - 6 à 4 coeurs : §bVitesse II§r\n - En dessous de 4 coeurs : §bVitesse III§r, §bNofall.§r\n";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		List<Player> viracochas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) viracochas.add(offlinePlayer.getPlayer());
		}

		sixHeartsSpeedAmplifier = manager.getMain().getConfig().getInt("timers.viracocha-six-hearts-speed-amplifier");
		fourHeartsSpeedAmplifier = manager.getMain().getConfig().getInt("timers.viracocha-four-hearts-speed-amplifier");
		heightHeartsSpeedAmplifier = manager.getMain().getConfig().getInt("timers.viracocha-height-hearts-speed-amplifier");

		arrowCooldownReductionOnHit = manager.getMain().getConfig().getInt("timers.viracocha-arrow-cooldown-reduction-on-hit");

		for(Player viracocha : viracochas) {
			viracocha.getInventory().addItem(new ItemManager.ItemBuilder(Material.ARROW, 64).build());
			viracocha.getInventory().addItem(new ItemManager.ItemBuilder(Material.BOW, 1).addSafeEnchant(Enchantment.ARROW_DAMAGE, 4).build());
		
			new BukkitRunnable() {
				
				@Override
				public void run() {
					
					if(viracocha.getHealth() < 4*2) {
						viracocha.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, fourHeartsSpeedAmplifier - 1), true);
					} else if(viracocha.getHealth() < 6*2) {
						viracocha.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, sixHeartsSpeedAmplifier  - 1), true);
					} else if(viracocha.getHealth() < 8*2) {
						viracocha.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, heightHeartsSpeedAmplifier-1), true);
					}
					
				}
			}.runTaskTimer(main, 0, 20l);
		}

		cooldownBetweenSpecialArrows = manager.getMain().getConfig().getInt("timers.viracocha-cooldown-between-special-arrows");
		fallDamageCancellation = manager.getMain().getConfig().getInt("timers.viracocha-fall-damage-cancellation");
	}
	
	@EventHandler
	public void onViracochaFallHard(EntityDamageEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getCause() != DamageCause.FALL) return;
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.VIRACOCHA)) return;
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.VIRACOCHA).contains(player.getUniqueId())) {
			if(player.getHealth() < fallDamageCancellation * 2) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onViracochaShootArrow(ProjectileLaunchEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if(!(source instanceof Player)) return;
		
		Player launcher = (Player)source;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.VIRACOCHA) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.VIRACOCHA).contains(launcher.getUniqueId())) {
			
			poisonousArrows.add(event.getEntity());
		}
	}
	
	@EventHandler
	public void onViracochaArrowHit(EntityDamageByEntityEvent event) {
		if(poisonousArrows.contains(event.getDamager()) && (event.getEntity() instanceof LivingEntity)) {
			if(lastArrowHit + cooldownUntilNextHit < System.currentTimeMillis()) {
				((LivingEntity)event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5*20, 0));
				cooldownUntilNextHit = cooldownBetweenSpecialArrows * 1000;
				lastArrowHit = System.currentTimeMillis();
			} else {
				cooldownUntilNextHit -= arrowCooldownReductionOnHit*1000;
			}
			numberOfHits++;
			if(numberOfHits % 10 == 0) {
				cooldownUntilNextHit = cooldownBetweenSpecialArrows * 1000;
			}
		}
	}

}
