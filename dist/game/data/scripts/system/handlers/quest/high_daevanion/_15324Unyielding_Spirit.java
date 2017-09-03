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

public class _15324Unyielding_Spirit extends QuestHandler
{
	private static final int questId = 15324;
	
	private static final int[] Cygnea =
	{
		235939,
		235940,
		235941,
		235942,
		235943,
		235944,
		235945,
		235946,
		235947
	};
	private static final int[] Levinshor =
	{
		233929,
		233930,
		233931,
		233932,
		233933,
		233934,
		233935,
		233936
	};
	private static final int[] Kaldor =
	{
		234277,
		234278,
		234279,
		234280,
		234524,
		234526,
		234527,
		234528,
		234531,
		234532
	};
	
	public _15324Unyielding_Spirit()
	{
		super(questId);
	}
	
	@Override
	public void register()
	{
		qe.registerQuestNpc(805331).addOnQuestStart(questId); // Machina.
		qe.registerQuestNpc(805331).addOnTalkEvent(questId); // Machina.
		for (int mob : Cygnea)
		{
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int mob : Levinshor)
		{
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int mob : Kaldor)
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
			if (targetId == 805331)
			{ // Machina.
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
			if (targetId == 805331)
			{ // Machina.
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
		if ((var == 0) && (var1 >= 0) && (var1 < 19))
		{
			return defaultOnKillEvent(env, Cygnea, var1, var1 + 1, 1);
		}
		else if ((var == 0) && (var1 == 19))
		{
			qs.setQuestVarById(1, 0);
			changeQuestStep(env, 0, 1, false);
			updateQuestStatus(env);
			return true;
		}
		if ((var == 1) && (var1 >= 0) && (var1 < 19))
		{
			return defaultOnKillEvent(env, Levinshor, var1, var1 + 1, 1);
		}
		else if ((var == 1) && (var1 == 19))
		{
			qs.setQuestVarById(1, 0);
			changeQuestStep(env, 1, 2, false);
			updateQuestStatus(env);
			return true;
		}
		if ((var == 2) && (var1 >= 0) && (var1 < 19))
		{
			return defaultOnKillEvent(env, Kaldor, var1, var1 + 1, 1);
		}
		else if ((var == 2) && (var1 == 19))
		{
			qs.setQuestVarById(1, 0);
			qs.setQuestVar(3);
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}
}