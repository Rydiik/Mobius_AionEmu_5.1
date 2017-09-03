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
package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.follow.FollowStartService;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplates;

public class SiegeWeaponController extends SummonController
{
	
	private final NpcSkillTemplates skills;
	
	public SiegeWeaponController(int npcId)
	{
		skills = DataManager.NPC_SKILL_DATA.getNpcSkillList(npcId);
	}
	
	@Override
	public void release(UnsummonType unsummonType)
	{
		getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
		getOwner().getMoveController().abortMove();
		super.release(unsummonType);
	}
	
	@Override
	public void restMode()
	{
		getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
		super.restMode();
		getOwner().getAi2().onCreatureEvent(AIEventType.STOP_FOLLOW_ME, getMaster());
	}
	
	@Override
	public void setUnkMode()
	{
		super.setUnkMode();
		getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
	}
	
	@Override
	public final void guardMode()
	{
		super.guardMode();
		getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
		getOwner().setTarget(getMaster());
		getOwner().getAi2().onCreatureEvent(AIEventType.FOLLOW_ME, getMaster());
		getOwner().getMoveController().moveToTargetObject();
		getMaster().getController().addTask(TaskId.SUMMON_FOLLOW, FollowStartService.newFollowingToTargetCheckTask(getOwner(), getMaster()));
	}
	
	@Override
	public void attackMode(int targetObjId)
	{
		super.attackMode(targetObjId);
		final Creature target = (Creature) getOwner().getKnownList().getObject(targetObjId);
		if (target == null)
		{
			return;
		}
		getOwner().setTarget(target);
		getOwner().getAi2().onCreatureEvent(AIEventType.FOLLOW_ME, target);
		getOwner().getMoveController().moveToTargetObject();
		getMaster().getController().addTask(TaskId.SUMMON_FOLLOW, FollowStartService.newFollowingToTargetCheckTask(getOwner(), target));
	}
	
	@Override
	public void onDie(Creature lastAttacker)
	{
		getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
		super.onDie(lastAttacker);
	}
	
	public NpcSkillTemplates getNpcSkillTemplates()
	{
		return skills;
	}
}