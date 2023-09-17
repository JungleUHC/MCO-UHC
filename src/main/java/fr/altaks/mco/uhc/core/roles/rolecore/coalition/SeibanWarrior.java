package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class SeibanWarrior implements Role {
	
	private HashMap<UUID, Short> alreadyChosenStuff = new HashMap<UUID, Short>();
	private List<Entity> potentiallyExplosiveArrows = new ArrayList<Entity>();

	private double arrowExplodesPercentage = 0.05;
	private double speedPercentage = 1.2;
	private int arrowExplosionRadius = 3;
	
	public HashMap<UUID, Short> getAlreadyChosenStuff(){
		return this.alreadyChosenStuff;
	}
	
	private Main main;
	
	public SeibanWarrior(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.SEIBAN_WARRIOR;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, un choix vous sera proposé, via la commande §2\"/mco equipement\"§r, vous pourrez choisir entre, un livre §bPower IV§r accompagné de §b5% qu'une flèche explose§r ou un livre §bDepth Strider II§r et §b20% de Speed§r\n"
	   + "Lorsqu'un Guerrier Seiban meurt, vous gagnez un §ccoeur supplémentaire§r.\n"
	   + "Voici l'identité des autres §aGuerriers Seibans§r : %coalition-seiban_warrior%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> seibans = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) seibans.add(offlinePlayer.getPlayer());
		}
		
		for(Player seiban : seibans) {
			
			BaseComponent[] total = 
					   new ComponentBuilder("§e----------------------------------------------------§r\n")
					   .append("Vous êtes un guerrier seiban, vous pouvez choisir votre équipement de départ : \n Votre choix \u00BB ").color(ChatColor.YELLOW)
					   .append("[1]")
					   		.color(ChatColor.AQUA)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement seibanwarrior 1"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Puissance IV + 5% de chance qu'une flèche explose").create()))
					   .append(" - ")
					   		.color(ChatColor.WHITE)
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .append("[2]")
					   		.color(ChatColor.BLUE)
					   		.bold(true)
					   		.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mco equipement seibanwarrior 2"))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Livre Depth Strider II + 20% de Speed").create()))
					   .append("\n§e----------------------------------------------------\n")
					   		.bold(false)
					   		.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""))
					   		.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()))
					   .create();
			
			seiban.spigot().sendMessage(total);
			
		}

		arrowExplodesPercentage = main.getConfig().getDouble("timers.seiban-warrior-arrow-explode-percentage");
		speedPercentage = main.getConfig().getDouble("timers.seiban-warrior-speed-percentage");
		arrowExplosionRadius = main.getConfig().getInt("timers.seiban-warrior-arrow-explosion-radius");

	}
	
	@EventHandler
	public void onMayaDeath(EntityDeathEvent event) {
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.SEIBAN_WARRIOR) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SEIBAN_WARRIOR).contains(event.getEntity().getUniqueId())) {
			
		   for(UUID seiban : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SEIBAN_WARRIOR)) {
			   if(!main.getCurrentGameManager().hasDied(seiban)) {
				   Bukkit.getPlayer(seiban).setMaxHealth(Bukkit.getPlayer(seiban).getMaxHealth() + 1*2);
			   }
		   }
		   
		}
	}
	
	@EventHandler
	public void onSeibanWarriorShootArrow(ProjectileLaunchEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if(!(source instanceof Player)) return;
		
		Player launcher = (Player)source;
		if(main.getCurrentGameManager().isRoleAttributed(RoleType.SEIBAN_WARRIOR) && 
		   main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SEIBAN_WARRIOR).contains(launcher.getUniqueId())) {
			
			if(this.alreadyChosenStuff.containsKey(launcher.getUniqueId()) && this.alreadyChosenStuff.get(launcher.getUniqueId()) == (short)1) {
				potentiallyExplosiveArrows.add(event.getEntity());
			}
		}
	}
	
	@EventHandler
	public void onSeibanWarriorArrowHit(ProjectileHitEvent event) {
		if(potentiallyExplosiveArrows.contains(event.getEntity())) {
			boolean explode = new Random().nextInt(100) <= arrowExplodesPercentage*100;
			if(explode) {
				event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), arrowExplosionRadius);
			}
		}
	}

	public double getSpeedPercentage() {
		return speedPercentage;
	}
}
