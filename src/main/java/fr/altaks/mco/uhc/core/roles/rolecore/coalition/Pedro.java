package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
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

public class Pedro implements Role {
	
	private Main main;
	private int minutesBetweenRoublardiseUsage = 5;
	
	public Pedro(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.PEDRO;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous vous verrez octroyer un effet de §9Vitesse 1§r ainsi qu’un §6Roublardise§r qui vous permet de voler une pomme dorée en frappant un joueur avec ce dernier (cooldown 5 minutes).\n"
	   + "Étant quelqu’un de très cupide vous êtes en capacité de connaître le nombre de pomme dorée d’un joueur via la commande §2\"/mco richesse\"§r. §oVous disposez de seulement cinq utilisations pour ce pouvoir.§r\n"
	   + "Voici l’identité de §aSancho§r: %coalition-sancho%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		List<Player> pedros = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) pedros.add(offlinePlayer.getPlayer());
		}
		
		for(Player pedro : pedros) {
			
			pedro.getInventory().addItem(Artifacts.RoleItems.ROUBLARDISE.clone());
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(!pedro.hasPotionEffect(PotionEffectType.SPEED))
						pedro.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1_000_000, 0));
					
				}
			}.runTaskTimer(manager.getMain(), 0, 20l);
			
			
		}
		minutesBetweenRoublardiseUsage = manager.getMain().getConfig().getInt("timers.pedro-time-between-roublardise-usages");
	}
	
	private long lastRoublardiseUsageTimeStamp = 0;
	
	@EventHandler
	public void onPedroHitWithRoublardise(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
				
		Player victim = (Player)event.getEntity();
		Player damager = (Player)event.getDamager();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.PEDRO)) return;
		
		// if the damager is a Pedro
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.PEDRO).contains(damager.getUniqueId())) {
		

			// if item in hand is Roublardise
			if(ItemManager.lightCompare(damager.getInventory().getItemInHand(), Artifacts.RoleItems.ROUBLARDISE)) {
				
				
				// if cooldown is done
				if(lastRoublardiseUsageTimeStamp + minutesBetweenRoublardiseUsage * 60 * 1000 > System.currentTimeMillis()) {

					damager.sendMessage(Main.PREFIX + "§cLa capacité de cet objet est toujours en récupération !");
					return;
				}
				
				if(victim.getInventory().contains(Material.GOLDEN_APPLE)) {
					
					
					damager.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
					
					int firstSlotThatHasGapples = victim.getInventory().first(Material.GOLDEN_APPLE);
					if(victim.getInventory().getItem(firstSlotThatHasGapples).getAmount() == 1) {

						victim.getInventory().setItem(firstSlotThatHasGapples, null);
					} else {
						ItemStack gappleStack = victim.getInventory().getItem(firstSlotThatHasGapples);
						gappleStack.setAmount(gappleStack.getAmount() - 1);
						victim.getInventory().setItem(firstSlotThatHasGapples, gappleStack);
					}
					

					lastRoublardiseUsageTimeStamp = System.currentTimeMillis();
				}
			}
			
		}
		
	}

}
