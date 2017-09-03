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
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author KID
 */
public class UnBanMac extends AdminCommand
{
	
	public UnBanMac()
	{
		super("unbanmac");
	}
	
	@Override
	public void execute(Player player, String... params)
	{
		if ((params == null) || (params.length < 1))
		{
			onFail(player, null);
			return;
		}
		
		final String address = params[0];
		final boolean result = BannedMacManager.getInstance().unbanAddress(address, "uban;mac=" + address + ", " + player.getObjectId() + "; admin=" + player.getName());
		if (result)
		{
			PacketSendUtility.sendMessage(player, "mac " + address + " has unbanned");
		}
		else
		{
			PacketSendUtility.sendMessage(player, "mac " + address + " is not banned");
		}
	}
	
	@Override
	public void onFail(Player player, String message)
	{
		PacketSendUtility.sendMessage(player, "Syntax: //unbanmac <mac>");
	}
}