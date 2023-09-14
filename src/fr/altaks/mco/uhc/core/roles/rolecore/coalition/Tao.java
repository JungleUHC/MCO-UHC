package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;
import fr.altaks.mco.uhc.util.ItemManager;
import fr.altaks.mco.uhc.util.ItemManager.BookBuilder;

public class Tao implements Role {
	
	private HashMap<UUID, Boolean> hasKilledACoalitionMember = new HashMap<>();
	private UUID lastHourglassKiller = null;
	private UUID pichuTarget = null;
	
	private boolean canSaveACity = true;
	private Location savedCity = null;

	private int taoBookUpdateRate = 20;

	private Main main;
	
	public Tao(Main main) {
		this.main = main;
	}

	@EventHandler
	public void onPlayerKill(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
		
		// damage has to be lethal
		if(event.getFinalDamage() < ((Player)event.getEntity()).getHealth()) return;
		
		Player killer = (Player)event.getDamager();
		Player victim = (Player)event.getEntity();
		
		if(main.getCurrentGameManager().getTeamOfPlayer(killer.getUniqueId()) == GameTeam.HOURGLASS) {
			lastHourglassKiller = killer.getUniqueId();
		}
		
		if(main.getCurrentGameManager().getTeamOfPlayer(victim.getUniqueId()) == GameTeam.COALITION) {
			hasKilledACoalitionMember.put(killer.getUniqueId(), true);
			Main.logIfDebug(Main.PREFIX + "TAO : kill from {" + killer.getDisplayName() + "} of coalition member {" + victim.getDisplayName() + "}.");
		}
		
		if(killer.getUniqueId().equals(pichuTarget)) {
			for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO)) {
				Player tao = Bukkit.getPlayer(id);
				if(tao != null) tao.sendMessage(Main.PREFIX + "§bTao \u00BB §c" + killer.getDisplayName() + " vient de tuer " + victim.getDisplayName() + "...");
			}
		} else if(victim.getUniqueId().equals(pichuTarget)) {
			for(UUID id : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO)) {
				Player tao = Bukkit.getPlayer(id);
				if(tao != null) tao.sendMessage(Main.PREFIX + "§bTao \u00BB §c" + victim.getDisplayName() + " vient de se faire tuer par " + killer.getDisplayName() + "...");
			}
		}
	}
	
	@EventHandler
	public void onPlayerEatGapple(PlayerItemConsumeEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem().getType() == Material.GOLDEN_APPLE) {
			if(event.getPlayer().getUniqueId().equals(pichuTarget)) {
				for(UUID uuid : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.TAO)) {
					Player tao = Bukkit.getPlayer(uuid);
					if(tao != null) {
						tao.sendMessage(Main.PREFIX + "§bTao §7\u00BB §c" + event.getPlayer().getDisplayName() + " vient de manger une pomme dorée");
					}
				}
			}
		}
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.TAO;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de la §aCoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce, des rôles vous recevez un §aLivre Écrit§r qui vous permet, à chaque fin d'épisode, de lire le rôle du dernier membre de L'§cOrdre du Sablier§r ayant fait un kill.\n"
	   + "Si vous êtes présent dans une §6Cité d'Or§r et que cette dernière voit son coeur détruit, vous disposerez de seulement 10 secondes pour choisir de la sauver ou non,si vous choisissez de la sauver, alors la destruction de la cité sera annulée et toutes les personnes présentes à l'intérieur seront téléportées à l'extérieur et le cœur réapparaîtra, vous disposez d’une seule utilisation pour ce pouvoir.\n"
	   + "Vous êtes quelqu’un de très méfiant et intelligent par conséquent il possède la commande §2\"/mco trust\"§r qui vous permettra de savoir si oui ou non le joueur à déjà tué un membre de la §aCoalition§r. Utilisable §b3 fois§r dans la partie.\n"
	   + "Vous possédez la commande §2\"/mco Pichu\"§r qui vous permet de surveiller les actions du joueurs ciblé savoir s’il mange des pommes dorées s'il tue des joueurs ou connaître même son meurtrier s’il se fait tuer.\n"
	   + " Si vous effectuez la commande sur 2 joueurs différents §aPichu§r surveillera uniquement les actions du dernier joueur ciblé.\n"
	   + "Si jamais §aEsteban§r et §aZia§r viennent à mourir avant §cAmbrosius§r, vous rejoindrez l'§cOrdre du sablier§r.\n";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		for(UUID playersOfGame : manager.roleOfPlayer().keySet()) {
			hasKilledACoalitionMember.put(playersOfGame, false);
		}
		
		List<Player> taos = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) taos.add(offlinePlayer.getPlayer());
		}


		taoBookUpdateRate = main.getConfig().getInt("timers.tao-book-update-rate");

		for(Player tao : taos) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					
					int slotToPlace = -1;
					for(int slot = 0; slot < tao.getInventory().getSize(); slot++) {
						if(tao.getInventory().getItem(slot) == null) continue;
						if(ItemManager.compare(tao.getInventory().getItem(slot), Artifacts.RoleItems.LIVRE)) {
							tao.getInventory().setItem(slot, null);
							slotToPlace = slot;
							break;
						}
					}
					
					BookBuilder builder = new ItemManager.BookBuilder(Artifacts.RoleItems.LIVRE.clone());
					
					if(lastHourglassKiller != null) {
						builder.addPages("Le dernier membre de l'Ordre du Sablier ayant tué est " + main.getCurrentGameManager().roleOfPlayer().get(lastHourglassKiller).getRoleName());
					} else {
						builder.addPages("Aucun membre de l'Ordre du Sablier n'a commis de meurtre pour le moment ...");
					}
					
					if(tao.getItemOnCursor() != null && ItemManager.compare(tao.getItemOnCursor(), Artifacts.RoleItems.LIVRE)) {
						tao.setItemOnCursor(builder.build());
					} else if(slotToPlace != -1){
						tao.getInventory().setItem(slotToPlace, builder.build());
					} else {
						tao.getInventory().addItem(builder.build());
					}
				}
			}.runTaskTimer(main, 20l, taoBookUpdateRate * 20l); // 20 (min) * 60 (sec) * 20 (ticks)
			
		}
	}
	
	public HashMap<UUID, Boolean> getHasKilledACoalitionMember(){
		return this.hasKilledACoalitionMember;
	}
	
	public void setNewPichuTarget(UUID id) {
		this.pichuTarget = id;
	}

	public boolean getCanSaveACity() {
		return canSaveACity;
	}

	public void setCanSaveACity(boolean canSaveACity) {
		this.canSaveACity = canSaveACity;
	}

	public Location getSavedCity() {
		return savedCity;
	}

	public void setSavedCity(Location savedCity) {
		this.savedCity = savedCity;
	}
	
	@Override
	public GameTeam getTeamOfPlayer() {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.AMBROSIUS) && main.getCurrentGameManager().isRoleAttributed(RoleType.TAO) && main.getCurrentGameManager().isRoleAttributed(RoleType.ZIA)) {
			if(!main.getCurrentGameManager().getDeadRoles().contains(RoleType.AMBROSIUS)) {
				if(main.getCurrentGameManager().getDeadRoles().contains(RoleType.ESTEBAN) && main.getCurrentGameManager().getDeadRoles().contains(RoleType.ZIA)) {
					return GameTeam.HOURGLASS;
				}
			}
		}
		return Role.super.getTeamOfPlayer();
	}

}
