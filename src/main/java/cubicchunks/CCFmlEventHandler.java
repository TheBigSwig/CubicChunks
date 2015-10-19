/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CCFmlEventHandler {

	private final CubicChunkSystem ccSystem;

	public CCFmlEventHandler(CubicChunkSystem ccSystem) {
		this.ccSystem = ccSystem;
	}

	@SubscribeEvent
	public void onWorldServerTick(TickEvent.WorldTickEvent evt) {
		World world = evt.world;
		//Forge (at least version 11.14.3.1521) doesn't call this event for client world.
		if (evt.phase == TickEvent.Phase.END && ccSystem.isTallWorld(world) && evt.side == Side.SERVER) {
			ccSystem.onWorldServerTick((WorldServer) world);
		}
	}

	@SubscribeEvent
	public void onWorldClientTickEvent(TickEvent.ClientTickEvent evt) {
		World world = Minecraft.getMinecraft().theWorld;
		//does the world exist? Is the game paused?
		//TODO: Maybe we should still process light updates when game is paused?
		if(world == null || Minecraft.getMinecraft().isGamePaused()) {
			return;
		}
		if (evt.phase == TickEvent.Phase.END && ccSystem.isTallWorld(world)) {
			ccSystem.onWorldClientTick((WorldClient) world);
		}
	}

	@SubscribeEvent
	public void onEntityPlayerMPTick(TickEvent.PlayerTickEvent evt) {
		if(evt.side.isServer() && evt.player instanceof EntityPlayerMP) {
			ccSystem.processChunkLoadQueue((EntityPlayerMP) evt.player);
		}
	}
}
