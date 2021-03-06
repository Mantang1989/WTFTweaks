package wtftweaks.util;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import wtfcore.tweaksmethods.FracMethods;
import wtfcore.utilities.BlockSets;
import wtftweaks.WTFtweaks;
import wtftweaks.configs.WTFTweaksConfig;
import wtftweaks.entities.WTFcreeper;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

public class WTFEventMonitor {

	Random random = new Random();

	@SubscribeEvent
	public void SpawnReplacer (LivingSpawnEvent event)
	{
		if (WTFTweaksConfig.replaceCreepers && !event.world.isRemote && event.entityLiving instanceof EntityCreeper)
		{
			EntityCreeper vCreeper = (EntityCreeper) event.entityLiving;
			if (vCreeper.getCanSpawnHere())
			{
				WTFcreeper newCreeper = new WTFcreeper(event.world);
				newCreeper.setLocationAndAngles(vCreeper.posX, vCreeper.posY, vCreeper.posZ, vCreeper.rotationYaw, vCreeper.rotationPitch);
				event.world.spawnEntityInWorld(newCreeper);
				vCreeper.setDead();
			}
		}
	}


	//slows mining of stone
	@SubscribeEvent
	public void StoneBreakSpeed (BreakSpeed event)
	{
		if (!event.entityPlayer.capabilities.isCreativeMode){
			if (BlockSets.hasCobblestone(event.block))
			{
				event.newSpeed = WTFTweaksConfig.stoneBreakSpeed * event.originalSpeed;
			}
		}
	}

	//Deals with whenever a player places a block
	@SubscribeEvent
	public void PlayerPlaceBlock (PlaceEvent event)
	{

		if(WTFTweaksConfig.enableNameGetter){
			event.player.addChatMessage(new ChatComponentText("The block name is : " + GameData.getBlockRegistry().getNameForObject(event.block)));
			event.player.addChatMessage(new ChatComponentText("The block metadata is : " + event.world.getBlockMetadata(event.x, event.y, event.z)));
		}


		Block block = event.block;
		if (BlockSets.shouldFall(event.block)){

			WTFmethods.dropBlock(event.world, event.x, event.y, event.z);
		}
		if (!event.player.capabilities.isCreativeMode){
			if (WTFTweaksConfig.replaceExplosives && event.block == Blocks.tnt)
			{
				event.world.setBlock(event.x, event.y, event.z, WTFtweaks.blockWTFtnt);
			}
			if (WTFTweaksConfig.oreFractures && BlockSets.isOre(block)){
				event.setCanceled (true);
			}
			if (WTFTweaksConfig.enableFiniteTorch > 0 && event.block == Blocks.torch){
				event.world.setBlock(event.x, event.y, event.z, WTFtweaks.finitetorch_unlit, event.world.getBlockMetadata(event.x, event.y, event.z), 3);;
			}
		}
	}

	@SubscribeEvent
	public void BlockBreakEvent(BreakEvent event)
	{

		int x = event.x;
		int y = event.y;
		int z = event.z;
		World world = event.world;
		Block block = event.block;

		if (!event.getPlayer().capabilities.isCreativeMode && BlockSets.hasCobblestone(block) && WTFTweaksConfig.stoneFracturesBeforeBreaking)
		{
			if (FracMethods.fracStone(x, y, z, world)){
				event.setResult(Event.Result.DENY);
			}
		}
		//This checks if the block is an ore block, then calls the fracture method associated with it in the ore blocks hashmap if it is

		if (BlockSets.isOre(block) && WTFTweaksConfig.oreFractures)
		{
			BlockSets.getFrac(block).fracture(world, x, y, z);
		}
		//checks for fall of the block above the block thats been broken
		WTFmethods.disturbBlock(event.world, x, y+1, z);

	}




}
