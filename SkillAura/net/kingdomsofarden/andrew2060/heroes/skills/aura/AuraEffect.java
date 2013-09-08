
package net.kingdomsofarden.andrew2060.heroes.skills.aura;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.PeriodicEffect;

public class AuraEffect extends PeriodicEffect {
	public AuraWrapper fWrapper;
	public AuraEffect(Heroes plugin, AuraWrapper funcWrapper) {
		super(plugin, "AuraEffect", 40L);
		fWrapper = funcWrapper;
	}
	
	@Override
	public void applyToHero(Hero h) {
		fWrapper.onApply(h);
		if(fWrapper != null && !fWrapper.auraName.equalsIgnoreCase("none")) {
		    broadcast(h.getPlayer().getLocation(), "�7[�2Skill�7]$1 has activated the aura $2", new Object[] {h.getName(),fWrapper.auraName});
		}
	}
	
	@Override
	public void tickHero(Hero h) {
		fWrapper.onTick(h);
	}
	@Override
	public void removeFromHero(Hero h) {
		fWrapper.onEnd(h);
		if(fWrapper != null && !fWrapper.auraName.equalsIgnoreCase("none")) {
            broadcast(h.getPlayer().getLocation(), "�7[�2Skill�7]$1 has stopped using the aura $2", new Object[] {h.getName(),fWrapper.auraName});
        }
	}
}
