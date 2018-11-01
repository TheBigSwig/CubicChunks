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
package io.github.opencubicchunks.cubicchunks.api.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CubeGeneratorsRegistry {

    private static TreeSet<GeneratorWrapper> sortedGeneratorList = new TreeSet<>();

    /**
     * Register a world generator - something that inserts new block types into the world on population stage
     *
     * @param populator the generator
     * @param weight a weight to assign to this generator. Heavy weights tend to sink to the bottom of
     * list of world generators (i.e. they run later)
     */
    public static void register(ICubicPopulator populator, int weight) {
        Preconditions.checkNotNull(populator);
        sortedGeneratorList.add(new GeneratorWrapper(populator, weight));
    }

    /**
     * Callback hook for cube gen - if your mod wishes to add extra mod related
     * generation to the world call this
     *
     * @param random the cube specific {@link Random}.
     * @param pos is position of the populated cube
     * @param world The {@link ICubicWorld} we're generating for
     * @param biome The biome we are generating in
     */
    public static void generateWorld(World world, Random random, CubePos pos, Biome biome) {
        for (GeneratorWrapper wrapper : sortedGeneratorList) {
            wrapper.populator.generate(world, random, pos, biome);
        }
    }

    private static class GeneratorWrapper implements Comparable<GeneratorWrapper> {

        private final ICubicPopulator populator;
        private final int weight;

        public GeneratorWrapper(ICubicPopulator populator, int weight) {
            this.populator = populator;
            this.weight = weight;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GeneratorWrapper)) {
                return false;
            }

            GeneratorWrapper that = (GeneratorWrapper) o;

            if (weight != that.weight) {
                return false;
            }
            if (!populator.equals(that.populator)) {
                return false;
            }

            return true;
        }

        @Override public int hashCode() {
            int result = populator.hashCode();
            result = 31 * result + weight;
            return result;
        }

        @Override public int compareTo(GeneratorWrapper o) {
            return Integer.compare(weight, o.weight);
        }
    }
}
