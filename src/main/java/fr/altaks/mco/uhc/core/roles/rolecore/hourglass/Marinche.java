package fr.altaks.mco.uhc.core.roles.rolecore.hourglass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import fr.altaks.mco.uhc.Main;
import fr.altaks.mco.uhc.core.game.GameManager;
import fr.altaks.mco.uhc.core.roles.Role;
import fr.altaks.mco.uhc.core.roles.RoleType;

public class Marinche implements Role {
	
	private Scoreboard manipulationScoreboard;
	private Main main;
	private List<UUID> manipulatedPlayers = new ArrayList<>();
	private HashMap<Integer, Short> usagesOfManipulationByEpisode = new HashMap<>();
	private HashMap<Integer, Short> manipulationsByEpisode = new HashMap<>();
	public long gameStart;
	
	public Marinche(Main main) {
		this.manipulationScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.manipulationScoreboard.registerNewObjective("manipulated", "dummy");
		this.manipulationScoreboard.getObjective("manipulated").setDisplaySlot(DisplaySlot.BELOW_NAME);
		this.manipulationScoreboard.getObjective("manipulated").setDisplayName("%");
		this.main = main;
		this.gameStart = System.currentTimeMillis();
	}
	
	@Override
	public RoleType getRole() {
		return RoleType.MARINCHE;
	}

	@Override
	public String getRelativeExplications() {
		return 
	     "\u30FBObjectifs: Vous faites partie de l'§cOrdre du Sablier§r, votre objectif est donc de tuer tous les joueurs de la §aCoalition§r.\n"
	   + "\u30FBParticularités de votre rôle:\n"
	   + "Dès l’annonce des rôles, vous aurez accès au §2\"/mco manipuler\"§r, vous permettant d’obtenir une barre de manipulation au-dessus du pseudonyme du joueur qui augmente de §b1% par seconde§r si vous êtes à une distance maximum de §b20 blocs§r de ce dernier. À la fin de cette barre, le joueur sera considéré comme §6manipulé§r. §oVous disposez seulement de trois utilisations pour ce pouvoir.§r\n"
	   + "Une fois manipuler, vous aurez accès au §2\"/mco manipulation\"§r, ce qui vous permettra d'ouvrir une interface ou vous verrez le nom des joueurs §6manipulés§r, vous pourrez donc exécuter un malus sur le joueur sélectionné, vous aurez le choix entre les trois malus suivants : \n"
	   + "Lui voler un cinquième de ses pommes dorées restantes / Connaître son rôle / Lui retirer un cœur permanent. §oVous disposez seulement de deux utilisations par épisode pour ce pouvoir.§r\n"
	   + "Vous disposez également d'un chat utilisable via la commande §2“/mco chat”§r vous permettant de parler avec §cFernando Laguerra§r.";
	}

	@Override
	public void onGameStart(ArrayList<UUID> players, GameManager manager) {
		// TODO Auto-generated method stub

	}

	@EventHandler
	public void onMarincheManipulate(InventoryClickEvent event) {
		// §8Joueurs manipulés
		if(event.getInventory().getName().equalsIgnoreCase("§8Joueurs manipulés")) {
			if(event.getClickedInventory() == null) return;
			if(event.getClickedInventory().equals(event.getView().getTopInventory())) {
				ItemStack current = event.getCurrentItem();
				if(current == null || current.getType() != Material.SKULL_ITEM) return;
				
				Player target = Bukkit.getPlayer(((SkullMeta)current.getItemMeta()).getOwner());
				if(target == null) return;
				switch (event.getClick()) {
					case LEFT:
					case SHIFT_LEFT:
						// voler gapple
						int gappleCount = 0;
						
						for(int slot = 0; slot < target.getInventory().getSize(); slot++) {
							if(target.getInventory().getItem(slot) == null) continue;
							if(target.getInventory().getItem(slot).getType().equals(Material.GOLDEN_APPLE)) {
								gappleCount += target.getInventory().getItem(slot).getAmount();
							}
						}
						
						gappleCount /= 5;
						if(gappleCount == 0) {
							event.getWhoClicked().sendMessage(Main.PREFIX + "§cLe joueur n'avait pas de pomme dorées");
							return;
						}
						
						for(int i = 0; i < gappleCount; i++) {
							int slot = target.getInventory().first(Material.GOLDEN_APPLE);
							target.getInventory().getItem(slot).setAmount(target.getInventory().getItem(slot).getAmount() - 1);
						}
						
						event.getWhoClicked().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, gappleCount));
						event.getWhoClicked().sendMessage(Main.PREFIX + "§cVous venez de voler " + gappleCount + " pommes dorées à " + target.getName());
						break;
					case RIGHT:
					case SHIFT_RIGHT:
						// retirer 1 coeur perma
						target.setMaxHealth(target.getMaxHealth() - 1*2);
						event.getWhoClicked().sendMessage(Main.PREFIX + "§cVous venez de retirer un coeur à " + target.getDisplayName());
						break;
					case MIDDLE:
						// role
						event.getWhoClicked().sendMessage(Main.PREFIX + "§6Le joueur §e" + target.getDisplayName() + "§e est §6" + main.getCurrentGameManager().roleOfPlayer().get(target.getUniqueId()).getRoleName());
						break;
				default:
					return;
				}
				event.setCancelled(true);
				event.getWhoClicked().closeInventory();
				
				int episode = (int)(System.currentTimeMillis() - gameStart) / (main.getConfig().getInt("timers.episode-duration") * 60 * 1000);
				
				if(!manipulationsByEpisode.containsKey(episode)) {
					manipulationsByEpisode.put(episode, (short)1);
				} else {
					manipulationsByEpisode.put(episode, (short)(manipulationsByEpisode.get(episode) + 1));
				}
			}
		}
	}
	
	public Scoreboard getManipulationScoreboard() {
		return this.manipulationScoreboard;
	}
	
	public List<UUID> getManipulatedPlayers(){
		return this.manipulatedPlayers;
	}
	
	public HashMap<Integer, Short> getManipulationsByEpisode(){
		return this.manipulationsByEpisode;
	}
	
	public HashMap<Integer, Short> getUsagesByEpisode(){
		return this.usagesOfManipulationByEpisode;
	}

}
