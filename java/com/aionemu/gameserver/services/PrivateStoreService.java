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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PrivateStore;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.model.trade.TradePSItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE_NAME;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 */
public class PrivateStoreService
{
	
	private static final Logger log = LoggerFactory.getLogger("EXCHANGE_LOG");
	
	/**
	 * @param activePlayer
	 * @param itemObjId
	 * @param itemId
	 * @param itemAmount
	 * @param itemPrice
	 */
	public static void addItems(Player activePlayer, TradePSItem[] tradePSItems)
	{
		if (CreatureState.ACTIVE.getId() != activePlayer.getState())
		{
			return;
		}
		
		/**
		 * Check if player already has a store, if not create one
		 */
		// TODO synchronization
		if (activePlayer.getStore() == null)
		{
			createStore(activePlayer);
		}
		
		final PrivateStore store = activePlayer.getStore();
		
		/**
		 * Check if player owns itemObjId else don't add item
		 */
		for (TradePSItem tradePSItem : tradePSItems)
		{
			final Item item = getItemByObjId(activePlayer, tradePSItem.getItemObjId());
			if ((item != null) && item.isTradeable(activePlayer))
			{
				if (validateItem(store, item, tradePSItem))
				{
					store.addItemToSell(tradePSItem.getItemObjId(), tradePSItem);
				}
			}
		}
	}
	
	private static boolean validateItem(PrivateStore store, Item item, TradePSItem psItem)
	{
		final int itemId = psItem.getItemId();
		final long itemCount = psItem.getCount();
		if (item.getItemTemplate().getTemplateId() != itemId)
		{
			return false;
		}
		if ((itemCount > item.getItemCount()) || (itemCount < 1))
		{
			return false;
		}
		final TradePSItem addedPsItem = store.getTradeItemByObjId(psItem.getItemObjId());
		if (addedPsItem != null)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * This method will create the player's store
	 * @param activePlayer
	 */
	private static void createStore(Player activePlayer)
	{
		if (activePlayer.isInState(CreatureState.RESTING))
		{
			return;
		}
		activePlayer.setStore(new PrivateStore(activePlayer));
		activePlayer.setState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, EmotionType.OPEN_PRIVATESHOP, 0, 0), true);
	}
	
	/**
	 * This method will destroy the player's store
	 * @param activePlayer
	 */
	public static void closePrivateStore(Player activePlayer)
	{
		activePlayer.setStore(null);
		activePlayer.unsetState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, EmotionType.CLOSE_PRIVATESHOP, 0, 0), true);
	}
	
	/**
	 * This method will move the item to the new player and move kinah to item owner
	 * @param seller
	 * @param buyer
	 * @param tradeList
	 */
	public static void sellStoreItem(Player seller, Player buyer, TradeList tradeList)
	{
		/**
		 * 1. Check if we are busy with two valid participants
		 */
		if (!validateParticipants(seller, buyer))
		{
			return;
		}
		
		/**
		 * Define store to make life easier
		 */
		final PrivateStore store = seller.getStore();
		
		/**
		 * 2. Load all item object id's and validate if seller really owns them
		 */
		tradeList = loadObjIds(seller, tradeList);
		if (tradeList == null)
		{
			return; // Invalid items found or store was empty
		}
		
		/**
		 * 3. Check free slots
		 */
		final Storage inventory = buyer.getInventory();
		final int freeSlots = (inventory.getLimit() - inventory.getItemsWithKinah().size()) + 1;
		if (freeSlots < tradeList.size())
		{
			return; // TODO message
		}
		
		/**
		 * Create total price and items
		 */
		final long price = getTotalPrice(store, tradeList);
		
		// Kinah exploit fix
		if (price < 0)
		{
			return;
		}
		
		/**
		 * Check if player has enough kinah
		 */
		if (buyer.getInventory().getKinah() >= price)
		{
			for (TradeItem tradeItem : tradeList.getTradeItems())
			{
				final Item item = getItemByObjId(seller, tradeItem.getItemId());
				if (item != null)
				{
					final TradePSItem storeItem = store.getTradeItemByObjId(tradeItem.getItemId());
					// Fix "Private store stackable items dupe" by Asanka
					if (item.getItemCount() < tradeItem.getCount())
					{
						PacketSendUtility.sendMessage(buyer, "You cannot buy more than player can sell.");
						return;
					}
					
					// Decrease/remove item from store and add them to buyer
					decreaseItemFromPlayer(seller, item, tradeItem);
					ItemService.addItem(buyer, item.getItemId(), tradeItem.getCount(), item);
					if (storeItem.getCount() == tradeItem.getCount())
					{
						store.removeItem(storeItem.getItemObjId());
					}
				}
			}
			// Decrease kinah for buyer and Increase kinah for seller
			decreaseKinahAmount(buyer, price);
			increaseKinahAmount(seller, price);
			
			/**
			 * Remove item from store and check if last item
			 */
			if (store.getSoldItems().size() == 0)
			{
				closePrivateStore(seller);
			}
			return;
		}
	}
	
	/**
	 * Decrease item count and update inventory
	 * @param seller
	 * @param item
	 */
	private static void decreaseItemFromPlayer(Player seller, Item item, TradeItem tradeItem)
	{
		seller.getInventory().decreaseItemCount(item, tradeItem.getCount());
		seller.getStore().getTradeItemByObjId(item.getObjectId()).decreaseCount(tradeItem.getCount());
	}
	
	/**
	 * @param seller
	 * @param tradeList
	 * @return
	 */
	private static TradeList loadObjIds(Player seller, TradeList tradeList)
	{
		final PrivateStore store = seller.getStore();
		final TradeList newTradeList = new TradeList();
		
		for (TradeItem tradeItem : tradeList.getTradeItems())
		{
			int i = 0;
			for (int itemObjId : store.getSoldItems().keySet())
			{
				if (i == tradeItem.getItemId())
				{
					newTradeList.addPSItem(itemObjId, tradeItem.getCount());
				}
				i++;
			}
		}
		
		/**
		 * Check if player still owns items
		 */
		if (!validateBuyItems(seller, newTradeList))
		{
			return null;
		}
		
		return newTradeList;
	}
	
	/**
	 * @param itemOwner
	 * @param newOwner
	 * @param player1
	 * @param player2
	 * @return
	 */
	private static boolean validateParticipants(Player itemOwner, Player newOwner)
	{
		return (itemOwner != null) && (newOwner != null) && itemOwner.isOnline() && newOwner.isOnline() && itemOwner.getRace().equals(newOwner.getRace());
	}
	
	/**
	 * @param seller
	 * @param tradeList
	 * @return
	 */
	private static boolean validateBuyItems(Player seller, TradeList tradeList)
	{
		for (TradeItem tradeItem : tradeList.getTradeItems())
		{
			final Item item = seller.getInventory().getItemByObjId(tradeItem.getItemId());
			
			// 1) don't allow to sell fake items;
			if (item == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This method will decrease the kinah amount of a player
	 * @param player
	 * @param price
	 */
	private static void decreaseKinahAmount(Player player, long price)
	{
		player.getInventory().decreaseKinah(price);
	}
	
	/**
	 * This method will increase the kinah amount of a player
	 * @param player
	 * @param price
	 */
	private static void increaseKinahAmount(Player player, long price)
	{
		player.getInventory().increaseKinah(price);
	}
	
	/**
	 * This method will return the item in a inventory by object id
	 * @param seller
	 * @param itemObjId
	 * @param player
	 * @param tradePSItems
	 * @return
	 */
	private static Item getItemByObjId(Player seller, int itemObjId)
	{
		return seller.getInventory().getItemByObjId(itemObjId);
	}
	
	/**
	 * This method will return the total price of the tradelist
	 * @param store
	 * @param tradeList
	 * @return
	 */
	private static long getTotalPrice(PrivateStore store, TradeList tradeList)
	{
		long totalprice = 0;
		for (TradeItem tradeItem : tradeList.getTradeItems())
		{
			final TradePSItem item = store.getTradeItemByObjId(tradeItem.getItemId());
			if (item == null)
			{
				continue;
			}
			totalprice += item.getPrice() * tradeItem.getCount();
		}
		return totalprice;
	}
	
	/**
	 * @param activePlayer
	 * @param name
	 */
	public static void openPrivateStore(Player activePlayer, String name)
	{
		final Player playerActive = activePlayer;
		if (name != null)
		{
			activePlayer.getStore().setStoreMessage(name);
			PacketSendUtility.broadcastPacket(playerActive, new SM_PRIVATE_STORE_NAME(playerActive.getObjectId(), name), true);
		}
		else
		{
			PacketSendUtility.broadcastPacket(playerActive, new SM_PRIVATE_STORE_NAME(playerActive.getObjectId(), ""), true);
		}
	}
}