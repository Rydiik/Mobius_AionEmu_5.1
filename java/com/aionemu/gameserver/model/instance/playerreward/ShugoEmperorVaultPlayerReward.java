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
package com.aionemu.gameserver.model.instance.playerreward;

/****/
/**
 * Author Rinzler (Encom) /
 ****/

public class ShugoEmperorVaultPlayerReward extends InstancePlayerReward
{
	private int scoreAP;
	private int rustedVaultKey;
	private boolean isRewarded = false;
	
	public ShugoEmperorVaultPlayerReward(Integer object)
	{
		super(object);
	}
	
	public boolean isRewarded()
	{
		return isRewarded;
	}
	
	public void setRewarded()
	{
		isRewarded = true;
	}
	
	public int getScoreAP()
	{
		return scoreAP;
	}
	
	public void setScoreAP(int ap)
	{
		scoreAP = ap;
	}
	
	public int getRustedVaultKey()
	{
		return rustedVaultKey;
	}
	
	public void setRustedVaultKey(int rustedVaultKey)
	{
		this.rustedVaultKey = rustedVaultKey;
	}
}