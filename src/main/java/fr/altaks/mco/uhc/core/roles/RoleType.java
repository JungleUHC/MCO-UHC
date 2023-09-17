package fr.altaks.mco.uhc.core.roles;

import fr.altaks.mco.uhc.core.roles.teams.GameTeam;

public enum RoleType {
	
	SPECTATING(        "§7Spectateur§r",  	GameTeam.UNDEFINED, "game-undef"),
	
	ESTEBAN(			"Esteban", 			GameTeam.COALITION, "coalition-esteban"),
	ZIA(				"Zia", 				GameTeam.COALITION, "coalition-zia"),
	TAO(				"Tao", 				GameTeam.COALITION, "coalition-tao"),
	MENDOZA(			"Mendoza", 			GameTeam.COALITION, "coalition-mendoza"),
	PEDRO(				"Pedro", 			GameTeam.COALITION, "coalition-pedro"),
	SANCHO(				"Sancho", 			GameTeam.COALITION, "coalition-sancho"),
	WAINA(				"Waina", 			GameTeam.COALITION, "coalition-waina"),
	KETCHA(				"Ketcha", 			GameTeam.COALITION, "coalition-ketcha"),
	KRAKA(				"Kraka", 			GameTeam.COALITION, "coalition-kraka"),
	VIRACOCHA(			"Viracocha", 		GameTeam.COALITION, "coalition-viracocha"),
	OLMEQUE_SOLDIER(	"Soldat olmèque",   GameTeam.COALITION, "coalition-olmeque_soldier"),
	MAYA_SOLDIER(		"Soldat maya", 		GameTeam.COALITION, "coalition-maya_soldier"),
	SEIBAN_WARRIOR(		"Guerrier Seiban", 	GameTeam.COALITION, "coalition-seiban_warrior"),
	URUBUS_WARRIOR(		"Guerrier Urubus", 	GameTeam.COALITION, "coalition-urubus_warrior"),
	
	AMBROSIUS(			"Ambrosius", 		GameTeam.HOURGLASS, "hourglass-ambrosius"),
	ATHANAOS(			"Athanaos", 		GameTeam.HOURGLASS, "hourglass-athanaos"),
	FERNANDO_LAGUERRA(	"Fernando Laguerra",GameTeam.HOURGLASS, "hourglass-fernando"),
	MARINCHE(			"Marinché", 		GameTeam.HOURGLASS, "hourglass-marinche"),
	TETEOLA(			"Teteola", 			GameTeam.HOURGLASS, "hourglass-teteola"),
	GOMEZ(				"Gomez", 			GameTeam.HOURGLASS, "hourglass-gomez"),
	HELVETIUS(			"Helvetius", 		GameTeam.HOURGLASS, "hourglass-helvetius"),
	HORTENSE(			"Hortense", 		GameTeam.HOURGLASS, "hourglass-hortense"),
	NOSTRADAMUS(		"Nostradamus", 		GameTeam.HOURGLASS, "hourglass-nostradamus"),
	GASPARD(			"Gaspard", 			GameTeam.HOURGLASS, "hourglass-gaspard"),
	PIZARRO(			"Pizarro", 			GameTeam.HOURGLASS, "hourglass-pizarro"),
	ISABELLA_LAGUERRA(	"Isabella Laguerra",GameTeam.HOURGLASS, "hourglass-isabella"),
	CINESUS(            "Cinésus",          GameTeam.HOURGLASS, "hourglass-cinesus"),
	HIPPOLYTE(          "Hippolyte",        GameTeam.HOURGLASS, "hourglass-hippolyte"),
	
	YUPANQUI(			"Yupanqui", 		GameTeam.INDEPENDANT, "independant-yupanqui"),
	CALMEQUE(			"Calmèque", 		GameTeam.INDEPENDANT, "independant-calmeque"),
	MENATOR(			"Menator", 			GameTeam.INDEPENDANT, "independant-menator"),
	TAKASHI(			"Takashi", 			GameTeam.INDEPENDANT, "independant-takashi");
	
	private String 	roleName = "Undefined", 
					roleId = "undef-notdef";
	
	private GameTeam roleTeam;
	private int roleAmount = 1;
	
	private RoleType(String roleName, GameTeam roleTeam, String roleId) {
		this.roleName = roleName;
		this.roleTeam = roleTeam;
		this.roleId = roleId;
	}
	
	public String getRoleName() {
		return this.roleName;
	}
	
	public GameTeam getRoleTeam() {
		return this.roleTeam;
	}
	
	public String getRoleId() {
		return this.roleId;
	}
	
	public int getRoleAmountOfPlayers() {
		return this.roleAmount;
	}
	
	public void setRoleAmountOfPlayers(int amount) {
		this.roleAmount = amount;
	}

}
