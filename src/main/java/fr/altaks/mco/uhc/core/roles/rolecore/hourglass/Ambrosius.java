package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class Ambrosius implements Role {
	
	private Main main;
	private int 
		pyramide_mu_usages = 1,
		poche_artifacts_openings = 1;
	
	private HashMap<ItemStack, Boolean> hasAlreadyBeenPicked = new HashMap<>();
	private Scoreboard lifeScoreboard;
	public long gameStart = 0;
	private int matrixCooldown = 60;

	private int pyramideMuDuration = 60;
	
	public Ambrosius(Main main) {
		this.main = main;
		
		hasAlreadyBeenPicked.put(Artifacts.RoleItems.PYRAMIDE_MU, false);
		hasAlreadyBeenPicked.put(Artifacts.CityItems.COURONNE_TELEK, false);
		hasAlreadyBeenPicked.put(Artifacts.RoleItems.LUMINARION, false);
		hasAlreadyBeenPicked.put(Artifacts.RoleItems.MATR_ORICHALQUE, false);
		
		this.lifeScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.lifeScoreboard.registerNewObjective("hp", "health");
		this.lifeScoreboard.getObjective("hp").setDisplaySlot(DisplaySlot.BELOW_NAME);
		this.lifeScoreboard.getObjective("hp").setDisplayName(" PV");
	}
	
	public HashMap<ItemStack, Boolean> getHasAlreadyBeenPicked(){
		return this.hasAlreadyBeenPicked;
	}

	@Override
	public RoleType getRole() {
		return RoleType.AMBROSIUS;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§2Ordre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous pourrez choisir un artefact avec la commande §2\"/mco artefact\"§r\n"
	   + "Chaque artefact est à usage unique, après avoir choisi votre premier §bartefact§r, pour chaque kill effectué, il vous sera possible de choisir un nouvel artefact.\n"
	   + "Vous possédez la commande §b\"/mco info pseudo\"§r qui vous permet de connaître le camp du joueur visé. (Les rôles solitaires apparaissent comme des membres de la coalition)\n"
	   + "Liste des Artefacts : \n"
	   + "- §cPyramide de Mu§r : Lors de son activation le joueur peut voir la vie des joueurs ainsi que leur aura pendant §bune minute§r (Une aura de particules rouge autour des membres de l’ordre et une aura de particules bleues autour des membres de la coalition.(Les rôles solitaires apparaissent comme des membres de la coalition).\n"
	   + "- §cUne Couronne télékinétique§r : Le joueur visé §blévite§r pendant §bcinq secondes§r, il sera ensuite §bimmobilisé§r pendant §bcinq secondes§r.\n"
	   + "- §cLuminarion§r : Un bloc que si vous le posez dans de l’eau remplace toutes les sources d’eau coller à ce dernier par de la lave.\n"
	   + "- §cMatrice d'Orichalque§r : Une fois l’artefact reçu, son utilisateur recevra un lingot d’or toutes les minutes.\n"
	   + "Dès §b30 minutes de jeu§r, et ce toutes les §b10 minutes§r, vous pouvez convoquer §b3 membres aléatoires§r de §cl'Ordre du Sablier§r dans sa §4Nef§r pendant §b1 minute§r, "
	   + "les joueurs en questions seront informés qu'il seront téléportés §b30 secondes avant§r la téléportation.\n"
	   + "Pour finir, vous pourrez sacrifier un Artefact avec la commande §2\"/mco sacrifice\"§r en tenant l’artefact en main ce qui vous conférera en échange un §6Médaillon du Soleil§r complet.\n"
	   + "\n"
	   + "Voici l’identité de §cAthanaos§r: %hourglass-athanaos%\n"
	   + "Voici l’identité de §cFernando Laguerra§r: %hourglass-fernando%\n";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		// Envoyer un message du style : (dont celui de TAO)
		// Liste des pseudonymes : %pseudo1% pseudo2% pseudo3%

		matrixCooldown = main.getConfig().getInt("timers.ambrosius-matrix-cooldown");

		List<Player> ambrosiuses = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				ambrosiuses.add(offlinePlayer.getPlayer());
			}
		}
		
		ArrayList<String> allPlayers = new ArrayList<>();
		ArrayList<String> taos = new ArrayList<>();
		
		if(manager.isRoleAttributed(RoleType.TAO)) {
			for(UUID player : manager.roleOfPlayer().keySet()) {
				if(manager.isPlaying(player)) {
					if(manager.roleOfPlayer().get(player) == RoleType.TAO) {
						taos.add(Bukkit.getOfflinePlayer(player).getName());
					} else allPlayers.add(Bukkit.getOfflinePlayer(player).getName());
				}
			}
		}
		
		for(Player ambrosius : ambrosiuses) {
			if(manager.isRoleAttributed(RoleType.TAO)) {
				ambrosius.sendMessage(Main.PREFIX + "§6Voici votre liste " + RolesUtil.getListOfPlayerContaining(taos, allPlayers, 2));
			}
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					boolean hasMatrix = false;
					for(int slot = 0; slot < ambrosius.getInventory().getSize(); slot++) {
						if(ambrosius.getInventory().getItem(slot) == null) continue;
						if(ItemManager.compare(ambrosius.getInventory().getItem(slot), Artifacts.RoleItems.MATR_ORICHALQUE)) {
							hasMatrix = true;
							break;
						}
					}
					if(hasMatrix) {
						ambrosius.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
					}
					
				}
			}.runTaskTimer(main, 0, 60 * 20l);
		}
		
		gameStart = System.currentTimeMillis();

		pyramideMuDuration = main.getConfig().getInt("timers.ambrosius-pyramide-mu-duration");

	}
	
	@EventHandler
	public void onPyramideMuUse(PlayerInteractEvent event) {
		if(!event.hasItem()) return;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(event.getPlayer().getUniqueId())) {
			if(ItemManager.lightCompare(event.getItem(), Artifacts.RoleItems.PYRAMIDE_MU)) {
				if(pyramide_mu_usages != 0) {

					List<Player> nearbyPlayers = new ArrayList<Player>();
					for(UUID id : main.getCurrentGameManager().roleOfPlayer().keySet()) {
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
						if(offlinePlayer.isOnline() && !main.getCurrentGameManager().hasDied(id)) {
							nearbyPlayers.add(offlinePlayer.getPlayer());
						}
					}
					
					for(Player player : nearbyPlayers) {
						new BukkitRunnable() {
							
							int timer = pyramideMuDuration, // 60 sec
								points = 40;
							
							double radius = 0.5;
							
							Location origin = player.getLocation();
							
							@Override
							public void run() {
								if(timer == 0) {
									event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
									cancel();
								}
								
								origin = player.getLocation();
								for (int i = 0; i < points; i++) {
								    double angle = 2 * Math.PI * i / points;
								    Location point = origin.clone().add(radius * Math.sin(angle), 1d, radius * Math.cos(angle));
								    if(main.getCurrentGameManager().getTeamOfPlayer(player.getUniqueId()) == GameTeam.HOURGLASS) {
								    	sendParticle(event.getPlayer(), point, EnumParticle.REDSTONE, 216, 51, 15);
								    } else {
								    	sendParticle(event.getPlayer(), point, EnumParticle.REDSTONE, 21, 232, 222);
								    }
								}
								timer--;
								
							}
						}.runTaskTimer(main, 0, 20l);
					}
					
					event.getPlayer().setScoreboard(this.lifeScoreboard);
					
					pyramide_mu_usages--;
				} else {
					event.getPlayer().sendMessage(Main.PREFIX + "§cVous avez trop utilisé votre Pyramide !");
				}
			}
		}
		
	}
	@EventHandler
	public void onLuminarionUse(PlayerInteractEvent event) {
		if(event.getPlayer().getItemInHand() == null) return;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(event.getPlayer().getUniqueId())) {
			if(ItemManager.lightCompare(event.getPlayer().getItemInHand(), Artifacts.RoleItems.LUMINARION)) {
				
				Set<Material> NullSet = null;
				Block targetblock = event.getPlayer().getTargetBlock(NullSet, 3);
				if(targetblock == null || targetblock.getType() == Material.AIR) return;
				
				for(BlockFace direction : BlockFace.values()) {
					Material blocktype = targetblock.getRelative(direction).getType();
					if(blocktype == Material.WATER || blocktype == Material.STATIONARY_WATER) {
						targetblock.getRelative(direction).setType(Material.LAVA);
					}
				}
			}
		}
	}
	
	public void sendParticle(Player player, Location loc, EnumParticle p, float red, float green, float blue) {
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(p, false, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), red/255, green/255, blue/255, 1, 0);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}
	
	public boolean canOpenPocheArtefact() {
		return this.poche_artifacts_openings > 0;
	}
	
	@EventHandler
	public void onPocheArtefactInventoryClickEvent(InventoryClickEvent event) {
		if(event.getInventory().getName().equalsIgnoreCase("§8Poche d'artéfacts")) {
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().equals(event.getView().getTopInventory())) {
				if(event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_SOME || event.getAction() == InventoryAction.PICKUP_ALL) {
					if(event.getCurrentItem() != null) {
						ItemStack chosenItem = event.getCurrentItem();
						this.hasAlreadyBeenPicked.put(chosenItem, true);
						
						event.setCancelled(true);
						event.getWhoClicked().closeInventory();
						event.getWhoClicked().getInventory().addItem(chosenItem);
						this.poche_artifacts_openings--;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onAmbrosiusKillsPlayer(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
				
		Player damager = (Player)event.getDamager();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS)) return;
		if(event.getFinalDamage() < ((Player)event.getEntity()).getHealth()) return;
		
		// if the damager is ambrosius
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(damager.getUniqueId())) {
			this.poche_artifacts_openings++;
		}
	}

}
