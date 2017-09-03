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
package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.ChallengeTasksDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.TownDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.challenge.ChallengeQuest;
import com.aionemu.gameserver.model.challenge.ChallengeTask;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.challenge.ChallengeTaskTemplate;
import com.aionemu.gameserver.model.templates.challenge.ChallengeType;
import com.aionemu.gameserver.model.templates.challenge.ContributionReward;
import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHALLENGE_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

import javolution.util.FastMap;

public class ChallengeTaskService
{
	private static final Logger log = LoggerFactory.getLogger(ChallengeTaskService.class);
	private final Map<Integer, Map<Integer, ChallengeTask>> cityTasks;
	private final Map<Integer, Map<Integer, ChallengeTask>> legionTasks;
	
	private static class SingletonHolder
	{
		protected static final ChallengeTaskService instance = new ChallengeTaskService();
	}
	
	public static ChallengeTaskService getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private ChallengeTaskService()
	{
		cityTasks = new FastMap<Integer, Map<Integer, ChallengeTask>>().shared();
		legionTasks = new FastMap<Integer, Map<Integer, ChallengeTask>>().shared();
		log.info("ChallengeTaskService initialized.");
	}
	
	public void showTaskList(Player player, ChallengeType challengeType, int ownerId)
	{
		if (CustomConfig.CHALLENGE_TASKS_ENABLED)
		{
			int ownerLevel = 0;
			switch (challengeType)
			{
				case LEGION:
					ownerLevel = player.getLegion().getLegionLevel();
					break;
				case TOWN:
					ownerLevel = TownService.getInstance().getTownById(ownerId).getLevel();
					break;
				default:
					break;
			}
			final List<ChallengeTask> availableTasks = buildTaskList(player, challengeType, ownerId, ownerLevel);
			PacketSendUtility.sendPacket(player, new SM_CHALLENGE_LIST(2, ownerId, challengeType, availableTasks));
			for (final ChallengeTask task : availableTasks)
			{
				PacketSendUtility.sendPacket(player, new SM_CHALLENGE_LIST(7, ownerId, challengeType, task));
			}
		}
	}
	
	private List<ChallengeTask> buildTaskList(Player player, ChallengeType challengeType, int ownerId, int ownerLevel)
	{
		Map<Integer, Map<Integer, ChallengeTask>> taskMap = null;
		if (challengeType == ChallengeType.LEGION)
		{
			taskMap = legionTasks;
		}
		else if (challengeType == ChallengeType.TOWN)
		{
			taskMap = cityTasks;
		}
		final int playerTownId = TownService.getInstance().getTownResidence(player);
		final List<ChallengeTask> availableTasks = new ArrayList<>();
		if (!taskMap.containsKey(ownerId))
		{
			final Map<Integer, ChallengeTask> tasks = DAOManager.getDAO(ChallengeTasksDAO.class).load(ownerId, challengeType);
			taskMap.put(ownerId, tasks);
		}
		for (final ChallengeTask ct : taskMap.get(ownerId).values())
		{
			if (ct.getTemplate().isRepeatable())
			{
				availableTasks.add(ct);
			}
			else if (!ct.isCompleted())
			{
				availableTasks.add(ct);
			}
		}
		for (final ChallengeTaskTemplate template : DataManager.CHALLENGE_DATA.getTasks().values())
		{
			if ((template.getType() == challengeType) && (template.getRace() == player.getRace()))
			{
				if (!taskMap.get(ownerId).containsKey(template.getId()))
				{
					if ((ownerLevel >= template.getMinLevel()) && (ownerLevel <= template.getMaxLevel()))
					{
						if (template.isTownResidence() && (playerTownId != ownerId))
						{
							continue;
						}
						if (template.getPrevTask() == null)
						{
							final ChallengeTask task = new ChallengeTask(ownerId, template);
							taskMap.get(ownerId).put(task.getTaskId(), task);
							DAOManager.getDAO(ChallengeTasksDAO.class).storeTask(task);
							availableTasks.add(task);
							continue;
						}
						else
						{
							final int prevTaskId = template.getPrevTask();
							if (taskMap.get(ownerId).containsKey(prevTaskId))
							{
								final ChallengeTask prevTask = taskMap.get(ownerId).get(prevTaskId);
								if (prevTask.isCompleted())
								{
									final ChallengeTask task = new ChallengeTask(ownerId, template);
									taskMap.get(ownerId).put(task.getTaskId(), task);
									DAOManager.getDAO(ChallengeTasksDAO.class).storeTask(task);
									availableTasks.add(task);
								}
							}
						}
					}
				}
			}
		}
		return availableTasks;
	}
	
	public void onChallengeQuestFinish(Player player, int questId)
	{
		final ChallengeTaskTemplate taskTemplate = DataManager.CHALLENGE_DATA.getTaskByQuestId(questId);
		switch (taskTemplate.getType())
		{
			case TOWN:
				onCityTaskFinish(player, taskTemplate, questId);
				break;
			case LEGION:
				onLegionTaskFinish(player, taskTemplate, questId);
				break;
		}
	}
	
	private void onCityTaskFinish(Player player, ChallengeTaskTemplate taskTemplate, int questId)
	{
		final int townId = TownService.getInstance().getTownIdByPosition(player);
		if (cityTasks.get(townId) == null)
		{
			buildTaskList(player, ChallengeType.TOWN, townId, TownService.getInstance().getTownById(townId).getLevel());
			if (cityTasks.get(townId) == null)
			{
				return;
			}
		}
		final ChallengeTask task = cityTasks.get(townId).get(taskTemplate.getId());
		if ((task == null) || (task.getQuests().get(questId) == null))
		{
			return;
		}
		final ChallengeQuest quest = task.getQuests().get(questId);
		if (quest.getCompleteCount() >= quest.getMaxRepeats())
		{
			return;
		}
		if (!task.isCompleted())
		{
			task.updateCompleteTime();
			quest.increaseCompleteCount();
			DAOManager.getDAO(ChallengeTasksDAO.class).storeTask(task);
			final Town town = TownService.getInstance().getTownById(townId);
			if (town != null)
			{
				final int oldLevel = town.getLevel();
				town.increasePoints(quest.getScorePerQuest());
				if (task.isCompleted())
				{
					switch (taskTemplate.getReward().getType())
					{
						case POINT:
							town.increasePoints(taskTemplate.getReward().getValue());
							break;
						case SPAWN:
							break;
						default:
							break;
					}
				}
				if (town.getLevel() != oldLevel)
				{
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401520, town.getNameId(), town.getLevel()));
				}
				DAOManager.getDAO(TownDAO.class).store(town);
			}
		}
	}
	
	private void onLegionTaskFinish(Player player, ChallengeTaskTemplate taskTemplate, int questId)
	{
		if (player.getLegion() == null)
		{
			return;
		}
		final int legionId = player.getLegion().getLegionId();
		if (!legionTasks.containsKey(legionId))
		{
			return;
		}
		if (legionTasks.get(legionId).get(taskTemplate.getId()) == null)
		{
			return;
		}
		final ChallengeTask task = legionTasks.get(player.getLegion().getLegionId()).get(taskTemplate.getId());
		final ChallengeQuest quest = task.getQuests().get(questId);
		if (quest.getCompleteCount() >= quest.getMaxRepeats())
		{
			return;
		}
		player.getLegionMember().increaseChallengeScore(quest.getScorePerQuest());
		if (!task.isCompleted())
		{
			task.updateCompleteTime();
			quest.increaseCompleteCount();
			DAOManager.getDAO(ChallengeTasksDAO.class).storeTask(task);
			if (task.isCompleted())
			{
				TreeMap<Integer, List<Integer>> winnersByPoints = new TreeMap<>();
				for (final Integer memberObjId : player.getLegion().getLegionMembers())
				{
					final Player member = World.getInstance().findPlayer(memberObjId);
					if (member != null)
					{
						final int score = member.getLegionMember().getChallengeScore();
						if (winnersByPoints.get(score) == null)
						{
							winnersByPoints.put(score, new ArrayList<Integer>());
						}
						winnersByPoints.get(score).add(member.getObjectId());
						member.getLegionMember().setChallengeScore(0);
						continue;
					}
					else
					{
						final LegionMember legionMember = DAOManager.getDAO(LegionMemberDAO.class).loadLegionMember(memberObjId);
						final int score = legionMember.getChallengeScore();
						if (score <= 0)
						{
							continue;
						}
						if (winnersByPoints.get(score) == null)
						{
							winnersByPoints.put(score, new ArrayList<Integer>());
						}
						winnersByPoints.get(score).add(legionMember.getObjectId());
						legionMember.setChallengeScore(0);
						DAOManager.getDAO(LegionMemberDAO.class).storeLegionMember(memberObjId, legionMember);
					}
				}
				int rewardsAdded = 0, itemId, itemCount;
				for (final Entry<Integer, List<Integer>> e : winnersByPoints.descendingMap().entrySet())
				{
					for (final int objectId : e.getValue())
					{
						for (final ContributionReward reward : taskTemplate.getContrib())
						{
							if (rewardsAdded <= reward.getNumber())
							{
								rewardsAdded++;
								itemId = reward.getRewardId();
								itemCount = reward.getItemCount();
								final String recipientName = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(objectId).getName();
								SystemMailService.getInstance().sendMail("Legion reward", recipientName, "", "", itemId, itemCount, 0, LetterType.NORMAL);
								break;
							}
						}
					}
					e.getValue().clear();
				}
				winnersByPoints.clear();
				winnersByPoints = null;
			}
		}
	}
	
	public boolean canRaiseLegionLevel(int legionId, int legionLevel)
	{
		Map<Integer, ChallengeTask> tasks;
		if (legionTasks.containsKey(legionId))
		{
			tasks = legionTasks.get(legionId);
		}
		else
		{
			tasks = DAOManager.getDAO(ChallengeTasksDAO.class).load(legionId, ChallengeType.LEGION);
		}
		for (final ChallengeTask task : tasks.values())
		{
			if ((task.getTemplate().getMinLevel() == legionLevel) && task.isCompleted())
			{
				return true;
			}
		}
		return false;
	}
}