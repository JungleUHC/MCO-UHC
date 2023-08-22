package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.ItemManager;
import fr.altaks.mco.uhc.util.RolesUtil;

public class Athanaos implements Role {
	

	private Scoreboard lifeScoreboard;

	private HashMap<UUID, List<UUID>> hasMet = new HashMap<>();
	private HashMap<UUID, Long> hasDonePrier = new HashMap<>();
	
	private Main main;

	private int prierProtectionDuration = 60;
	
	public Athanaos(Main main) {
		this.main = main;
		

		this.lifeScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.lifeScoreboard.registerNewObjective("hp", "health");
		this.lifeScoreboard.getObjective("hp").setDisplaySlot(DisplaySlot.BELOW_NAME);
		this.lifeScoreboard.getObjective("hp").setDisplayName(" PV");
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.ATHANAOS;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous recevez un §bMasque Enchantée§r, celui-ci vous permet de voir la vie des joueurs lorsque vous le portez, ainsi qu'une §aPendule§r, celle-ci a la capacité de vous informer via un message dans le chat lorsque vous passez à moins de §b5 blocs§r d'un membre de L'§2Ordre du Sablier§r pour la première fois. \n"
	   + "Si jamais vous venez être dans un rayon de §b30 blocs§r autour du lieu du meurtre d'§aEsteban§r, vous écoperez d'un effet de §4Weakness I§r de façon permanente. \n"
	   + "Vous disposez également de la commande §6\"/mco prière\"§r, celle-ci vous permet de si vous venez à mourir dans la minute qui suit l'exécution de la commande, vous serait revive en échange de votre §bMasque Enchantée§r. §oVous disposez seulement d'une utilisation pour ce pouvoir§r\n"
	   + "Voici l’identité d'§cAmbrosius§r: %hourglass-ambrosius%\n"
	   + "Voici l’identité de §cFernando Laguerra§r: %hourglass-fernando%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> athanaoses = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) athanaoses.add(offlinePlayer.getPlayer());
		}

		prierProtectionDuration = main.getConfig().getInt("timers.athanaos-prier-protection-duration");
		
		ArrayList<String> allPlayers = new ArrayList<>();
		ArrayList<String> estebans = new ArrayList<>();
		
		if(manager.isRoleAttributed(RoleType.ESTEBAN)) {
			for(UUID player : manager.roleOfPlayer().keySet()) {
				if(manager.isPlaying(player)) {
					if(manager.roleOfPlayer().get(player) == RoleType.ESTEBAN) {
						estebans.add(Bukkit.getOfflinePlayer(player).getName());
					} else allPlayers.add(Bukkit.getOfflinePlayer(player).getName());
				}
			}
		}
		
		for(Player athanaos : athanaoses) {
			athanaos.getInventory().addItem(Artifacts.RoleStuffs.MASQUE_ENCHANTE);
			athanaos.getInventory().addItem(Artifacts.RoleItems.PENDULE);
			
			if(manager.isRoleAttributed(RoleType.ESTEBAN)) {
				athanaos.sendMessage(Main.PREFIX + "§6Votre liste : " + RolesUtil.getListOfPlayerContaining(estebans, allPlayers, 2));
			}
			
			hasMet.put(athanaos.getUniqueId(), new ArrayList<>());
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					boolean hasPendule = false;
					for(int slot = 0; slot < athanaos.getInventory().getSize(); slot++) {
						if(athanaos.getInventory().getItem(slot) == null) continue;
						if(ItemManager.compare(athanaos.getInventory().getItem(slot), Artifacts.RoleItems.PENDULE)) {
							hasPendule = true;
							break;
						}
					}
					if(hasPendule) {
						for(Entity entity : athanaos.getNearbyEntities(5, 5, 5)) {
							if((entity instanceof Player) && (main.getCurrentGameManager().getTeamOfPlayer(((Player)entity).getUniqueId()) == GameTeam.COALITION) && !hasMet.get(athanaos.getUniqueId()).contains(entity.getUniqueId())) {
								hasMet.get(athanaos.getUniqueId()).add(entity.getUniqueId());
								athanaos.sendMessage(Main.PREFIX + "§cVous venez de croiser un membre de la §cCoalition ...");
							}
						}
					}
				}
			}.runTaskTimer(main, 0, 20l);
		}
	}
	
	private InventoryAction[] pickupActions = {
			InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_HALF,
	}, placeActions = {
			InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME
	};
	
	
	@EventHandler
	public void onPlayerEquipOrDesequipHelmet(InventoryClickEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getRawSlot() == 5) { // if its the helmet slot
			if(main.getCurrentGameManager().isRoleAttributed(RoleType.ATHANAOS) && main.getCurrentGameManager().isPlaying(event.getWhoClicked().getUniqueId())) {
				if(main.getCurrentGameManager().roleOfPlayer().get(event.getWhoClicked().getUniqueId()) == RoleType.ATHANAOS) {

					Player player = (Player)event.getWhoClicked();
					
					if(Arrays.asList(pickupActions).contains(event.getAction())) {
						if(ItemManager.lightCompare(event.getCurrentItem(), Artifacts.RoleStuffs.MASQUE_ENCHANTE)) {

							// retire son casque
							player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

						}
					} else if(Arrays.asList(placeActions).contains(event.getAction())) {
						if(ItemManager.lightCompare(event.getCursor(), Artifacts.RoleStuffs.MASQUE_ENCHANTE)) {

							// place son casque
							player.setScoreboard(lifeScoreboard);
						}
					}
					
				}
			}
		}
	}
	
	public HashMap<UUID, Long> hasDonePrier(){
		return this.hasDonePrier;
	}
	
	@Override
	public void onPlayerDeath(UUID id, GameManager manager) {
		if(Bukkit.getOfflinePlayer(id).isOnline()) {
			if(this.hasDonePrier.containsKey(id)) {
				if(this.hasDonePrier.get(id) + prierProtectionDuration * 1000 > System.currentTimeMillis()) {
					Player player = Bukkit.getPlayer(id);
					Location loc = player.getLocation().clone();
					player.spigot().respawn();
					player.teleport(loc);
					player.sendMessage(Main.PREFIX + "§6Les dieux ont veillé sur vous");
					return;
				}
			}
		}
		Role.super.onPlayerDeath(id, manager);
	}

}
