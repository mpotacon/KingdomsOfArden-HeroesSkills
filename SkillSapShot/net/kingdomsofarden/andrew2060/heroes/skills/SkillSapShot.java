package net.kingdomsofarden.andrew2060.heroes.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.HeroRegainHealthEvent;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class SkillSapShot extends ActiveSkill {
	public SkillSapShot(Heroes plugin) {
		super(plugin, "SapShot");
		setUsage("/skill sapshot");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill sapshot" });
		setDescription("On use, arrows fired for the next $1 seconds will have a sapping effect, $2% of damage dealt (ignores armor) is restored in health. If the shot is a killing shot and the target is a monster, the life force of the target will be trapped inside an egg if available.");
		Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(this), plugin);
	}
	public String getDescription(Hero hero) {
		int level = hero.getLevel(hero.getHeroClass());
		int levelDuration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE.node(), 500, false);
		int effectDurationMillis = level * levelDuration + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false);
		return getDescription()
				.replace("$1", effectDurationMillis / 1000 +"")
				.replace("$2", SkillConfigManager.getUseSetting(hero, this, "sapPercentage", 20, false) +"");
	}

	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set("sapPercentage", Integer.valueOf(20));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(5000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(500));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(120000));
		return node;
	}
	public void init() {
		super.init();
	}

	public SkillResult use(Hero hero, String[] args) {
		broadcastExecuteText(hero);
		int level = hero.getLevel(hero.getHeroClass());
		int levelDuration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE.node(), 500, false);
		int effectDurationMillis = level * levelDuration + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false);
		hero.addEffect(new SapShotEffect(this, effectDurationMillis));
		return SkillResult.NORMAL;
	}

	public class SapShotEffect extends ExpirableEffect {
		public SapShotEffect(Skill skill, long duration) {
			super(skill, "SapShotEffect", duration);
			this.types.add(EffectType.BENEFICIAL);
		}

		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
			broadcast(hero.getPlayer().getLocation(), "$1's shots now sap life!".replace("$1", hero.getName()), new Object[0]);
		}

		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
			broadcast(hero.getPlayer().getLocation(), "$1's shots no longer sap life!".replace("$1", hero.getName()), new Object[0]);
		}
	}

	public class SkillListener implements Listener {
		private Skill skill;

		public SkillListener(Skill skill) { this.skill = skill; }

		@EventHandler(priority=EventPriority.MONITOR)
		public void onWeaponDamage(WeaponDamageEvent event) {
			if (event.isCancelled()) {
				return;
			}
			if (!(event.getDamager() instanceof Hero)) {
				return;
			}
			Hero h = (Hero)event.getDamager();
			if (!h.hasEffect("SapShotEffect")) {
				return;
			}
			double d = event.getDamage();
			int sapPercentage = SkillConfigManager.getUseSetting(h, this.skill, "sapPercentage", 20, false);
			double healAmount = (d * sapPercentage * 0.01D);
			HeroRegainHealthEvent hEvent = new HeroRegainHealthEvent(h, healAmount, skill, h);
			Bukkit.getPluginManager().callEvent(hEvent);
			if(!hEvent.isCancelled()) {
			    h.heal(healAmount);
	            h.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Skill" + ChatColor.GRAY + "] Sapped " + ChatColor.AQUA + d * sapPercentage * 0.01D + ChatColor.GRAY + " Health!");
			}
		}

		@SuppressWarnings("deprecation")
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
		public void onEntityDamagebyEntity(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Arrow)) {
				return;
			}
			if ((!((event.getEntity() instanceof Monster) || (event.getEntity() instanceof Ghast) || (event.getEntity() instanceof Animals))) || event.getEntity() instanceof Wither) {
				return;
			}
			Arrow arrow = (Arrow)event.getDamager();
			if (!(arrow.getShooter() instanceof Player)) {
				return;
			}
			Player p = (Player)arrow.getShooter();
			Hero h = SkillSapShot.this.plugin.getCharacterManager().getHero(p);
			if (!h.hasEffect("SapShotEffect")) {
				return;
			}
			if(((LivingEntity)event.getEntity()).getHealth()-event.getDamage() > 0) {
			    return;
			}
			if (p.getInventory().contains(Material.EGG, 1)) {
				p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.MONSTER_EGG, 1, event.getEntityType().getTypeId()));
				p.getWorld().playEffect(event.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 5);
				p.getWorld().playEffect(p.getLocation(), Effect.GHAST_SHRIEK, 1);
				event.getEntity().playEffect(EntityEffect.DEATH);
				event.getEntity().remove();
				int stackID = p.getInventory().first(Material.EGG);
				ItemStack eggStack = p.getInventory().getItem(stackID);
				if (eggStack.getAmount() > 1) {
					eggStack.setAmount(eggStack.getAmount() - 1);
					p.updateInventory();
					p.sendMessage(ChatColor.AQUA + "Monster Soul Captured");
					return;
				}
				p.getInventory().remove(eggStack);
				p.updateInventory();
				p.sendMessage(ChatColor.AQUA + "Monster Soul Captured");
				return;
			}

			p.sendMessage(ChatColor.GRAY + "No Eggs!");
		}
	}
}