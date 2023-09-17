package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;
import fr.altaks.mco.uhc.core.roles.rolecore.coalition.Zia;
import fr.altaks.mco.uhc.core.roles.teams.GameTeam;

public class Pizarro implements Role {

	private List<UUID> alreadyBeenTargeted = new ArrayList<UUID>();
	private UUID pizarroTarget = null;
	private Main main;
	
	public Pizarro(Main main) {
		this.main = main;
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.PIZARRO;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l'annonce des rôles, vous obtenez des effets de potions différents selon le nombre de pommes dorées que possèdent les joueurs de la coalition dans un rayon de 30 blocs autour de vous.\n"
	   + "Entre 0 et 15 : Aucun effet / Entre 15 et 25 : §cForce 1§r / Entre 25 et 40 / §cForce 1§r et §bVitesse 1§r / 64+ / §cForce 1§r, §bVitesse 1§r et §9Résistance 1§r.\n"
	   + "Vous possédez la commande §2\"/mco cible\"§r qui retire trois coeurs permanents aux joueurs pour les cinq prochaines minutes à savoir que tous les joueurs de la partie seront informés du nom de la cible de §cPizarro§r.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		// TODO Auto-generated method stub
		
		List<Player> pizarros = new ArrayList<Player>();
		for(UUID id : players) {
			if(!manager.isPlaying(id)) continue;
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(id);
			if(offlinePlayer.isOnline()) {
				pizarros.add(offlinePlayer.getPlayer());
			}
		}

		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				for(Player pizarro : pizarros) {
					
					List<Player> nearbyCoalitionPlayers = new ArrayList<>();
					for(Entity entity : pizarro.getNearbyEntities(30, 30, 30)) {
						if(manager.isPlaying(entity.getUniqueId())) {
							if(manager.getTeamOfPlayer(entity.getUniqueId()) == GameTeam.COALITION) {
								nearbyCoalitionPlayers.add((Player)entity);
							}
						}
					}
					
					int gappleCount = 0;
					for(Player coalitionMember : nearbyCoalitionPlayers) {
						for(ItemStack item : coalitionMember.getInventory().getContents()) {
							if(item == null) continue;
							if(item.getType() == Material.GOLDEN_APPLE) {
								gappleCount += item.getAmount();
							}
						}
					}
					
					if(gappleCount >= 15) {
						pizarro.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0), false);
					}
					
					if(gappleCount >= 25) {
						pizarro.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0), false);
					}
					
					if(gappleCount >= 64) {
						pizarro.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 0), false);
					}
					
				}
				
			}
		}.runTaskTimer(manager.getMain(), 0, 20);
		
	}
	
	@Override
	public void onPlayerDeath(UUID id, GameManager manager) {
		Role.super.onPlayerDeath(id, manager);
		
		if(manager.isRoleAttributed(RoleType.ZIA)) {
			boolean areAllPizarrosDead = true;
			for(UUID pizarro : manager.getPlayersFromRole().get(manager.getRoleFromRoleType().get(RoleType.PIZARRO))) {
				if(!manager.hasDied(pizarro)) {
					areAllPizarrosDead = false;
					break;
				}
			}
			if(areAllPizarrosDead) {
				for(UUID ziaUUID : manager.getPlayersFromRole().get(manager.getRoleFromRoleType().get(RoleType.ZIA))) {
					if(!manager.hasDied(ziaUUID)) {
						Player zia = Bukkit.getPlayer(ziaUUID);
						zia.removePotionEffect(PotionEffectType.WEAKNESS);
					}
				}
				Zia ziaRole = (Zia) manager.getRoleFromRoleType().get(RoleType.ZIA);
				ziaRole.setCanUseMCOClaim(true);
			}
		}
	}
	
	@EventHandler
	public void onPizarroKillsTarget(EntityDamageByEntityEvent event) {
		if(main.getCurrentGameManager().getCurrentGameState() != GameState.PLAYING) return;
		// both entities have to be players
		if(!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
				
		Player damager = (Player)event.getDamager();
		
		if(!main.getCurrentGameManager().isRoleAttributed(RoleType.PIZARRO)) return;
		if(event.getFinalDamage() < ((Player)event.getEntity()).getHealth()) return;
		
		// if the damager is ambrosius
		if(main.getCurrentGameManager().getPlayersThatOwnsRole(RoleType.PIZARRO).contains(damager.getUniqueId())) {
			if(this.pizarroTarget.equals(event.getEntity().getUniqueId())) {
				// target has been killed
				this.alreadyBeenTargeted.add(pizarroTarget);
				this.pizarroTarget = null;
				
				int gappleCount = 0;
				for(ItemStack item : ((Player)event.getEntity()).getInventory().getContents()) {
					if(item == null) continue;
					if(item.getType() == Material.GOLDEN_APPLE) {
						gappleCount += item.getAmount();
					}
				}
				
				gappleCount *= 2;
				
				for(int i = 0; i < (int)(gappleCount / 64); i++) {
					damager.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 64));
				}
				damager.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, (gappleCount % 64)));
				
			}
		}
	}
	
	public void setPizarroTarget(UUID pizarroTarget) {
		this.pizarroTarget = pizarroTarget;
	}

	public List<UUID> getAlreadyBeenTargeted() {
		return alreadyBeenTargeted;
	}

	public UUID getPizarroTarget() {
		return pizarroTarget;
	}

}
