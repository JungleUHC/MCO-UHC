package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;

public class Hortense implements Role {
	
	private Main main;
	private List<UUID> poisonnedApple = new ArrayList<UUID>();
	private ArrayList<UUID> alreadyToldHourglassMembers = new ArrayList<UUID>();
	private int timeBetweenCoalitionGuesses = 20;
	private int nearbyMembersWeaknessDuration = 120;
	
	public Hortense(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.HORTENSE;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, et cela, à chaque début d'épisode, vous vous verrez confier le pseudonyme d'un membre de l'§cOrdre du Sablier§r\n"
	   + "Vous possédez également la commande §2\"/mco poison\"§r, celle-ci infecte une pomme dorée du joueur ciblée, lui faisant perdre §b2 coeurs non permanents§r. §oVous ne disposez que de cinq utilisations pour ce pouvoir.§r\n"
	   + "Si vous venez à mourir, tous les membres de la §aCoalition§r présents dans un rayon de §b30 blocs§r obtiennent l’effet §4Faiblesse 1§r pendant §b2 minutes.§r";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		// TODO Auto-generated method stub
		for(UUID hortense : players) {
			this.alreadyToldHourglassMembers.add(hortense);
		}
		timeBetweenCoalitionGuesses = main.getConfig().getInt("timers.hortense-hourglass-members-guess-cooldown");
		nearbyMembersWeaknessDuration = main.getConfig().getInt("timers.hortense-nearby-members-weakness-duration");

		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				for(OfflinePlayer player : manager.getTeamPlayers(GameTeam.HOURGLASS)) {
					if(!alreadyToldHourglassMembers.contains(player.getUniqueId()) && manager.isPlaying(player.getUniqueId())) {
						
						for(UUID id : players) {
							if(manager.isPlaying(id)) {
								Bukkit.getPlayer(id).sendMessage(Main.PREFIX + "§6Vous comprenez que " + player.getName() + " est un joueur de l'Ordre du Sablier");
							}
						}
						alreadyToldHourglassMembers.add(player.getUniqueId());
						break;
					}
				}
				
			}
		}.runTaskTimer(main, 0, timeBetweenCoalitionGuesses * 60 * 20);

	}
	
	@EventHandler
	public void onPoisonedAppleGetEaten(PlayerItemConsumeEvent event) {
		if(this.poisonnedApple.contains(event.getPlayer().getUniqueId())) {
			if(event.getItem().getType() == Material.GOLDEN_APPLE) {
				event.setCancelled(true);
				event.getPlayer().damage(4);
				this.poisonnedApple.remove(event.getPlayer().getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void onHortenseDie(EntityDeathEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			if(main.getCurrentGameManager().isPlaying(player.getUniqueId())) {
				if(main.getCurrentGameManager().roleOfPlayer().get(player.getUniqueId()) == RoleType.HORTENSE) {
					
					List<Entity> nearbyEntities = player.getNearbyEntities(30, 30, 30);
					for(Entity entity : nearbyEntities) {
						if(main.getCurrentGameManager().isPlaying(entity.getUniqueId())) {
							if(main.getCurrentGameManager().getTeamOfPlayer(entity.getUniqueId()) == GameTeam.COALITION) {
								((Player)entity).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, nearbyMembersWeaknessDuration * 20, 0), true);
							}
						}
					}
					
				}
			}
		}
	}

	public List<UUID> getPoisonedApple() {
		return poisonnedApple;
	}

}
