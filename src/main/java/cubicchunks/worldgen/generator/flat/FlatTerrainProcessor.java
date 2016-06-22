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
package cubicchunks.worldgen.generator.flat;

import cubicchunks.util.processor.CubeProcessor;
import cubicchunks.world.cube.Cube;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class FlatTerrainProcessor implements CubeProcessor {

	@Override public void calculate(Cube cube) {
		if (cube.getY() >= 0) {
			return;
		}
		if (cube.getY() == -1) {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					pos.setPos(x, 15, z);
					cube.setBlockForGeneration(pos, Blocks.GRASS.getDefaultState());
					for (int y = 14; y >= 10; y--) {
						pos.setPos(x, y, z);
						cube.setBlockForGeneration(pos, Blocks.DIRT.getDefaultState());
					}
					for (int y = 9; y >= 0; y--) {
						pos.setPos(x, y, z);
						cube.setBlockForGeneration(pos, Blocks.STONE.getDefaultState());
					}
				}
			}
			return;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 16; y++) {
					pos.setPos(x, y, z);
					cube.setBlockForGeneration(pos, Blocks.STONE.getDefaultState());
				}
			}
		}
	}
}
