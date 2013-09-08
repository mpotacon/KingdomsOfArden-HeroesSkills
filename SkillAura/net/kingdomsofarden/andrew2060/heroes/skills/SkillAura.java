package net.kingdomsofarden.andrew2060.heroes.skills;

import net.kingdomsofarden.andrew2060.heroes.skills.aura.AuraEffect;
import net.kingdomsofarden.andrew2060.heroes.skills.aura.AuraWrapper;

import org.bukkit.ChatColor;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;

public class SkillAura extends ActiveSkill {

	public SkillAura(Heroes plugin) {
		super(plugin, "Aura");
		setDescription("Passive: this hero carries an aura that provides bonuses to allies in the party. Active: displays the currently active aura");
		setIdentifiers("skill aura", "skill activeaura");
		setUsage("/skill Aura");
	}

	@Override
	public String getDescription(Hero arg0) {
		return getDescription();
	}

	@Override
	public SkillResult use(Hero h, String[] arg1) {
		String name;
		if(!h.hasEffect("AuraEffect")) {
			h.addEffect(new AuraEffect(this.plugin,new NeutralAuraFWrapper()));
			name = "None";
		} else {
			AuraEffect aEffect = (AuraEffect) h.getEffect("AuraEffect");
			name = aEffect.fWrapper.auraName;
		}
		h.getPlayer().sendMessage(ChatColor.GRAY + "Currently Active Aura: " + name);
		return SkillResult.NORMAL;
	}
	
	
	private class NeutralAuraFWrapper extends AuraWrapper {
		public NeutralAuraFWrapper() {
			super("None");
		}
		@Override
		public void onApply(Hero h) {
			
		}
		
		@Override
		public void onTick(Hero h) {
			
		}

		@Override
		public void onEnd(Hero h) {
			
		}
		
	}
}
