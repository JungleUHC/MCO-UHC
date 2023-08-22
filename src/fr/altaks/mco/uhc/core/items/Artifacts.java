package fr.altaks.mco.uhc.core.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import fr.altaks.mco.uhc.util.ItemManager;

public class Artifacts {
	
	public static final ItemStack 	MEDAILLON_SOLAIRE_INCOMPLET = new ItemManager.ItemBuilder(Material.WATCH, 1).setDisplayName("§cMédaillon du Soleil [Incomplet]").addFakeEnchant().setUnbreakable(true).build(),
									MEDAILLON_SOLAIRE_COMPLETE  = new ItemManager.ItemBuilder(Material.COMPASS, 1).setDisplayName("§6Médaillon du Soleil").addFakeEnchant().setUnbreakable(true).build(),
									CLE_CITE_OR				    = new ItemManager.ItemBuilder(Material.GOLD_NUGGET, 1).setDisplayName("§aClé de cité d'or").addFakeEnchant().setUnbreakable(true).build();

	public static class RoleItems {
		
		public static final ItemStack 	
			
			TELEKINESIS		    = 	new ItemManager.ItemBuilder(Material.BLAZE_ROD, 1)			.setDisplayName("§eTelekinesis").addFakeEnchant().setUnbreakable(true).setUnbreakable(true).build(),
			LIVRE				=	new ItemManager.ItemBuilder(Material.BOOK_AND_QUILL, 1)		.setDisplayName("§eLivre").addFakeEnchant().setUnbreakable(true).build(),
			ROUBLARDISE 		= 	new ItemManager.ItemBuilder(Material.GOLD_INGOT, 1)			.setDisplayName("§eRoublardise").addFakeEnchant().setUnbreakable(true).build(),
			REPERAGE 			=  	new ItemManager.ItemBuilder(Material.COMPASS, 1)			.setDisplayName("§eRepérage").addFakeEnchant().setUnbreakable(true).build(),
			PYRAMIDE_MU 		= 	new ItemManager.ItemBuilder(Material.NETHER_STAR, 1)		.setDisplayName("§ePyramide de Mu").addFakeEnchant().setUnbreakable(true).build(),
			LUMINARION 		    = 	new ItemManager.ItemBuilder(Material.FIREBALL, 1)			.setDisplayName("§eLuminarion").addFakeEnchant().setUnbreakable(true).build(),
			MATR_ORICHALQUE 	= 	new ItemManager.ItemBuilder(Material.GOLD_NUGGET, 1)		.setDisplayName("§eMatrice d'Orichalque").addFakeEnchant().setUnbreakable(true).build(),
			PENDULE		 		= 	new ItemManager.ItemBuilder(Material.WATCH, 1)				.setDisplayName("§ePendule").addFakeEnchant().setUnbreakable(true).build(),
			SOLEIL_NOIR 		= 	new ItemManager.ItemBuilder(Material.COAL_BLOCK, 1)			.setDisplayName("§eSoleil noir").addFakeEnchant().setUnbreakable(true).build(),
			FOUET				= 	new ItemManager.ItemBuilder(Material.DIAMOND_SWORD, 1)		.setDisplayName("§eFouet").addSafeEnchant(Enchantment.DAMAGE_ALL, 3).setUnbreakable(true).build(),
			ART_ESQUIVE 		= 	new ItemManager.ItemBuilder(Material.GOLD_SWORD, 1)			.setDisplayName("§eArt de l'esquive").addFakeEnchant().setUnbreakable(true).build(),
			BOMBES_AVEUGL 		= 	new ItemManager.ItemBuilder(Material.SNOW_BALL, 5)			.setDisplayName("§eBombes aveuglantes").addFakeEnchant().setUnbreakable(true).build(),
			TRACE_DU_FELIN      =   new ItemManager.ItemBuilder(Material.MONSTER_EGG, 1)        .setDisplayName("§eTrace du félin").addFakeEnchant().setUnbreakable(true).build(),
			JOUVENCE            =   new ItemManager.ItemBuilder(Material.POTION, 1)             .setDisplayName("§eJouvence").addFakeEnchant().setUnbreakable(true).build(),
			CHAUDRON_HIPPOLYTE    =   new ItemManager.ItemBuilder(Material.CAULDRON_ITEM, 1)      .setDisplayName("§eChaudron d'alchimie").addFakeEnchant().setUnbreakable(true).build(),
			ALCHIMIE_SACREE     =   new ItemManager.ItemBuilder(Material.BLAZE_POWDER, 1)             .setDisplayName("§eAlchimie sacrée").addFakeEnchant().setUnbreakable(true).build();
			

	}
	
	public static class RoleStuffs {
		
		public static final ItemStack
			DAGUE				=   new ItemManager.ItemBuilder(Material.DIAMOND_SWORD, 1)      .setDisplayName("§eDague").setUnbreakable(true).addSafeEnchant(Enchantment.DAMAGE_ALL, 3).build(),
			EPEE_MENDOZA		=   new ItemManager.ItemBuilder(Material.DIAMOND_SWORD, 1)		 .addSafeEnchant(Enchantment.DAMAGE_ALL, 4).setUnbreakable(true).build(),
			MASQUE_ENCHANTE	= 	new ItemManager.ItemBuilder(Material.DIAMOND_HELMET, 1)	.setDisplayName("§eMasque enchanté").setUnbreakable(true).addSafeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
			PIOCHE_YUPANQUI   =   new ItemManager.ItemBuilder(Material.DIAMOND_PICKAXE, 1).setDisplayName("§ePioche de fortune").setUnbreakable(true).addSafeEnchant(Enchantment.DIG_SPEED, 3).addSafeEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1).build(),
			EPEE_TAKASHI       =	EPEE_MENDOZA.clone();
		
	}
	
	public static class CityItems {
		

		public static final ItemStack 
			CONDORS 			= 	new ItemManager.ItemBuilder(Material.GOLD_BARDING, 1)		.setDisplayName("§eCondors").addFakeEnchant().setUnbreakable(true).build(),
			COURONNE_TELEK 		= 	new ItemManager.ItemBuilder(Material.BLAZE_ROD, 1)			.setDisplayName("§eCouronne télékinétique").addFakeEnchant().setUnbreakable(true).build(),
			PIERRE_OPHIR 		= 	new ItemManager.ItemBuilder(Material.FLINT, 1)				.setDisplayName("§ePierre d'Ophir").addFakeEnchant().setUnbreakable(true).build();

	}
	
	public static final ItemStack[] everyItems() {
		return new ItemStack[]{
			MEDAILLON_SOLAIRE_INCOMPLET, MEDAILLON_SOLAIRE_COMPLETE, CLE_CITE_OR, 
			
			RoleItems.TELEKINESIS, RoleItems.LIVRE, RoleItems.ROUBLARDISE, RoleItems.REPERAGE, RoleItems.PYRAMIDE_MU, 
			RoleItems.LUMINARION, RoleItems.MATR_ORICHALQUE, RoleItems.PENDULE, RoleItems.SOLEIL_NOIR, RoleItems.FOUET, RoleItems.ART_ESQUIVE, RoleItems.BOMBES_AVEUGL,
			
			RoleStuffs.DAGUE, RoleStuffs.MASQUE_ENCHANTE,
			
			CityItems.CONDORS, CityItems.COURONNE_TELEK, CityItems.PIERRE_OPHIR
			
		};
	}
	
}
