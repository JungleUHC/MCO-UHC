package fr.altaks.mco.uhc.core.roles.rolecore.independant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.items.Artifacts;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.util.ItemManager;

public class Yupanqui implements Role {

	private RoleType[] neededDeadRolesForBuff = {RoleType.CALMEQUE, RoleType.MENATOR, RoleType.TAKASHI};
	private boolean isLastIndependant = false;
	private Scoreboard lifeScoreboard;
	
	private Main main;
	
	public Yupanqui(Main main) {
		this.main = main;
		
		this.lifeScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.getLifeScoreboard().registerNewObjective("hp", "health");
		this.getLifeScoreboard().getObjective("hp").setDisplaySlot(DisplaySlot.BELOW_NAME);
		this.getLifeScoreboard().getObjective("hp").setDisplayName(" PV");
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.YUPANQUI;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous êtes §dSolitaire§r, votre objectif est donc de tuer tous les joueurs de la partie.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous vous voyez octroyer l'effet §9Résistance I§r de façon permanente ainsi qu'une pioche en diamant enchantée §bEfficacité III§r et §bFortune I§r, elle double l’or miné.\n"
	   + "Vous possédez la commande §2\"/mco garnison\"§r qui crée une zone d’un rayon de §b50 blocs§r limité par des particules uniquement visible par vous où vous possédez l’effet §9Résistance II§r ainsi que §cForce I§r et vous permet de voir la vie des joueurs pendant §bdeux minutes§r."
	   + "§oVous ne disposez que de deux utilisations pour ce pouvoir§r.\n"
	   + "Si jamais vous êtes le dernier §dSolitaire§r, votre armure devient alors incassable.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {

		List<Player> yupanquis = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				yupanquis.add(offlinePlayer.getPlayer());
			}
		}

		for(Player yupanqui : yupanquis) {
			
			yupanqui.getInventory().addItem(Artifacts.RoleStuffs.PIOCHE_YUPANQUI);
			
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {

				for(Player yupanqui : yupanquis) {
					yupanqui.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 0), false);
				}
				if(!isLastIndependant) {
					boolean isLast = true;
					for(RoleType role : neededDeadRolesForBuff) {
						if(manager.isRoleAttributed(role)) {
							if(!manager.getDeadRoles().contains(role)) {
								isLast = false;
								break;
							}
						}
					}
					if(isLast) isLastIndependant = true;
				}
			}
		}.runTaskTimer(manager.getMain(), 0, 10);

	}
	
	@EventHandler
	public void onPlayerBreakGoldBlockEvent(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.GOLD_ORE) {
			if(ItemManager.lightCompare(Artifacts.RoleStuffs.PIOCHE_YUPANQUI, event.getPlayer().getItemInHand())) {
				event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.GOLD_ORE, 2));
			}
		}
	}
	
	@EventHandler
	public void onPlayerArmorDamages(PlayerItemDamageEvent event) {
		if(isLastIndependant) {
			if(main.getCurrentGameManager().isPlaying(event.getPlayer().getUniqueId())) {
				if(main.getCurrentGameManager().roleOfPlayer().get(event.getPlayer().getUniqueId()) == RoleType.YUPANQUI) {
					if(Arrays.asList(event.getPlayer().getInventory().getArmorContents()).contains(event.getItem())) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	public Scoreboard getLifeScoreboard() {
		return lifeScoreboard;
	}


}
