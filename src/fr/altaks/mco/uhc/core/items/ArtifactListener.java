package fr.altaks.mco.uhc.core.items;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager.GameState;
import fr.altaks.mco.uhc.util.ItemManager;
import fr.altaks.mco.uhc.util.RayTraceUtil;

public class ArtifactListener implements Listener {
	
	private long condorsLastUse = 0;
	private Main main;
	
	public ArtifactListener(Main main) {
		this.main = main;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(main.getCurrentGameManager().getCurrentGameState() == GameState.PLAYING) {
					for(UUID id : main.getCurrentGameManager().roleOfPlayer().keySet()) {
						if(main.getCurrentGameManager().isPlaying(id)) {
							Player player = Bukkit.getPlayer(id);
							boolean hasOphir = false;
							for(int slot = 0; slot < player.getInventory().getSize(); slot++) {
								if(player.getInventory().getItem(slot) == null) continue;
								if(ItemManager.compare(player.getInventory().getItem(slot), Artifacts.CityItems.PIERRE_OPHIR)) {
									hasOphir = true;
									break;
								}
							}
							if(hasOphir) {
								player.setMaxHealth(player.getMaxHealth() + 4);
								new BukkitRunnable() {
									
									@Override
									public void run() {
										if(player.getMaxHealth() > player.getHealth()) {
											player.setHealth(player.getHealth() + 1);
										}
									}
								}.runTaskTimer(main, 0, 10 * 20);
								cancel();
							}
						}
						
					}

				}
			}
		}.runTaskTimer(main, 0, 20);
	}
	
	@EventHandler
	public void onCondorsUse(PlayerInteractEvent event) {
		if(!event.hasItem()) return;
		if(ItemManager.lightCompare(event.getItem(), Artifacts.CityItems.CONDORS)) {
			if(condorsLastUse + 10 * 60 * 1000 <= System.currentTimeMillis()) {
				event.getPlayer().setAllowFlight(true);
				event.getPlayer().setFlying(true);
				condorsLastUse = System.currentTimeMillis();
				new BukkitRunnable() {
					@Override
					public void run() {
						event.getPlayer().setAllowFlight(false);
						event.getPlayer().setFlying(false);
					}
				}.runTaskLater(main, 10 * 20);
			} else {
				event.getPlayer().sendMessage(Main.PREFIX + "Cet item est en cours de rechargement !");
			}
		}
	}
	
	@EventHandler
	public void onCouronneTelekinetiqueUse(PlayerInteractEvent event) {
		if(!event.hasItem()) return;
		if(ItemManager.lightCompare(event.getItem(), Artifacts.CityItems.COURONNE_TELEK)) {
			
			Player target = RayTraceUtil.getTargetPlayer(event.getPlayer());
			if(target == null) return;
			
			target.setAllowFlight(true);
			target.setFlying(true);
			
			new BukkitRunnable() {
				
				int timer = 5 * 20;
				
				@Override
				public void run() {
					if(timer != 0) {
						
						Location calculated = event.getPlayer().getEyeLocation().add(
								event.getPlayer()
									 .getEyeLocation()
									 .getDirection()
									 .normalize()
									 .multiply(5)
						);
						
						int highestYatXZ = calculated.getWorld().getHighestBlockYAt(calculated.getBlockX(), calculated.getBlockZ());
						
						double choosenY = calculated.getY();
						if(calculated.getY() <= highestYatXZ) {
							choosenY = highestYatXZ;
						}
						
						Location toTeleport = new Location(
								calculated.getWorld(), 
								calculated.getX(), 
								choosenY,
								calculated.getZ()
						);
						
						target.teleport(toTeleport);
						timer--;
						
					} else {
						cancel();
					}
					
				}
			}.runTaskTimer(main, 0, 1l);
			
			new BukkitRunnable() {
				
				int timer = 5 * 20;
				
				Location originLoc = null;
				
				@Override
				public void run() {
						if(originLoc == null) originLoc = target.getLocation();
						if(timer != 0) {
							target.teleport(originLoc);
							timer--;
						} else {
							target.setFlying(false);
							target.setAllowFlight(false);
							cancel();
						}
					
				}
			}.runTaskTimer(main, 5 * 20, 1l);
				
		}
		
	}

}
