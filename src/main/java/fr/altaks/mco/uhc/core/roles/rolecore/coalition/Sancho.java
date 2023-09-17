package fr.altaks.mco.uhc.core.roles.rolecore.coalition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Sancho implements Role {
	
	private Main main;
	private int hungerDuration = 10; // seconds
	private double fallingAggravation = 1.25;
	
	public Sancho(Main main) {
		this.main = main;
	}

	@Override
	public RoleType getRole() {
		return RoleType.SANCHO;
	}

	@Override
	public String getRelativeExplications() {
		return
		  "\u30FBObjectifs: Vous faites partie de la §acoalition§r, votre objectif est donc de tuer tous les joueurs de l’§cOrdre du Sablier§r.\n"
		+ "\u30FBParticularités de votre rôle:\n"
		+ "Dès l'annonce des rôles, vous vous verrez octroyer §b2 coeurs supplémentaires§r, vous êtes extrêmement maladroit, vous prenez 25 % de dégâts supplémentaires lors de vos chutes, si vous tombez en dessous de cinq cœurs liés à un dégât de chute, vous obtenez §4Slowness 1§r durant dix secondes.\n"
		+ "Vous êtes quelqu’un de très gourmand par conséquent quand un joueur mangera à moins de vingt blocs de vous vous obtiendrez l’effet §2Faim 1§r pendant dix secondes. Cependant, lorsque vous remplissez votre barre de nourriture, vous obtenez un effet de régénération vous permettant d'également régénérer votre vie.§r\n"
		+ "Voici l’identité de §aPedro§r: %coalition-pedro%";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		
		List<Player> sanchos = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) sanchos.add(offlinePlayer.getPlayer());
		}
		
		for(Player sancho : sanchos) {
			sancho.setMaxHealth(24);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(sancho.getFoodLevel() == 20) {
						sancho.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 50, 0), true);
					}
				}
				
			}.runTaskTimer(manager.getMain(), 0, 20l);
		}
		hungerDuration = manager.getMain().getConfig().getInt("timers.sancho-hunger-duration");
		fallingAggravation = manager.getMain().getConfig().getDouble("timers.sancho-falling-aggravation");
	}
	
	@EventHandler
	public void onSanchoFallHard(EntityDamageEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.SANCHO)) return;
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SANCHO).contains(player.getUniqueId())) {
			
			Main.logIfDebug("Damage taken " + event.getDamage());
			event.setDamage(event.getDamage() * fallingAggravation);
			Main.logIfDebug("Damage taken " + event.getDamage());
			
			if((event.getCause() == DamageCause.FALL) && (player.getHealth() - event.getDamage() < 5*2)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10*20, 0));
			}
		}
	}
	
	@EventHandler
	public void onPlayerEatNearSancho(PlayerItemConsumeEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		if(event.getItem().getType().isEdible()) {
			Player player = event.getPlayer();
			if(main.getCurrentGameManager().isPlaying(player.getUniqueId()) && main.getCurrentGameManager().roleOfPlayer().get(player.getUniqueId()) == RoleType.SANCHO) return;
			
			if(!main.getCurrentGameManager().isRoleAttributed(RoleType.SANCHO)) return;
			for(UUID uuid : main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.SANCHO)) {
				Player sancho = Bukkit.getPlayer(uuid);
				if(sancho != null) {
					if(sancho.getLocation().distance(player.getLocation()) <= 20.0d) {
						sancho.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, hungerDuration*20, 0));
					}
				}
			}
		}
	}

}
