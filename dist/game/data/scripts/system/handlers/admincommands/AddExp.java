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
package system.handlers.admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Wakizashi
 */
public class AddExp extends AdminCommand
{
	public AddExp()
	{
		super("addexp");
	}
	
	@Override
	public void execute(Player player, String... params)
	{
		if (params.length != 1)
		{
			onFail(player, null);
			return;
		}
		
		Player target = null;
		final VisibleObject creature = player.getTarget();
		
		if (creature == null)
		{
			target = player;
		}
		else if (creature instanceof Player)
		{
			target = (Player) creature;
		}
		else
		{
			return;
		}
		
		final String paramValue = params[0];
		long exp;
		try
		{
			exp = Long.parseLong(paramValue);
		}
		catch (final NumberFormatException e)
		{
			PacketSendUtility.sendMessage(player, "<exp> must be an Integer");
			return;
		}
		
		exp += target.getCommonData().getExp();
		target.getCommonData().setExp(exp, false);
		PacketSendUtility.sendMessage(player, "You added " + params[0] + " exp points to the target.");
	}
	
	@Override
	public void onFail(Player player, String message)
	{
		PacketSendUtility.sendMessage(player, "syntax //addexp <exp>");
	}
}
