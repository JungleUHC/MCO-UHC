package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Esteban implements Role {

	private GameManager manager;

	private long lastAnalyseUsage = 0;
	private int announceMetAthanaos = 60;

	private HashMap<Player, Pair<Player, Long>> meetingTimestamp = new HashMap<>();
	
	@Override
	public RoleType getRole() {
		return RoleType.ESTEBAN;
	}

	@Override
	public String getRelativeExplications() {
		return 
	   "\u30FBObjectifs: Vous faites partie de la §aCoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier.§r\n"
	 + "\u30FBParticularités de votre rôle: Dès l’annonce des rôles, vous recevez une Dague possédant Tranchant 3 ainsi qu’un §6Médaillon du Soleil§r: \n"
	 + "Pour utiliser ce dernier vous devrez assembler les 2 §6Médaillons du Soleil§r en restant à côté de §aZia§r pendant §b5 minutes§r à moins de §b20 blocs§r.\n"
	 + "Une fois assemblé vous obtiendrez une version complète du médaillon qui vous permettra de tracker les cités d’or de vos choix.\n"
	 + "Vous possédez la commande §2“/mco coalition”§r qui vous permet de savoir le nombre de membres de la coalition présent dans un rayon de §a30 blocs§r utilisable §b3§r fois dans la partie. \n"
	 + "De plus §b1 minute§r après avoir croisé §aAthanatos§r pour la première fois vous recevez un message. §b“J’ai une sensation de déjà vue”§r."
	 + "Vous possédez la commande §2/mco analyse§r vous permettant d'avoir un pourcentage de chance de déduire le camp d'un des joueurs de la partie, "
	 + "cette capacité à 75% de chance de réussite et cela affichera le véritable camp du joueur, néanmoins, "
	 + "il possède 25% de chance de se tromper dans son analyse et donc le camp du joueur sera aléatoire."
	 + "§o(Cette capacité n’est utilisable uniquement cinq fois par partie à une intervalle de 5 minutes)§r";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		this.manager = manager;

		List<Player> estebans = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) estebans.add(offlinePlayer.getPlayer());
		}

		announceMetAthanaos = manager.getMain().getConfig().getInt("timers.esteban-announce-met-athanaos");
		
		for(Player esteban : estebans) {
			
			esteban.getInventory().addItem(Artifacts.RoleStuffs.DAGUE);
			esteban.getInventory().addItem(Artifacts.MEDAILLON_SOLAIRE_INCOMPLET);
		
			if(manager.isRoleAttributed(RoleType.ATHANAOS)) {

				List<Player> athanaoses = new ArrayList<Player>();
				for(UUID id : manager.getPlayersThatOwnsRole(RoleType.ATHANAOS)) {
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
					if(offlinePlayer.isOnline()) athanaoses.add(offlinePlayer.getPlayer());
				}
				
				new BukkitRunnable() {
					
					List<Player> alreadyMetAthanaos = new ArrayList<Player>();
					
					@Override
					public void run() {
						
						// if athanaos has never been met
						if(!alreadyMetAthanaos.contains(esteban)) {
							for(Player athanaos : athanaoses) {
								if(athanaos.getLocation().distance(esteban.getLocation()) <= 5) {
									// players have met themselves
									alreadyMetAthanaos.add(esteban);
									new BukkitRunnable() {
										@Override
										public void run() {
											esteban.sendMessage(Main.PREFIX + "§bEsteban \u00BB J'ai une sensation de déjà vu...");
										}
									}.runTaskLater(manager.getMain(), announceMetAthanaos * 20l);


									meetingTimestamp.put(esteban, Pair.of(athanaos, System.currentTimeMillis()));
									if(!meetingTimestamp.containsKey(athanaos)) meetingTimestamp.put(athanaos, Pair.of(esteban, System.currentTimeMillis()));
									break;
								}
							}
						}
						
					}
				}.runTaskTimerAsynchronously(manager.getMain(), 0, 20l);
				
			}
			
		}
	}

	@EventHandler
	public void onPlayerTakesDamage(EntityDamageEvent event){
		// make sure entity is player
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		// if the player is in the hashmap, it means he has met athanaos or esteban and has a damage resistance of 10% + (2% per 5min since the meeting)
		if(meetingTimestamp.containsKey(player) && this.manager.isPlaying(meetingTimestamp.get(player).getLeft().getUniqueId())) {
			long timeSinceMeeting = System.currentTimeMillis() - meetingTimestamp.get(player).getRight();
			double damageResistance = 0.1 + ((double)timeSinceMeeting / 5d * 60d * 1000d) * 0.02;
			event.setDamage(event.getDamage() * (1 - damageResistance));
		}
	}
	
	@Override
	public void onPlayerDeath(UUID id, GameManager manager) {
		Role.super.onPlayerDeath(id, manager);
		if(!Bukkit.getOfflinePlayer(id).isOnline()) return;
		if(manager.isRoleAttributed(RoleType.ATHANAOS)) {
			
			List<Player> athanaoses = new ArrayList<>();
			for(UUID uuid : manager.getPlayersThatOwnsRole(RoleType.ATHANAOS)) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
				if(offlinePlayer.isOnline() && manager.isPlaying(uuid)) athanaoses.add(offlinePlayer.getPlayer());
			}
			
			for(Player athanaos : athanaoses) {
				if(Bukkit.getPlayer(id).getLocation().distance(athanaos.getLocation()) <= 30) {
					new BukkitRunnable() {
						@Override
						public void run() {
							athanaos.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1_000_000, 0), true);
						}
					}.runTaskTimer(manager.getMain(), 0, 20);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onAthanaosDeath(EntityDamageEvent event){
		if(manager == null) return;
		// make sure entity is player
		if(!(event.getEntity() instanceof Player)) return;
		// make sure the player was in the game according to the manager, and it was athanaos
		if(!manager.isPlaying(event.getEntity().getUniqueId()) || !manager.isRoleAttributed(RoleType.ATHANAOS)) return;
		// make sure the player was athanaos
		if(!manager.getPlayersThatOwnsRole(RoleType.ATHANAOS).contains(event.getEntity().getUniqueId())) return;
		// make sure the final damages were fatal
		if(meetingTimestamp.containsKey(event.getEntity()) && ((Player)event.getEntity()).getHealth() <= event.getFinalDamage()){
			// if it's athanaos that died, make the corresponding esteban lose 2 max hearts for 10 mins
			// make sure player that died was playing Athanaos
			Player esteban = meetingTimestamp.get(event.getEntity()).getLeft();
			esteban.setMaxHealth(esteban.getMaxHealth() - 4);
			new BukkitRunnable() {
				@Override
				public void run() {
					esteban.setMaxHealth(esteban.getMaxHealth() + 4);
				}
			}.runTaskLater(manager.getMain(), 10 * 60 * 20l);

		}
	}

	public long getLastAnalyseUsage() {
		return lastAnalyseUsage;
	}

	public void setLastAnalyseUsage(long lastAnalyseUsage) {
		this.lastAnalyseUsage = lastAnalyseUsage;
	}

}
