/*
 * This file is part of the Aion-Emu project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.stats.CalculatedPlayerStatsTemplate;
import com.aionemu.gameserver.model.templates.stats.PlayerStatsTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

@XmlRootElement(name = "player_stats_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerStatsData
{
	@XmlElement(name = "player_stats", required = true)
	private List<PlayerStatsType> templatesList = new ArrayList<>();
	
	private final TIntObjectHashMap<PlayerStatsTemplate> playerTemplates = new TIntObjectHashMap<>();
	
	void afterUnmarshal(Unmarshaller u, Object parent)
	{
		for (PlayerStatsType pt : templatesList)
		{
			final int code = makeHash(pt.getRequiredPlayerClass(), pt.getRequiredLevel());
			final PlayerStatsTemplate template = pt.getTemplate();
			template.setMaxMp(Math.round((template.getMaxMp() * 100f) / template.getWill()));
			template.setMaxHp(Math.round((template.getMaxHp() * 100f) / template.getHealth()));
			int agility = template.getAgility();
			agility = (agility - 100);
			template.setEvasion(Math.round(template.getEvasion() - (template.getEvasion() * agility * 0.003f)));
			template.setBlock(Math.round(template.getBlock() - (template.getBlock() * agility * 0.0025f)));
			template.setParry(Math.round(template.getParry() - (template.getParry() * agility * 0.0025f)));
			template.setStrikeResist(template.getStrikeResist());
			template.setSpellResist(template.getSpellResist());
			playerTemplates.put(code, pt.getTemplate());
		}
		playerTemplates.put(makeHash(PlayerClass.WARRIOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.WARRIOR));
		playerTemplates.put(makeHash(PlayerClass.GLADIATOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.GLADIATOR));
		playerTemplates.put(makeHash(PlayerClass.TEMPLAR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.TEMPLAR));
		playerTemplates.put(makeHash(PlayerClass.SCOUT, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SCOUT));
		playerTemplates.put(makeHash(PlayerClass.ASSASSIN, 0), new CalculatedPlayerStatsTemplate(PlayerClass.ASSASSIN));
		playerTemplates.put(makeHash(PlayerClass.RANGER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.RANGER));
		playerTemplates.put(makeHash(PlayerClass.PRIEST, 0), new CalculatedPlayerStatsTemplate(PlayerClass.PRIEST));
		playerTemplates.put(makeHash(PlayerClass.CHANTER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CHANTER));
		playerTemplates.put(makeHash(PlayerClass.CLERIC, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CLERIC));
		playerTemplates.put(makeHash(PlayerClass.MAGE, 0), new CalculatedPlayerStatsTemplate(PlayerClass.MAGE));
		playerTemplates.put(makeHash(PlayerClass.SORCERER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SORCERER));
		playerTemplates.put(makeHash(PlayerClass.SPIRIT_MASTER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SPIRIT_MASTER));
		
		// News Class 4.3
		playerTemplates.put(makeHash(PlayerClass.MUSE, 0), new CalculatedPlayerStatsTemplate(PlayerClass.MUSE));
		playerTemplates.put(makeHash(PlayerClass.SONGWEAVER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SONGWEAVER));
		playerTemplates.put(makeHash(PlayerClass.TECHNIST, 0), new CalculatedPlayerStatsTemplate(PlayerClass.TECHNIST));
		playerTemplates.put(makeHash(PlayerClass.GUNSLINGER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.GUNSLINGER));
		// News Class 4.5
		playerTemplates.put(makeHash(PlayerClass.AETHERTECH, 0), new CalculatedPlayerStatsTemplate(PlayerClass.AETHERTECH));
		templatesList.clear();
		templatesList = null;
	}
	
	public PlayerStatsTemplate getTemplate(Player player)
	{
		PlayerStatsTemplate template = getTemplate(player.getCommonData().getPlayerClass(), player.getLevel());
		if (template == null)
		{
			template = getTemplate(player.getCommonData().getPlayerClass(), 0);
		}
		return template;
	}
	
	public PlayerStatsTemplate getTemplate(PlayerClass playerClass, int level)
	{
		PlayerStatsTemplate template = playerTemplates.get(makeHash(playerClass, level));
		if (template == null)
		{
			template = getTemplate(playerClass, 0);
		}
		return template;
	}
	
	public int size()
	{
		return playerTemplates.size();
	}
	
	@XmlRootElement(name = "playerStatsTemplateType")
	private static class PlayerStatsType
	{
		@XmlAttribute(name = "class", required = true)
		private PlayerClass requiredPlayerClass;
		
		@XmlAttribute(name = "level", required = true)
		private int requiredLevel;
		
		@XmlElement(name = "stats_template")
		private PlayerStatsTemplate template;
		
		public PlayerClass getRequiredPlayerClass()
		{
			return requiredPlayerClass;
		}
		
		public int getRequiredLevel()
		{
			return requiredLevel;
		}
		
		public PlayerStatsTemplate getTemplate()
		{
			return template;
		}
	}
	
	// In 4.5 (11)
	private static int makeHash(PlayerClass playerClass, int level)
	{
		return (level << 11) | playerClass.ordinal();
	}
}