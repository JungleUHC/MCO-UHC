package fr.altaks.mco.uhc.core.roles.teams;

public enum GameTeam {
	
	UNDEFINED("§7Spectateurs§r"),
	COALITION("§aLa Coalition§r"),
	HOURGLASS("§cL'Ordre du Sablier§r"),
	INDEPENDANT("§dLes Solitaires§r");
	
	private String teamName;
	
	private GameTeam(String teamName) {
		this.teamName = teamName;
	}
	
	public String getTeamName() {
		return this.teamName;
	}

}
