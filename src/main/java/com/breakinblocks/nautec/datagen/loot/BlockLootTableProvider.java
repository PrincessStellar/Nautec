package com.breakinblocks.nautec.datagen.loot;

import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import com.breakinblocks.nautec.content.multiblocks.AugmentationStationMultiblock;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BlockLootTableProvider extends BlockLootSubProvider {

    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();

    public BlockLootTableProvider(HolderLookup.Provider pRegistries) {
        super(Collections.emptySet(), FeatureFlags.VANILLA_SET, pRegistries);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {
        super.add(block, table);
        knownBlocks.add(block);
    }

    private void dropKeepingPlant(Block block) {
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(block)
                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                .include(NTDataComponents.BIOME_TANK_PLANT.get()))))));
    }

    private void dropKeepingLink(Block block) {
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(block)
                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                .include(NTDataComponents.RESONANT_LINK.get()))))));
    }

    @Override
    protected void generate() {
        prismarineSand(NTBlocks.PRISMARINE_SAND.get());
        dropSelf(NTBlocks.AQUATIC_CATALYST.get());
        dropSelf(NTBlocks.UPLINK_ARRAY.get());
        dropSelf(NTBlocks.RESONANCE_NODE.get());
        dropSelf(NTBlocks.CONDUIT_BEACON.get());
        dropKeepingLink(NTBlocks.RESONANT_VAULT.get());
        dropKeepingLink(NTBlocks.RESONANT_CISTERN.get());
        dropSelf(NTBlocks.AQUARINE_DISH_STORAGE.get());
        dropSelf(NTBlocks.DEEP_STEEL_DISH_STORAGE.get());
        dropSelf(NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get());
        dropKeepingPlant(NTBlocks.BIOME_TANK.get());
        dropSelf(NTBlocks.PRISMATIC_EMITTER.get());
        dropSelf(NTBlocks.TIDAL_ROTOR.get());
        dropSelf(NTBlocks.THERMAL_VENT_TAP.get());
        dropSelf(NTBlocks.COMBUSTION_DYNAMO.get());
        dropSelf(NTBlocks.FUSION_CASING.get());
        dropSelf(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get());
        dropSelf(NTBlocks.CONTAINMENT_COIL.get());
        dropSelf(NTBlocks.FUSION_CONTROLLER.get());
        dropSelf(NTBlocks.LASER_INJECTOR.get());
        dropSelf(NTBlocks.FUSION_COLLECTOR.get());
        dropSelf(NTBlocks.FUSION_PORT.get());
        dropSelf(NTBlocks.PRISMARINE_RELAY.get());
        dropSelf(NTBlocks.LASER_JUNCTION.get());
        dropSelf(NTBlocks.SUBMARINE_DOCK.get());
        dropSelf(NTBlocks.PRESSURE_FORGE.get());
        dropSelf(NTBlocks.GATEWAY.get());
        dropSelf(NTBlocks.GATEWAY_RING.get());
        add(NTBlocks.GATEWAY_RING_PART.get(), noDrop());
        dropSelf(NTBlocks.RESONANCE_CHAMBER.get());
        dropSelf(NTBlocks.FOCUSING_LENS.get());
        dropSelf(NTBlocks.AQUARINE_STEEL_BLOCK.get());
        dropSelf(NTBlocks.DARK_PRISMARINE_PILLAR.get());
        dropSelf(NTBlocks.CHISELED_DARK_PRISMARINE.get());
        dropSelf(NTBlocks.POLISHED_PRISMARINE.get());
        dropSelf(NTBlocks.MIXER.get());
        dropSelf(NTBlocks.LASER_CRAFTING_MATRIX.get());
        dropSelf(NTBlocks.CHARGER.get());
        dropSelf(NTBlocks.CONFINED_SPAWNER.get());
        dropSelf(NTBlocks.CRYSTAL_CRADLE.get());
        dropSelf(NTBlocks.AUGMENTATION_STATION_EXTENSION.get());
        dropSelf(NTBlocks.AUGMENTATION_STATION.get());
        dropSelf(NTBlocks.DRAIN.get());
        dropSelf(NTBlocks.DRAIN_WALL.get());
        dropSelf(NTBlocks.BROWN_POLYMER_BLOCK.get());
        dropSelf(NTBlocks.CAST_IRON_BLOCK.get());
        dropSelf(NTBlocks.ANCHOR.get());
        dropSelf(NTBlocks.OIL_BARREL.get());
        dropSelf(NTBlocks.CREATIVE_POWER_SOURCE.get());
        dropSelf(NTBlocks.CREATIVE_ENERGY_SOURCE.get());
        dropSelf(NTBlocks.ENERGY_CONVERTER.get());
        add(NTBlocks.AUGMENTATION_STATION_PART.get(), multiblockPartDrop(NTBlocks.AUGMENTATION_STATION_PART.get(), AugmentationStationMultiblock.AS_PART, Map.of(
                0, NTBlocks.AQUARINE_STEEL_BLOCK.get(),
                1, NTBlocks.POLISHED_PRISMARINE.get(),
                2, NTBlocks.AQUARINE_STEEL_BLOCK.get(),
                3, NTBlocks.POLISHED_PRISMARINE.get(),
                4, NTBlocks.AQUARINE_STEEL_BLOCK.get(),
                5, NTBlocks.POLISHED_PRISMARINE.get(),
                6, NTBlocks.AQUARINE_STEEL_BLOCK.get(),
                7, NTBlocks.POLISHED_PRISMARINE.get(),
                8, NTBlocks.AQUARINE_STEEL_BLOCK.get()
        )));
        drainPartDrop(NTBlocks.DRAIN_PART.get());
        dropSelf(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get());
        dropSelf(NTBlocks.INCUBATOR.get());
        dropSelf(NTBlocks.MUTATOR.get());
        dropSelf(NTBlocks.GRAFTING_STATION.get());
        dropSelf(NTBlocks.DISTRIBUTOR.get());
        dropSelf(NTBlocks.AQUARINE_COPPER_BLOCK.get());
        dropSelf(NTBlocks.CURRENT_CONDUIT.get());
        dropOther(NTBlocks.CONDUIT_TAP.get(), NTBlocks.CURRENT_CONDUIT.get());
        dropSelf(NTBlocks.BUBBLE_ANCHOR.get());
        dropSelf(NTBlocks.COLONY_REPLICATOR.get());
        dropSelf(NTBlocks.HYDROTHERMAL_VENT.get());
        dropSelf(NTBlocks.OXYGEN_DIFFUSER.get());
        add(NTBlocks.PRESSURE_HATCH.get(), createDoorTable(NTBlocks.PRESSURE_HATCH.get()));
        dropSelf(NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
        dropSelf(NTBlocks.BACTERIAL_FUEL_CELL.get());
        dropSelf(NTBlocks.BIO_REACTOR.get());
        add(NTBlocks.BIO_REACTOR_PART.get(), createStateDrop(NTBlocks.BIO_REACTOR_PART.get(), BioReactorMultiblock.BIO_REACTOR_PART, Map.of(
                0, NTBlocks.DARK_PRISMARINE_PILLAR.get(),
                1, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get(),
                2, NTBlocks.DARK_PRISMARINE_PILLAR.get(),
                3, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get(),
                4, NTBlocks.POLISHED_PRISMARINE.get(),
                5, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get(),
                6, NTBlocks.DARK_PRISMARINE_PILLAR.get(),
                7, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get(),
                8, NTBlocks.DARK_PRISMARINE_PILLAR.get()
        )));
        dropSelf(NTBlocks.INDUSTRIAL_BIO_REACTOR.get());
        add(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get(), industrialBioReactorPartDrop(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get()));
        dropSelf(NTBlocks.BACTERIAL_ANALYZER.get());
        dropSelf(NTBlocks.FISHING_STATION.get());
        dropSelf(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get());
        add(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL_PART.get(), noDrop());

        add(NTBlocks.BUDDING_PRISMARINE.get(), block -> createSilkTouchDispatchTable(block,
                this.applyExplosionDecay(block, LootItem.lootTableItem(NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 6.0F))))));
        add(NTBlocks.PRISMARINE_CLUSTER.get(), block -> createSilkTouchDispatchTable(block,
                this.applyExplosionDecay(block, LootItem.lootTableItem(NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
                        .apply(ApplyBonusCount.addOreBonusCount(this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)))
                        .when(this.hasSilkTouch().invert()))));
        add(NTBlocks.SMALL_PRISMARINE_BUD.get(), this::createSilkTouchOnlyTable);
        add(NTBlocks.MEDIUM_PRISMARINE_BUD.get(), this::createSilkTouchOnlyTable);
        add(NTBlocks.LARGE_PRISMARINE_BUD.get(), this::createSilkTouchOnlyTable);

        add(NTBlocks.DEEP_KELP.get(), this::createShearsOnlyDrop);
        add(NTBlocks.DEEP_KELP_PLANT.get(), noDrop());
        add(NTBlocks.LUMINESCENT_ALGAE.get(), this::createShearsOnlyDrop);
        add(NTBlocks.PRISMARINE_FROND.get(), this::createShearsOnlyDrop);
        add(NTBlocks.VENT_TUBEWORM.get(), this::createShearsOnlyDrop);
        add(NTBlocks.ABYSSAL_CORAL.get(), this::createShearsOnlyDrop);
        add(NTBlocks.GLOW_POLYP.get(), this::createShearsOnlyDrop);
    }

    protected LootTable.Builder industrialBioReactorPartDrop(Block block) {
        Map<Integer, Block> definition = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get().getDefinition();
        LootPool.Builder builder = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        for (int layer = 0; layer < IndustrialBioReactorMultiblock.HEIGHT; layer++) {
            for (int cell = 0; cell < IndustrialBioReactorMultiblock.SIZE * IndustrialBioReactorMultiblock.SIZE; cell++) {
                int key = IndustrialBioReactorMultiblock.keyAt(layer, cell);
                if (key == IndustrialBioReactorMultiblock.CHAMBER || key == IndustrialBioReactorMultiblock.CONTROLLER) {
                    continue;
                }
                builder.add(LootItem.lootTableItem(definition.get(key))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(IndustrialBioReactorMultiblock.LAYER, layer)
                                        .hasProperty(IndustrialBioReactorMultiblock.CELL, cell))));
            }
        }
        return LootTable.lootTable().withPool(this.applyExplosionCondition(block, builder));
    }

    protected LootTable.Builder createStateDrop(
            Block block, IntegerProperty property, Map<Integer, Block> drops
    ) {
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F));

        for (Integer key : drops.keySet().stream().sorted().toList()) {
            builder.add(LootItem.lootTableItem(drops.get(key))
                    .when(
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, key))
                    ));
        }

        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                block,
                                builder
                        )
                );
    }


    protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createStateDrop(
            Block block, Property<T> property, Map<T, Block> drops
    ) {
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F));

        for (T key : drops.keySet().stream().sorted().toList()) {
            builder.add(LootItem.lootTableItem(drops.get(key))
                    .when(
                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, key))
                    ));
        }

        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                block,
                                builder
                        )
                );
    }


    protected void prismarineSand(Block prismarineSandOre) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        add(
                prismarineSandOre,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .add(LootItem.lootTableItem(prismarineSandOre))
                                        .apply(SetItemCountFunction.setCount(new ConstantValue(1)))
                                        .when(this.hasSilkTouch())
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(Items.PRISMARINE_SHARD)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                                        )
                                        .when(this.doesNotHaveSilkTouch())
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                                        )
                                        .when(this.doesNotHaveSilkTouch())
                        )
        );
    }

    protected LootTable.Builder multiblockPartDrop(
            Block block, IntegerProperty partIndex, Map<Integer, Block> index2blocks
    ) {
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F));
        Integer last = partIndex.getPossibleValues().stream().toList().getLast();
        for (int i = 0; i < last; i++) {
            builder.add(LootItem.lootTableItem(index2blocks.get(i))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(partIndex, i))));
        }
        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                block,
                                builder
                        )
                );
    }

    protected LootTable.Builder drainPartDrop(
            Block block
    ) {
        LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F));
        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block))
                        )
                );
    }

}
