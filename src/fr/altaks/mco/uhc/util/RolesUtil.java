package fr.altaks.mco.uhc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;

import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;

public class RolesUtil {
	
	private GameManager gameManager;
	
	public RolesUtil(GameManager manager) {
		this.gameManager = manager;
	}
	
	public String replacedString(String text) {
		// Copier le texte original
		String newText = text;
		
		// pour chaque role attribué
		for(Role role : gameManager.getPlayersFromRole().keySet()) {
			// si le role est détecté dans le string, le remplacer par
			if(newText.contains("%" + role.getRole().getRoleId() + "%")) {
				
				// préparer la chaine jointe des noms des joueurs
				StringJoiner joiner = new StringJoiner(", ");
				for(UUID id : gameManager.getPlayersFromRole().get(role)) {
					if(gameManager.isPlaying(id)) joiner.add(Bukkit.getPlayer(id).getDisplayName());
				}
				
				// remplacer dans le texte
				newText = newText.replace("%" + role.getRole().getRoleId() + "%", joiner.toString());
				
			}
		}
		return newText;
	}
	
	public static String getListOfPlayerContaining(ArrayList<String> target, ArrayList<String> playersFeed, int amountOfPplToFeedWith) {
		
		if(amountOfPplToFeedWith > playersFeed.size()) {
			amountOfPplToFeedWith = playersFeed.size();
		}
		
		StringJoiner total = new StringJoiner(", ");
		
		String pick = target.get(new Random().nextInt(target.size()));
		
		ArrayList<String> picks = new ArrayList<String>();
		picks.add(pick);
		
		for(int i = 0; i < amountOfPplToFeedWith-1; i++) {
			String newestPick = playersFeed.get(new Random().nextInt(playersFeed.size()));
			picks.add(newestPick);
			playersFeed.remove(newestPick);
		}
		
		Collections.shuffle(picks);
		
		for(String playerName : picks) total.add(playerName);
		
		return total.toString();
	}

}
