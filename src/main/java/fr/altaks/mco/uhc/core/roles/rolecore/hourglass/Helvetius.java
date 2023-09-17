package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class Helvetius implements Role {

	private HashMap<UUID, Integer> progress = new HashMap<>();
	private boolean isAmbrosiusDead = false;
	private List<UUID> hasGivenDarkSun = new ArrayList<UUID>();
	private List<UUID> hasGotDarkSun = new ArrayList<UUID>();
	private Main main;
	private int swordHits = 0;
	
	public Helvetius(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.HELVETIUS;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez octroyer l’effet §bRésistance I§r si vous êtes à moins de §b10 blocs d'§cAmbrosius§r.\n"
	   + "Également, lorsque vous restez dans un rayon de §b10 blocs§r d'§cAmbrosius§r, votre barre de progression augmente de §b1% par minute§r ainsi que de §b30%§r lorsque §cAmbrosius§r effectue un meurtre dans un rayon de §b30 blocs§r autour de vous. Si jamais §aAmbrosius§r meurt avant la fin de la progression, elle passera de §b1%§r à §b2% par minute§r.\n"
	   + "Quand votre barre de progression atteint §b100 %§r, vous recevez un §lSoleil Noir§r, celui-ci avec son interface vous permet d'attribué à un joueur présent dans un rayon de §b20 blocs§r autour de vous. \n"
	   + "Le §lSoleil Noir§r confère un effet de §bFire Aspect§r et de §bFlame I§r au joueur à qui il est attribué. Enfin, tous les six coups d'épée le §lSoleil Noir§r permet de remplacer les sceaux d'eux par des seaux d'eau vide aux joueurs ayant subi les coups.\n"
	   + "Voici l’identité d’§cAmbrosius§r: %hourglass-ambrosius%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		List<Player> helvetiuss = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				helvetiuss.add(offlinePlayer.getPlayer());
			}
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player helvetius : helvetiuss) {
					boolean hasAmbrosiusNear = false;
					
					if(!isAmbrosiusDead) {
						for(Entity nearbyEntity : helvetius.getNearbyEntities(10, 10, 10)) {
							if(manager.isPlaying(nearbyEntity.getUniqueId())) {
								if(manager.roleOfPlayer().get(nearbyEntity.getUniqueId()) == RoleType.AMBROSIUS) {
									hasAmbrosiusNear = true;
									break;
								}
							}
						}
					}
					
					if(isAmbrosiusDead || hasAmbrosiusNear) {
						helvetius.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 0), true);
						
						if(!hasGivenDarkSun.contains(helvetius.getUniqueId())) {
							if(!progress.containsKey(helvetius.getUniqueId())) {
								progress.put(helvetius.getUniqueId(), 0);
							}
							if(progress.get(helvetius.getUniqueId()) < 60 * 100) {
								// increase
								progress.put(helvetius.getUniqueId(), progress.get(helvetius.getUniqueId()) + (isAmbrosiusDead ? 2 * 60 : 60));
								
								// actionbar msg
								int percentage = progress.get(helvetius.getUniqueId()) / 60;
								int remain = 100 - percentage;
								
								sendActionBarMessage(helvetius, createProgressBar(percentage, remain));
							} else {
								// finish
								helvetius.getInventory().addItem(Artifacts.RoleItems.SOLEIL_NOIR.clone());
								helvetius.sendMessage(Main.PREFIX + "§cVous venez de recevoir un objet particulier...");
								hasGivenDarkSun.add(helvetius.getUniqueId());
							}
						}
					}
				}
			}
		}.runTaskTimer(main, 0, 20);

	}
	
	@EventHandler
	public void onAmbrosiusDeath(EntityDeathEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(!(event.getEntity() instanceof Player)) return;
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS)) return;
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(event.getEntity().getUniqueId())){
			this.isAmbrosiusDead = true;
		}
	}
	
	@EventHandler
	public void onAmbrosiusKills(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
				
		Player damager = (Player)event.getDamager();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS)) return;
		if(event.getFinalDamage() < ((Player)event.getEntity()).getHealth()) return;
		
		// if the damager is ambrosius
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(damager.getUniqueId())) {
			for(Entity entity : damager.getNearbyEntities(30, 30, 30)) {
				if(this.progress.containsKey(entity.getUniqueId())) {
					this.progress.put(entity.getUniqueId(), this.progress.get(entity.getUniqueId()) + 30 * 60);
				}
			}
		}
	}
	
	@EventHandler
	public void onDarkSunHolderHit(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
		
		Player damager = (Player)event.getDamager();
		if(main.getCurrentGameManager().roleOfPlayer().get(damager.getUniqueId()) == RoleType.HELVETIUS) return;
		
		boolean holdsDarkSun = false;
		
		for(int slot = 0; slot < damager.getInventory().getSize(); slot++) {
			if(damager.getInventory().getItem(slot) != null) {
				if(ItemManager.lightCompare(damager.getInventory().getItem(slot), Artifacts.RoleItems.SOLEIL_NOIR)){
					holdsDarkSun = true;
					break;
				}
			}
		}
		
		if(holdsDarkSun) {
			event.getEntity().setFireTicks(80); // 4 sec fire
			if(event.getEntity() instanceof Player) {
				Player victim = (Player)event.getEntity();
				
				Material weapon = damager.getInventory().getItemInHand().getType();
				if(weapon.toString().toLowerCase().contains("sword")) {
					this.swordHits++;
					if(this.swordHits % 6 == 0) {
						if(victim.getInventory().contains(Material.WATER_BUCKET)) {
							victim.getInventory().remove(new ItemStack(Material.WATER_BUCKET));
							victim.getInventory().addItem(new ItemStack(Material.BUCKET));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDarkSunGive(InventoryClickEvent event) {
		if(event.getInventory().getName().equalsIgnoreCase("§8Joueurs proches")) {
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().equals(event.getView().getTopInventory())) {
				event.setCancelled(true);
				ItemStack current = event.getCurrentItem();
				if(current == null || current.getType() != Material.SKULL_ITEM) return;
				
				Player target = Bukkit.getPlayer(((SkullMeta)current.getItemMeta()).getOwner());
				if(target == null) return;
				
				for(int slot = 0; slot < event.getWhoClicked().getInventory().getSize(); slot++) {
					if(event.getWhoClicked().getInventory().getItem(slot) != null) {
						if(ItemManager.lightCompare(event.getWhoClicked().getInventory().getItem(slot), Artifacts.RoleItems.SOLEIL_NOIR)){
							event.getWhoClicked().getInventory().setItem(slot, null);
							break;
						}
					}
				}
				
				target.getInventory().addItem(Artifacts.RoleItems.SOLEIL_NOIR.clone());
				this.hasGotDarkSun.add(target.getUniqueId());
				event.getWhoClicked().closeInventory();
				event.getWhoClicked().sendMessage(Main.PREFIX + "§cVous avez donné votre Soleil Noir à " + target.getDisplayName());
			}
		}
	}
	
	@EventHandler
	public void onDarkSunUse(PlayerInteractEvent event) {
		if(!event.hasItem()) return;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.HELVETIUS) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.HELVETIUS).contains(event.getPlayer().getUniqueId())) {
			if(ItemManager.lightCompare(event.getItem(), Artifacts.RoleItems.SOLEIL_NOIR)) {
				event.setCancelled(true);
				
				List<Player> nearbyPlayers = new ArrayList<Player>();
				for(Entity nearbyEntity : event.getPlayer().getNearbyEntities(20, 20, 20)) {
					if(main.getCurrentGameManager().isPlaying(nearbyEntity.getUniqueId())){
						nearbyPlayers.add((Player)nearbyEntity);
					}
				}
				
				Inventory inv = Bukkit.createInventory(null, (int)(nearbyPlayers.size() / 9) * 9 + 9, "§8Joueurs proches");
				for(Player player : nearbyPlayers) {
					ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
					SkullMeta meta = (SkullMeta)skull.getItemMeta();
					meta.setLore(Arrays.asList("§l§6Cliquez pour donner votre Soleil Noir"));
					meta.setOwner(player.getName());
					meta.setDisplayName(meta.getOwner());
					skull.setItemMeta(meta);
					inv.addItem(skull);
				}
				
				event.getPlayer().openInventory(inv);
				
			}
		}
	}
	
	public void sendActionBarMessage(Player player, String message){
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte)2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
	
	public String createProgressBar(int percentage, int remain) {
		String total = "§7[§6§l";
		for(int begin = 0; begin < (percentage / 2); begin++) {
			total += "\u25AA";
		}
		total += "§7§l";
		for(int end = 0; end < (remain / 2); end++) {
			total += "\u25AA";
		}
		total += "§r§7]";
		return total;
	}


}
