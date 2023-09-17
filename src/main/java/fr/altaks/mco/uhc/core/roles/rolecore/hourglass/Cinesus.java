package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;
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
import fr.altaks.mco.uhc.util.ItemManager;

public class Cinesus implements Role {

	private Main main;
	private long gameStart = 0;

	private int joiningAmbrosiusCooldown = 90;
	private int jouvenceHealthRecupCooldown = 10;

	public Cinesus(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.CINESUS;
	}

	@Override
	public String getRelativeExplications() {
		return 
			   "Objectifs: Vous faites partie de l'§4Ordre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
			   + "Particularités de votre rôle : Vous possédez l’effet §4Weakness 1§r et ce de manière permanente ainsi qu’un item nommé §bJouvence§r qui lors de son utilisation vous fera perdre votre Weakness pour vous donner l’effet §bSpeed 1§r ainsi que §cForce 1§r et ce pendant 5 minutes §o(utilisable 2 fois par parties)§r"
			   + " néanmoins, à la fin du temps imparti vous perdrez 2 coeurs permanents §o(récupérables toutes les 10mins).§r \n"
			   + "\n"
			   + "Néanmoins, à cause de vos différends avec Ambrosius, s’il est toujours vivant à 1h30 de jeu, vous devrez gagner avec les membres de la §aCoalition§r. \n"
			   + "\n"
			   + "Étant créateur de la Couronne Télékinésique, s’il arrive à récupérer celle-ci, il devra gagner §6tout seul§r. "
			   + "Vous perdrez votre effet de Weakness permanent et obtiendrez §5Résistance 1§r ainsi que §bSpeed 1§r, de plus, "
			   + "les membres de l’§4Ordre du Sablier§r ne seront pas au courant de ce changement de camp.\n"
			   + "";
	}
	
	private boolean hasGotTelekinetikCrown = false;
	private List<UUID> isProtectedFromWeakness = new ArrayList<>();

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		joiningAmbrosiusCooldown = main.getConfig().getInt("timers.cinesus-joining-ambrorius-cooldown");

		List<Player> cinesuses = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				cinesuses.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player cinesus : cinesuses) {
			cinesus.getInventory().addItem(Artifacts.RoleItems.JOUVENCE);
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(hasGotTelekinetikCrown) cancel();
				
				for(Player cinesus : cinesuses) {
					if(isProtectedFromWeakness.contains(cinesus.getUniqueId())) continue;
					cinesus.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,	1_000_000, 0), false);
				}
			}
		}.runTaskTimer(main, 0, 20);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(hasGotTelekinetikCrown) {
					for(Player cinesus : cinesuses) {
						cinesus.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,	            1_000_000, 0), false);
						cinesus.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,	1_000_000, 0), false);
					}
				}
			}
		}.runTaskTimer(main, 0, 20);
		
		gameStart = System.currentTimeMillis();

		jouvenceHealthRecupCooldown = this.main.getConfig().getInt("timers.cinesus-jouvence-health-recuperation-cooldown");
	}
	
	private HashMap<UUID, Integer> usesOfJouvence = new HashMap<>();
	
	@EventHandler
	public void onCinesusUsesJouvence(PlayerInteractEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem() == null) return;
		if(ItemManager.lightCompare(event.getItem(), Artifacts.RoleItems.JOUVENCE)) {
			event.getPlayer().setItemInHand(null);
			if(usesOfJouvence.containsKey(event.getPlayer().getUniqueId())) {
				usesOfJouvence.put(event.getPlayer().getUniqueId(), 2);
			} else {
				usesOfJouvence.put(event.getPlayer().getUniqueId(), 1);
			}
			
			this.isProtectedFromWeakness.add(event.getPlayer().getUniqueId());
			event.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
			
			new BukkitRunnable() {
				
				int timer = 5 * 60;
				
				@Override
				public void run() {
					if(timer == 0) {
						isProtectedFromWeakness.remove(event.getPlayer().getUniqueId());
						if(usesOfJouvence.get(event.getPlayer().getUniqueId()) <= 1) {
							event.getPlayer().getInventory().addItem(Artifacts.RoleItems.JOUVENCE);
						}
						event.getPlayer().setMaxHealth(event.getPlayer().getMaxHealth() - 4);
						new BukkitRunnable() {
							
							@Override
							public void run() {
								event.getPlayer().setMaxHealth(event.getPlayer().getMaxHealth() + 4);
							}
						}.runTaskLater(main, jouvenceHealthRecupCooldown * 60 * 20);
						cancel();
					}
					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 0), true);
					event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 25, 0), true);
					timer--;
				}
			}.runTaskTimer(main, 0, 20);
		}
	}
	
	@Override
	public GameTeam getTeamOfPlayer() {
		if(hasGotTelekinetikCrown) {
			return GameTeam.INDEPENDANT;
		} else if(((System.currentTimeMillis() - gameStart) / 1000 < joiningAmbrosiusCooldown * 60) && !main.getCurrentGameManager().getDeadRoles().contains(RoleType.AMBROSIUS) && main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS)) {
			return GameTeam.COALITION;
		}
		return Role.super.getTeamOfPlayer();
	}
	

	@EventHandler
	public void onCinesusGetsCrown(PlayerPickupItemEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem() == null) return;
		if(ItemManager.lightCompare(event.getItem().getItemStack(), Artifacts.CityItems.COURONNE_TELEK)) {
			this.hasGotTelekinetikCrown = true;
			isProtectedFromWeakness.add(event.getPlayer().getUniqueId());
		}
	}
}
