package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Nostradamus implements Role {

	private long lastDeathPredictionUsage = 0;
	private Main main;
	private int nostradamusPredictionCooldown = 5;
	
	public Nostradamus(Main main) {
		this.main = main;
	}
	
	
	@Override
	public RoleType getRole() {
		return RoleType.NOSTRADAMUS;
	}

	@Override
	public String getRelativeExplications() {
		return
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous recevez trois potions d'§4Instant Damage I§r ainsi que les commandes  §2\"/mco astres\"§r, qui trois fois dans la partie, vous permet de connaître la position du joueur ciblé via une flèche au dessus de votre hotbar et §2\"/mco prediction\"§r qui vous permet, deux fois dans la partie de connaître toutes les causes de mort pendant §bcinq minutes§r.\n";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		// TODO Auto-generated method stub

		List<Player> nostradamuss = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				nostradamuss.add(offlinePlayer.getPlayer());
			}
		}
		
		for(Player nostradamus : nostradamuss) {
			Potion pot = new Potion(PotionType.INSTANT_DAMAGE);
			pot.setSplash(true);
			nostradamus.getInventory().addItem(pot.toItemStack(3));
		}

		nostradamusPredictionCooldown = main.getConfig().getInt("timers.nostradamus-prediction-cooldown");

		
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		if(lastDeathPredictionUsage + nostradamusPredictionCooldown * 60 * 1000 > System.currentTimeMillis()) {
			for(UUID nostradamus : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.NOSTRADAMUS)) {
				if(main.getCurrentGameManager().isPlaying(nostradamus)) {
					Bukkit.getPlayer(nostradamus).sendMessage(Main.PREFIX + event.getDeathMessage());
				}
			}
		}

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

	public long getLastDeathPredictionUsage() {
		return lastDeathPredictionUsage;
	}

	public void setLastDeathPredictionUsage(long lastDeathPredictionUsage) {
		this.lastDeathPredictionUsage = lastDeathPredictionUsage;
	}


}
