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
package system.handlers.quest.daevanion;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestDialog;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/****/
/**
 * Author Rinzler (Encom) /
 ****/

public class _1990A_Sages_Gift extends QuestHandler
{
	private static final int questId = 1990;
	
	private int A = 0;
	private int B = 0;
	private int C = 0;
	private int ALL = 0;
	
	public _1990A_Sages_Gift()
	{
		super(questId);
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		return defaultOnLvlUpEvent(env, 1989, true);
	}
	
	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		final int[] mobs =
		{
			256617,
			253720,
			253721,
			254513,
			254514
		};
		qe.registerQuestNpc(203771).addOnQuestStart(questId);
		qe.registerQuestNpc(203771).addOnTalkEvent(questId);
		for (int mob : mobs)
		{
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final QuestDialog dialog = env.getDialog();
		final int targetId = env.getTargetId();
		if ((qs == null) || (qs.getStatus() == QuestStatus.NONE))
		{
			if (targetId == 203771)
			{
				if (env.getDialog() == QuestDialog.START_DIALOG)
				{
					if (isDaevanionArmorEquipped(player))
					{
						return sendQuestDialog(env, 4762);
					}
					else
					{
						return sendQuestDialog(env, 4848);
					}
				}
				else
				{
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			final int var = qs.getQuestVarById(0);
			final int var1 = qs.getQuestVarById(1);
			if (targetId == 203771)
			{
				switch (dialog)
				{
					case START_DIALOG:
					{
						if (var == 0)
						{
							return sendQuestDialog(env, 1011);
						}
						else if ((var == 2) && (var1 == 60))
						{
							return sendQuestDialog(env, 1693);
						}
						else if (var == 3)
						{
							return sendQuestDialog(env, 2034);
						}
					}
					case CHECK_COLLECTED_ITEMS:
					{
						return checkQuestItems(env, 0, 1, false, 10000, 10001);
					}
					case SELECT_ACTION_2035:
					{
						final int currentDp = player.getCommonData().getDp();
						final int maxDp = player.getGameStats().getMaxDp().getCurrent();
						final long burner = player.getInventory().getItemCountByItemId(186000040);
						if ((currentDp == maxDp) && (burner >= 1))
						{
							removeQuestItem(env, 186000040, 1);
							player.getCommonData().setDp(0);
							changeQuestStep(env, 3, 3, true);
							return sendQuestDialog(env, 5);
						}
						else
						{
							return sendQuestDialog(env, 2120);
						}
					}
					case STEP_TO_2:
					{
						return defaultCloseDialog(env, 1, 2);
					}
					case STEP_TO_3:
					{
						qs.setQuestVar(3);
						updateQuestStatus(env);
						return sendQuestSelectionDialog(env);
					}
					case FINISH_DIALOG:
					{
						return sendQuestSelectionDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 203771)
			{
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if ((qs != null) && (qs.getStatus() == QuestStatus.START))
		{
			final int var = qs.getQuestVarById(0);
			if (var == 2)
			{
				switch (env.getTargetId())
				{
					case 256617:
					{ // Strange Lake Spirit.
						if ((A >= 0) && (A < 30))
						{
							++A;
							ALL = C;
							ALL = ALL << 7;
							ALL += B;
							ALL = ALL << 7;
							ALL += A;
							ALL = ALL << 7;
							ALL += 2;
							qs.setQuestVar(ALL);
							updateQuestStatus(env);
						}
						break;
					}
					case 253720: // Lava Hoverstone.
					case 253721:
					{ // Lava Hoverstone.
						if ((B >= 0) && (B < 30))
						{
							++B;
							ALL = C;
							ALL = ALL << 7;
							ALL += B;
							ALL = ALL << 7;
							ALL += A;
							ALL = ALL << 7;
							ALL += 2;
							qs.setQuestVar(ALL);
							updateQuestStatus(env);
						}
						break;
					}
					case 254513: // Disturbed Resident.
					case 254514:
					{ // Disturbed Resident.
						if ((C >= 0) && (C < 30))
						{
							++C;
							ALL = C;
							ALL = ALL << 7;
							ALL += B;
							ALL = ALL << 7;
							ALL += A;
							ALL = ALL << 7;
							ALL += 2;
							qs.setQuestVar(ALL);
							updateQuestStatus(env);
						}
						break;
					}
				}
				if ((qs.getQuestVarById(0) == 2) && (A == 30) && (B == 30) && (C == 30))
				{
					qs.setQuestVarById(1, 60);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isDaevanionArmorEquipped(Player player)
	{
		final int plate = player.getEquipment().itemSetPartsEquipped(9);
		final int chain = player.getEquipment().itemSetPartsEquipped(8);
		final int leather = player.getEquipment().itemSetPartsEquipped(7);
		final int cloth = player.getEquipment().itemSetPartsEquipped(6);
		final int gunslinger = player.getEquipment().itemSetPartsEquipped(378);
		if ((plate == 5) || (chain == 5) || (leather == 5) || (cloth == 5) || (gunslinger == 5))
		{
			return true;
		}
		return false;
	}
}