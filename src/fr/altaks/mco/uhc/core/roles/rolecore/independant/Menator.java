package fr.altaks.mco.uhc.core.roles.rolecore.independant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.RolesUtil;

public class Menator implements Role {
	
	private List<UUID> infectedOlmeques = new ArrayList<UUID>();
	private Main main;
	
	public Menator(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.MENATOR;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous êtes en duo avec §dCalmèque§r, votre objectif commun est donc de tuer tous les joueurs de la partie.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous verrez attribuer l'effet §bRésistance I§r de manière permanente.\n"
	   + "Dès l'apparition des §6Cités d'Or§r, vous aurez la possibilité de vous y téléporter quand vous le voudrez, dans celle-ci, vous obtenez l'effet §cForce I§r.\n"
	   + "Vous êtes avertis une avant l’apparition d’une §6Cité d’Or§r et obtenez ses coordonnées.\n"
	   + "Vous pouvez sortir d’une cité avec la commande §2\"/mco leave\"§r\n"
	   + "Quand un joueur entre dans une §6Cité d’Or§r, vous obtenez son camp, les §dSolitaires§r comptent comme membre de §aLa Coalition§r. \n"
	   + "Si jamais vous restez autour d'un §aSoldat Olmèque§r durant §bcinq minutes§r, celui-ci se devra de gagner avec vous et §dCalmèque§r.\n"
	   + "Vous gagnez un §bcoeur supplémentaire§r pour chaque infection.\n"
	   + "Voici l’identité de §dCalmèque§r : %independant-calmeque%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> menators = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				menators.add(offlinePlayer.getPlayer());
			}
		}
		
		ArrayList<String> allPlayers = new ArrayList<>();
		ArrayList<String> olmeques = new ArrayList<>();
		
		if(manager.isRoleAttributed(RoleType.OLMEQUE_SOLDIER)) {
			for(UUID player : manager.roleOfPlayer().keySet()) {
				if(manager.isPlaying(player)) {
					if(manager.roleOfPlayer().get(player) == RoleType.OLMEQUE_SOLDIER) {
						olmeques.add(Bukkit.getOfflinePlayer(player).getName());
					} else allPlayers.add(Bukkit.getOfflinePlayer(player).getName());
				}
			}
			String message = Main.PREFIX + "§cVoici votre liste " + RolesUtil.getListOfPlayerContaining(olmeques, allPlayers, 8);
			for(Player menator : menators) {
				menator.sendMessage(message);
			}
		}

		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player menator : menators) {
					menator.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 0), true);
					if(menator.getWorld().getName().equals("cities")) {
						menator.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 50, 0), true);
							
					}
				}
			}
		}.runTaskTimer(manager.getMain(), 0, 20);
		
		if(manager.isRoleAttributed(RoleType.OLMEQUE_SOLDIER)) {
			new BukkitRunnable() {
				
				private final int finishContaminationTime = 10;
				private HashMap<UUID, Integer> contaminationState = new HashMap<>();
				
				@Override
				public void run() {
					for(Player menator : menators) 
						for(Entity nearbyEntity : menator.getNearbyEntities(5, 5, 5)) {
							if(manager.isPlaying(nearbyEntity.getUniqueId()) && manager.getPlayersThatOwnsRole(RoleType.OLMEQUE_SOLDIER).contains(nearbyEntity.getUniqueId())) {
								if(infectedOlmeques.contains(nearbyEntity.getUniqueId())) continue;
								
								if(!contaminationState.containsKey(nearbyEntity.getUniqueId())) {
									contaminationState.put(nearbyEntity.getUniqueId(), 1);
								} else {
									contaminationState.put(nearbyEntity.getUniqueId(), contaminationState.get(nearbyEntity.getUniqueId()) + 1);
									if(contaminationState.get(nearbyEntity.getUniqueId()) >= finishContaminationTime) {
										infectedOlmeques.add(nearbyEntity.getUniqueId());
										this.contaminationState.remove(nearbyEntity.getUniqueId());
										for(Player menator_ : menators) {
											menator_.setMaxHealth(menator_.getMaxHealth() + 2);
										}
									}
								}
							}
						}
				}
			};
		}
	}
	
	@EventHandler
	public void onPlayerEnterCity(PlayerChangedWorldEvent event) {
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.MENATOR)) return;
		if(!event.getPlayer().getWorld().getName().equalsIgnoreCase("cities")) return;
		if(main.getCurrentGameManager().isPlaying(event.getPlayer().getUniqueId())) {
			GameTeam teamOfPlayer = main.getCurrentGameManager().getTeamOfPlayer(event.getPlayer().getUniqueId());
			if(teamOfPlayer == GameTeam.COALITION) return;
			for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.MENATOR)) {
				if(main.getCurrentGameManager().isPlaying(id)) {
					Bukkit.getPlayer(id).sendMessage(Main.PREFIX + "§c" + event.getPlayer().getName() + " (" + teamOfPlayer.getTeamName() + "§c) vient d'entrer dans une cité !");
				}
			}
		}
	}

}
