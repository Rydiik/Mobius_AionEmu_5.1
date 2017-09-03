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

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Kamui
 */
public class AddCube extends AdminCommand
{
	public AddCube()
	{
		super("addcube");
	}
	
	@Override
	public void execute(Player admin, String... params)
	{
		if (params.length != 1)
		{
			PacketSendUtility.sendMessage(admin, "Syntax: //addcube <player name>");
			return;
		}
		
		final Player receiver = World.getInstance().findPlayer(Util.convertName(params[0]));
		if (receiver == null)
		{
			PacketSendUtility.sendMessage(admin, "The player " + Util.convertName(params[0]) + " is not online.");
			return;
		}
		
		if (receiver.getNpcExpands() < 9)
		{
			CubeExpandService.expand(receiver, true);
			PacketSendUtility.sendMessage(admin, "9 cube slots successfully added to player " + receiver.getName() + "!");
			PacketSendUtility.sendMessage(receiver, "Admin " + admin.getName() + " gave you a cube expansion!");
		}
		else
		{
			PacketSendUtility.sendMessage(admin, "Cube expansion cannot be added to " + receiver.getName() + "!\nReason: player cube already fully expanded.");
			return;
		}
	}
	
	@Override
	public void onFail(Player admin, String message)
	{
		PacketSendUtility.sendMessage(admin, "Syntax: //addcube <player name>");
	}
}
