package com.oitsjustjose.vtweaks.event.itemtweaks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockJukebox.TileEntityJukebox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HangingItemFix
{
	/*
	 * Idea borrowed from maruohon Execution is completely different and unique, however
	 */
	@SubscribeEvent
	public void registerItemFixes(AttackEntityEvent event)
	{
		if (event.target == null || event.entityPlayer == null)
			return;

		Entity entity = event.target;
		EntityPlayer player = event.entityPlayer;

		if (entity instanceof EntityPainting)
		{
			EntityItem paintingItemEntity = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, new ItemStack(Items.painting, 1));

			if (!player.inventory.addItemStackToInventory(new ItemStack(Items.painting, 1)))
				event.entityPlayer.worldObj.spawnEntityInWorld(paintingItemEntity);
			entity.setDead();
		}

		// Not using an instanceof check because Tinkers' frames extend EntityItemFrame, and I have no way to properly capture the right item to drop
		if (entity.getClass().getName().startsWith("net.minecraft.entity.item.EntityItemFrame"))
		{
			EntityItem itemFrameItemEntity = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, new ItemStack(Items.item_frame, 1));
			EntityItemFrame frame = (EntityItemFrame) entity;
			ItemStack framedStack = frame.getDisplayedItem();

			if (framedStack == null)
			{
				if (!player.inventory.addItemStackToInventory(new ItemStack(Items.item_frame, 1)))
					event.entityPlayer.worldObj.spawnEntityInWorld(itemFrameItemEntity);
				entity.setDead();
			}
			else
			{
				EntityItem framedItemEntity = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, framedStack);
				if (!player.inventory.addItemStackToInventory(framedStack))
				{
					event.entityPlayer.worldObj.spawnEntityInWorld(framedItemEntity);
					frame.setDisplayedItem(null);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void registerBlockFixes(PlayerInteractEvent event)
	{
		if (event.action != Action.RIGHT_CLICK_BLOCK || event.world.getBlockState(event.pos) == null)
			return;

		World world = event.world;
		Block block = world.getBlockState(event.pos).getBlock();

		if (block instanceof BlockJukebox)
		{
			TileEntityJukebox jukebox = (TileEntityJukebox) world.getTileEntity(event.pos);
			if (jukebox != null && !world.isRemote)
			{
				if (jukebox.getRecord() == null)
					return;

				ItemStack recordStack = jukebox.getRecord().copy();

				jukebox.setRecord(null);
				world.playAuxSFX(1005, event.pos, 0);
				world.playRecord(event.pos, (String) null);

				EntityItem record = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, recordStack);
				world.spawnEntityInWorld(record);
			}
		}
	}
}