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
package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatDualWeaponMasteryFunction;
import com.aionemu.gameserver.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponDualEffect")
public class WeaponDualEffect extends BuffEffect
{
	
	@Override
	public void startEffect(Effect effect)
	{
		if (change == null)
		{
			return;
		}
		
		if (effect.getEffected() instanceof Player)
		{
			((Player) effect.getEffected()).setDualEffectValue(value);
		}
		
		final List<IStatFunction> modifiers = getModifiers(effect);
		final List<IStatFunction> masteryModifiers = new ArrayList<>(modifiers.size());
		for (final IStatFunction modifier : modifiers)
		{
			masteryModifiers.add(new StatDualWeaponMasteryFunction(effect, modifier));
		}
		if (masteryModifiers.size() > 0)
		{
			effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
		}
	}
	
	@Override
	public void endEffect(Effect effect)
	{
		if (effect.getEffected() instanceof Player)
		{
			((Player) effect.getEffected()).setDualEffectValue(0);
		}
		
		super.endEffect(effect);
	}
	
}