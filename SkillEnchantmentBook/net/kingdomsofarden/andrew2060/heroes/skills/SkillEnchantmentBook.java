package net.kingdomsofarden.andrew2060.heroes.skills;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.util.Util;

public class SkillEnchantmentBook extends ActiveSkill {
	HashMap<Player,PlayerExecuteData> executors;
	
	private class PlayerExecuteData {
		Map<Enchantment, Integer> enchant;
		long expirationTime;
		ItemStack hand;
		int heldSlot;
		public PlayerExecuteData(Map<Enchantment, Integer> enchant, long expirationTime, ItemStack hand, int heldSlot) {
			this.enchant = enchant;
			this.expirationTime = expirationTime;
			this.hand =  hand;
			this.heldSlot = heldSlot;
		}
		
	}
	public SkillEnchantmentBook(Heroes plugin) {
		super(plugin, "EnchantmentBook");
		setUsage("/skill EnchantmentBook");
		setDescription("Applies enchantment books to an item.");
		setIdentifiers("skill enchantmentbook");
		setArgumentRange(0,0);
		executors = new LinkedHashMap<Player, PlayerExecuteData>();
	}

	@SuppressWarnings("deprecation")
	@Override
	public SkillResult use(Hero h, String[] args) {
		Player p = h.getPlayer();
		if(!executors.containsKey(p)) {
			ItemStack hand = p.getItemInHand();
			if(!hand.getType().equals(Material.ENCHANTED_BOOK)) {
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] This is not an Enchanted Book!");
				return SkillResult.INVALID_TARGET_NO_MSG;
			}
			Map<Enchantment, Integer> enchant = ((EnchantmentStorageMeta)hand.getItemMeta()).getStoredEnchants();
			executors.put(p, new PlayerExecuteData(enchant, System.currentTimeMillis() + 30000 , hand, p.getInventory().getHeldItemSlot()));
			p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] Select an item to enchant by using this skill again!");
			return SkillResult.INVALID_TARGET_NO_MSG; //Prevent cooldowns/reagent use from triggering
		} else {
			if(executors.get(p).expirationTime <= System.currentTimeMillis()) {
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] Your selection has expired, please try again");
				executors.remove(p);
				return SkillResult.INVALID_TARGET_NO_MSG;
			}
			ItemStack tool = p.getItemInHand();
			if(!(Util.isArmor(tool.getType()) || Util.isWeapon(tool.getType()))) {
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] This is not an enchantable item!");
				return SkillResult.INVALID_TARGET_NO_MSG;
			}
			PlayerExecuteData struct = executors.get(p);
			executors.remove(p);
			if(p.getInventory().getItem(struct.heldSlot) == null || (!p.getInventory().getItem(struct.heldSlot).equals(struct.hand))) {
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] Cannot find the original enchantment book inside your inventory anymore! Did you move it?");
				return SkillResult.FAIL;
			}		
			try {
				tool.addEnchantments(struct.enchant);
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] Enchantment Successfully applied from book!");
				p.getInventory().clear(struct.heldSlot);
				p.updateInventory(); //Blah blah deprecated but bukkit doesn't include new functionality for it
			} catch (IllegalArgumentException e) {
				p.sendMessage(ChatColor.GRAY + "�7[�2Skill�7] Enchantment cannot be applied to this type of item!");
				return SkillResult.FAIL;
			}
		}
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Hero arg0) {
		return getDescription();
	}
}
