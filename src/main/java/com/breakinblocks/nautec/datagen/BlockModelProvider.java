package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.content.conduits.ConduitArm;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlock;
import com.breakinblocks.nautec.content.conduits.CurrentConduitBlock;
import com.breakinblocks.nautec.content.conduits.TapArm;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlock;
import com.breakinblocks.nautec.content.blocks.OxygenDiffuserBlock;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.CrateBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.OilBarrelBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.IntegerRange;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockModelProvider extends ModelProvider {
    private static final TextureSlot SLOT_2 = TextureSlot.create("2");
    private static final TextureSlot SLOT_4 = TextureSlot.create("4");
    private static final TextureSlot SLOT_5 = TextureSlot.create("5");
    private static final TextureSlot CROSS_EMISSIVE = TextureSlot.create("cross_emissive");
    private static final Map<Direction, TextureSlot> FACE_SLOTS = new EnumMap<>(Direction.class);
    private static final Map<Direction, TextureSlot> GLOW_SLOTS = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            FACE_SLOTS.put(direction, TextureSlot.create(direction.getName()));
            GLOW_SLOTS.put(direction, TextureSlot.create(direction.getName() + "_emissive"));
        }
    }

    private final Map<Identifier, Identifier> createdModels = new HashMap<>();
    private BlockModelGenerators blockModels;

    public BlockModelProvider(PackOutput output) {
        super(output, Nautec.MODID);
    }

    @Override
    public String getName() {
        return "NauTec Block Model Definitions";
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        MultiblockModelHelper helper = new MultiblockModelHelper(this);

        axisBlock(NTBlocks.DARK_PRISMARINE_PILLAR.get());
        simpleBlock(NTBlocks.CHISELED_DARK_PRISMARINE.get());
        simpleBlock(NTBlocks.PRISMARINE_SAND.get());
        simpleBlock(NTBlocks.POLISHED_PRISMARINE.get());
        simpleBlock(NTBlocks.AQUARINE_STEEL_BLOCK.get());
        simpleBlock(NTBlocks.CAST_IRON_BLOCK.get());

        simpleBlock(NTBlocks.BUDDING_PRISMARINE.get());
        prismarineBud(NTBlocks.SMALL_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.MEDIUM_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.LARGE_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.PRISMARINE_CLUSTER.get());

        simpleBlock(NTBlocks.DEEP_KELP.get(), emissiveCross(NTBlocks.DEEP_KELP.get()));
        simpleBlock(NTBlocks.DEEP_KELP_PLANT.get(), emissiveCross(NTBlocks.DEEP_KELP_PLANT.get()));
        blockModels.registerSimpleFlatItemModel(NTBlocks.DEEP_KELP.get());
        waterPlant(NTBlocks.LUMINESCENT_ALGAE.get());
        waterPlant(NTBlocks.PRISMARINE_FROND.get());
        waterPlant(NTBlocks.VENT_TUBEWORM.get());
        waterPlant(NTBlocks.ABYSSAL_CORAL.get());
        blockModels.createMultifaceBlockStates(NTBlocks.GLOW_POLYP.get());
        blockModels.registerSimpleFlatItemModel(NTBlocks.GLOW_POLYP.get());

        simpleBlock(NTBlocks.CREATIVE_POWER_SOURCE.get());
        simpleBlock(NTBlocks.CREATIVE_ENERGY_SOURCE.get(), cubeAll(name(NTBlocks.CREATIVE_ENERGY_SOURCE.get()),
                blockTexture(NTBlocks.CREATIVE_POWER_SOURCE.get())));
        simpleBlock(NTBlocks.ENERGY_CONVERTER.get(), artModel(NTBlocks.ENERGY_CONVERTER.get()));
        fusionPlant();
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(NTBlocks.RESONANCE_NODE.get(),
                        BlockModelGenerators.plainVariant(existingModelFile(NTBlocks.RESONANCE_NODE.get())))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        simpleBlock(NTBlocks.PRISMATIC_EMITTER.get(), existingModelFile(NTBlocks.PRISMATIC_EMITTER.get()));
        simpleBlock(NTBlocks.UPLINK_ARRAY.get(), existingModelFile(NTBlocks.UPLINK_ARRAY.get()));
        simpleBlock(NTBlocks.SATELLITE_ARRAY_TOP.get(), existingModelFile(NTBlocks.SATELLITE_ARRAY_TOP.get()));
        generators();
        aquaticCatalyst(NTBlocks.AQUATIC_CATALYST.get());

        existingFacingBlock(NTBlocks.PRISMARINE_RELAY.get(), NTBlocks.PRISMARINE_RELAY.get());

        simpleBlock(NTBlocks.SUBMARINE_DOCK.get(), artModel(NTBlocks.SUBMARINE_DOCK.get()));

        simpleBlock(NTBlocks.PRESSURE_FORGE.get(), artModel(NTBlocks.PRESSURE_FORGE.get()));

        simpleBlock(NTBlocks.GATEWAY.get(), artModel(NTBlocks.GATEWAY.get()));
        simpleBlock(NTBlocks.GATEWAY_RING.get());
        simpleBlock(NTBlocks.GATEWAY_RING_PART.get(), ModelTemplates.PARTICLE_ONLY.create(NTBlocks.GATEWAY_RING_PART.get(),
                TextureMapping.particle(blockTexture(NTBlocks.GATEWAY_RING.get())), blockModels.modelOutput));

        simpleBlock(NTBlocks.RESONANCE_CHAMBER.get(), artModel(NTBlocks.RESONANCE_CHAMBER.get()));

        facingBlock(NTBlocks.FOCUSING_LENS.get(), artModel(NTBlocks.FOCUSING_LENS.get()));
        laserJunction(NTBlocks.LASER_JUNCTION.get());
        currentConduit(NTBlocks.CURRENT_CONDUIT.get());
        conduitTap(NTBlocks.CONDUIT_TAP.get());
        simpleBlock(NTBlocks.AQUARINE_COPPER_BLOCK.get());

        simpleBlock(NTBlocks.MIXER.get(), existingModelFile(NTBlocks.MIXER.get()));
        simpleBlock(NTBlocks.LASER_CRAFTING_MATRIX.get(), existingModelFile(NTBlocks.LASER_CRAFTING_MATRIX.get()));
        simpleBlock(NTBlocks.RESONANT_VAULT.get(), existingModelFile(NTBlocks.RESONANT_VAULT.get()));
        simpleBlock(NTBlocks.RESONANT_CISTERN.get(), existingModelFile(NTBlocks.RESONANT_CISTERN.get()));
        simpleBlock(NTBlocks.CHARGER.get(), existingModelFile(NTBlocks.CHARGER.get()));
        simpleBlock(NTBlocks.CONFINED_SPAWNER.get(), existingModelFile(NTBlocks.CONFINED_SPAWNER.get()));
        simpleBlock(NTBlocks.CRYSTAL_CRADLE.get(), existingModelFile(NTBlocks.CRYSTAL_CRADLE.get()));
        simpleBlock(NTBlocks.FISHING_STATION.get(), existingModelFile(NTBlocks.FISHING_STATION.get()));
        crateBlock(NTBlocks.CRATE.get());
        rustyCrateBlock(NTBlocks.RUSTY_CRATE.get());

        simpleBlock(NTBlocks.MUTATOR.get(), existingModelFile(NTBlocks.MUTATOR.get()));
        simpleBlock(NTBlocks.INCUBATOR.get(), existingModelFile(NTBlocks.INCUBATOR.get()));

        facingBlock(NTBlocks.BACTERIAL_FUEL_CELL.get(), artModel(NTBlocks.BACTERIAL_FUEL_CELL.get()));

        helper.drainController(NTBlocks.DRAIN.get());
        helper.drainPart(NTBlocks.DRAIN_PART.get(), IntegerRange.of(0, 8));

        helper.augmentationStationController(NTBlocks.AUGMENTATION_STATION.get());
        helper.augmentationStationPart(NTBlocks.AUGMENTATION_STATION_PART.get(), IntegerRange.of(0, 8));
        helper.augmentationStationExtension(NTBlocks.AUGMENTATION_STATION_EXTENSION.get());

        helper.bioReactorPart(NTBlocks.BIO_REACTOR_PART.get());
        helper.bioReactorController(NTBlocks.BIO_REACTOR.get());
        helper.industrialBioReactorPart(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get());
        helper.industrialBioReactorController(NTBlocks.INDUSTRIAL_BIO_REACTOR.get());

        simpleBlock(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get());

        simpleBlock(NTBlocks.DRAIN_WALL.get());
        simpleBlock(NTBlocks.BROWN_POLYMER_BLOCK.get());

        oilBarrel(NTBlocks.OIL_BARREL.get(), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get())
        ), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()) + "_open",
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_open")
        ));

        horizontalBlock(NTBlocks.BACTERIAL_ANALYZER.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER.get()));
        horizontalBlock(NTBlocks.BACTERIAL_ANALYZER_TOP.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER_TOP.get()));
    }

    private void generators() {
        simpleBlock(NTBlocks.TIDAL_ROTOR.get(), cubeBottomTop("tidal_rotor", blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_side"),
                blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_bottom"), blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_top")));
        Block tap = NTBlocks.THERMAL_VENT_TAP.get();
        Identifier idle = existingModelFile(tap);
        Identifier lit = existingModelFile("thermal_vent_tap_lit");
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(tap)
                .with(BlockModelGenerators.createBooleanModelDispatch(ThermalVentTapBlock.LIT,
                        BlockModelGenerators.plainVariant(lit), BlockModelGenerators.plainVariant(idle))));

        Block dynamo = NTBlocks.COMBUSTION_DYNAMO.get();
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(dynamo)
                .with(BlockModelGenerators.createBooleanModelDispatch(CombustionDynamoBlock.LIT,
                        BlockModelGenerators.plainVariant(existingModelFile("combustion_dynamo_lit")),
                        BlockModelGenerators.plainVariant(existingModelFile(dynamo))))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void fusionPlant() {
        Material casing = blockTexture(NTBlocks.FUSION_CASING.get());
        simpleBlock(NTBlocks.FUSION_CASING.get(), cubeAll("fusion_casing", casing));
        simpleBlock(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get(), cubeAll("aquamarine_structural_glass", blockTexture(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get())));
        Block coil = NTBlocks.CONTAINMENT_COIL.get();
        simpleBlock(coil, emissiveCube("containment_coil", faces(blockTexture(coil, "_end"), blockTexture(coil, "_end"),
                blockTexture(coil, "_side"), blockTexture(coil, "_side")), blockTexture(coil, "_side"),
                glow(blockTexture(coil, "_end_emissive"), blockTexture(coil, "_end_emissive"), blockTexture(coil, "_side_emissive"))));
        Block port = NTBlocks.FUSION_PORT.get();
        simpleBlock(port, emissiveCube("fusion_port", faces(blockTexture(port), blockTexture(port), blockTexture(port), blockTexture(port)),
                blockTexture(port), glow(blockTexture(port, "_emissive"), blockTexture(port, "_emissive"), blockTexture(port, "_emissive"))));
        Block collector = NTBlocks.FUSION_COLLECTOR.get();
        simpleBlock(collector, emissiveCube("fusion_collector", faces(blockTexture(collector, "_lens"), blockTexture(collector, "_lens"),
                blockTexture(collector, "_side"), blockTexture(collector, "_side")), blockTexture(collector, "_side"),
                glow(blockTexture(collector, "_lens_emissive"), blockTexture(collector, "_lens_emissive"), null)));
        Block injector = NTBlocks.LASER_INJECTOR.get();
        facingBlock(injector, emissiveCube("laser_injector", faces(blockTexture(injector, "_back"), blockTexture(injector, "_front"),
                blockTexture(injector, "_side"), blockTexture(injector, "_side")), blockTexture(injector, "_side"),
                glow(null, blockTexture(injector, "_front_emissive"), null)));

        Block controller = NTBlocks.FUSION_CONTROLLER.get();
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/fusion_controller"), key -> ModelTemplates.CUBE_ORIENTABLE.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(controller, "_front"))
                .put(TextureSlot.SIDE, casing)
                .put(TextureSlot.TOP, casing), blockModels.modelOutput));
        Identifier active = emissiveCube("fusion_controller_active", faces(casing, casing, blockTexture(controller, "_front_active"), casing),
                blockTexture(controller, "_front_active"), front(blockTexture(controller, "_front_active_emissive")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(controller)
                .with(BlockModelGenerators.createBooleanModelDispatch(FusionControllerBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        graftingStation(NTBlocks.GRAFTING_STATION.get());
        advancedAnalyzer(NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
        bubbleAnchor(NTBlocks.BUBBLE_ANCHOR.get());
        replicator(NTBlocks.COLONY_REPLICATOR.get());
        blockModels.createDoor(NTBlocks.PRESSURE_HATCH.get());
        oxygenDiffuser(NTBlocks.OXYGEN_DIFFUSER.get());
        conduitBeacon(NTBlocks.CONDUIT_BEACON.get());
        dishStorage(NTBlocks.AQUARINE_DISH_STORAGE.get());
        dishStorage(NTBlocks.DEEP_STEEL_DISH_STORAGE.get());
        dishStorage(NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get());
        simpleBlock(NTBlocks.BIOME_TANK.get(), existingModelFile(NTBlocks.BIOME_TANK.get()));
        simpleBlock(NTBlocks.HYDROTHERMAL_VENT.get(), existingModelFile(NTBlocks.HYDROTHERMAL_VENT.get()));
        simpleBlock(NTBlocks.DISTRIBUTOR.get(), cubeBottomTop("nautechnical_distributor", blockTexture(NTBlocks.DISTRIBUTOR.get(), "_side"),
                blockTexture(NTBlocks.DISTRIBUTOR.get(), "_bottom"), blockTexture(NTBlocks.DISTRIBUTOR.get(), "_top")));
    }

    private void replicator(Block block) {
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/colony_replicator"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        Identifier active = emissiveCube("colony_replicator_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"),
                front(blockTexture(block, "_front_active_emissive")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(ColonyReplicatorBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void conduitBeacon(Block block) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(ConduitBeaconBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(existingModelFile("conduit_beacon_active")),
                        BlockModelGenerators.plainVariant(existingModelFile(block)))));
    }

    private void dishStorage(Block block) {
        Identifier model = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput);
        horizontalBlock(block, model);
    }

    private void oxygenDiffuser(Block block) {
        Identifier idle = cubeBottomTop("oxygen_diffuser", blockTexture(block, "_side"), blockTexture(block, "_bottom"), blockTexture(block, "_top"));
        Identifier active = emissiveCube("oxygen_diffuser_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_side_active"), blockTexture(block, "_side_active")), blockTexture(block, "_side_active"),
                glow(null, blockTexture(block, "_top_active_emissive"), blockTexture(block, "_side_active_emissive")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(OxygenDiffuserBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle))));
    }

    private void bubbleAnchor(Block block) {
        Identifier idle = cubeBottomTop("bubble_anchor", blockTexture(block, "_side"), blockTexture(block, "_bottom"), blockTexture(block, "_top"));
        Identifier active = emissiveCube("bubble_anchor_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_side_active"), blockTexture(block, "_side_active")), blockTexture(block, "_side_active"),
                glow(null, blockTexture(block, "_top_active_emissive"), blockTexture(block, "_side_active_emissive")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(BubbleAnchorBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle))));
    }

    private void advancedAnalyzer(Block block) {
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/advanced_bacterial_analyzer"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        Identifier active = emissiveCube("advanced_bacterial_analyzer_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"),
                front(blockTexture(block, "_front_active_emissive")));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(AdvancedBacterialAnalyzerBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void graftingStation(Block block) {
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/grafting_station"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        Map<Direction, Material> graftingGlow = front(blockTexture(block, "_front_active_emissive"));
        graftingGlow.put(Direction.UP, blockTexture(block, "_top_active_emissive"));
        Identifier active = emissiveCube("grafting_station_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"), graftingGlow);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(GraftingStationBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    public BlockModelGenerators blockModels() {
        return blockModels;
    }

    private void axisBlock(Block block) {
        Identifier model = createdModels.computeIfAbsent(ModelLocationUtils.getModelLocation(block), id ->
                ModelTemplates.CUBE_COLUMN.create(id, new TextureMapping()
                        .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                        .put(TextureSlot.END, blockTexture(block, "_end")), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private void simpleBlock(Block block) {
        blockModels.createTrivialCube(block);
    }

    private void prismarineBud(Block block) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(emissiveCross(block)))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
        blockModels.registerSimpleFlatItemModel(block);
    }

    private void waterPlant(Block block) {
        simpleBlock(block, emissiveCross(block));
        blockModels.registerSimpleFlatItemModel(block);
    }

    private Identifier emissiveCross(Block block) {
        Identifier id = ModelLocationUtils.getModelLocation(block);
        return createdModels.computeIfAbsent(id, key -> new ModelTemplate(Optional.of(Nautec.rl("block/template_emissive_cross")), Optional.empty(),
                TextureSlot.CROSS, CROSS_EMISSIVE).create(key, new TextureMapping()
                .put(TextureSlot.CROSS, blockTexture(block))
                .put(CROSS_EMISSIVE, blockTexture(block, "_emissive")), blockModels.modelOutput));
    }

    private static Map<Direction, Material> faces(Material down, Material up, Material north, Material side) {
        Map<Direction, Material> faces = new EnumMap<>(Direction.class);
        faces.put(Direction.DOWN, down);
        faces.put(Direction.UP, up);
        faces.put(Direction.NORTH, north);
        faces.put(Direction.SOUTH, side);
        faces.put(Direction.WEST, side);
        faces.put(Direction.EAST, side);
        return faces;
    }

    private static Map<Direction, Material> glow(Material down, Material up, Material sides) {
        Map<Direction, Material> glow = new EnumMap<>(Direction.class);
        if (down != null) {
            glow.put(Direction.DOWN, down);
        }
        if (up != null) {
            glow.put(Direction.UP, up);
        }
        if (sides != null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                glow.put(direction, sides);
            }
        }
        return glow;
    }

    private static Map<Direction, Material> front(Material north) {
        Map<Direction, Material> glow = new EnumMap<>(Direction.class);
        glow.put(Direction.NORTH, north);
        return glow;
    }

    private Identifier emissiveCube(String name, Map<Direction, Material> faces, Material particle, Map<Direction, Material> glow) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> {
            StringBuilder letters = new StringBuilder();
            List<TextureSlot> slots = new ArrayList<>();
            TextureMapping mapping = new TextureMapping().put(TextureSlot.PARTICLE, particle);
            slots.add(TextureSlot.PARTICLE);
            for (Direction direction : Direction.values()) {
                slots.add(FACE_SLOTS.get(direction));
                mapping.put(FACE_SLOTS.get(direction), faces.get(direction));
                Material emissive = glow.get(direction);
                if (emissive != null) {
                    letters.append(direction.getName().charAt(0));
                    slots.add(GLOW_SLOTS.get(direction));
                    mapping.put(GLOW_SLOTS.get(direction), emissive);
                }
            }
            ModelTemplate template = new ModelTemplate(Optional.of(Nautec.rl("block/template_emissive_cube_" + letters)), Optional.empty(),
                    slots.toArray(TextureSlot[]::new));
            return template.create(key, mapping, blockModels.modelOutput);
        });
    }

    private void simpleBlock(Block block, Identifier model) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    public void horizontalBlock(Block block, Identifier model) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void aquaticCatalyst(AquaticCatalystBlock block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.values()) {
            int xRot = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
            int yRot = dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360;
            for (int stage : AquaticCatalystBlock.STAGE.getPossibleValues()) {
                for (boolean active : AquaticCatalystBlock.ACTIVE.getPossibleValues()) {
                    builder = builder.with(BlockModelGenerators.condition()
                                    .term(BlockStateProperties.FACING, dir)
                                    .term(AquaticCatalystBlock.STAGE, stage)
                                    .term(AquaticCatalystBlock.ACTIVE, active),
                            rotated(BlockModelGenerators.plainVariant(createActiveACModel(block, stage, active)), xRot, yRot));
                }
            }
            for (boolean linked : AquaticCatalystBlock.LINKED.getPossibleValues()) {
                builder = builder.with(BlockModelGenerators.condition()
                                .term(BlockStateProperties.FACING, dir)
                                .term(AquaticCatalystBlock.LINKED, linked),
                        rotated(BlockModelGenerators.plainVariant(existingModelFile(linked ? "aquatic_catalyst_lamp_linked" : "aquatic_catalyst_lamp_unlinked")), xRot, yRot));
            }
        }
        blockModels.blockStateOutput.accept(builder);
    }

    private static final int[][] ARM_ROTATIONS = {{0, 0}, {180, 0}, {90, 180}, {90, 0}, {90, 90}, {90, 270}};

    private void currentConduit(Block block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block)
                .with(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_core")));
        for (Direction direction : Direction.values()) {
            int[] rotation = ARM_ROTATIONS[direction.ordinal()];
            builder = builder.with(BlockModelGenerators.condition(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.CONNECTED),
                            rotated(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_arm")), rotation[0], rotation[1]))
                    .with(BlockModelGenerators.condition().term(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.NONE, ConduitArm.BLOCKED),
                            rotated(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_cap")), rotation[0], rotation[1]));
            if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                builder = builder.with(BlockModelGenerators.condition(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.CONNECTED),
                        rotated(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_collar")), rotation[0], rotation[1]));
            }
        }
        blockModels.blockStateOutput.accept(builder);
    }

    private void conduitTap(Block block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block)
                .with(BlockModelGenerators.plainVariant(existingModelFile("conduit_tap_core")));
        for (Direction direction : Direction.values()) {
            int[] rotation = ARM_ROTATIONS[direction.ordinal()];
            builder = builder
                    .with(BlockModelGenerators.condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.CONDUIT),
                            rotated(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_arm")), rotation[0], rotation[1]))
                    .with(BlockModelGenerators.condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.MACHINE),
                            rotated(BlockModelGenerators.plainVariant(existingModelFile("conduit_tap_flange_" + direction.getSerializedName())), rotation[0], rotation[1]));
            if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                builder = builder.with(BlockModelGenerators.condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.CONDUIT),
                        rotated(BlockModelGenerators.plainVariant(existingModelFile("current_conduit_collar")), rotation[0], rotation[1]));
            }
        }
        blockModels.blockStateOutput.accept(builder);
    }

    private void laserJunction(Block block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block);
        builder = laserJunctionConnection(builder, block, Direction.DOWN, 0, 0);
        builder = laserJunctionConnection(builder, block, Direction.UP, 180, 0);
        builder = laserJunctionConnection(builder, block, Direction.NORTH, 90, 180);
        builder = laserJunctionConnection(builder, block, Direction.EAST, 90, 270);
        builder = laserJunctionConnection(builder, block, Direction.SOUTH, 90, 0);
        builder = laserJunctionConnection(builder, block, Direction.WEST, 90, 90);
        builder = builder.with(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_base")));
        blockModels.blockStateOutput.accept(builder);
    }

    private MultiPartGenerator laserJunctionConnection(MultiPartGenerator builder, Block block, Direction direction, int x, int y) {
        MultiVariant in = rotated(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_connection_in")), x, y);
        MultiVariant out = rotated(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_connection_out")), x, y);
        return builder
                .with(BlockModelGenerators.condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.INPUT), in)
                .with(BlockModelGenerators.condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.OUTPUT), out);
    }

    public void existingFacingBlock(Block block, Block modelOf) {
        facingBlock(block, existingModelFile(modelOf));
    }

    public void facingBlock(Block block, Identifier model) {
        MultiVariant variant = BlockModelGenerators.plainVariant(model);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.FACING)
                        .select(Direction.UP, variant)
                        .select(Direction.DOWN, rotated(variant, 180, 0))
                        .select(Direction.NORTH, rotated(variant, 90, 0))
                        .select(Direction.SOUTH, rotated(variant, 90, 180))
                        .select(Direction.EAST, rotated(variant, 90, 90))
                        .select(Direction.WEST, rotated(variant, 90, 270))));
    }

    public void oilBarrel(Block block, Identifier model, Identifier openModel) {
        PropertyDispatch.C2<MultiVariant, Direction, Boolean> dispatch = PropertyDispatch.initial(BlockStateProperties.FACING, OilBarrelBlock.OPEN);
        for (boolean open : new boolean[]{false, true}) {
            MultiVariant variant = BlockModelGenerators.plainVariant(open ? openModel : model);
            dispatch = dispatch
                    .select(Direction.UP, open, variant)
                    .select(Direction.DOWN, open, rotated(variant, 180, 0))
                    .select(Direction.NORTH, open, rotated(variant, 90, 0))
                    .select(Direction.SOUTH, open, rotated(variant, 90, 180))
                    .select(Direction.EAST, open, rotated(variant, 90, 90))
                    .select(Direction.WEST, open, rotated(variant, 90, 270));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private void crateBlock(CrateBlock crateBlock) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(crateBlock)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.OPEN,
                        BlockModelGenerators.plainVariant(extend(existingModelFile(crateBlock), "_open")),
                        BlockModelGenerators.plainVariant(existingModelFile(crateBlock)))));
    }

    private void rustyCrateBlock(CrateBlock crateBlock) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(crateBlock)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.OPEN,
                        BlockModelGenerators.plainVariant(rustedCrateModel(crateBlock, true)),
                        BlockModelGenerators.plainVariant(rustedCrateModel(crateBlock, false)))));
    }

    private Identifier rustedCrateModel(CrateBlock block, boolean open) {
        Identifier id = Nautec.rl("block/" + name(block) + (open ? "_open" : ""));
        return createdModels.computeIfAbsent(id, key -> {
            ModelTemplate template = new ModelTemplate(
                    Optional.of(extend(existingModelFile(NTBlocks.CRATE.get()), open ? "_open" : "")),
                    Optional.empty(), SLOT_2, SLOT_4, SLOT_5, TextureSlot.PARTICLE);
            return template.create(key, new TextureMapping()
                    .put(SLOT_2, new Material(Nautec.rl("block/crate/rusty_top_inner")))
                    .put(SLOT_4, new Material(Nautec.rl("block/crate/rusty")))
                    .put(SLOT_5, new Material(Nautec.rl("block/crate/rusty_top")))
                    .put(TextureSlot.PARTICLE, new Material(Nautec.rl("block/crate/rusty"))), blockModels.modelOutput);
        });
    }

    public Material multiblockTexture(Multiblock multiblock, String name) {
        return new Material(Nautec.rl("block/multiblock/" + NTRegistries.MULTIBLOCK.getKey(multiblock).getPath() + "/" + name));
    }

    private Identifier createActiveACModel(AquaticCatalystBlock block, int stage, boolean active) {
        String suffix = active ? "_active" : "";
        if (active) {
            Map<Direction, Material> faces = faces(blockTexture(block, "_bottom_active"), blockTexture(block, "_top_" + stage),
                    blockTexture(block, "_side_active"), blockTexture(block, "_side_active"));
            Map<Direction, Material> glow = glow(blockTexture(block, "_bottom_active_emissive"), null, blockTexture(block, "_side_active_emissive"));
            return emissiveCube(name(block) + suffix + (stage != 0 ? ("_" + stage) : ""), faces, blockTexture(block, "_side"), glow);
        }
        return cube(name(block) + suffix + (stage != 0 ? ("_" + stage) : ""),
                blockTexture(block, "_bottom" + suffix),
                blockTexture(block, "_top_" + stage),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side"));
    }

    public Identifier cube(String name, Material down, Material up, Material north, Material south, Material east, Material west, Material particle) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE.create(key, new TextureMapping()
                .put(TextureSlot.DOWN, down)
                .put(TextureSlot.UP, up)
                .put(TextureSlot.NORTH, north)
                .put(TextureSlot.SOUTH, south)
                .put(TextureSlot.EAST, east)
                .put(TextureSlot.WEST, west)
                .put(TextureSlot.PARTICLE, particle), blockModels.modelOutput));
    }

    private Identifier artModel(Block block) {
        Identifier id = ModelLocationUtils.getModelLocation(block);
        ModelTemplate template = new ModelTemplate(Optional.of(Nautec.rl("block/art/" + name(block))), Optional.empty());
        return template.create(id, new TextureMapping(), blockModels.modelOutput);
    }

    public Identifier cubeTop(Block block, Material side, Material top) {
        Identifier id = ModelLocationUtils.getModelLocation(block);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_TOP.create(key, new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, top), blockModels.modelOutput));
    }

    public Identifier cubeAll(String name, Material all) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_ALL.create(key, new TextureMapping()
                .put(TextureSlot.ALL, all), blockModels.modelOutput));
    }

    public Identifier cubeBottomTop(String name, Material side, Material bottom, Material top) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_BOTTOM_TOP.create(key, new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top), blockModels.modelOutput));
    }

    public static MultiVariant rotated(MultiVariant variant, int xRot, int yRot) {
        MultiVariant result = variant;
        switch (xRot) {
            case 90 -> result = result.with(BlockModelGenerators.X_ROT_90);
            case 180 -> result = result.with(BlockModelGenerators.X_ROT_180);
            case 270 -> result = result.with(BlockModelGenerators.X_ROT_270);
        }
        switch (yRot) {
            case 90 -> result = result.with(BlockModelGenerators.Y_ROT_90);
            case 180 -> result = result.with(BlockModelGenerators.Y_ROT_180);
            case 270 -> result = result.with(BlockModelGenerators.Y_ROT_270);
        }
        return result;
    }

    public Material blockTexture(Block block) {
        return blockTexture(block, "");
    }

    public Material blockTexture(Block block, String suffix) {
        Identifier name = key(block);
        return new Material(ModelPaths.blockModel(name, suffix));
    }

    public Identifier existingModelFile(Block block) {
        Identifier name = key(block);
        return ModelPaths.blockModel(name);
    }

    public Identifier existingModelFile(String name) {
        return Nautec.rl("block/" + name);
    }

    public Identifier key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public String name(Block block) {
        return key(block).getPath();
    }

    public Identifier extend(Identifier rl, String suffix) {
        return ModelPaths.extend(rl, suffix);
    }
}
