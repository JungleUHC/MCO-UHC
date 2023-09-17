package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Gaspard implements Role {
	
	private Main main;
	private List<UUID> hasFoundGomezOrPizzaro = new ArrayList<UUID>();
	
	public Gaspard(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.GASPARD;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous recevez une épée §bSharpness III§r en diamant, vous vous voyez également octroyer l'effet §cForce I§r de façon permanente.\n"
	   + "Si vous tuez §aEsteban§r, §aTao§r et §aZia§r, vous obtenez un §bcoeur supplémentaire§r par élimination.\n"
	   + "Si vous croisez §cGomez§r en premier, vous obtenez un livre enchanté §bDepth Strider II§r accompagné de son identité.\n"
	   + "Si vous croisez §cPizarro§r en premier, vous obtenez un livre enchanté §2Fire Aspect I§r accompagné de son identité.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		List<Player> gaspards = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				gaspards.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player gaspard : gaspards) {
			gaspard.getInventory().addItem(new ItemManager.ItemBuilder(Material.DIAMOND_SWORD, 1).addSafeEnchant(Enchantment.DAMAGE_ALL, 3).build());
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player gaspard : gaspards) {
					gaspard.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1_000_000, 0), true);
					
					if(!hasFoundGomezOrPizzaro.contains(gaspard.getUniqueId())){
						List<Entity> entities = gaspard.getNearbyEntities(10, 10, 10);
						for(Entity entity : entities) {
							if(!(entity instanceof Player)) continue;
							if(!(manager.roleOfPlayer().containsKey(entity.getUniqueId()))) return;
							RoleType role = manager.roleOfPlayer().get(entity.getUniqueId());

							if(role == RoleType.GOMEZ || role == RoleType.PIZARRO) {
								if(role == RoleType.GOMEZ) {
									gaspard.sendMessage(Main.PREFIX + "§cVous venez de croiser " + ((Player)entity).getDisplayName() + " (Gomez), il vous offre quelque chose");
									gaspard.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.ENCHANTED_BOOK, 1, "§6Cadeau de Gomez").addEnchant(Enchantment.DEPTH_STRIDER, 2, true).build());
								} else {
									gaspard.sendMessage(Main.PREFIX + "§cVous venez de croiser " + ((Player)entity).getDisplayName() + " (Pizarro), il vous offre quelque chose");
									gaspard.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.ENCHANTED_BOOK, 1, "§6Cadeau de Pizarro").addEnchant(Enchantment.FIRE_ASPECT, 1, false).build());
								}
								hasFoundGomezOrPizzaro.add(gaspard.getUniqueId());
							}
						}
					}
				}
			}
					
		}.runTaskTimer(manager.getMain(), 0, 20);
	}
	
	@EventHandler
	public void onGaspardKills(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
				
		Player damager = (Player)event.getDamager();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.GASPARD)) return;
		if(event.getFinalDamage() < ((Player)event.getEntity()).getHealth()) return;
		
		// if the damager is gaspard
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.GASPARD).contains(damager.getUniqueId())) {
			Player victim = (Player)event.getEntity();
			if(main.getCurrentGameManager().isPlaying(victim.getUniqueId())) {
				RoleType roleOfVictim = main.getCurrentGameManager().roleOfPlayer().get(victim.getUniqueId());
				if(roleOfVictim == RoleType.ESTEBAN || roleOfVictim == RoleType.TAO || roleOfVictim == RoleType.ZIA) {
					damager.setMaxHealth(damager.getMaxHealth() + 2);
				}
			}
		}
	}

}
