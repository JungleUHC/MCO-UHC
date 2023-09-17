package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;
import fr.altaks.mco.uhc.util.RayTraceUtil;
import fr.altaks.mco.uhc.util.RolesUtil;

public class Zia implements Role {
	
	private boolean canUseMCOClaim = false;
	private HashMap<UUID, Long> telekinesisCooldowns = new HashMap<>();

	private int weaknessAmplifier = 1;
	private int medaillonAssemblyDuration = 300;

	private Main main;
	
	public Zia(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.ZIA;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous obtenez l'effet §4Weakness 1§r tant que §cPizarro§r est toujours en vie. Vous possédez également un Médaillon du Soleil : \n"
	   + " Pour utiliser ce dernier, vous devrez assembler les 2 §6Médaillons du Soleil§r en restant à côté de §aEsteban§r pendant §b5 minutes§r à moins de §b20 blocs.§r Une fois assemblé, vous obtiendrez une version complète du médaillon qui vous permettra de tracker les §6Cités d’or§r.\n"
	   + "Dès §b30 minutes de jeu§r, vous recevrez une liste contenant §b5 pseudonymes§r de joueurs présents dans la partie, dont celui de §cPizarro§r.\n"
	   + "Si jamais §cPizarro§r vient à mourir dans un rayon de §b30 blocs§r de vous alors vous débloquerez un item renommé §2Telekinesis§r grâce à la commande §2\"/mco claim\"§r\n"
	   + "Cet item vous permettra en visant un joueur dans un rayon de §b20 blocs§r de le repousser d’une dizaine de blocs. §o(cooldown de 5 minutes)§r.";
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		weaknessAmplifier = manager.getMain().getConfig().getInt("timers.zia-weakness-amplifier");
		medaillonAssemblyDuration = manager.getMain().getConfig().getInt("timers.zia-medaillon-assembly-duration");

		List<Player> zias = new ArrayList<>();
		
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				zias.add(offlinePlayer.getPlayer());
			}
		}
		
		ArrayList<String> allPlayers = new ArrayList<>();
		ArrayList<String> pizarros = new ArrayList<>();
		
		if(manager.isRoleAttributed(RoleType.PIZARRO)) {
			for(UUID player : manager.roleOfPlayer().keySet()) {
				if(manager.isPlaying(player)) {
					if(manager.roleOfPlayer().get(player) == RoleType.PIZARRO) {
						pizarros.add(Bukkit.getOfflinePlayer(player).getName());
					} else allPlayers.add(Bukkit.getOfflinePlayer(player).getName());
				}
			}
		}
		
		
		for(Player zia : zias) {
			
			zia.getInventory().addItem(Artifacts.MEDAILLON_SOLAIRE_INCOMPLET);

			if(manager.isRoleAttributed(RoleType.PIZARRO)) {
				zia.sendMessage("§6Voici votre liste : " + RolesUtil.getListOfPlayerContaining(pizarros, allPlayers, 1));
				zia.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1_000_000, 0));
			}
			
			// check if Esteban is near Zia
			if(manager.isRoleAttributed(RoleType.ESTEBAN)) {
				
				ArrayList<Player> estebans = new ArrayList<>();
				for(UUID id : manager.getPlayersThatOwnsRole(RoleType.ESTEBAN)) {
					Player player = Bukkit.getPlayer(id);
					if(player != null) estebans.add(player);
				}
				
				Bukkit.getScheduler().runTaskTimerAsynchronously(manager.getMain(), new BukkitRunnable() {
					
					
					ArrayList<UUID> alreadyAssembling = new ArrayList<>();
					
					@Override
					public void run() {
						
						for(Player esteban : estebans) {
							
							if(esteban.getLocation().distance(zia.getLocation()) <= 20.0d && !alreadyAssembling.contains(esteban.getUniqueId()) && !alreadyAssembling.contains(zia.getUniqueId())) {
								
								// send message to both players
								esteban.sendMessage(Main.PREFIX + "§6Vous commencez à assembler les deux médaillons");
								zia.sendMessage(Main.PREFIX + "§6Vous commencez à assembler les deux médaillons");
								
								alreadyAssembling.add(esteban.getUniqueId());
								alreadyAssembling.add(zia.getUniqueId());
								
								// condition acquired to start the combination
								new BukkitRunnable() {
									
									//          5min * 60sec
									int timer = medaillonAssemblyDuration;
									
									@Override
									public void run() {
										if(timer >= 0) {
											if(esteban.getLocation().distance(zia.getLocation()) > 20) {
												esteban.sendMessage(Main.PREFIX + "§cVous arrêtez d'assembler les deux médaillons");
												zia.sendMessage(Main.PREFIX + "§cVous arrêtez d'assembler les deux médaillons");
												
												alreadyAssembling.remove(esteban.getUniqueId());
												alreadyAssembling.remove(zia.getUniqueId());
												cancel();
											} else {
												timer--;
											}
										} else {
											// assemblage réussi : 
											
											esteban.getInventory().remove(Artifacts.MEDAILLON_SOLAIRE_INCOMPLET);
											zia.getInventory().remove(Artifacts.MEDAILLON_SOLAIRE_INCOMPLET);
											
											esteban.sendMessage(Main.PREFIX + "§6Vous avez assemblé les deux médaillons !");
											zia.sendMessage(Main.PREFIX + "§6Vous avez assemblé les deux médaillons !");
											
											esteban.getInventory().addItem(Artifacts.MEDAILLON_SOLAIRE_COMPLETE);
											cancel();
										}
									}
								}.runTaskTimer(manager.getMain(), 0, 20l);
							}
							
						}
						
					}
					
				}, 0, 20l);
			}
		}

	}
	
	@EventHandler
	public void onTelekinesisUse(PlayerInteractEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem() == null) return;
		if(ItemManager.compare(event.getItem(), Artifacts.RoleItems.TELEKINESIS)) {
			Player zia = event.getPlayer();
			
			Player targetedPlayer = RayTraceUtil.getTargetPlayer(zia);
			if(!telekinesisCooldowns.containsKey(zia.getUniqueId()) || telekinesisCooldowns.get(zia.getUniqueId()) + (5 * 60 * 1000) <= System.currentTimeMillis()) {
				if(targetedPlayer != null && (targetedPlayer.getLocation().distance(zia.getLocation()) <= 20.0d)) {
					
					targetedPlayer.setVelocity(zia.getLocation().getDirection().multiply(2).setY(0.5));
					
					telekinesisCooldowns.put(zia.getUniqueId(), System.currentTimeMillis());
				}
			} else {
				zia.sendMessage(Main.PREFIX + "§cVous devez attendre 5 minutes entre chaque utilisation !");
			}
		}
	}
	
	public boolean canUseMCOClaim() {
		return this.canUseMCOClaim;
	}
	
	public void setCanUseMCOClaim(boolean newValue) {
		this.canUseMCOClaim = newValue;
	}

}
