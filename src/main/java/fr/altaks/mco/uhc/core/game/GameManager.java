package fr.altaks.mco.uhc.core.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.altaks.mcoapi.core.configs.events.*;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.world.registry.WorldData;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.SpectatorRole;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Esteban;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Ketcha;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Kraka;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.MayaSoldier;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Mendoza;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.OlmequeSoldier;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Pedro;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Sancho;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.SeibanWarrior;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Tao;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.UrubusWarrior;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Viracocha;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Waina;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Zia;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Ambrosius;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Athanaos;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Cinesus;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Fernando;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Gaspard;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Gomez;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Helvetius;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Hippolyte;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Hortense;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Isabella;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Marinche;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Nostradamus;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Pizarro;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Teteola;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Calmeque;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Menator;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Takashi;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Yupanqui;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.FastBoard;
import fr.altaks.mco.uhc.util.RolesUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class GameManager implements Listener {
	
	private Main main;
	private GameState currentGameState = GameState.WAITING;
	
	private HashMap<Role, ArrayList<UUID>> attributedRoles = new HashMap<>();
	private HashMap<UUID, RoleType> roleOfPlayer = new HashMap<>();
	
	private HashMap<Role, Integer> remainingPlacesPerRole = new HashMap<>();
	private HashMap<RoleType, Role> roleFromRoleType = new HashMap<>();
	
	private List<UUID> hasDied = new ArrayList<>();
	private List<RoleType> deadRoles = new ArrayList<>();
	
	private List<FastBoard> scoreboards = new ArrayList<FastBoard>();
	
	private HashMap<UUID, Integer> playerKills = new HashMap<>();
	
	private SpectatorRole spectatorRole = new SpectatorRole();
	
	private long gameStart;
	private HashMap<Pair<UUID, Integer>, Integer> playerEpisodeToTimeSpent = new HashMap<>();

	private int timeBeforeTakingDamages = 5 /* minutes */,
	            timeBeforeFinalHeal = 20    /* minutes */,
				timeBeforeMeetup = 20       /* minutes */,
				timeBeforePvp = 20          /* minutes */;

	private int rolesTaskID,
	            pvpTaskID,
				borderTaskID;

	private boolean rolesTaskHasBeenSpedUp = false, pvpTaskHasBeenSpedUp = false, borderHasBeenSpedUp = false;

	private boolean arePlayersAbleToMineUnderground = true;

	public boolean isHaveRolesBeenEnabled() {
		return haveRolesBeenEnabled;
	}

	private boolean haveRolesBeenEnabled = false;
	
	public GameManager(Main main) {
		this.main = main;

		timeBeforeTakingDamages = main.getConfig().getInt("timers.time-before-taking-damage");
		timeBeforeMeetup = main.getConfig().getInt("timers.time-before-meetup");
		timeBeforeFinalHeal = main.getConfig().getInt("timers.time-before-final-heal");
		timeBeforePvp = main.getConfig().getInt("timers.time-before-pvp");

		// ajout des roles dans les roles disponibles
		Role[] roles = {
				new Esteban(),     new Zia(main),        new Tao(main),       new Mendoza(main),  new Pedro(main),   new Sancho(main),
				new Waina(main),   new Ketcha(main),     new Kraka(main),         new Viracocha(main), 
				new MayaSoldier(main), new OlmequeSoldier(main), new SeibanWarrior(main), new UrubusWarrior(main),
				
				new Ambrosius(main),   new Athanaos(main),       new Fernando(main),      new Marinche(main),     new Gomez(),       new Teteola(),
				new Helvetius(main),   new Hortense(main), 	  new Nostradamus(main),  new Gaspard(main),     new Pizarro(main), new Cinesus(main),
				new Isabella(main),    new Hippolyte(main),
				
				new Yupanqui(main),    new Menator(main),        new Calmeque(main),      new Takashi(main)
		};
		for(Role role : roles) {
			if(role.getRole().getRoleAmountOfPlayers() > 0) {
				remainingPlacesPerRole.put(role, role.getRole().getRoleAmountOfPlayers());
				roleFromRoleType.put(role.getRole(), role);

				// register role as eventlistener
				Bukkit.getPluginManager().registerEvents(role, main);
			}
		}
		
		// ajout du role par défaut : spectator.
		attributedRoles.put(spectatorRole, new ArrayList<>());
	}

	@EventHandler
	public void onGameStartHasBeenCalled(GameStartEvent event){
		start(event.getPlayer(), new String[]{});
	}
	
	public void start(Player host, String[] args) {

		for(Player player : Bukkit.getOnlinePlayers()){
			playerKills.put(player.getUniqueId(), 0);
		}
		
		gameStart = System.currentTimeMillis();
		
		// éviter le lancement de deux parties simultanées
		if(this.currentGameState != GameState.WAITING) {
			host.sendMessage(Main.PREFIX + "Une partie est déjà en cours sur ce serveur !");
			return;
		} else host.sendMessage(Main.PREFIX + "La partie se lance !");
		
		// récupérer tous les joueurs qui ne sont pas en spectateur
		List<Player> playersAbleToPlay = new ArrayList<>();
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if(onlinePlayer.getGameMode() != GameMode.SPECTATOR) {
				playersAbleToPlay.add(onlinePlayer);
			}
		}
		
		// randomize player selection
		Collections.shuffle(playersAbleToPlay);
		
		// si le testeur a spécifié un role
		if(args.length > 0 && !args[0].equalsIgnoreCase("random")) {
			playersAbleToPlay.remove(host);
			RoleType type = RoleType.SPECTATING;
			for(RoleType roleType : RoleType.values()) {
				if(args[0].equalsIgnoreCase(roleType.getRoleId())) {
					type = roleType;
					break;
				}
			}
			Role role = roleFromRoleType.get(type);
			
			if(!attributedRoles.containsKey(role)) attributedRoles.put(role, new ArrayList<UUID>());
			
			// ajouter le role au joueur
			attributedRoles.get(role).add(host.getUniqueId());
			roleOfPlayer.put(host.getUniqueId(), role.getRole());
			
			// changer le nombre de places restantes sur ce role, ou retirer ce role si 0.
			if(remainingPlacesPerRole.get(role) > 1) {
				
				// on décrémente le nb de places restantes
				remainingPlacesPerRole.put(role, remainingPlacesPerRole.get(role) - 1);
			} else {
				
				// on retire le role des roles disponibles
				remainingPlacesPerRole.remove(role);
			}
		}
		
		if(args.length > 1) {
			for(String str : args) {
				if(str.equals(args[0])) continue;
				
				String playername = str.split(":")[0];
				String rolename = str.split(":")[1];
				
				Player player = Bukkit.getPlayer(playername);
				
				playersAbleToPlay.remove(player);
				RoleType type = RoleType.SPECTATING;
				for(RoleType roleType : RoleType.values()) {
					if(rolename.equalsIgnoreCase(roleType.getRoleId())) {
						type = roleType;
						break;
					}
				}
				if(type == RoleType.SPECTATING) {
					attributedRoles.get(spectatorRole).add(player.getUniqueId());
					continue;
				}
				
				Role role = roleFromRoleType.get(type);
				
				if(!attributedRoles.containsKey(role)) attributedRoles.put(role, new ArrayList<UUID>());
				
				// ajouter le role au joueur
				attributedRoles.get(role).add(player.getUniqueId());
				roleOfPlayer.put(player.getUniqueId(), role.getRole());
				
				// changer le nombre de places restantes sur ce role, ou retirer ce role si 0.
				if(remainingPlacesPerRole.get(role) > 1) {
					
					// on décrémente le nb de places restantes
					remainingPlacesPerRole.put(role, remainingPlacesPerRole.get(role) - 1);
				} else {
					
					// on retire le role des roles disponibles
					remainingPlacesPerRole.remove(role);
				}
			}
		}
		
		// distribution de rôles à chaque joueur autre que le testeur
		for(Player player : playersAbleToPlay) {
			
			// déterminer s'il reste des roles, sinon, placer les joueurs en spectateurs
			if(remainingPlacesPerRole.size() != 0) {
				
				// choisir un role aléatoire parmis les roles restants
				Role role = remainingPlacesPerRole.keySet().stream().collect(Collectors.toList()).get(new Random().nextInt(remainingPlacesPerRole.size()));
				
				if(!attributedRoles.containsKey(role)) attributedRoles.put(role, new ArrayList<>());
				// ajouter le role au joueur
				attributedRoles.get(role).add(player.getUniqueId());
				roleOfPlayer.put(player.getUniqueId(), role.getRole());
				
				// changer le nombre de places restantes sur ce role, ou retirer ce role si 0.
				if(remainingPlacesPerRole.get(role) > 1) {
					
					// on décrémente le nb de places restantes
					remainingPlacesPerRole.put(role, remainingPlacesPerRole.get(role) - 1);
				} else {
					
					// on retire le role des roles disponibles
					remainingPlacesPerRole.remove(role);
				}
				
			} else { // il ne reste plus de roles

				attributedRoles.get(spectatorRole).add(player.getUniqueId());
				
			}
			
		}
		
		RolesUtil util = new RolesUtil(this);
		
		// affectation des roles aux joueurs
		for(Entry<Role, ArrayList<UUID>> affectedRole : attributedRoles.entrySet()) {
			for(UUID id : affectedRole.getValue()) {
				
				Player player = Bukkit.getPlayer(id);

				Main.logIfDebug("Role { " + affectedRole.getKey().getRole().getRoleName() + " } affected to " + player.getDisplayName() + ". §r[" + id + "].");

				player.updateInventory();

				player.setMaxHealth(20.0d);
				player.setHealth(20.0d);
				player.setFoodLevel(20);
				for(PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
				
			}
		}

		rolesTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
			for(Entry<Role, ArrayList<UUID>> affectedRole : attributedRoles.entrySet()) {
				for(UUID id : affectedRole.getValue()) {
					Bukkit.getPlayer(id).sendMessage(
							"§7§m----------------------------------------------------§r\n"
							+ "\u00bb §eVous jouez " + affectedRole.getKey().getRole().getRoleName() + " de " + affectedRole.getKey().getTeamOfPlayer().getTeamName() + ".\n"
							+ "§7§m----------------------------------------------------§r\n"
							+ "\n§6Explications de vôtre rôle : §r\n" + util.replacedString(affectedRole.getKey().getRelativeExplications())
							+ "\n§7§m----------------------------------------------------§r\n");
				}
				affectedRole.getKey().onGameStart(affectedRole.getValue(), this);
				haveRolesBeenEnabled = true;

			}
		}, timeBeforePvp * 20L);

		this.currentGameState = GameState.PLAYING;

		// teleport all players and spread them in "world"
		World world = Bukkit.getWorld("world");
		for(Entry<Role, ArrayList<UUID>> affectedRole : attributedRoles.entrySet()) {
			for(UUID id : affectedRole.getValue()) {
				Player player = Bukkit.getPlayer(id);

				// randomize X and Z
				int x = new Random().nextInt(400) - 200;
				int z = new Random().nextInt(400) - 200;

				// get the highest Y
				int y = world.getHighestBlockYAt(x, z) + 1;

				// teleport the player
				player.teleport(new Location(world, x, y, z));
			}
		}
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			FastBoard playerboard = new FastBoard(player);
			
			playerKills.put(player.getUniqueId(), 0);
			
			playerboard.updateTitle("§6MCO UHC");
			playerboard.updateLines(
					"§r\u00BB Joueurs : §b" + this.roleOfPlayer().size(),
					"§r\u00BB Hôte : §b" + host.getDisplayName(),
					"",
					"§r\u00BB Temps : §a00:00",
					"§r\u00BB PvP : §cNon",
					"§r\u00BB Kill(s) : §a" + playerKills.get(player.getUniqueId()),
					"",
					"§r\u00BB Bordures : §aSans limite"
			);
			
			this.scoreboards.add(playerboard);
		}
		
		new BukkitRunnable() {
			
			private final long gameStart = System.currentTimeMillis();
			
			@Override
			public void run() {
				for(FastBoard playerboard : scoreboards) {
					playerboard.updateLines(
							"§r\u00BB Joueurs : " + roleOfPlayer().size(),
							"§r\u00BB Hôte : " + host.getDisplayName(),
							"",
							"§r\u00BB Temps : §a" + getCurrentTime(),
							"§r\u00BB PvP : " + (playerboard.getPlayer().getWorld().getPVP() ? "§aOui" : "§cNon") ,
							"§r\u00BB Kill(s) : §a" + playerKills.get(playerboard.getPlayer().getUniqueId()),
							"",
							"§r\u00BB Bordures : §a" + String.format("%.2f", playerboard.getPlayer().getWorld().getWorldBorder().getSize() / 2)
					);
				}
			}
			
			public String getCurrentTime() {
				long min = (System.currentTimeMillis() - gameStart) / 60_000;
				long sec = (System.currentTimeMillis() - gameStart) % 60_000 / 1000;
				return min + "min" + sec + "s";
			}
			
		}.runTaskTimerAsynchronously(main, 0, 20);

		int minBeforeFirstCitySpawn = main.getConfig().getInt("timers.time-before-first-city-spawn"),
		    minBetweenCitySpawns = main.getConfig().getInt("timers.time-between-city-spawns");

		startCitySpawnRunnable(host, "entry-1", (minBeforeFirstCitySpawn)*60 + new Random().nextInt(5), 30, host.getLocation().getWorld(), 0, 0, 0);
		startCitySpawnRunnable(host, "entry-2", (minBeforeFirstCitySpawn + minBetweenCitySpawns) *60 + new Random().nextInt(5), 60, host.getLocation().getWorld(), 0, 0, 0);
		startCitySpawnRunnable(host, "entry-3", (minBeforeFirstCitySpawn + 2*minBetweenCitySpawns)*60 - new Random().nextInt(5), 90, host.getLocation().getWorld(), 0, 0, 0);
		
		GameManager manager = this;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Role attributedRole : attributedRoles.keySet()) {
					if(deadRoles.contains(attributedRole.getRole())) continue;
					if(attributedRole.isWinning(manager)) {
						// Game has been won
						currentGameState = GameState.ENDED;
						RolesUtil util = new RolesUtil(manager);

						String winnerTitle;
						switch (attributedRole.getTeamOfPlayer()) {
							case COALITION:
								winnerTitle = "§aCoalition !";
								break;
							case HOURGLASS:
								winnerTitle = "§4L'ordre du Sablier !";
								break;
							case INDEPENDANT:
								winnerTitle = attributedRole.getRole().getRoleName();
								break;
							default:
								return;
						}
						
						for(Player player : Bukkit.getOnlinePlayers()) {
							player.sendMessage(Main.PREFIX + "§aLa partie a été remportée par " + attributedRole.getRole().getRoleName() + util.replacedString("! \n Ce rôle était joué par %"+ attributedRole.getRole().getRoleId() +"%"));

							player.sendTitle(
									ChatColor.BOLD + "" + ChatColor.RED + "Fin de la partie !",
									ChatColor.WHITE + "Victoire de : " + winnerTitle
							);

							player.getInventory().clear();
							player.getInventory().setHelmet(null);
							player.getInventory().setChestplate(null);
							player.getInventory().setLeggings(null);
							player.getInventory().setBoots(null);
							player.updateInventory();

							player.setMaxHealth(20.0d);
							player.setHealth(20.0d);
							player.setFoodLevel(20);
							for(PotionEffect effect : player.getActivePotionEffects()) {
								player.removePotionEffect(effect.getType());
							}
						}
						for(FastBoard board : scoreboards) board.delete();
						scoreboards.clear();
						playerKills.clear();
						cancel();


						for(Entry<UUID, RoleType> entry : roleOfPlayer.entrySet()){
							if(isPlaying(entry.getKey())){
								// broadcast : Player : Role in GOLD color
								Bukkit.broadcastMessage(Main.PREFIX + "§6" + Bukkit.getPlayer(entry.getKey()).getDisplayName() + "§7 : §6" + entry.getValue().getRoleName() + "§7 !");
							} else {
								// broadcast : Player : Role in strikethrough GOLD color
								Bukkit.broadcastMessage(Main.PREFIX + "§6§m" + Bukkit.getPlayer(entry.getKey()).getDisplayName() + "§7 : §6§m" + entry.getValue().getRoleName() + "§7 !");
							}
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(main, 0, 20);

		if(main.getConfig().isSet("timers.time-before-pvp")) {
			for(World w : Bukkit.getWorlds()) w.setPVP(false);
			pvpTaskID = new BukkitRunnable() {
				@Override
				public void run() {
					for(World w : Bukkit.getWorlds()) w.setPVP(true);
				}
			}.runTaskLater(main, main.getConfig().getInt("timers.time-before-pvp") * 20 + 1).getTaskId();
		}

		// runnable du final heal
		if(main.getConfig().isSet("timers.time-before-final-heal")) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Main.logIfDebug("Final healing for all players");
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.setHealth(p.getMaxHealth());
						p.setFoodLevel(20);
						p.setSaturation(20);
					}
				}
			}.runTaskLater(main, main.getConfig().getInt("timers.time-before-final-heal") * 60 * 20); // time is in minutes
		}

		// runnable du meetup
		if(main.getConfig().isSet("timers.time-before-meetup")) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Main.logIfDebug("Meetup started");
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.sendMessage(Main.PREFIX + "§cLe meetup a commencé !");
						for(Player player : Bukkit.getOnlinePlayers()) {
							player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
							// teleport player if he's underground and in the overwold
						}
					}
					arePlayersAbleToMineUnderground = false;
				}
			}.runTaskLater(main, main.getConfig().getInt("timers.time-before-meetup") * 60 * 20); // time is in minutes
		}

		// set all the borders to a certain size according to the config property
		// create a runnable for border reduction within a certain time using the seconds parameters and some math :)
		int maxBorderSize = main.getConfig().getInt("timers.border-size-before-retraction");
		int minBorderSize = main.getConfig().getInt("timers.border-size-after-retraction");

		long timeBeforeRetraction = main.getConfig().getInt("timers.time-before-border-retraction") * 60 * 20l;
		int retractionSpeed = main.getConfig().getInt("timers.border-reduction-speed");

		long retractionDuration = (long)((double)(maxBorderSize - minBorderSize) / retractionSpeed);

		for(World w : Bukkit.getWorlds()){
			w.getWorldBorder().setSize(maxBorderSize);
		}
		new BukkitRunnable(){

			@Override
			public void run() {
				for(World w : Bukkit.getWorlds()){
					w.getWorldBorder().setSize(minBorderSize, retractionDuration);
				}
			}

		}.runTaskLater(main, timeBeforeRetraction);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		String deathMessage = "§7§m-----------------------------------------\n§c"
				+ event.getEntity().getDisplayName() + "§7 est mort(e)\n";

		// en fonction de la team du joueur adverse, on change la couleur du rôle
		deathMessage += "Son rôle était : ";
		switch(main.getCurrentGameManager().roleOfPlayer().get(event.getEntity().getUniqueId()).getRoleTeam()) {
			case COALITION:
				deathMessage += "§a";
				break;
			case HOURGLASS:
				deathMessage += "§c";
				break;
			case INDEPENDANT:
				deathMessage += "§6";
				break;
			default:
				return;
		}

		deathMessage += main.getCurrentGameManager().roleOfPlayer().get(event.getEntity().getUniqueId()).getRoleName();
		deathMessage += "\n§7§m-----------------------------------------";

		event.setDeathMessage(deathMessage);
	}

	@EventHandler
	public void onHostCallsForceRoleActivation(CallForceRolesEvent event){
		if(this.currentGameState != GameState.PLAYING) return;
		if(!this.rolesTaskHasBeenSpedUp && this.rolesTaskID != 0){
			// speed up the task to 10 secs remaining before execution.
			Bukkit.getScheduler().cancelTask(this.rolesTaskID);
			new BukkitRunnable(){

				int timer = 10;

				@Override
				public void run() {
					if(timer > 0){
						if(timer <= 5){
							Bukkit.broadcastMessage(Main.PREFIX + "§6Activation des rôles dans §e" + timer + "§6 ...");
						}
						timer--;
					} else {
						RolesUtil util = new RolesUtil(main.getCurrentGameManager());
						for(Entry<Role, ArrayList<UUID>> affectedRole : attributedRoles.entrySet()) {
							for(UUID id : affectedRole.getValue()) {
								Bukkit.getPlayer(id).sendMessage(
										"§7§m----------------------------------------------------§r\n"
												+ "\u00bb §eVous jouez " + affectedRole.getKey().getRole().getRoleName() + " de " + affectedRole.getKey().getTeamOfPlayer().getTeamName() + ".\n"
												+ "§7§m----------------------------------------------------§r\n"
												+ "\n§6Explications de vôtre rôle : §r\n" + util.replacedString(affectedRole.getKey().getRelativeExplications())
												+ "\n§7§m----------------------------------------------------§r\n");
							}
							affectedRole.getKey().onGameStart(affectedRole.getValue(), main.getCurrentGameManager());

						}
						cancel();
					}
				}
			}.runTaskTimer(main, 0, 20L);
		}
	}

	@EventHandler
	public void onHostCallsForcePvPActivation(CallForcePvPEvent event){
		if(this.currentGameState != GameState.PLAYING) return;
		if(!this.pvpTaskHasBeenSpedUp && this.pvpTaskID != 0){
			// speed up the task to 10 secs remaining before execution.
			Bukkit.getScheduler().cancelTask(this.pvpTaskID);
			new BukkitRunnable(){

				int timer = 10;

				@Override
				public void run() {
					if(timer > 0){
						if(timer <= 5){
							Bukkit.broadcastMessage(Main.PREFIX + "§6Activation du PVP dans §e" + timer + "§6 ...");
						}
						timer--;
					} else {
						timeBeforeTakingDamages = 0;
						for(World w : Bukkit.getWorlds()) w.setPVP(true);
						cancel();
					}
				}
			}.runTaskTimer(main, 0, 20L);
		}
	}

	@EventHandler
	public void onHostCallsBorderRetractation(CallForceBorderEvent event){
		int minBorderSize = main.getConfig().getInt("timers.border-size-after-retraction");
		int retractionSpeed = main.getConfig().getInt("timers.border-reduction-speed");

		new BukkitRunnable(){

			int timer = 10;

			@Override
			public void run() {
				if(timer > 0){
					if(timer <= 5){
						Bukkit.broadcastMessage(Main.PREFIX + "§6Activation de la réduction des bordures dans §e" + timer + "§6...");
					}
					timer--;
				} else {
					for(World w : Bukkit.getWorlds()){
						long retractionDuration = (long)((w.getWorldBorder().getSize() - minBorderSize) / retractionSpeed);
						w.getWorldBorder().setSize(minBorderSize, retractionDuration);
					}
					cancel();
				}
			}
		}.runTaskTimer(main, 0, 20L);

	}

	@EventHandler
	public void onPlayerMinesBlockUnderground(BlockBreakEvent event) {
		if(this.currentGameState != GameState.PLAYING) return;
		if(!arePlayersAbleToMineUnderground && event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
			if(event.getPlayer().getLocation().getY() < 60) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Main.PREFIX + "§cVous ne pouvez pas miner sous la couche 60 durant la phase de Meetup !");
			}
		}
	}

	@EventHandler
	public void onPlayerTakesDamagesBeforeBeingAbleToTakeDamage(EntityDamageEvent event){
		if(this.currentGameState == GameState.PLAYING){
			// check timestamp (5 min -> secs -> ms)
			if(this.gameStart + timeBeforeTakingDamages * 60 * 1000 >= System.currentTimeMillis()){
				if(event.getEntity() instanceof Player){
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		// if it's a player from the game
		if(this.currentGameState != GameState.PLAYING) return;
		if(this.roleOfPlayer.containsKey(event.getEntity().getUniqueId())) {
			Role role = this.roleFromRoleType.get(this.roleOfPlayer.get(event.getEntity().getUniqueId()));
			role.onPlayerDeath(event.getEntity().getUniqueId(), this);
		}
	}
	
	@EventHandler
	public void onPlayerKillsAnOtherPlayer(EntityDamageByEntityEvent event) {
		if(this.currentGameState != GameState.PLAYING) return;
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			if(((Player)event.getEntity()).getHealth() <= event.getFinalDamage()){
				this.playerKills.put(event.getDamager().getUniqueId(), this.playerKills.get(event.getDamager().getUniqueId()) + 1);
			}
		}
	}
	
	public boolean isRoleAttributed(RoleType role) {
		return this.roleOfPlayer.containsValue(role);
	}
	
	public ArrayList<UUID> getPlayersThatOwnsRole(RoleType role) {
		return this.attributedRoles.get(this.roleFromRoleType.get(role));
	}
	
	public List<UUID> getDeadPlayers(){
		return this.hasDied;
	}
	
	public GameTeam getTeamOfPlayer(UUID id) {
		return this.roleFromRoleType.get(roleOfPlayer.get(id)).getTeamOfPlayer();
	}
	
	public GameState getCurrentGameState() {
		return this.currentGameState;
	}
	
	public HashMap<RoleType, Role> getRoleFromRoleType(){
		return this.roleFromRoleType;
	}
	
	public List<RoleType> getDeadRoles(){
		return this.deadRoles;
	}
	
	public HashMap<Role, ArrayList<UUID>> getPlayersFromRole(){
		return this.attributedRoles;
	}
	
	public HashMap<Role, Integer> getRemainingRoles(){
		return this.remainingPlacesPerRole;
	}
	
	public HashMap<UUID, RoleType> roleOfPlayer(){
		return this.roleOfPlayer;
	}
	
	public Main getMain() {
		return this.main;
	}
	
	public boolean hasDied(UUID id) {
		return this.hasDied.contains(id);
	}
	
	public boolean isPlaying(UUID id) {
		return this.roleOfPlayer.containsKey(id) && !this.hasDied.contains(id) && Bukkit.getPlayer(id) != null;
	}

	public void revivePlayer(UUID id) {
		if(this.hasDied.contains(id)) {
			this.hasDied.remove(id);
			// make sure the role is not considered as dead anymore if it was
			if(this.deadRoles.contains(this.roleOfPlayer.get(id))) {
				this.deadRoles.remove(this.roleOfPlayer.get(id));
			}
			// set the player in survival mode and teleport him somewhere on the map safe
			Player player = Bukkit.getPlayer(id);

			player.setMaxHealth(20);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setSaturation(20);

			player.setGameMode(GameMode.SURVIVAL);

			player.getInventory().clear();
			player.getInventory().setArmorContents(null);

			// teleport the player somewhere safe
			int x = new Random().nextInt(400) - 200;
			int z = new Random().nextInt(400) - 200;

			// get the highest Y
			int y = Bukkit.getWorld("world").getHighestBlockYAt(x, z) + 1;

			// teleport the player
			player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));
			player.sendMessage(Main.PREFIX + "§aVous avez été réssucité !");
		}
	}

	@EventHandler
	public void onPlayerKillOfflineCallFromHost(CallKillOfflineEvent event){
		if(this.currentGameState != GameState.PLAYING) return;

		for(OfflinePlayer player : event.getTokill()){
			killOfflinePlayer(player);
		}
	}

	public void killOfflinePlayer(OfflinePlayer player){
		// make sure the player is offline
		// make the player die as a role user
		// if the player comes back on the server, it has to be able to be revived
		if(!player.isOnline()){
			// perform onDeath actions of the role
			if(this.roleOfPlayer.containsKey(player.getUniqueId())){
				Role role = this.roleFromRoleType.get(this.roleOfPlayer.get(player.getUniqueId()));
				role.onPlayerDeath(player.getUniqueId(), this);
			}
			// make the player in spectator mode while he's dead
			new BukkitRunnable() {
				@Override
				public void run() {
					if(player.isOnline() && hasDied(player.getUniqueId())){
						Player onlinePlayer = player.getPlayer();
						onlinePlayer.setGameMode(GameMode.SPECTATOR);
					}
				}
			}.runTaskTimer(this.main,0, 20);
		}
	}

	@EventHandler
	public void onRevivePlayerEventRecieve(CallRevivePlayerEvent event){
		if(this.currentGameState != GameState.PLAYING) return;
		if(this.haveRolesBeenEnabled) return;

		for(OfflinePlayer player : event.getToRevive()){
			revivePlayer(player.getUniqueId());
		}
	}
	
	public List<OfflinePlayer> getTeamPlayers(GameTeam team){
		List<OfflinePlayer> players = new ArrayList<>();
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(isPlaying(player.getUniqueId())) {
				if(getTeamOfPlayer(player.getUniqueId()) == team) {
					players.add(player);
				}
			}
		}
		return players;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if(this.currentGameState == GameState.WAITING) {
			event.setJoinMessage(Main.PREFIX + "[+] " + event.getPlayer().getDisplayName());
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if(this.currentGameState == GameState.WAITING) {
			event.setQuitMessage(Main.PREFIX + "[-] " + event.getPlayer().getDisplayName());
		}
	}
	
	public enum GameState {
		WAITING("En attente"), PLAYING("En cours"), ENDED("Partie finie");
		
		public String desc;
		
		GameState(String desc) {
			this.desc = desc;
		}
	}
	
	private List<String> cityWorldsNames = new ArrayList<>();
	private HashMap<Location, String> cityCoreToCityName = new HashMap<>();
	private HashMap<String, Long>     citySpawnTimeStamp = new HashMap<>();
	private HashMap<Location, BukkitTask> cityTeleportTask_CityCoreLocation = new HashMap<Location, BukkitTask>();

	private long cityIndestructibleDelay = 5;
	
	@EventHandler
	public void onPlayerBreaksCityCore(BlockBreakEvent event) {
		if(this.currentGameState != GameState.PLAYING) return;
		if(this.cityTeleportTask_CityCoreLocation.containsKey(event.getBlock().getLocation())) {
			
			if(this.citySpawnTimeStamp.get(this.cityCoreToCityName.get(event.getBlock().getLocation())) + cityIndestructibleDelay <= System.currentTimeMillis()) {
				
				long cityDestroyDelay = 0;
				
				// tao role is defined 
				if(main.getCurrentGameManager().isRoleAttributed(RoleType.TAO)) {

					Tao taoRole = ((Tao)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAO));
					if(taoRole.getCanSaveACity()) {
						
						Location loc = event.getBlock().getLocation();
						
						BaseComponent[] total = 
								   new ComponentBuilder("§e----------------------------------------------------§r\n")
								   .append("Une cité est sur le point d'être détruite ! Mais vous pouvez la sauver : \n Votre choix \u00BB ").color(ChatColor.YELLOW)
								   .append("[1]")
								   		.color(ChatColor.GREEN)
								   		.bold(true)
								   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco taosave accept "+ loc.getWorld().getName() + " " + loc.getBlockX() + " "  + loc.getBlockY() + " "  + loc.getBlockZ()))
								   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Sauver la cité").create()))
								   .append(" - ")
								   		.color(ChatColor.WHITE)
								   		.bold(false)
								   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
								   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
								   .append("[2]")
								   		.color(ChatColor.RED)
								   		.bold(true)
								   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco taosave ignore"))
								   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Ignorer et laisser la cité se détruire").create()))
								   .append("\n§e----------------------------------------------------\n")
								   		.bold(false)
								   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
								   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
								   .create();
						
						if(main.getCurrentGameManager().roleOfPlayer().get(event.getPlayer().getUniqueId()) == RoleType.TAO) {
							cityDestroyDelay = 10 * 20;
							event.getPlayer().spigot().sendMessage(total);
						}
						
						for(Entity entity : event.getPlayer().getNearbyEntities(45, 45, 45)) {
							if(!main.getCurrentGameManager().isPlaying(entity.getUniqueId())) continue;
							if(main.getCurrentGameManager().roleOfPlayer().get(entity.getUniqueId()) == RoleType.TAO) {
								cityDestroyDelay = 10 * 20;
								((Player)entity).spigot().sendMessage(total);
							}
							
						}
					}
				}
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						
						if(main.getCurrentGameManager().isRoleAttributed(RoleType.TAO)) {

							Tao taoRole = ((Tao)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAO));
							if(taoRole.getSavedCity() != null) {
								 // a city has been saved
								if(event.getBlock().getLocation().equals(taoRole.getSavedCity())) {
									citySpawnTimeStamp.put(
											cityCoreToCityName.get(event.getBlock().getLocation()), 
											citySpawnTimeStamp.get(
													cityCoreToCityName.get(event.getBlock().getLocation())  + cityIndestructibleDelay)
											);
									
									event.getBlock().setType(Material.BEACON);
									
									org.bukkit.World world = Bukkit.getWorld("world");
									
									for(Player player : event.getPlayer().getWorld().getPlayers()) {
										// randomize player positions in the overworld
										
										int    x = new Random().nextInt(90),
											   z = new Random().nextInt(90),
											   y = world.getHighestBlockYAt(x, z) + 1;
										
										player.teleport(new Location(world, x, y, z));
									}

									event.setCancelled(true);
									return;
								}
							}
						}
						
						// a city core has been destroyed
						Main.log(Main.DEBUG + "§cA city core has been destroyed by " + event.getPlayer().getDisplayName());
						
						cityTeleportTask_CityCoreLocation.get(event.getBlock().getLocation()).cancel(); // cancels teleportations from city entry point
						
						String cityName = cityCoreToCityName.get(event.getBlock().getLocation());
						
						event.getPlayer().getInventory().addItem(CityArtifacts.getItemFromCityName(cityName));
						
						// reteleport all players from that city back to the overworld
						
						org.bukkit.World world = Bukkit.getWorld("world");
						
						for(Player player : event.getPlayer().getWorld().getPlayers()) {
							// randomize player positions in the overworld
							
							int    x = new Random().nextInt(90),
								   z = new Random().nextInt(90),
								   y = world.getHighestBlockYAt(x, z) + 1;
							
							player.teleport(new Location(world, x, y, z));
							player.damage(5*2);
						}
						
						for(Player player : Bukkit.getOnlinePlayers()) {
							if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) {
								if(main.getCurrentGameManager().getTeamOfPlayer(player.getUniqueId()) == GameTeam.COALITION) {
									player.setMaxHealth(player.getMaxHealth() - 2*1);
									player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5*60*20, 0), true);
								}
							}
						}
					}
				}.runTaskLater(main, cityDestroyDelay);
				
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Main.PREFIX + "§cVous ne pouvez pas récupérer l'artéfact de cette cité tout de suite !");
				// la cité est trop jeune
			}
			
		} else if(this.cityWorldsNames.contains(event.getBlock().getLocation().getWorld().getName())) {
			// a block other that a core 
			
			Main.logIfDebug(Main.DEBUG + "§cA city block other that core has been tried to destroy by " + event.getPlayer().getDisplayName());
			event.setCancelled(true);
		}
	}
	
	public void startCitySpawnRunnable(Player host, String cityName, int secondsBeforeSpawn, int radiusOfSpawn, org.bukkit.World world, int spawnX, int spawnZ, int verticalOffSet) {
		int x = spawnX + new Random().nextInt(radiusOfSpawn),
			z = spawnZ + new Random().nextInt(radiusOfSpawn);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				int y = world.getHighestBlockYAt(x, z) + 1 + verticalOffSet;
				
				try {
					Pair<Location, Double> centerOfCity = pasteSchematic(host, cityName, new Location(world,x,y,z), true);
					
					Location realCenter = centerOfCity.getLeft();
					double distance = centerOfCity.getRight();
					
					Main.logIfDebug(Main.DEBUG + "§cSchematic real center after paste : " + realCenter.getWorld().getName() +" "+ realCenter.getX()+" "+ realCenter.getY()+" "+ realCenter.getZ());
					Main.logIfDebug(Main.DEBUG + "§cDistance for teleportation: " + distance);
					
					if(main.getCurrentGameManager().isRoleAttributed(RoleType.KRAKA)) {
						Kraka krakaRole = ((Kraka)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.KRAKA));
						krakaRole.setLastCityEntryPointSpawn(realCenter);
					}
					
					BukkitTask teleportFromEntranceTask = new BukkitRunnable() {
						
						Location cityDoorPoint = queryCityCoordinates(cityName);
						private final int maxTimePerEpisodeInCity = 10 * 60; // 10 minutes
						
						@Override
						public void run() {
							
							int episode = (int)((System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000));
							
							for(Player player : Bukkit.getOnlinePlayers()) {
								if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) {
									if(player.getWorld().equals(realCenter.getWorld())) {
										if(player.getLocation().distance(realCenter) <= distance) {
											if(!(12542 <= player.getWorld().getTime() && player.getWorld().getTime() <= 23460)) {
												if(!playerEpisodeToTimeSpent.containsKey(Pair.of(player.getUniqueId(), episode)) || playerEpisodeToTimeSpent.get(Pair.of(player.getUniqueId(), episode)) <= maxTimePerEpisodeInCity) {
													player.teleport(cityDoorPoint);
													continue;	
												}
											} else if(main.getCurrentGameManager().roleOfPlayer().get(player.getUniqueId()) == RoleType.CALMEQUE) {
												player.teleport(cityDoorPoint);
												continue;
											} else if(main.getCurrentGameManager().roleOfPlayer().get(player.getUniqueId()) == RoleType.MENATOR) {
												player.teleport(cityDoorPoint);
												continue;
											}
										}
										
									} else if(player.getWorld().equals(cityDoorPoint.getWorld())) { 
										if(!playerEpisodeToTimeSpent.containsKey(Pair.of(player.getUniqueId(), episode))) {
											playerEpisodeToTimeSpent.put(Pair.of(player.getUniqueId(), episode), 0);
										}
										
										// si le joueur est dans une cité
										if(playerEpisodeToTimeSpent.get(Pair.of(player.getUniqueId(), episode)) <= maxTimePerEpisodeInCity) {

											// si c'est inférieur  10 minutes
												// augmenter son score
											playerEpisodeToTimeSpent.put(Pair.of(player.getUniqueId(), episode), playerEpisodeToTimeSpent.get(Pair.of(player.getUniqueId(), episode)) + 1);
										} else {

											// si c'est supérieur ou égal a 10 minutes
												// téléporter le joueur en dehors 
											int    x = new Random().nextInt(90),
													   z = new Random().nextInt(90),
													   y = Bukkit.getWorld("world").getHighestBlockYAt(x, z) + 1;
												
											player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));
											player.sendMessage(Main.PREFIX + "§cVous venez de sortir de la cité (10 minutes écoulées)");
											
										}
												
									}
								}
							}
						}
					}.runTaskTimer(main, 0, 20);
					
					for(Player player : Bukkit.getOnlinePlayers()) {
						player.setCompassTarget(realCenter);
					}
					
					Location cityCore = queryCityCoreCoordinates(cityName);
					
					cityTeleportTask_CityCoreLocation.put(cityCore, teleportFromEntranceTask);
					cityCoreToCityName.put(cityCore, cityName);
					citySpawnTimeStamp.put(cityName, System.currentTimeMillis());
					
				} catch (MaxChangedBlocksException | IOException e) {
					e.printStackTrace();
				}
			}
		}.runTaskLater(main, secondsBeforeSpawn * 20);
		
		
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.MENATOR)) {
			int secondsBeforeTellingMendozas = secondsBeforeSpawn - 60;
			if(secondsBeforeTellingMendozas < 0) { 
				secondsBeforeTellingMendozas = 0;
			}
			new BukkitRunnable() {
				
				@Override
				public void run() {
					for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENATOR)) {
						if(main.getCurrentGameManager().isPlaying(id)) {
							Bukkit.getPlayer(id).sendMessage(Main.PREFIX + "Une cité va apparaître aux coordonnées suivantes : " + x + " | " + z);
						}
					}
				}
			}.runTaskLater(main, secondsBeforeTellingMendozas * 20);
		}
		
	}
	
	public Pair<Location, Double> pasteSchematic(Player player, String fileName, Location pastingAnchor, boolean ignoreAirBlocks) throws MaxChangedBlocksException, FileNotFoundException, IOException {

		String filePath = main.getDataFolder() + File.separator + "cities" + File.separator + fileName + ".schematic";
		File file = new File(filePath);
		if(!file.exists()) throw new FileNotFoundException("The desired file " + fileName + " has not been found in " + filePath);
		
		Vector pastingPoint = new Vector(pastingAnchor.getBlockX(), pastingAnchor.getBlockY(), pastingAnchor.getBlockZ()); // Where you want to paste

		com.sk89q.worldedit.world.World weWorld = new BukkitWorld(player.getWorld());
		WorldData worldData = weWorld.getWorldData();
		
		Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(file)).read(worldData);

		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);

		Pair<Location, Double> relativeCenter = findRelativeCenter(fileName, pastingAnchor);
		Location realCenter = pastingAnchor.clone().add(relativeCenter.getLeft());
		
		Main.logIfDebug(Main.DEBUG + "§cSchematic real center     : " + realCenter.getWorld().getName() +" "+ realCenter.getX()+" "+ realCenter.getY()+" "+ realCenter.getZ());
		Main.logIfDebug(Main.DEBUG + "§cDistance for teleportation: " + relativeCenter.getRight());
		
		ForwardExtentCopy paste = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), editSession, pastingPoint);
		
		if (ignoreAirBlocks) {
		    paste.setSourceMask(new ExistingBlockMask(clipboard));
		}
		Operations.completeLegacy(paste);
		editSession.flushQueue();
		return Pair.of(realCenter, relativeCenter.getRight());
	}
	
	public Pair<Location, Double> findRelativeCenter(String schematicName, Location pastingAnchor) throws FileNotFoundException {
		String ymlPath = main.getDataFolder() + File.separator + "cities" + File.separator + schematicName + ".yml";
		File ymlFile = new File(ymlPath);
		if(!ymlFile.exists()) throw new FileNotFoundException("The desired file " + schematicName + " has not been found in " + ymlPath);
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
		org.bukkit.World world = Bukkit.getWorld(yml.getString("relative-center-coords.world"));
		
		int relX = yml.getInt("relative-center-coords.x");
		int relY = yml.getInt("relative-center-coords.y");
		int relZ = yml.getInt("relative-center-coords.z");
		
		double distanceFromCenterToTeleport = yml.getDouble("distance-from-center-to-teleport");
		
		return Pair.of(new Location(world, relX, relY, relZ), distanceFromCenterToTeleport);
	}
	
	public Location queryCityCoordinates(String schematicName) throws FileNotFoundException {
		String ymlPath = main.getDataFolder() + File.separator + "cities" + File.separator + schematicName + ".yml";
		File ymlFile = new File(ymlPath);
		if(!ymlFile.exists()) throw new FileNotFoundException("The desired file " + schematicName + " has not been found in " + ymlPath);
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
		org.bukkit.World world = main.getServer().createWorld(new WorldCreator(yml.getString("city-teleport-coordinates.world")));
		world.loadChunk(0, 0);
		
		int x = yml.getInt("city-teleport-coordinates.x");
		int y = yml.getInt("city-teleport-coordinates.y");
		int z = yml.getInt("city-teleport-coordinates.z");
		
		this.cityWorldsNames.add(world.getName());
		
		return new Location(world, x, y, z);
	}
	
	public Location queryCityCoreCoordinates(String schematicName) throws FileNotFoundException {
		String ymlPath = main.getDataFolder() + File.separator + "cities" + File.separator + schematicName + ".yml";
		File ymlFile = new File(ymlPath);
		if(!ymlFile.exists()) throw new FileNotFoundException("The desired file " + schematicName + " has not been found in " + ymlPath);
		
		FileConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
		org.bukkit.World world = main.getServer().createWorld(new WorldCreator(yml.getString("city-teleport-coordinates.world")));
		world.loadChunk(0, 0);
		
		int x = yml.getInt("city-core-coordinates.x");
		int y = yml.getInt("city-core-coordinates.y");
		int z = yml.getInt("city-core-coordinates.z");
		
		world.getBlockAt(x, y, z).setType(Material.BEACON);
		
		return new Location(world, x, y, z);
	}
	
	public static enum CityArtifacts {
		
		CITY1("entry-1", Artifacts.CityItems.CONDORS),
		CITY2("entry-2", Artifacts.CityItems.PIERRE_OPHIR),
		CITY3("entry-3", Artifacts.CityItems.COURONNE_TELEK);
		
		public String fileId;
		public ItemStack artifact;
		
		private CityArtifacts(String fileId, ItemStack artifact) {
			this.fileId = fileId;
			this.artifact = artifact;
		}
		
		public static ItemStack getItemFromCityName(String cityName) {
			for(CityArtifacts val : values()) {
				if(val.fileId.equals(cityName)) return val.artifact;
			}
			return null;
		}
		
	}

}
