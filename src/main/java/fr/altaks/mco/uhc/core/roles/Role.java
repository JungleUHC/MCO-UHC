package fr.altaks.mco.uhc.core.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;

public interface Role extends Listener {
	
	public abstract RoleType getRole();
	
	public abstract String   getRelativeExplications();
	
	public abstract void     onGameStart(ArrayList<UUID> players, GameManager manager);
	
	public default void      onPlayerDeath(UUID id, GameManager manager) {
		if(!manager.hasDied(id)) {
			manager.getDeadPlayers().add(id);
			// get all players from role, keep non-dead players, if size == 0, then set role as dead
			
			List<UUID> players = new ArrayList<>();
			for(UUID uuid : manager.getPlayersFromRole().get(this)) {
				if(!manager.hasDied(uuid)) players.add(uuid);
			}
			
			if(players.size() == 0) {
				manager.getDeadRoles().add(getRole());
			}
			
			Player player = Bukkit.getPlayer(id);
			if(player == null) return;
			player.spigot().respawn();
			player.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	public default boolean  isWinning(GameManager manager) {
		// get every player
		GameTeam selfTeam = getTeamOfPlayer();
		for(UUID id : manager.roleOfPlayer().keySet()) {
			
			if(!manager.isPlaying(id)) continue;
			if(manager.roleOfPlayer().get(id) == getRole()) continue;
			
			GameTeam otherPlayerTeam = manager.getTeamOfPlayer(id);
			if(selfTeam != otherPlayerTeam) {
				// if player is on an opposite team, if player is not dead, then return false
				return false;
			} else {
				if(otherPlayerTeam == GameTeam.INDEPENDANT) {
					// check for menator and calmeque, if yes, then continue
					if(getRole() == RoleType.MENATOR) {
						if(manager.roleOfPlayer().get(id) != RoleType.CALMEQUE) return false;
					} else if(getRole() == RoleType.CALMEQUE) {
						if(manager.roleOfPlayer().get(id) != RoleType.MENATOR) return false;
					} 
				}
			}
		}
		return true;
	}
	
	public default GameTeam  getTeamOfPlayer() {
		return getRole().getRoleTeam();
	}
	
}
