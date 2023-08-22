package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.ItemManager;

public class Isabella implements Role {
	
	private HashMap<UUID, Long> fouetCooldown = new HashMap<>();
	private List<Short> artEsquiveUse = new ArrayList<>();
	private long lastArtEquiveUse = 0;
	private long gameStart = 0;
	private Main main;
	
	public Isabella(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.ISABELLA_LAGUERRA;
	}

	@Override
	public String getRelativeExplications() {
		return 
	    "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	  + "\u30FBParticularités de votre rôle:\n"
	  + "Dès l'annonce des rôles, vous recevez un §cFouet§r, ce dernier permet toutes les §b45 secondes§r, en un clic droit sur une cible de lui remplacer l'item qu'elle a en main par un item aléatoire dans son inventaire. "
	  + "Ainsi que l'§bArt de l'Esquive§r, qui lors de son activation vous rend invulnérable à tout type de dégâts pendant §bdix secondes§r, mais pendant ses 10 secondes, vous ne pourrez infliger de dégâts. Vous ne disposez seulement que d'une utilisation par épisode pour ce pouvoir.\n"
	  + "Si §cFernando Laguerra§r vient à mourir avant §aMendoza§r, vous devez alors gagner avec la §aCoalition§r.\n"
	  + "Si §cFernando Laguerra§r vient à mourir, votre §bFouet§r gagne 1 niveau de Tranchant.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		// TODO Auto-generated method stub
		
		List<Player> isabellas = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				isabellas.add(offlinePlayer.getPlayer());
			}
		}

		for(Player isabella : isabellas) {
			
			isabella.getInventory().addItem(Artifacts.RoleItems.FOUET);
			isabella.getInventory().addItem(Artifacts.RoleItems.ART_ESQUIVE);
			
		}
		gameStart = System.currentTimeMillis();
		
	}
	
	@EventHandler
	public void onArtEsquiveUse(PlayerInteractEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getPlayer().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getPlayer().getUniqueId()) == RoleType.ISABELLA_LAGUERRA) {
				if(ItemManager.lightCompare(event.getPlayer().getItemInHand(), Artifacts.RoleItems.ART_ESQUIVE)) {
					int episode = (int)(System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
					if(!this.artEsquiveUse.contains((short)episode)) {
						this.lastArtEquiveUse = System.currentTimeMillis();
						this.artEsquiveUse.add((short)episode);
					} else {
						event.getPlayer().sendMessage(Main.PREFIX + "§cVous vous en êtes déjà servi pendant cet épisode !");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onIsabellaTakeDamageWhileHavingEsquive(EntityDamageEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getEntity().getUniqueId()) == RoleType.ISABELLA_LAGUERRA) {
				if(this.lastArtEquiveUse + 10 * 1000 >= System.currentTimeMillis()) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onIsabellaDealDamageWhileHavingEsquive(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getDamager().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getDamager().getUniqueId()) == RoleType.ISABELLA_LAGUERRA) {
				if(this.lastArtEquiveUse + 10 * 1000 >= System.currentTimeMillis()) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onFouetUse(PlayerInteractAtEntityEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getPlayer().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getPlayer().getUniqueId()) == RoleType.ISABELLA_LAGUERRA) {
				if(ItemManager.lightCompare(event.getPlayer().getItemInHand(), Artifacts.RoleItems.FOUET)) {
					if(event.getRightClicked() instanceof Player) {
						if(!this.fouetCooldown.containsKey(event.getPlayer().getUniqueId()) || this.fouetCooldown.get(event.getPlayer().getUniqueId()) + 45 * 1000 < System.currentTimeMillis()) {
							Player target = (Player)event.getRightClicked();
							boolean hasItem[] = new boolean[target.getInventory().getSize()];
							boolean hasAtLeastOneItem = false;
							for(int slot = 0; slot < target.getInventory().getSize(); slot++) {
								if(target.getInventory().getItem(slot) != null) {
									hasItem[slot] = true;
									hasAtLeastOneItem = true;
								}
							}
							
							
							if(!hasAtLeastOneItem) return;
							
							int randomSlot = new Random().nextInt(hasItem.length);
							while(!hasItem[randomSlot]) randomSlot = new Random().nextInt(hasItem.length);
							
							// swap items
							ItemStack itemInHand = target.getInventory().getItemInHand().clone();
							ItemStack itemInSlot = target.getInventory().getItem(randomSlot).clone();
							
							target.setItemInHand(itemInSlot);
							target.getInventory().setItem(randomSlot, itemInHand);
							
							target.updateInventory();
							this.fouetCooldown.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
						} else {
							event.getPlayer().sendMessage(Main.PREFIX + "§cVous ne pouvez pas vous servir de votre fouet pour le moment !");
						}
					}
				}
			}
		}
	}
	
	@Override
	public GameTeam getTeamOfPlayer() {
		if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.FERNANDO_LAGUERRA) && !main.getCurrentGameManager().getDeadRoles().contains(RoleType.MENDOZA)) {
			return GameTeam.COALITION;
		}
		return Role.super.getTeamOfPlayer();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId()) && main.getCurrentGameManager().roleOfPlayer().get(event.getEntity().getUniqueId()) == RoleType.FERNANDO_LAGUERRA) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.FERNANDO_LAGUERRA)) {
						for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ISABELLA_LAGUERRA)) {
							if(main.getCurrentGameManager().isPlaying(id)) {
								Player isabella = Bukkit.getPlayer(id);
								// trouver le fouet dans l'inventaire
								
								for(int slot = 0; slot < isabella.getInventory().getSize(); slot++) {
									if(isabella.getInventory().getItem(slot) == null) continue;
									if(ItemManager.lightCompare(Artifacts.RoleItems.FOUET, isabella.getInventory().getItem(slot))) {
										// changer le niveau du fouet
										ItemMeta meta = isabella.getInventory().getItem(slot).getItemMeta();
										meta.removeEnchant(Enchantment.DAMAGE_ALL);
										meta.addEnchant(Enchantment.DAMAGE_ALL, 4, false);
										isabella.getInventory().getItem(slot).setItemMeta(meta);
									}
								}
							}
						}
					}
				}
			}.runTaskLater(main, 20);
		}
	}

}
