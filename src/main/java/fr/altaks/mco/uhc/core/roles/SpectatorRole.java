package fr.altaks.mco.uhc.core.roles;

import java.util.ArrayList;
import java.util.UUID;

import fr.altaks.mco.uhc.core.game.GameManager;

public class SpectatorRole implements Role {

	@Override
	public RoleType getRole() {
		return RoleType.SPECTATING;
	}

	@Override
	public String getRelativeExplications() {
		return "Vous observez la partie sans communiquer quoi que ce soit aux joueurs";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
	}

}
