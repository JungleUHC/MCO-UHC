package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.*;

import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.util.ItemManager;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Cauldron;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Hippolyte implements Role {

	private int remainingChoices = 2;
	private long lastChoiceTimestamp = 0;
	private int effectsDuration = 300;
	
	private boolean canTakeFallDamage = true;
	private Main main;
	
	public Hippolyte(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.HIPPOLYTE;
	}

	public int getRemainingChoices() {
		return remainingChoices;
	}

	public void setRemainingChoices(int remainingChoices) {
		this.remainingChoices = remainingChoices;
	}

	public long getLastChoiceTimestamp() {
		return lastChoiceTimestamp;
	}

	public void setLastChoiceTimestamp(int lastChoiceTimestamp) {
		this.lastChoiceTimestamp = lastChoiceTimestamp;
	}

	private ArrayList<Material> loadedMaterialList = new ArrayList<>();
	private HashMap<UUID, Integer> remainingAlchimieSacreeHitProtection = new HashMap<>();

	@Override
	public String getRelativeExplications() {
		return 
				  "Objectifs: Vous faites partie de l'§4Ordre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
				+ "Particularités de votre rôle : Vous possédez la capacité de choisir un effet parmi §cForce§r, §bSpeed§r, "
				+ "§5Résistance§r ainsi qu’§2Anti-Chute§r durant 5 minutes grâce à la commande §a/mco choix§r §o(cooldown de 10 minutes et 2 utilisations par parties)§r. "
				+ "Vous recevez également un chaudron que vous devrez remplir d’eau et jeter une liste d’item à l’intérieur, "
				+ "une fois cette tâche effectuée, vous obtiendrez un item nommé §6Alchimie Sacrée§r qui vous permettra "
				+ "d’absorber 10 coups mis par un adversaire, ceux-ci seront vus par les autres néanmoins vous n’en prendrez pas les dégâts.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		effectsDuration = main.getConfig().getInt("timers.hippolyte-effects-duration");

		List<Player> hyppolytes = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				hyppolytes.add(offlinePlayer.getPlayer());
			}
		}

		for(Player hypolyte : hyppolytes) {
			hypolyte.getInventory().addItem(Artifacts.RoleItems.CHAUDRON_HIPPOLYTE);
		}


		for(Object materialName : this.main.getConfig().getList("hippolyte-items-list")) {
			this.loadedMaterialList.add(Material.matchMaterial((String)materialName));
		}
	}

	@EventHandler
	public void onHippolytePlacesCauldron(BlockPlaceEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getBlockPlaced() == null) return;
		if(ItemManager.lightCompare(event.getItemInHand(), Artifacts.RoleItems.CHAUDRON_HIPPOLYTE)) {
			Main.logIfDebug("Hippolyte : Chaudron placé");

			ArrayList<Material> materialsToCollect = new ArrayList<Material>();

			while(materialsToCollect.size() < 3) {
				int nb = new Random().nextInt(this.loadedMaterialList.size());
				if(!materialsToCollect.contains(this.loadedMaterialList.get(nb))) materialsToCollect.add(this.loadedMaterialList.get(nb));
			}

			Main.logIfDebug("Hippolyte : Matériaux à collecter : ");
			for(Material m : materialsToCollect) Main.logIfDebug("Hippolyte : " + m.name().toLowerCase().replace("_", " "));

			StringJoiner joiner = new StringJoiner(", ");

			for(Material m : materialsToCollect) joiner.add(m.name().toLowerCase().replace("_", " "));

			event.getPlayer().sendMessage("Pour réussir votre expérience, il faudra ajouter au chaudron les ingrédients suivants : " + joiner.toString());
			new BukkitRunnable() {

				Location cauldronLoc = event.getBlockPlaced().getLocation().add(0.5d, 0.5d, 0.5d);

				@Override
				public void run() {
					if(cauldronLoc.getBlock().getType() != Material.CAULDRON) {
						Main.logIfDebug("Hippolyte : Chaudron cassé/bloc différent");
						event.getPlayer().getInventory().addItem(Artifacts.RoleItems.CHAUDRON_HIPPOLYTE);
						cancel();
					} else {
						if(!((Cauldron)cauldronLoc.getBlock().getState().getData()).isFull()) return;
						Collection<Entity> nearbyEntities = cauldronLoc.getWorld().getNearbyEntities(cauldronLoc, 0.7, 0.7, 0.7);
						if(nearbyEntities.size() == 0) return;
						ArrayList<Material> materialsToCollectCopy = (ArrayList<Material>) materialsToCollect;
						ArrayList<Entity> entitiesToRemove = new ArrayList<>();
						for(Entity entity : nearbyEntities) {
							if(entity instanceof Item && materialsToCollectCopy.contains(((Item)entity).getItemStack().getType())) {
								materialsToCollectCopy.remove(((Item)entity).getItemStack().getType());
								entitiesToRemove.add(entity);
								Main.logIfDebug("Hippolyte : Item trouvé : " + ((Item)entity).getItemStack().getType().name().toLowerCase().replace("_", " "));
							} else {
								Main.logIfDebug("Hippolyte : Item non trouvé : " + entity.getClass().getName());
							}
						}
						if(materialsToCollectCopy.size() == 0) {
							// all items are there
							event.getPlayer().getInventory().addItem(Artifacts.RoleItems.ALCHIMIE_SACREE);
							remainingAlchimieSacreeHitProtection.put(event.getPlayer().getUniqueId(), 10);
							for(Entity entity : entitiesToRemove) entity.remove();
							cancel();
						} else {
							Main.logIfDebug("Hippolyte : Items manquants : ");
							for(Material m : materialsToCollectCopy) Main.logIfDebug("Hippolyte : " + m.name().toLowerCase().replace("_", " "));
						}
					}
				}
			}.runTaskTimer(main, 0, 20);
		}
	}

	@EventHandler
	public void onHippolyteGetsHitByAnOtherPlayer(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().isPlaying(event.getEntity().getUniqueId()) && main.getCurrentGameManager().isPlaying(event.getDamager().getUniqueId())) {
			if(main.getCurrentGameManager().roleOfPlayer().get(event.getEntity().getUniqueId()) == RoleType.CINESUS) {
				if(this.remainingAlchimieSacreeHitProtection.containsKey(event.getEntity().getUniqueId()) && this.remainingAlchimieSacreeHitProtection.get(event.getEntity().getUniqueId()) > 0) {
					this.remainingAlchimieSacreeHitProtection.put(event.getEntity().getUniqueId(), this.remainingAlchimieSacreeHitProtection.get(event.getEntity().getUniqueId())-1);
					event.setDamage(0.0d);
					Player player = (Player) event.getEntity();

					if(this.remainingAlchimieSacreeHitProtection.get(event.getEntity().getUniqueId()) == 0) {
						for(int i = 0; i < player.getInventory().getSize(); i++) {
							if(player.getInventory().getItem(i) != null) {
								ItemStack item = player.getInventory().getItem(i);
								if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
									if(item.getItemMeta().getDisplayName().equals(Artifacts.RoleItems.ALCHIMIE_SACREE.getItemMeta().getDisplayName())) {
										player.getInventory().setItem(i, null);
									}
								}
							}
						}
						this.remainingAlchimieSacreeHitProtection.remove(event.getEntity().getUniqueId());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChoosesEffect(InventoryClickEvent event) {
		if(event.getInventory().getName().equalsIgnoreCase("§8Choix d'effet")) {
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().equals(event.getView().getTopInventory())) {
				if(event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_SOME || event.getAction() == InventoryAction.PICKUP_ALL) {
					event.setCancelled(true);
					switch (event.getSlot()) {
					case 1:
						event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, effectsDuration*20, 0), true);
						break;
					case 3:
						event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, effectsDuration*20, 0), true);
						break;
					case 5:
						event.getWhoClicked().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, effectsDuration*20, 0), true);
						break;
					case 7:
						canTakeFallDamage = false;
						new BukkitRunnable() {
							
							@Override
							public void run() {
								canTakeFallDamage = true;
							}
						}.runTaskLater(main, effectsDuration*20);
						break;

					default:
						return;
					}
					event.getWhoClicked().closeInventory();
					lastChoiceTimestamp = System.currentTimeMillis();
					remainingChoices--;
				}
			}
		}
	}
	
	@EventHandler
	public void onFallDamage(EntityDamageEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getCause() != DamageCause.FALL) return;
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.HIPPOLYTE)) return;
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.HIPPOLYTE).contains(player.getUniqueId())) {
			if(!canTakeFallDamage) event.setCancelled(true);
		}
	}


}
