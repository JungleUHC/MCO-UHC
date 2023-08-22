package fr.altaks.mco.uhc.commands.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Esteban;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Kraka;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.MayaSoldier;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Mendoza;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.OlmequeSoldier;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.SeibanWarrior;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Tao;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.UrubusWarrior;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Zia;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Ambrosius;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Athanaos;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Fernando;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Gomez;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Hippolyte;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Hortense;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Marinche;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Nostradamus;
import fr.altaks.mco.uhc.core.roles.rolecore.hourglass.Pizarro;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Takashi;
import fr.altaks.mco.uhc.core.roles.rolecore.independant.Yupanqui;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.ItemManager;
import fr.altaks.mco.uhc.util.RolesUtil;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class MCOCommand implements CommandExecutor {

	private Main main;
	
	public MCOCommand(Main main) {
		this.main = main;
	}
	
	private int 
		esteban_mco_coalition_usages = 3,
		tao_mco_trust_usages         = 3,
		mendoza_mco_courage_usages	 = 1,
		pedro_mco_richesse_usages    = 5,
		ambrosius_mco_info_usages	 = 2,
		hortense_mco_poison_usages   = 5,
		nostradamus_mco_astre_usages = 3,
		nostradamus_mco_prediction   = 2,
		yupanqui_mco_garnison_usages = 2,
		calmeque_mco_piege_usages    = 3,
		esteban_mco_analyse_usages   = 5;
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) {
			sender.sendMessage(Main.PREFIX + "§cAucune partie n'est en cours !");
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("mco") && sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length > 0 && args[0].equalsIgnoreCase("coalition")) {
				// check if sender is esteban.
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.ESTEBAN) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ESTEBAN).contains(player.getUniqueId())) {
					
					if(esteban_mco_coalition_usages > 0) {
						
						// query all players around team
						int playersAroundEsteban = 0;
									
						for(UUID id : main.getCurrentGameManager().roleOfPlayer().keySet()) {
							OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
							if(offlinePlayer.isOnline()) {
								Player onlinePlayer = offlinePlayer.getPlayer();
								if(main.getCurrentGameManager().getTeamOfPlayer(onlinePlayer.getUniqueId()) != GameTeam.HOURGLASS) {
									if(player.getLocation().distance(onlinePlayer.getLocation()) <= 30.0d) {
										playersAroundEsteban++;
									}
								}
							}
						}
						
						
						
						player.sendMessage(Main.PREFIX + "§eIl y a " + (playersAroundEsteban-1) + " joueurs de la §aCoalition§r §eautour de vous.");
						esteban_mco_coalition_usages--;
						return true;
						
					} else {
						player.sendMessage(Main.PREFIX + "Vous ne pouvez plus utiliser cette commande dans la partie ! (nombre d'utilisations atteint)");
						return true;
					}
				}
			} else if(args.length > 0 && args[0].equalsIgnoreCase("claim")) {
				
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.ZIA) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ZIA).contains(player.getUniqueId())) {
					
					if(((Zia)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.ZIA)).canUseMCOClaim()) {
						if(!player.getInventory().contains(Artifacts.RoleItems.TELEKINESIS)) {
							player.getInventory().addItem(Artifacts.RoleItems.TELEKINESIS);
						} else {
							player.sendMessage(Main.PREFIX + "§cVous possédez déjà cet objet !");
						}
						return true;	
					}				
				}
				
			} else if(args.length > 1 && args[0].equalsIgnoreCase("trust")) {
				
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.TAO) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO).contains(player.getUniqueId())) {
					
					if(tao_mco_trust_usages != 0) {
						Player targeted = Bukkit.getPlayer(args[1]);
						if(targeted != null) {
							if(((Tao)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAO)).getHasKilledACoalitionMember().get(targeted.getUniqueId())) {
								// target has killed a coalition member
								player.sendMessage(Main.PREFIX + "§bTao §7\u00BB §c" + targeted.getDisplayName() + " a du sang sur les mains ...");
							} else {
								player.sendMessage(Main.PREFIX + "§bTao §7\u00BB §a" + targeted.getDisplayName() + " me paraît fiable");
							}
							tao_mco_trust_usages--;
							return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide !");
							return true;
						}
					} else {
						player.sendMessage(Main.PREFIX + "Vous ne pouvez plus utiliser cette commande dans la partie ! (nombre d'utilisations atteint)");
						return true;
					}
				}
			} else if(args.length > 1 && args[0].equalsIgnoreCase("pichu")) {

				if( main.getCurrentGameManager().isRoleAttributed(RoleType.TAO) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO).contains(player.getUniqueId())) {
					
					Player targeted = Bukkit.getPlayer(args[1]);
					if(targeted != null) {
						Tao taoRole = ((Tao)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAO));
						taoRole.setNewPichuTarget(targeted.getUniqueId());
						return true;
					} else {
						player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide !");
						return true;
					}
				}
			} else if(args.length > 0 && args[0].equalsIgnoreCase("courage")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.MENDOZA) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENDOZA).contains(player.getUniqueId())) {
					
					if(mendoza_mco_courage_usages != 0) {
						
						Mendoza mendozaRole = ((Mendoza)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MENDOZA));
						
						mendozaRole.setLastUsageCourage(System.currentTimeMillis());
						player.sendMessage(Main.PREFIX + "Vous donnez du courage à vos alliés et vous infligez tous 10% de dégats supplémentaires pendant 1 minute");
						
						for(UUID coalitionPlayer : main.getCurrentGameManager().roleOfPlayer().keySet()) {
							if(main.getCurrentGameManager().getTeamOfPlayer(coalitionPlayer) == GameTeam.COALITION) {
								Bukkit.getPlayer(coalitionPlayer).sendMessage(Main.PREFIX + "§6Mendoza vous donne du courage !");
							}
						}
						
						mendoza_mco_courage_usages--;
						return true;
						
					} else {
						player.sendMessage(Main.PREFIX + "Vous ne pouvez plus utiliser cette commande dans la partie ! (nombre d'utilisations atteint)");
						return true;
					}
					
				}
					
			} else if(args.length > 1 && args[0].equalsIgnoreCase("mendozaseek")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.MENDOZA) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENDOZA).contains(player.getUniqueId())) {
					
					Player target = Bukkit.getPlayer(args[1]);
					Mendoza mendozaRole = ((Mendoza)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MENDOZA));
					if(mendozaRole.getEstebansInDanger().contains(target.getUniqueId())) {
						mendozaRole.getEstebansInDanger().remove(target.getUniqueId());
						mendozaRole.reduceMendozaSeekUsages();
						player.sendMessage(Main.PREFIX + "§cEsteban \u00BB Aide moi ! Je suis en " + target.getLocation().getBlockX() + " | " + target.getLocation().getBlockZ() + " !");
					} else {
						player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas/plus le sauver !");
					}
					return true;
				}
				
			} else if(args.length > 1 && args[0].equalsIgnoreCase("mendozaignore")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.MENDOZA) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENDOZA).contains(player.getUniqueId())) {

					Player target = Bukkit.getPlayer(args[1]);
					Mendoza mendozaRole = ((Mendoza)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MENDOZA));
					if(mendozaRole.getEstebansInDanger().contains(target.getUniqueId())) {
						mendozaRole.getEstebansInDanger().remove(target.getUniqueId());
						player.sendMessage(Main.PREFIX + "§bMendoza \u00BB Il saura survivre sans moi.");
					} else {
						player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas/plus le sauver !");
					}
					return true;
					
				}
			} else if(args.length > 1 && args[0].equalsIgnoreCase("richesse")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.PEDRO) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.PEDRO).contains(player.getUniqueId())) {

					if(pedro_mco_richesse_usages != 0) {
						Player target = Bukkit.getPlayer(args[1]);
						if(target != null) {
								// count all gapples of player
								int gappleCount = 0;
								
								for(int slot = 0; slot < target.getInventory().getSize(); slot++) {
									if(target.getInventory().getItem(slot) == null) continue;
									if(target.getInventory().getItem(slot).getType().equals(Material.GOLDEN_APPLE)) {
										gappleCount += target.getInventory().getItem(slot).getAmount();
									}
								}
								
								player.sendMessage(Main.PREFIX + "§bPedro \u00BB §6Cette personne possède §e" + gappleCount + "§6 pommes dorées");
								pedro_mco_richesse_usages--;
								return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide !");
							return true;
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus utiliser cette commande !");
						return true;
					}
						
				}
			} else if(args.length > 0 && args[0].equalsIgnoreCase("scan")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.VIRACOCHA) && 
				    main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.VIRACOCHA).contains(player.getUniqueId())) {
					
					Set<Player> nearbyPlayers = new HashSet<>();
					
					for(Entity entity : player.getNearbyEntities(15, 15, 15)) {
						if(entity instanceof Player) {
							nearbyPlayers.add((Player)entity);
						}
					}
					
					int soldiersCount = 0, coalitionCount = 0, hourglassCount = 0;
					
					for(Player nearbyPlayer : nearbyPlayers) {
						if(!main.getCurrentGameManager().roleOfPlayer().containsKey(nearbyPlayer.getUniqueId())) continue;
						RoleType role = main.getCurrentGameManager().roleOfPlayer().get(nearbyPlayer.getUniqueId());
						if(role == RoleType.MAYA_SOLDIER || role == RoleType.OLMEQUE_SOLDIER || role == RoleType.SEIBAN_WARRIOR || role == RoleType.URUBUS_WARRIOR) {
							soldiersCount++;
						}
						GameTeam team = main.getCurrentGameManager().getTeamOfPlayer(nearbyPlayer.getUniqueId());
						if(team == GameTeam.COALITION) coalitionCount++;
						if(team == GameTeam.HOURGLASS) hourglassCount++;
					}
					
					if(soldiersCount >= 2) {
						new BukkitRunnable() {
							
							int timer = 5; // 5 sec
							
							int points = 50;
							double radius = 3.0d;
							Location origin = player.getLocation();
							
							@Override
							public void run() {
								if(timer == 0) cancel();
								
								origin = player.getLocation();
								for (int i = 0; i < points; i++) {
								    double angle = 2 * Math.PI * i / points;
								    Location point = origin.clone().add(radius * Math.sin(angle), 1d, radius * Math.cos(angle));
								    sendParticle(player, point, EnumParticle.REDSTONE, 25, 110, 2);
								}
								timer--;
							}
						}.runTaskTimer(main, 0, 20);
					} else if(coalitionCount >= 2) {
						new BukkitRunnable() {
							
							
							int timer = 5; // 5 sec
							
							int points = 50;
							double radius = 3.0d;
							Location origin = player.getLocation();
							
							@Override
							public void run() {
								if(timer == 0) cancel();
								
								origin = player.getLocation();
								for (int i = 0; i < points; i++) {
								    double angle = 2 * Math.PI * i / points;
								    Location point = origin.clone().add(radius * Math.sin(angle), 1d, radius * Math.cos(angle));
								    sendParticle(player, point, EnumParticle.REDSTONE, 29, 237, 9);
								}
								timer--;
							}
						}.runTaskTimer(main, 0, 20);
					} else if(hourglassCount >= 3) {
						new BukkitRunnable() {
							
							
							int timer = 5; // 5 sec
							
							int points = 50;
							double radius = 3.0d;
							Location origin = player.getLocation();
							
							@Override
							public void run() {
								if(timer == 0) cancel();
								
								origin = player.getLocation();
								for (int i = 0; i < points; i++) {
								    double angle = 2 * Math.PI * i / points;
								    Location point = origin.clone().add(radius * Math.sin(angle), 1d, radius * Math.cos(angle));
								    sendParticle(player, point, EnumParticle.REDSTONE, 153, 5, 7);
								}
								timer--;
							}
						}.runTaskTimer(main, 0, 20);
					}
					
				}
			} else if(args.length > 2 && args[0].equalsIgnoreCase("equipement")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.MAYA_SOLDIER) && 
				    main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MAYA_SOLDIER).contains(player.getUniqueId()) &&
				    args[1].equalsIgnoreCase("mayasoldier") && (args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))) {
					
					MayaSoldier role = ((MayaSoldier)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MAYA_SOLDIER));
					if(!role.getAlreadyChosenStuff().containsKey(player.getUniqueId())) {
						if(args[2].equalsIgnoreCase("1")) {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.DAMAGE_ALL, 3, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)1);
						} else {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)2);
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous avez déjà fait votre choix !");	
					}
					return true;
					
					
				} else if( main.getCurrentGameManager().isRoleAttributed(RoleType.OLMEQUE_SOLDIER) && 
					       main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.OLMEQUE_SOLDIER).contains(player.getUniqueId()) &&
						   args[1].equalsIgnoreCase("olmequesoldier") && (args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))) {
					
					OlmequeSoldier role = ((OlmequeSoldier)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.OLMEQUE_SOLDIER));
					if(!role.getAlreadyChosenStuff().containsKey(player.getUniqueId())) {
						if(args[2].equalsIgnoreCase("1")) {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)1);
						} else {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.DURABILITY, 3, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)2);
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous avez déjà fait votre choix !");	
					}
					return true;
					
				} else if( main.getCurrentGameManager().isRoleAttributed(RoleType.SEIBAN_WARRIOR) && 
					       main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SEIBAN_WARRIOR).contains(player.getUniqueId()) &&
						   args[1].equalsIgnoreCase("seibanwarrior") && (args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))) {
					
					SeibanWarrior role = ((SeibanWarrior)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.SEIBAN_WARRIOR));
					if(!role.getAlreadyChosenStuff().containsKey(player.getUniqueId())) {
						if(args[2].equalsIgnoreCase("1")) {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.ARROW_DAMAGE, 4, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)1);
						} else {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.DEPTH_STRIDER, 2, false).build());
							player.setWalkSpeed(player.getWalkSpeed() * (float)role.getSpeedPercentage());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)2);
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous avez déjà fait votre choix !");	
					}
					return true;
					
				} else if( main.getCurrentGameManager().isRoleAttributed(RoleType.URUBUS_WARRIOR) && 
					       main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.URUBUS_WARRIOR).contains(player.getUniqueId()) &&
						   args[1].equalsIgnoreCase("urubuswarrior") && (args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))) {
					
					UrubusWarrior role = ((UrubusWarrior)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.URUBUS_WARRIOR));
					if(!role.getAlreadyChosenStuff().containsKey(player.getUniqueId())) {
						if(args[2].equalsIgnoreCase("1")) {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.ARROW_FIRE, 1, false).build());
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)1);
						} else {
							player.getInventory().addItem(new ItemManager.EnchantedBookBuilder(Material.BOOK, 1, "§r§d\u00BB Instructions de combat \u00AB").addEnchant(Enchantment.FIRE_ASPECT, 1, false).build());
							player.setWalkSpeed(player.getWalkSpeed() * 1.2f);
							role.getAlreadyChosenStuff().put(player.getUniqueId(), (short)2);
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous avez déjà fait votre choix !");	
					}
					return true;
					
				} 
			} else if(args.length > 1 && args[0].equalsIgnoreCase("info")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
				    main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(player.getUniqueId())) {
					
					if(ambrosius_mco_info_usages != 0) {
						Player target = Bukkit.getPlayer(args[1]);
						if(target != null) {
							if(main.getCurrentGameManager().roleOfPlayer().containsKey(target.getUniqueId())) {
								player.sendMessage(Main.PREFIX + "§6" + target.getDisplayName() + " fait partie de §e" + main.getCurrentGameManager().getTeamOfPlayer(target.getUniqueId()).getTeamName());
								ambrosius_mco_info_usages--;
							} else {
								player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un joueur de la partie !");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVeuillez entrer un pseudonyme valide !");
							return true;
						}
					} else {
						player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus utiliser cette commande");
						return true;
					}
				}
			} else if(args.length > 0 && args[0].equalsIgnoreCase("artefact")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
				    main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(player.getUniqueId())) {
					
					Ambrosius role = ((Ambrosius)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.AMBROSIUS));
					
					if(role.canOpenPocheArtefact()) {
						Inventory inv = Bukkit.createInventory(null, 1*9, "§8Poche d'artéfacts");
						
						if(!role.getHasAlreadyBeenPicked().get(Artifacts.RoleItems.PYRAMIDE_MU)) 	inv.setItem(1, Artifacts.RoleItems.PYRAMIDE_MU);
						if(!role.getHasAlreadyBeenPicked().get(Artifacts.CityItems.COURONNE_TELEK)) inv.setItem(3, Artifacts.CityItems.COURONNE_TELEK);
						if(!role.getHasAlreadyBeenPicked().get(Artifacts.RoleItems.LUMINARION)) 		inv.setItem(5, Artifacts.RoleItems.LUMINARION);
						if(!role.getHasAlreadyBeenPicked().get(Artifacts.RoleItems.MATR_ORICHALQUE)) inv.setItem(7, Artifacts.RoleItems.MATR_ORICHALQUE);
						
						player.openInventory(inv);
					} else {
						player.sendMessage(Main.PREFIX + "§cVous ne trouvez plus rien dans votre poche d'artéfacts...");
						return true;
					}
					
				}
			} else if(args.length > 0 && args[0].equalsIgnoreCase("sacrifice")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(player.getUniqueId())) {
					
					
					if( ItemManager.lightCompare(player.getInventory().getItemInHand(), Artifacts.RoleItems.PYRAMIDE_MU) ||
						ItemManager.lightCompare(player.getInventory().getItemInHand(), Artifacts.CityItems.COURONNE_TELEK) || 
						ItemManager.lightCompare(player.getInventory().getItemInHand(), Artifacts.RoleItems.LUMINARION) || 
						ItemManager.lightCompare(player.getInventory().getItemInHand(), Artifacts.RoleItems.MATR_ORICHALQUE)) {
									
						player.setItemInHand(null);
						player.getInventory().addItem(Artifacts.MEDAILLON_SOLAIRE_COMPLETE);
						player.sendMessage(Main.PREFIX + "§6Vous venez de sacrifier votre artéfact pour un médaillon ...");
						return true;
					}
					
				}
			} else if(args.length > 0 && (args[0].equalsIgnoreCase("priere") || args[0].equalsIgnoreCase("prière"))) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.ATHANAOS) && 
					main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ATHANAOS).contains(player.getUniqueId())) {
					
					Athanaos role = ((Athanaos)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.ATHANAOS));
					
					if(!role.hasDonePrier().containsKey(player.getUniqueId())) {
						role.hasDonePrier().put(player.getUniqueId(), System.currentTimeMillis());
						player.sendMessage(Main.PREFIX + "Vos prières sont entendues et vous serez protégé temporairement");
					} else {
						player.sendMessage(Main.PREFIX + "Vous avez déjà prié");
					}
					return true;
				}
			} else if(args.length > 1 && args[0].equalsIgnoreCase("fernandoheal")) {
				if( main.getCurrentGameManager().isRoleAttributed(RoleType.FERNANDO_LAGUERRA) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.FERNANDO_LAGUERRA).contains(player.getUniqueId())) {
						
						Player target = Bukkit.getPlayer(args[1]);
						Fernando fernandoRole = ((Fernando)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.FERNANDO_LAGUERRA));
						if(fernandoRole.getInDanger().contains(target.getUniqueId())) {
							fernandoRole.getInDanger().remove(target.getUniqueId());
							fernandoRole.reduceHealUsed();
							target.setHealth(target.getMaxHealth());
							target.sendMessage(Main.PREFIX + "§aVous recevez les soins de Fernando Laguerra");
							player.sendMessage(Main.PREFIX + "§cVous venez de soigner " + target.getDisplayName() + " !");
						} else {
							player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas/plus le sauver !");
						}
						return true;
					}
					
				} else if(args.length > 1 && args[0].equalsIgnoreCase("fernandoignore")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.FERNANDO_LAGUERRA) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.FERNANDO_LAGUERRA).contains(player.getUniqueId())) {

						Player target = Bukkit.getPlayer(args[1]);
						Fernando fernandoRole = ((Fernando)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.FERNANDO_LAGUERRA));
						if(fernandoRole.getInDanger().contains(target.getUniqueId())) {
							fernandoRole.getInDanger().remove(target.getUniqueId());
							player.sendMessage(Main.PREFIX + "§bFernando \u00BB Il saura survivre sans moi.");
						} else {
							player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas/plus le sauver !");
						}
						return true;
						
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("chat")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.FERNANDO_LAGUERRA) &&
						main.getCurrentGameManager().isRoleAttributed(RoleType.MARINCHE)) {
						
						List<Player> targets = new ArrayList<Player>();
						RoleType from = RoleType.FERNANDO_LAGUERRA;
						
						if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.FERNANDO_LAGUERRA).contains(player.getUniqueId())) {
							
							for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MARINCHE)) {
								if(main.getCurrentGameManager().isPlaying(id)) {
									Player target = Bukkit.getPlayer(id);
									if(target != null) {
										targets.add(target);
									}
									
								}
							}
							
							
							
						} else if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MARINCHE).contains(player.getUniqueId())) {
							from = RoleType.MARINCHE;
							for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.FERNANDO_LAGUERRA)) {
								if(main.getCurrentGameManager().isPlaying(id)) {
									Player target = Bukkit.getPlayer(id);
									if(target != null) {
										targets.add(target);
									}
									
								}
							}
						} else {
							Main.logIfDebug("wut");
							return false;
						}
						
						StringJoiner joiner = new StringJoiner(" ");
						for(int i = 1; i < args.length; i++) joiner.add(args[i]);
						
						for(Player target : targets) {
							target.sendMessage(Main.PREFIX + ((from == RoleType.FERNANDO_LAGUERRA) ? "§aFernando" : "§aMarinché") + " \u00BB " + joiner.toString());
						}
						player.sendMessage(Main.PREFIX + ((from == RoleType.FERNANDO_LAGUERRA) ? "§aFernando" : "§aMarinché") + " \u00BB " + joiner.toString());
						return true;
						
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("manipuler")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.MARINCHE) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MARINCHE).contains(player.getUniqueId())) {
						
						Player target = Bukkit.getPlayer(args[1]);
						
						if(target != null) {
							if(target.equals(player)) {
								player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas vous manipuler vous même... Si ?");
								return false;
							}
							
							
							Marinche marinche = ((Marinche)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MARINCHE));
							
							int episode = (int)(System.currentTimeMillis() - marinche.gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
							if(!marinche.getUsagesByEpisode().containsKey(episode)) {
								marinche.getUsagesByEpisode().put(episode, (short)1);
							} else {
								if(marinche.getUsagesByEpisode().get(episode) >= 2) {
									player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus manipuler qui que ce soit pour cet épisode...");
									return false;
								} else marinche.getUsagesByEpisode().put(episode, (short)2);
							}
							
							if(marinche.getManipulatedPlayers().contains(target.getUniqueId())) {
								player.sendMessage(Main.PREFIX + "§cCe joueur est déjà manipulé.");
								return true;
							}
							
							Scoreboard marincheScoreboard = marinche.getManipulationScoreboard();
							Objective manipObjective = marincheScoreboard.getObjective("manipulated");
							
							Score scoreOfTarget = manipObjective.getScore((OfflinePlayer)target);
							scoreOfTarget.setScore(0);
							
							player.setScoreboard(marincheScoreboard);
							
							new BukkitRunnable() {
								
								@Override
								public void run() {
									
									if(player.getLocation().distance(target.getLocation()) <= 20) {
										if(scoreOfTarget.getScore() < 100) {
											scoreOfTarget.setScore(scoreOfTarget.getScore() + 1);
										} else {
											marinche.getManipulatedPlayers().add(target.getUniqueId());
											player.sendMessage(Main.PREFIX + "§cVous avez réussi a manipuler " + target.getDisplayName());
											cancel();
										}
									}
									
								}
							}.runTaskTimer(main, 0, 3l);
							player.sendMessage(Main.PREFIX + "§cVous avez commencé à manipuler " + target.getDisplayName());
							return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide !");
							return true;
						}
					}
				} else if(args.length > 0 && args[0].equalsIgnoreCase("manipulation")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.MARINCHE) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MARINCHE).contains(player.getUniqueId())) {
						
						Marinche marinche = ((Marinche)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.MARINCHE));

						int episode = (int)(System.currentTimeMillis() - marinche.gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
						if(marinche.getManipulationsByEpisode().containsKey(episode) && marinche.getManipulationsByEpisode().get(episode) >= 2) {
							player.sendMessage(Main.PREFIX + "§cVous avez déjà manipulé 2 fois lors de cet épisode !");
							return true;
						}
						
						List<UUID> manipulated = marinche.getManipulatedPlayers();
						
						Inventory inv = Bukkit.createInventory(null, (int)(manipulated.size() / 9) * 9 + 9, "§8Joueurs manipulés");
						for(UUID id : manipulated) {
							if(Bukkit.getOfflinePlayer(id).isOnline()) {
								ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
								SkullMeta meta = (SkullMeta)skull.getItemMeta();
								meta.setLore(Arrays.asList("§r§6§lClic gauche§r§6 pour voler 1/5 des pommes dorées","§r§e§lClic molette§r§e pour connaître son rôle","§r§c§lClic droit§r§c pour lui retirer un coeur permanent"));
								meta.setOwner(Bukkit.getPlayer(id).getName());
								meta.setDisplayName("§cActions pour " + meta.getOwner());
								skull.setItemMeta(meta);
								inv.addItem(skull);
							}
						}
						
						player.openInventory(inv);
					}
					
				} else if(args.length > 0 && args[0].equalsIgnoreCase("mission")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.GOMEZ) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.GOMEZ).contains(player.getUniqueId())) {
						
						List<UUID> coalitionMembers = new ArrayList<UUID>();
												
						for(UUID uuid : main.getCurrentGameManager().roleOfPlayer().keySet()) {
							if(main.getCurrentGameManager().isPlaying(uuid) && main.getCurrentGameManager().getTeamOfPlayer(uuid) == GameTeam.COALITION) {
								coalitionMembers.add(uuid);
							}
						}
						
						if(coalitionMembers.size() <= 0) {
							player.sendMessage(Main.DEBUG + "§cUne erreur est survenue, veuillez en informer le développeur");
							return true;
						}
						
						UUID chosenUUID = coalitionMembers.get(new Random().nextInt(coalitionMembers.size()));
						Gomez gomezRole = ((Gomez)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.GOMEZ));
						
						if(coalitionMembers.size() == 0) {
							player.sendMessage(Main.PREFIX + "§cIl n'y a plus personne à traquer.");
							return false;
						} else {
							while(gomezRole.getTrackedPlayers().containsKey(chosenUUID)) {
								chosenUUID = coalitionMembers.get(new Random().nextInt(coalitionMembers.size()));
							}
						}
						
						OfflinePlayer chosenTarget = Bukkit.getOfflinePlayer(chosenUUID);
						
						gomezRole.getTrackedPlayers().put(chosenUUID, player.getUniqueId());
						
						player.sendMessage(Main.PREFIX + "§cMa nouvelle mission concerne " + chosenTarget.getName() + "...");
						return true;
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("poison")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.HORTENSE) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.HORTENSE).contains(player.getUniqueId())) {
						
						if(hortense_mco_poison_usages != 0) {
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								
								Hortense hortenseRole = ((Hortense)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.HORTENSE));
								hortenseRole.getPoisonedApple().add(target.getUniqueId());
								
								player.sendMessage(Main.PREFIX + "§2Vous venez d'empoisonner la pomme dorée de " + target.getDisplayName());
								hortense_mco_poison_usages--;
								return true;
								
							} else {
								player.sendMessage(Main.PREFIX + "§cVeuillez entrer un pseudonyme valide !");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus utiliser cette commande !");
						}

					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("astres")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.NOSTRADAMUS) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.NOSTRADAMUS).contains(player.getUniqueId())) {
						
						if(nostradamus_mco_astre_usages != 0) {
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								player.sendMessage(Main.PREFIX + "§cLes astres de " + target.getDisplayName() + " surveillent ce lieu : " + target.getLocation().getBlockX() + " | " + target.getLocation().getBlockZ());
								nostradamus_mco_astre_usages--;
								return true;
							} else {
								player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus vous servir de cette commande !");
							return true;
						}
						
					}
				} else if(args.length > 0 && args[0].equalsIgnoreCase("prediction")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.NOSTRADAMUS) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.NOSTRADAMUS).contains(player.getUniqueId())) {

						if(nostradamus_mco_prediction != 0) {
							Nostradamus nostradamusRole = ((Nostradamus)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.NOSTRADAMUS));
							if(nostradamusRole.getLastDeathPredictionUsage() + main.getConfig().getInt("timers.nostradamus-prediction-cooldown") * 60 * 1000 < System.currentTimeMillis()) {
								nostradamusRole.setLastDeathPredictionUsage(System.currentTimeMillis());
								player.sendMessage(Main.PREFIX + "§cVous commencez à prédire les morts des joueurs ...");
								nostradamus_mco_prediction--;
							} else {
								player.sendMessage(Main.PREFIX + "§cVous avez déjà commencé les morts des joueurs ...");
							}
							return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVous ne pouvez plus vous servir de cette commande !");
							return true;
						}
						
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("cible")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.PIZARRO) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.PIZARRO).contains(player.getUniqueId())) {
						
						Pizarro pizarroRole = ((Pizarro)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.PIZARRO));
						
						if(pizarroRole.getPizarroTarget() == null) {
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								
								if(pizarroRole.getAlreadyBeenTargeted().contains(target.getUniqueId())) {
									player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas cibler deux fois le même joueur");
									return true;
								}
								
								if(pizarroRole.getAlreadyBeenTargeted().size() >= 2) {
									player.sendMessage(Main.PREFIX + "§cVous ne pouvez pas cibler plus de deux joueurs dans une partie");
									return true;
								}
								
								target.setMaxHealth(target.getMaxHealth() - 3*2);
								for(Player p : Bukkit.getOnlinePlayers()) {
									p.sendMessage(Main.PREFIX + "§cPizarro \u00BB " + target.getDisplayName() + " sera dorénavant ma cible !");
								}
								
								pizarroRole.setPizarroTarget(target.getUniqueId());
								
								new BukkitRunnable() {
									
									@Override
									public void run() {
										if(!pizarroRole.getAlreadyBeenTargeted().contains(target.getUniqueId()) && pizarroRole.getPizarroTarget().equals(target.getUniqueId())) {
											target.setMaxHealth(target.getMaxHealth() + 3*2); // give max health back
											pizarroRole.getAlreadyBeenTargeted().add(target.getUniqueId());
											pizarroRole.setPizarroTarget(null);
											for(Player p : Bukkit.getOnlinePlayers()) {
												p.sendMessage(Main.PREFIX + "§cPizarro \u00BB " + target.getDisplayName() + " a survécu...");
											}
										}
									}
								}.runTaskLater(main, 5 * 60 * 20);
								
								return true;
							} else {
								player.sendMessage(Main.PREFIX + "§cVeuillez indiquer un pseudonyme valide");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVous traquez déjà quelqu'un !");
							return true;
						}
						
					}
				} else if(args.length > 0 && args[0].equalsIgnoreCase("garnison")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.YUPANQUI) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.YUPANQUI).contains(player.getUniqueId())) {
						
						Yupanqui yupanquiRole = ((Yupanqui)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.YUPANQUI));
						
						if(yupanqui_mco_garnison_usages != 0) {
							player.sendMessage(Main.PREFIX + "§cVous créez une zone qui vous rend plus combattif et déterminé !");
							new BukkitRunnable() {
								
								int timer = 2 * 60;
								final int points = 500;
								final int radius = 50;
								final Location origin = player.getLocation();
								
								@Override
								public void run() {
									if(timer != 0) {
										
										for (int i = 0; i < points; i++) {
										    double angle = 2 * Math.PI * i / points;
										    double x = radius * Math.sin(angle);
										    double z = radius * Math.cos(angle);

									    	sendParticle(player, origin.clone().add(x, 0d, z), EnumParticle.REDSTONE, 241, 141, 6);
									    	sendParticle(player, origin.clone().add(x, 1d, z), EnumParticle.REDSTONE, 241, 141, 6);
									    	sendParticle(player, origin.clone().add(x, 2d, z), EnumParticle.REDSTONE, 241, 141, 6);
										}
										
										if(player.getLocation().distance(origin) <= radius) {
											player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 1), true);
											player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,   50, 0), true);
											if(!player.getScoreboard().equals(yupanquiRole.getLifeScoreboard())) {
												player.setScoreboard(yupanquiRole.getLifeScoreboard());
											}
										} else if(player.getScoreboard().equals(yupanquiRole.getLifeScoreboard())) {
											player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
										}
										
										timer--;
									} else {
										if(player.getScoreboard().equals(yupanquiRole.getLifeScoreboard())) {
											player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
										}
										cancel();
									}
								}
							}.runTaskTimer(main, 0, 20);
							yupanqui_mco_garnison_usages--;
							return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVous avez déjà trop utilisé cette capacité !");
							return true;
						}
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("piege")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.CALMEQUE) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.CALMEQUE).contains(player.getUniqueId())) {
						
						if(calmeque_mco_piege_usages != 0) {
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								
								target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 30 * 20, 250), true);
								player.sendMessage(Main.PREFIX + "§cVous avez piégé " + target.getDisplayName() + " pour 30 secondes !");
								calmeque_mco_piege_usages--;
								return true;
								
							} else {
								player.sendMessage(Main.PREFIX + "§cVous devez spécifier un joueur valide !");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVous avez déjà trop utilisé cette capacité !");
							return true;
						}
						
					}
				} else if(args.length > 1 && args[0].equalsIgnoreCase("duel")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.TAKASHI) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAKASHI).contains(player.getUniqueId())) {
							
						Takashi takashiRole = ((Takashi)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAKASHI));
						
						if(takashiRole.canUseDuel()) {
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								
								takashiRole.getCanTakeDamageFrom().put(player.getUniqueId(), target.getUniqueId());
								takashiRole.getCanTakeDamageFrom().put(target.getUniqueId(), player.getUniqueId());
								
								new BukkitRunnable() {
									
									@Override
									public void run() {

										takashiRole.getCanTakeDamageFrom().remove(target.getUniqueId());
										takashiRole.getCanTakeDamageFrom().remove(player.getUniqueId());
									}
								}.runTaskLater(main, 30 * 20);
								
								takashiRole.markThisEpisodeDuelAsUsed();
								return true;
								
							} else {
								player.sendMessage(Main.PREFIX + "§cVous devez spécifier un joueur valide !");
								return true;
							}
						} else {
							player.sendMessage(Main.PREFIX + "§cVous avez déjà utilisé cette capacité durant cet épisode !");
							return true;
						}
						
					}
				} else if(args.length > 5 && args[0].equalsIgnoreCase("taosave") && args[1].equalsIgnoreCase("accept")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.TAO) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO).contains(player.getUniqueId())) {
							
						Tao taoRole = ((Tao)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.TAO));
						
							if(taoRole.getCanSaveACity()) {
								
								String worldname = args[2];
								int x = Integer.parseInt(args[3]);
								int y = Integer.parseInt(args[4]);
								int z = Integer.parseInt(args[5]);
								
								taoRole.setSavedCity(new Location(Bukkit.getWorld(worldname), x, y, z));
								
								taoRole.setCanSaveACity(false);
							} else {
								player.sendMessage(Main.PREFIX + "§cVous avez déjà sauvé une cité !");
								return true;
							}
							
						}
				} else if(args.length > 0 && args[0].equalsIgnoreCase("inca")) {
					if( main.getCurrentGameManager().isRoleAttributed(RoleType.KRAKA) && 
						main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.KRAKA).contains(player.getUniqueId())) {
								
							Kraka krakaRole = ((Kraka)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.KRAKA));
							
							if(krakaRole.hasUsedKey()) {
								if(krakaRole.getLastCityEntryPointSpawn() != null) {
									if(krakaRole.getLastCityIncaUsage() + 25 * 60 * 1000 <= System.currentTimeMillis()) {
										
										Location loc = krakaRole.getLastCityEntryPointSpawn();
										
										boolean isXChosen = new Random().nextFloat() < 0.5f;
										
										if(isXChosen) {
											player.sendMessage(Main.PREFIX + "§cUne cité est apparue en X=" + loc.getBlockX());
										} else {
											player.sendMessage(Main.PREFIX + "§cUne cité est apparue en Z=" + loc.getBlockZ());
										}
										
										krakaRole.setLastCityIncaUsage(System.currentTimeMillis());
										return true;
									} else {
										player.sendMessage(Main.PREFIX + "§cVous avez déjà utilisé cette compétence il y a peu !");
										return true;
									}
								} else {
									player.sendMessage(Main.PREFIX + "§cAucune cité n'est apparue pour le moment !");
									return true;
								}
							} else {
								player.sendMessage(Main.PREFIX + "§cCette compétence n'est obtensible qu'en utilisant votre clé !");
								return true;
							}
						} 
					} else if(args.length > 0 && args[0].equalsIgnoreCase("leave")) {
						if( main.getCurrentGameManager().isRoleAttributed(RoleType.MENATOR) && 
							main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENATOR).contains(player.getUniqueId())) {
							
							if(player.getWorld().getName().equalsIgnoreCase("cities")) {
								
								int    x = new Random().nextInt(90),
									   z = new Random().nextInt(90),
									   y = Bukkit.getWorld("world").getHighestBlockYAt(x, z) + 1;
								
								player.teleport(new Location(Bukkit.getWorld("world"), x, y, z));
								player.sendMessage(Main.PREFIX + "§cVous venez de sortir de la cité");
								
								return true;
							} else {
								player.sendMessage(Main.PREFIX + "§cVous êtes déjà hors d'une cité !");
								return true;
							}
							
						}
					} else if(args.length > 0 && args[0].equalsIgnoreCase("me")) {
						if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) {
							Role affectedRole = main.getCurrentGameManager().getRoleFromRoleType().get(main.getCurrentGameManager().roleOfPlayer().get(player.getUniqueId()));
							player.sendMessage("§e----------------------------------------------------\n"
							           + "\u00bb Vous jouez " + affectedRole.getRole().getRoleName() + " de " + affectedRole.getTeamOfPlayer().getTeamName() + ".\n" 
							           + "§e----------------------------------------------------\n"
							           + "\n§6Explications de vôtre rôle : §r\n" + new RolesUtil(main.getCurrentGameManager()).replacedString(affectedRole.getRelativeExplications())
			       				       + "\n§e----------------------------------------------------\n");
							return true;
						} else {
							player.sendMessage(Main.PREFIX + "§cVous n'êtes pas en train de jouer !");
							return true;
						}
					} else if(args.length > 0 && args[0].equalsIgnoreCase("nef")) {
						if( main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && 
							main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.AMBROSIUS).contains(player.getUniqueId())) {
							
							Ambrosius ambrosiusRole = ((Ambrosius)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.AMBROSIUS));
							
							// check if ambrosius can teleport players to his nef now :
							long currentTime = System.currentTimeMillis();
							currentTime -= ambrosiusRole.gameStart;
							
							if(currentTime <= 30 * 60 * 1000) {
								player.sendMessage(Main.PREFIX + "Vous ne pouvez téléporter personne avant 30 minutes de jeu !");
								return true;
							}
							currentTime /= 60 * 1000; // convert time in minutes
							
							if(currentTime % 10 == 0) { // si on est sur une dizaine de minute
								List<UUID> playersToTeleportToHisNef = new ArrayList<>();
								List<UUID> hourglassMembers = new ArrayList<>();
								
								for(UUID uuid : main.getCurrentGameManager().roleOfPlayer().keySet()) {
									if(main.getCurrentGameManager().isPlaying(uuid)) {
										if(main.getCurrentGameManager().getTeamOfPlayer(uuid) == GameTeam.HOURGLASS) {
											hourglassMembers.add(uuid);
										}
									}
								}
								if(hourglassMembers.size() == 0) {
									player.sendMessage(Main.PREFIX + "Vous ne pouvez plus téléporter qui que ce soit, ils sont tous tombés au combat");
								}
								
								for(int i = 0; i < 3; i++) {
									if(hourglassMembers.size() <= 0) break;
									if(hourglassMembers.size() == 1) {
										playersToTeleportToHisNef.add(hourglassMembers.get(0));
										hourglassMembers.remove(0);
										break;
									}
									int randomIndex = new Random().nextInt(hourglassMembers.size());
									playersToTeleportToHisNef.add(hourglassMembers.get(randomIndex));
									hourglassMembers.remove(randomIndex-1);
								}
								
								for(UUID id : playersToTeleportToHisNef) {
									Bukkit.getPlayer(id).sendMessage(Main.PREFIX + "Vous avez été convoqué par Ambrosius, vous serez téléporté dans 30 secondes.");
								}
								
								new BukkitRunnable() {
									
									@Override
									public void run() {
										for(UUID id : playersToTeleportToHisNef) {
											Bukkit.getPlayer(id).teleport(player);
										}
									}
								}.runTaskLater(main, 30*20);
							}
							
						}
					} else if(args.length > 0 && args[0].equalsIgnoreCase("choix")) {
						if( main.getCurrentGameManager().isRoleAttributed(RoleType.HIPPOLYTE) && 
							main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.HIPPOLYTE).contains(player.getUniqueId())) {
							
							Hippolyte hippolyteRole = ((Hippolyte)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.HIPPOLYTE));
							
							if(hippolyteRole.getRemainingChoices() > 0 && hippolyteRole.getLastChoiceTimestamp() + 10 * 60 * 1000 <= System.currentTimeMillis()) {
								
								Inventory inv = Bukkit.createInventory(null, 9 * 1, "§8Choix d'effet");
								inv.setItem(1,  new ItemManager.ItemBuilder(Material.IRON_SWORD, 1, "§4Force").build());
								inv.setItem(3,  new ItemManager.ItemBuilder(Material.CHAINMAIL_BOOTS, 1, "§bSpeed").build());
								inv.setItem(5,  new ItemManager.ItemBuilder(Material.DIAMOND_CHESTPLATE, 1, "§5Résistance").build());
								inv.setItem(7,  new ItemManager.ItemBuilder(Material.WATER_BUCKET, 1, "§2Anti-chutes").build());
								player.openInventory(inv);
								
							} else {
								player.sendMessage(Main.PREFIX + "Vous ne pouvez pas encore/plus accéder à votre choix");
							}
							
							return true;
						}
					}else if(args.length > 1 && args[0].equalsIgnoreCase("analyse")) {
						if( main.getCurrentGameManager().isRoleAttributed(RoleType.ESTEBAN) && 
							main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.ESTEBAN).contains(player.getUniqueId())) {
							
							if(esteban_mco_analyse_usages <= 0) {
								player.sendMessage(Main.PREFIX + "Vous ne pouvez plus utiliser cette commande ! ");
								return true;
							}

							Esteban estebanRole = ((Esteban)main.getCurrentGameManager().getRoleFromRoleType().get(RoleType.ESTEBAN));
							
							if((estebanRole.getLastAnalyseUsage() + 5 * 60 * 1000 > System.currentTimeMillis())) {
								player.sendMessage(Main.PREFIX + "Vous devez attendre 5min entre chaque utilisation de cette compétence !");
								return true;
							}
							
							Player target = Bukkit.getPlayer(args[1]);
							if(target != null) {
								if(main.getCurrentGameManager().isPlaying(target.getUniqueId())) {
									RoleType role = main.getCurrentGameManager().roleOfPlayer().get(target.getUniqueId());
									
									GameTeam team = GameTeam.UNDEFINED;
									
									int rd = new Random().nextInt(100);
									
									switch (role) {
									case ATHANAOS:
										if(rd < 75) team = GameTeam.COALITION;
										else team = role.getRoleTeam();
										break;
									case CINESUS:
										if(rd < 50) team = GameTeam.COALITION;
										else team = role.getRoleTeam();
										break;
									case VIRACOCHA:
										if(rd < 75) team = GameTeam.HOURGLASS;
										else team = role.getRoleTeam();
										break;
									case ISABELLA_LAGUERRA:
										team = GameTeam.COALITION;
										break;

									default:
										
										if(rd < 75) {
											team = role.getRoleTeam();
										} else {
											ArrayList<GameTeam> teams = new ArrayList<GameTeam>();
											
											for(GameTeam t : GameTeam.values()) if(t != GameTeam.UNDEFINED) teams.add(t);
											teams.remove(role.getRoleTeam());
											
											team = teams.get(new Random().nextInt(teams.size()));											
										}
										
										break;
									}
									
									player.sendMessage(Main.PREFIX + "Je pense que "  + target.getDisplayName() + " appartient à " + team.getTeamName());
									estebanRole.setLastAnalyseUsage(System.currentTimeMillis());
									return true;
								} else {
									player.sendMessage(Main.PREFIX + "Veuillez indiquer un joueur encore en vie !");
								}
							} else {
								player.sendMessage(Main.PREFIX + "Veuillez indiquer un joueur valide !");
							}
							
						}
					}
			

		}
		
		return false;
	}
	
	 public void sendParticle(Player player, Location loc, EnumParticle p, float red, float green, float blue) {
	        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(p, true, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), red/255, green/255, blue/255, 1, 0);
	        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	 }

}
