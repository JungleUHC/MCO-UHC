package fr.altaks.mco.uhc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
 
public class ItemManager {
    
    public static class PrebuiltItems {
        
        public static final ItemStack previousArrow = new ItemBuilder(Material.ARROW, 1, "§cPrevious").build();
        public static final ItemStack nextArrow = new ItemBuilder(Material.ARROW, 1, "§cNext").build();
        public static final ItemStack inventoryFillingGlassPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, " ").build();
        
        public static final ItemStack hubAndLobbyCompass = new ItemBuilder(Material.COMPASS, 1, "§6Teleport").build();
        
    }
    
    public static boolean lightCompare(ItemStack firstItem, ItemStack secondItem) {
    	if(firstItem == null ^ secondItem == null) return false;
        if(firstItem.getType() == secondItem.getType()) {
            if(firstItem.hasItemMeta() && secondItem.hasItemMeta()) {
                // verif les metas
                ItemMeta firstMeta = firstItem.getItemMeta();
                ItemMeta secondMeta = secondItem.getItemMeta();
                
                if(!firstMeta.equals(secondMeta)) return false;
                if(firstMeta.hasDisplayName() ^ secondMeta.hasDisplayName()) return false;
 
            }
            // v�rif si les un des deux a un meta et pas l'autre
            if(firstItem.hasItemMeta() ^ secondItem.hasItemMeta()) return false;
        } else return false;
        return true;
    }
    
    public static boolean compare(ItemStack firstItem, ItemStack secondItem) {
        if(firstItem.getType() == secondItem.getType()) {
            if(firstItem.hasItemMeta() && secondItem.hasItemMeta()) {
                // verif les metas
                ItemMeta firstMeta = firstItem.getItemMeta();
                ItemMeta secondMeta = secondItem.getItemMeta();
                
                if(firstMeta.hasDisplayName() && secondMeta.hasDisplayName()) {
                    
                    String firstDisplayName = firstMeta.getDisplayName();
                    String secondDisplayName = secondMeta.getDisplayName();
                    
                    if(!firstDisplayName.equals(secondDisplayName)) return false;
                }
                
                if(firstMeta.hasDisplayName() ^ secondMeta.hasDisplayName()) return false;
 
            }
            // v�rif si les un des deux a un meta et pas l'autre
            if(firstItem.hasItemMeta() ^ secondItem.hasItemMeta()) return false;
        } else return false;
        return true;
    }
    
    public static class ItemBuilder {
        
        protected ItemStack item;
        protected ItemMeta meta;
        
        public ItemStack build() {
            item.setItemMeta(meta);
            return item;
        }
        
        public ItemBuilder clone() {
            return new ItemBuilder(this.build());
        }

        
        public ItemBuilder(Material material) {
            this.item = new ItemStack(material);
            this.meta = item.getItemMeta();
        }
        
        
        public ItemBuilder(Material material, short damage) {
            this.item = new ItemStack(material, 0, (short)damage);
            this.meta = item.getItemMeta();
        }
        
        public ItemBuilder(Material material, int amount) {
            this.item = new ItemStack(material, amount);
            this.meta = item.getItemMeta();
        }
        
        public ItemBuilder(Material material, int amount, String displayName) {
            this.item = new ItemStack(material, amount);
            this.meta = item.getItemMeta();
            this.meta.setDisplayName(displayName);
        }
        
        public ItemBuilder(Material material, int amount, String displayName, short damage) {
            this.item = new ItemStack(material, amount, damage);
            this.meta = item.getItemMeta();
            this.meta.setDisplayName(displayName);
        }
        
        public ItemBuilder(Material material, int amount, short damage) {
            this.item = new ItemStack(material, amount, damage);
            this.meta = item.getItemMeta();
        }
        
        public ItemBuilder(ItemStack item) {
            this.item = item;
            this.meta = item.getItemMeta();
        }
        
        public ItemBuilder incrementAmount() throws ItemBuildingError {
            if(item.getType().getMaxStackSize() < item.getAmount() + 1) throw new ItemBuildingError("You cannot build an itemstack with more than max stack size of this type");
            item.setAmount(item.getAmount() + 1);
            return this;
        }
        
        public ItemBuilder decrementAmount() throws ItemBuildingError {
            if(0 < item.getAmount() - 1) throw new ItemBuildingError("You cannot build an itemstack with less than 1 item");
            item.setAmount(item.getAmount() - 1);
            return this;
        }
        
        public ItemBuilder addAmount(int amountToAdd) throws ItemBuildingError {
            if(item.getType().getMaxStackSize() < item.getAmount() + amountToAdd) throw new ItemBuildingError("You cannot build an itemstack with more than max stack size of this type");
            item.setAmount(item.getAmount() + amountToAdd);
            return this;
        }
        
        public ItemBuilder removeAmount(int amountToRemove) throws ItemBuildingError {
            if(0 < item.getAmount() - amountToRemove) throw new ItemBuildingError("You cannot build an itemstack with less than 1 item");
            item.setAmount(item.getAmount() - amountToRemove);
            return this;
        }
        
        public ItemBuilder setAmount(int amountToSet) {
            if(amountToSet < 0 && amountToSet > this.item.getType().getMaxStackSize()) return this;
            item.setAmount(amountToSet);
            return this;
        }
        
        public ItemBuilder setMaterial(Material material) {
            this.item.setType(material);
            return this;
        }
        
        public ItemBuilder setDisplayName(String displayName) {
            this.meta.setDisplayName(displayName);
            return this;
        }
        
        public ItemBuilder setUnbreakable(boolean isUnbreakable) {
        	this.meta.spigot().setUnbreakable(isUnbreakable);
        	return this;
        }
        
        public ItemBuilder removeItemName() {
            this.meta.setDisplayName("§4 ");
            return this;
        }
        
        public ItemBuilder setLore(String...loreLines) {
            this.meta.setLore(Arrays.asList(loreLines));
            return this;
        }
        
        public ItemBuilder addLore(String...loreLines) {
            List<String> actualLore = this.meta.getLore();
            for(String line : loreLines) actualLore.add(line);
            this.meta.setLore(actualLore);
            return this;
        }
        
        public ItemBuilder clearLore() {
            this.meta.setLore(new ArrayList<String>());
            return this;
        }
        
        public ItemBuilder removeLoreLines(int...linesToRemove) {
            List<String> actualLore = this.meta.getLore();
            for(int indexesToRemove : linesToRemove) actualLore.remove(indexesToRemove);
            this.meta.setLore(actualLore);
            return this;
        }
        
        public ItemBuilder modifyLoreLine(int lineIndex, String newValue) {
            List<String> actualLore = this.meta.getLore();
            actualLore.set(lineIndex, newValue);
            this.meta.setLore(actualLore);
            return this;
        }
        
        public ItemBuilder insertLoreLine(int lineIndex, String newValue) {
            List<String> actualLore = this.meta.getLore();
            actualLore.add(lineIndex, newValue);
            this.meta.setLore(actualLore);
            return this;
        }
        
        public String[] getLoreAsArray() {
            return this.meta.getLore().toArray(new String[this.meta.getLore().size()]);
        }
        
        public List<String> getLore(){
            return this.meta.getLore();
        }
        
        public ItemBuilder addItemFlags(ItemFlag...flags) {
            this.meta.addItemFlags(flags);
            return this;
        }
        
        public ItemBuilder removeItemFlags(ItemFlag...flags) {
            this.meta.removeItemFlags(flags);
            return this;
        }
        
        public ItemBuilder clearItemFlags() {
            this.meta.removeItemFlags(this.meta.getItemFlags().toArray(new ItemFlag[this.meta.getItemFlags().size()]));
            return this;
        }
        
        public ItemBuilder addSafeEnchant(Enchantment ench, int level) {
            this.meta.addEnchant(ench, level, true);
            return this;
        }
        
        public ItemBuilder addNotSafeEnchant(Enchantment ench, int level) {
        	this.meta.addEnchant(ench, level, false);
            return this;
        }
        
        public ItemBuilder removeEnchant(Enchantment ench) {
            this.meta.removeEnchant(ench);
            return this;
        }
        
        public ItemBuilder addFakeEnchant() {        	
            this.meta.addEnchant(Enchantment.DURABILITY, 1, false);
            this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            return this;
        }
        
        public ItemBuilder setEnchants(Map<Enchantment, Integer> enchants) {
            for(Enchantment enchant : this.item.getEnchantments().keySet()) {
                this.item.removeEnchantment(enchant);
            }
            for(Entry<Enchantment, Integer> enchantEntry : enchants.entrySet()) {
                this.item.addUnsafeEnchantment(enchantEntry.getKey(), enchantEntry.getValue());
            }
            return this;
        }
        
        public ItemBuilder incrementItemDurability() throws ItemBuildingError {
            if(this.item.getDurability() + 1 <= this.item.getType().getMaxDurability()) throw new ItemBuildingError("You cannot increment the durability further than the max durability");
            this.item.setDurability((short) (this.item.getDurability() + 1));
            return this;
        }
        
        public ItemBuilder decrementItemDurability() throws ItemBuildingError {
            if(this.item.getDurability() - 1 <= 0) throw new ItemBuildingError("You cannot decrement the durability under or equal to 0");
            this.item.setDurability((short)( this.item.getDurability() - 1));
            return this;
        }


        public ItemBuilder setDurability(short durability) throws ItemBuildingError {
            if(!(0 < durability && durability <= this.item.getType().getMaxDurability())) throw new ItemBuildingError("You cannot set a illegal durability to this item !");
            this.item.setDurability(durability);
            return this;
        }


        public ItemBuilder addDurability(short durability) throws ItemBuildingError {
            if(this.item.getDurability() + durability <= this.item.getType().getMaxDurability()) throw new ItemBuildingError("You cannot increment the durability further than the max durability");
            this.item.setDurability((short) (this.item.getDurability() + durability));
            return this;
        }

        public ItemBuilder removeDurability(short durability) throws ItemBuildingError {
            if(this.item.getDurability() - durability <= 0) throw new ItemBuildingError("You cannot decrement the durability under or equal to 0");
            this.item.setDurability((short)(this.item.getDurability() - durability));
            return this;
        }


        public ItemBuilder resetDurability() {
            this.item.setDurability(this.item.getType().getMaxDurability());
            return this;
        }
        
    }
    
    public static class PotionBuilder extends ItemBuilder {
 
        public PotionBuilder(int amount, String displayName) {
            super(Material.POTION, amount, displayName);
        }
        
        public PotionBuilder addEffect(PotionEffectType effecttype, int duration, int level, boolean showPotionParticles, boolean overwrite) {
            PotionMeta meta = (PotionMeta) this.meta;
            meta.addCustomEffect(new PotionEffect(effecttype, duration, level, showPotionParticles), overwrite);
            this.meta = meta;
            return this;
        }
        
        public PotionBuilder removeEffect(PotionEffectType effecttype) {
            PotionMeta meta = (PotionMeta) this.meta;
            meta.removeCustomEffect(effecttype);
            this.meta = meta;
            return this;
        }
        
        public PotionBuilder clearPotionEffects() {
            PotionMeta meta = (PotionMeta) this.meta;
            meta.clearCustomEffects();
            this.meta = meta;
            return this;
        }
        
        public List<PotionEffect> getPotionEffects(){
            return ((PotionMeta) meta).getCustomEffects();
        }
        
        public boolean hasCustomEffect(PotionEffectType potionType) {
            return ((PotionMeta) meta).hasCustomEffect(potionType);
        }


        public PotionBuilder setPotionMainEffect(PotionEffectType type) {
            PotionMeta meta = (PotionMeta) this.meta;
            meta.setMainEffect(type);
            this.meta = meta;
            return this;
        }
    }
    
    public static class BookBuilder extends ItemBuilder {
 
        public BookBuilder(int amount, String displayName) {
            super(Material.BOOK, amount, displayName);
        }
        
        public BookBuilder(ItemStack item) {
        	super(item);
        }
        
        public ItemStack build() {
            item.setItemMeta(meta);
            return item;
        }
        
        public BookBuilder setAuthor(String authorNickName) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setAuthor(authorNickName);
            this.meta = meta;
            return this;
        }
        
        public BookBuilder addPages(String... pages) {
            BookMeta meta = (BookMeta) this.meta;
            meta.addPage(pages);
            this.meta = meta;
            return this;
        }
        
        public BookBuilder clearPage(int index) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setPage(index, "");
            this.meta = meta;
            return this;
        }
        
        public BookBuilder setPageText(int page, String text) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setPage(page, text);
            this.meta = meta;
            return this;
        }
        
        public BookBuilder setPages(Map<Integer, String> pages) {
            BookMeta meta = (BookMeta) this.meta;
            for(Entry<Integer, String> page : pages.entrySet()) meta.setPage(page.getKey(), page.getValue());
            this.meta = meta;
            return this;
        }
        
        public BookBuilder setPages(String...pages) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setPages(pages);
            this.meta = meta;
            return this;
        }
        
        public BookBuilder setPages(List<String> pages) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setPages(pages);
            this.meta = meta;
            return this;
        }
        
        public BookBuilder setBookTitle(String title) {
            BookMeta meta = (BookMeta) this.meta;
            meta.setTitle(title);
            this.meta = meta;
            return this;
        }
        
        public String getAuthorNickname() {
            return ((BookMeta) meta).getAuthor();
        }
        
        public int getPageCount() {
            return ((BookMeta) meta).getPageCount();
        }
        
    }
    
    public static class EnchantedBookBuilder extends ItemBuilder {
 
        public EnchantedBookBuilder(Material material, int amount, String displayName) {
            super(Material.ENCHANTED_BOOK, amount, displayName);
        }
        
        public EnchantedBookBuilder addEnchant(Enchantment ench, int level, boolean ignoreSafety) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.meta;
            meta.addStoredEnchant(ench, level, ignoreSafety);
            this.meta = meta;
            return this;
        }
        
        public EnchantedBookBuilder removeEnchant(Enchantment ench) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) this.meta;
            meta.removeStoredEnchant(ench);
            this.meta = meta;
            return this;
        }
        
        public boolean willCauseIncompatibilty(Enchantment ench) {
            return ((EnchantmentStorageMeta) meta).hasConflictingStoredEnchant(ench);
        }
        
        public boolean hasEnchants() {
            return ((EnchantmentStorageMeta) meta).hasEnchants();
        }
    }
    
    public static class LeatherArmorBuilder extends ItemBuilder {
 
        public LeatherArmorBuilder(Material material, int amount, String displayName) throws ItemBuildingError {
            super(material, amount, displayName);
            if(!Arrays.asList(Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS).contains(material)) {
                throw new ItemBuildingError("You cannot build a leather armor without a leather-type material");
            }
        }
        
        public LeatherArmorBuilder setColor(Color color) {
            LeatherArmorMeta meta = (LeatherArmorMeta) this.meta;
            meta.setColor(color);
            this.meta = meta;
            return this;
        }
        
        public LeatherArmorBuilder setRGBColor(int red, int green, int blue) {
            LeatherArmorMeta meta = (LeatherArmorMeta) this.meta;
            meta.setColor(Color.fromRGB(red, green, blue));
            this.meta = meta;
            return this;
        }
        
        public LeatherArmorBuilder setHexColor(String hexcode) {
            LeatherArmorMeta meta = (LeatherArmorMeta) this.meta;
            java.awt.Color hexColor = java.awt.Color.decode(hexcode);
            meta.setColor(Color.fromRGB(hexColor.getRed(), hexColor.getGreen(), hexColor.getBlue()));
            this.meta = meta;
            return this;
        }
        
        public Color getColor() {
            return ((LeatherArmorMeta) meta).getColor();
        }
        
        
    }
    
    public static class ItemBuildingError extends Exception {
 
        static final long serialVersionUID = 8660602255405420460L;
        
        public ItemBuildingError(String errorMessage) {
            super(errorMessage);
        }
        
    }
}
