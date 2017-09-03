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
package system.handlers.quest.high_daevanion;

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

public class _25325Threat_The_Farm extends QuestHandler
{
	private static final int questId = 25325;
	
	private static final int[] Gelkmaros =
	{
		216119,
		216120,
		216121,
		216122,
		216123,
		216438,
		216439,
		216749,
		216804,
		216807,
		216808,
		216810,
		216811,
		216812,
		216816,
		216817,
		216820,
		216821,
		216822,
		216823,
		217082
	};
	
	public _25325Threat_The_Farm()
	{
		super(questId);
	}
	
	@Override
	public void register()
	{
		qe.registerQuestNpc(805343).addOnQuestStart(questId); // Mashinee.
		qe.registerQuestNpc(805343).addOnTalkEvent(questId); // Mashinee.
		for (final int mob : Gelkmaros)
		{
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final int targetId = env.getTargetId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final QuestDialog dialog = env.getDialog();
		if ((qs == null) || (qs.getStatus() == QuestStatus.NONE) || qs.canRepeat())
		{
			if (targetId == 805343)
			{ // Mashinee.
				if (dialog == QuestDialog.START_DIALOG)
				{
					return sendQuestDialog(env, 4762);
				}
				else
				{
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 805343)
			{ // Mashinee.
				if (env.getDialog() == QuestDialog.START_DIALOG)
				{
					return sendQuestDialog(env, 10002);
				}
				else if (env.getDialog() == QuestDialog.SELECT_REWARD)
				{
					return sendQuestDialog(env, 5);
				}
				else
				{
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final int var = qs.getQuestVarById(0);
		final int var1 = qs.getQuestVarById(1);
		if ((qs == null) || (qs.getStatus() != QuestStatus.START))
		{
			return false;
		}
		if ((var == 0) && (var1 >= 0) && (var1 < 29))
		{
			return defaultOnKillEvent(env, Gelkmaros, var1, var1 + 1, 1);
		}
		else if ((var == 0) && (var1 == 29))
		{
			qs.setQuestVarById(1, 0);
			qs.setQuestVar(1);
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}
}