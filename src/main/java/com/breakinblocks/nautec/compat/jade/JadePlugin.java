package com.breakinblocks.nautec.compat.jade;

import net.minecraft.world.level.block.Block;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.content.blocks.EnergyConverterBlock;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.DrainPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.ConfinedSpawnerBlock;
import com.breakinblocks.nautec.content.blocks.CrystalCradleBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.DrainBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalPartBlock;
import com.breakinblocks.nautec.content.blocks.MixerBlock;
import com.breakinblocks.nautec.content.blocks.LaserCraftingMatrixBlock;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.ThermalVentTapBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.TidalRotorBlockEntity;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.content.blocks.generators.TidalRotorBlock;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AquaticCatalystComponentProvider.INSTANCE, AquaticCatalystBlockEntity.class);
        registration.registerBlockDataProvider(ConfinedSpawnerComponentProvider.INSTANCE, ConfinedSpawnerBlockEntity.class);
        registration.registerBlockDataProvider(DrainComponentProvider.INSTANCE, DrainBlockEntity.class);
        registration.registerBlockDataProvider(CrystalCradleComponentProvider.INSTANCE, CrystalCradleBlockEntity.class);
        registration.registerBlockDataProvider(DrainComponentProvider.INSTANCE, DrainPartBlockEntity.class);
        registration.registerBlockDataProvider(FusionControllerComponentProvider.INSTANCE, FusionControllerBlockEntity.class);
        registration.registerBlockDataProvider(GeneratorComponentProvider.INSTANCE, TidalRotorBlockEntity.class);
        registration.registerBlockDataProvider(GeneratorComponentProvider.INSTANCE, ThermalVentTapBlockEntity.class);
        registration.registerBlockDataProvider(GeneratorComponentProvider.INSTANCE, CombustionDynamoBlockEntity.class);
        registration.registerBlockDataProvider(EnergyConverterComponentProvider.INSTANCE, EnergyConverterBlockEntity.class);
        registration.registerBlockDataProvider(LaserCraftingMatrixComponentProvider.INSTANCE, LaserCraftingMatrixBlockEntity.class);
        registration.registerBlockDataProvider(LaserPowerComponentProvider.INSTANCE, LaserBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BeamSpeedComponentProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(LaserPowerComponentProvider.Client.INSTANCE, Block.class);
        registration.registerBlockComponent(AquaticCatalystComponentProvider.Client.INSTANCE, AquaticCatalystBlock.class);
        registration.registerBlockComponent(LaserJunctionComponentProvider.INSTANCE, LaserJunctionBlock.class);
        registration.registerBlockComponent(MixerComponentProvider.INSTANCE, MixerBlock.class);
        registration.registerBlockComponent(ConfinedSpawnerComponentProvider.Client.INSTANCE, ConfinedSpawnerBlock.class);
        registration.registerBlockComponent(DrainComponentProvider.Client.INSTANCE, DrainBlock.class);
        registration.registerBlockComponent(CrystalCradleComponentProvider.Client.INSTANCE, CrystalCradleBlock.class);
        registration.registerBlockComponent(PrismarineCrystalComponentProvider.INSTANCE, PrismarineCrystalBlock.class);
        registration.registerBlockComponent(PrismarineCrystalComponentProvider.INSTANCE, PrismarineCrystalPartBlock.class);
        registration.registerBlockComponent(DrainComponentProvider.Client.INSTANCE, DrainPartBlock.class);
        registration.registerBlockComponent(FusionControllerComponentProvider.Client.INSTANCE, FusionControllerBlock.class);
        registration.registerBlockComponent(GeneratorComponentProvider.Client.INSTANCE, TidalRotorBlock.class);
        registration.registerBlockComponent(GeneratorComponentProvider.Client.INSTANCE, ThermalVentTapBlock.class);
        registration.registerBlockComponent(GeneratorComponentProvider.Client.INSTANCE, CombustionDynamoBlock.class);
        registration.registerBlockComponent(EnergyConverterComponentProvider.Client.INSTANCE, EnergyConverterBlock.class);
        registration.registerBlockComponent(LaserCraftingMatrixComponentProvider.Client.INSTANCE, LaserCraftingMatrixBlock.class);
    }
}