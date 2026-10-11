package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.conditions.SkyblockOption;
import com.breakinblocks.nautec.data.conditions.SkyblockOptionCondition;
import com.breakinblocks.nautec.datagen.recipeBuilder.AquaticCatalystChannelingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.CombustionAdditiveRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.AugmentationRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.ColonyFeedingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.IncubationRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.ItemEtchingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.ItemTransformationRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.LaserCraftingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.MixingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.MutationRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.PressureForgingRecipeBuilder;
import com.breakinblocks.nautec.datagen.recipeBuilder.ResonanceCraftingRecipeBuilder;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.utils.ranges.IntRange;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.ItemLike;
import com.breakinblocks.nautec.utils.templates.FluidStackTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RecipesProvider extends RecipeProvider {
    public RecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        RecipeOutput pRecipeOutput = output;
        aquaticCatalystRecipes(pRecipeOutput);
        combustionAdditiveRecipes(pRecipeOutput);
        beamOpticsRecipes(pRecipeOutput);
        resonanceRecipes(pRecipeOutput);
        gatewayRecipes(pRecipeOutput);
        pressureForgeRecipes(pRecipeOutput);
        dockRecipes(pRecipeOutput);
        waveJetRecipes(pRecipeOutput);
        atlanteanRifleRecipes(pRecipeOutput);
        neptunesTridentRecipes(pRecipeOutput);

        aquarineSteelRecipes(pRecipeOutput);
        laserCraftingRecipes(pRecipeOutput);

        ancientItemsRecipes(pRecipeOutput);
        guaranteedPartRecipes(pRecipeOutput);
        fusionPlantRecipes(pRecipeOutput);

        chemistryRecipes(pRecipeOutput);

        nautecFishingRodRecipe(pRecipeOutput);

        augmentPartRecipes(pRecipeOutput);

        augmentationRecipes(pRecipeOutput);

        aquarineSteelToolsRecipes(pRecipeOutput);

        divingArmorRecipes(pRecipeOutput);

        aquarineSteelArmorRecipes(pRecipeOutput);

        buildingBlockRecipes(pRecipeOutput);

        utilityRecipes(pRecipeOutput);

        castIronRecipes(pRecipeOutput);

        conduitRecipes(pRecipeOutput);

        resonantStorageRecipes(pRecipeOutput);

        miscItemsRecipes(pRecipeOutput);

        machineRecipes(pRecipeOutput);

        laserDeviceRecipes(pRecipeOutput);

        drainRecipes(pRecipeOutput);

        augmentationStationRecipes(pRecipeOutput);

        mutationRecipes(pRecipeOutput);

        incubationRecipes(pRecipeOutput);

        bioReactorRecipes(pRecipeOutput);

        skyblockRecipes(pRecipeOutput);

        shaped(RecipeCategory.MISC, NTItems.GLASS_VIAL.asItem(), 3)
                .pattern("G G")
                .pattern("G G")
                .pattern(" G ")
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy("has_item", has(Tags.Items.GLASS_BLOCKS_COLORLESS))
                .save(pRecipeOutput, key("glass_vial"));

        shaped(RecipeCategory.MISC, NTItems.CLAW_ROBOT_ARM.asItem(), 1)
                .pattern("AB ")
                .pattern(" AB")
                .pattern("  A")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('B', NTItems.CAST_IRON_ROD)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("claw_robot_arm"));

        shaped(RecipeCategory.MISC, NTItems.PRISM_MONOCLE.asItem(), 1)
                .pattern("AAA")
                .pattern("AP ")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('P', Tags.Items.GEMS_PRISMARINE)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("prism_monocle"));

        shapeless(RecipeCategory.MISC, NTItems.EYE_OF_THE_SEA.get(), 1)
                .requires(Items.PRISMARINE_SHARD)
                .requires(Tags.Items.ENDER_PEARLS)
                .unlockedBy("has_item", has(Tags.Items.ENDER_PEARLS))
                .save(pRecipeOutput, key("eye_of_the_sea"));

        brownPolymerRecipes(pRecipeOutput);

        shapeless(RecipeCategory.MISC, NTItems.NAUTEC_GUIDE.get(), 1)
                .requires(Items.BOOK)
                .requires(NTItems.CAST_IRON_NUGGET.get(), 1)
                .unlockedBy("has_item", has(NTItems.CAST_IRON_NUGGET.get()))
                .save(pRecipeOutput, key("nautec_guide"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get(), 1)
                .pattern("CCC")
                .pattern("CPC")
                .pattern("CCC")
                .define('C', Tags.Items.GEMS_PRISMARINE)
                .define('P', Items.PRISMARINE_SHARD)
                .unlockedBy("has_item", has(Tags.Items.GEMS_PRISMARINE))
                .save(pRecipeOutput, key("decorative_prismarine_crystal"));


        SimpleCookingRecipeBuilder.blasting(Ingredient.of(NTBlocks.ANCHOR), RecipeCategory.MISC, new ItemStack(NTItems.CAST_IRON_INGOT.get(), 11), 0.2f, 400)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(pRecipeOutput, key("cast_iron_ingot_from_anchor_blasting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(NTBlocks.OIL_BARREL), RecipeCategory.MISC, new ItemStack(NTItems.CAST_IRON_INGOT.get(), 5), 0.2f, 400)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(pRecipeOutput, key("cast_iron_ingot_from_oil_barrel_blasting"));

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(NTItems.CAST_IRON_COMPOUND), RecipeCategory.MISC, NTItems.CAST_IRON_INGOT.get(), 0.2f, 100)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(pRecipeOutput, key("cast_iron_ingot_blasting"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(NTItems.CAST_IRON_COMPOUND), RecipeCategory.MISC, NTItems.CAST_IRON_INGOT.get(), 0.2f, 200)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_IRON))
                .save(pRecipeOutput, key("cast_iron_ingot_smelting"));
    }

    private void conduitRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shapeless(RecipeCategory.MISC, NTItems.AQUARINE_COPPER_COMPOUND.get(), 2)
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .requires(Items.DRIED_KELP)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_COPPER))
                .save(pRecipeOutput, key("aquarine_copper_compound"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUARINE_COPPER_COMPOUND.get(), 6))
                .ingredients(iwcFromTag(Tags.Items.INGOTS_COPPER, 3),
                        iwcFromItemLike(NTItems.KELP_SLURRY.get(), 1))
                .duration(100)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 500))
                .fluidResult(null)
                .save(pRecipeOutput, key("aquarine_copper_compound_mixing"));

        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUARINE_COPPER_INGOT.get(), 4))
                .ingredient(NTItems.AQUARINE_COPPER_COMPOUND.get())
                .purity(2.1f)
                .duration(100)
                .save(pRecipeOutput, key("aquarine_copper_ingot"));

        shapeless(RecipeCategory.MISC, NTItems.AQUARINE_COPPER_NUGGET.get(), 9)
                .requires(NTItems.AQUARINE_COPPER_INGOT)
                .unlockedBy("has_item", has(NTItems.AQUARINE_COPPER_INGOT))
                .save(pRecipeOutput, key("aquarine_copper_nugget"));
        shaped(RecipeCategory.MISC, NTItems.AQUARINE_COPPER_INGOT.get())
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', NTItems.AQUARINE_COPPER_NUGGET)
                .unlockedBy("has_item", has(NTItems.AQUARINE_COPPER_NUGGET))
                .save(pRecipeOutput, key("aquarine_copper_ingot_from_nuggets"));
        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.AQUARINE_COPPER_BLOCK.get())
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', NTItems.AQUARINE_COPPER_INGOT)
                .unlockedBy("has_item", has(NTItems.AQUARINE_COPPER_INGOT))
                .save(pRecipeOutput, key("aquarine_copper_block"));
        shapeless(RecipeCategory.MISC, NTItems.AQUARINE_COPPER_INGOT.get(), 9)
                .requires(NTBlocks.AQUARINE_COPPER_BLOCK)
                .unlockedBy("has_item", has(NTBlocks.AQUARINE_COPPER_BLOCK))
                .save(pRecipeOutput, key("aquarine_copper_ingot_from_block"));

        shapeless(RecipeCategory.MISC, NTItems.CLAY_GASKET.get(), 8)
                .requires(Items.CLAY_BALL)
                .requires(Items.CLAY_BALL)
                .requires(Items.DRIED_KELP)
                .unlockedBy("has_item", has(Items.CLAY_BALL))
                .save(pRecipeOutput, key("clay_gasket"));

        shaped(RecipeCategory.TRANSPORTATION, NTBlocks.CURRENT_CONDUIT.get(), 8)
                .pattern("AAA")
                .pattern(" G ")
                .define('A', NTItems.AQUARINE_COPPER_INGOT)
                .define('G', NTItems.CLAY_GASKET)
                .unlockedBy("has_item", has(NTItems.AQUARINE_COPPER_INGOT))
                .save(pRecipeOutput, key("current_conduit"));

        shaped(RecipeCategory.MISC, NTItems.EDDY_UPGRADE.get())
                .pattern(" A ")
                .pattern("ARA")
                .pattern(" I ")
                .define('A', NTItems.AQUARINE_COPPER_INGOT)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_item", has(NTBlocks.CURRENT_CONDUIT))
                .save(pRecipeOutput, key("eddy_upgrade"));
        upgrade(pRecipeOutput, NTItems.SURGE_UPGRADE.get(), NTItems.EDDY_UPGRADE.get(), tag(Tags.Items.GEMS_EMERALD), "surge_upgrade");
        upgrade(pRecipeOutput, NTItems.RIPTIDE_UPGRADE.get(), NTItems.SURGE_UPGRADE.get(), tag(Tags.Items.GEMS_DIAMOND), "riptide_upgrade");
        upgrade(pRecipeOutput, NTItems.MAELSTROM_UPGRADE.get(), NTItems.RIPTIDE_UPGRADE.get(),
                Ingredient.of(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get()), "maelstrom_upgrade");

        shapeless(RecipeCategory.MISC, NTItems.FILTER.get())
                .requires(Items.PAPER)
                .requires(NTItems.CAST_IRON_NUGGET)
                .requires(Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_item", has(NTBlocks.CURRENT_CONDUIT))
                .save(pRecipeOutput, key("filter"));
        shapeless(RecipeCategory.MISC, NTItems.INTRICATE_FILTER.get())
                .requires(NTItems.FILTER)
                .requires(Tags.Items.GEMS_EMERALD)
                .unlockedBy("has_item", has(NTItems.FILTER))
                .save(pRecipeOutput, key("intricate_filter"));
    }

    private void resonantStorageRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTBlocks.RESONANT_VAULT.get())
                .pattern("AEA")
                .pattern("PCP")
                .pattern("ASA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('E', NTItems.EYE_OF_THE_SEA)
                .define('P', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('C', Tags.Items.CHESTS)
                .define('S', NTItems.RESONANT_SHARD)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("resonant_vault"));
        shaped(RecipeCategory.MISC, NTBlocks.RESONANT_CISTERN.get())
                .pattern("AEA")
                .pattern("GBG")
                .pattern("ASA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('E', NTItems.EYE_OF_THE_SEA)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', Tags.Items.BUCKETS_EMPTY)
                .define('S', NTItems.RESONANT_SHARD)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("resonant_cistern"));
        shaped(RecipeCategory.MISC, NTItems.RESONANT_EXPANSION.get())
                .pattern("CPC")
                .pattern("PSP")
                .pattern("CPC")
                .define('C', NTItems.AQUARINE_COPPER_INGOT)
                .define('P', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('S', NTItems.RESONANT_SHARD)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("resonant_expansion"));
    }

    private void upgrade(RecipeOutput pRecipeOutput, ItemLike result, ItemLike previous, Ingredient material, String name) {
        shaped(RecipeCategory.MISC, result)
                .pattern(" M ")
                .pattern("RPR")
                .define('M', material)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('P', previous)
                .unlockedBy("has_item", has(previous))
                .save(pRecipeOutput, key(name));
    }

    private void aquaticCatalystRecipes(@NotNull RecipeOutput pRecipeOutput) {
        AquaticCatalystChannelingRecipeBuilder.newRecipe(tag(Tags.Items.GEMS_PRISMARINE))
                .powerAmount(3750)
                .purity(0.8f)
                .duration(600)
                .save(pRecipeOutput, key("prismarine_crystals_to_ap"));

        AquaticCatalystChannelingRecipeBuilder.newRecipe(Ingredient.of(Items.PRISMARINE_SHARD))
                .powerAmount(7500)
                .purity(0.4f)
                .duration(600)
                .save(pRecipeOutput, key("prismarine_shards_to_ap"));

        AquaticCatalystChannelingRecipeBuilder.newRecipe(Ingredient.of(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .powerAmount(9000)
                .purity(1.2f)
                .duration(750)
                .save(pRecipeOutput, key("prismarine_crystal_shards_to_ap"));
    }

    private void combustionAdditiveRecipes(@NotNull RecipeOutput pRecipeOutput) {
        CombustionAdditiveRecipeBuilder.newRecipe(tag(Tags.Items.DUSTS_REDSTONE))
                .outputMultiplier(1.5f)
                .fuelMultiplier(1.1f)
                .duration(1200)
                .save(pRecipeOutput, key("combustion_additive/redstone"));
    }

    private void resonanceRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.DECORATIONS, NTBlocks.RESONANCE_CHAMBER.asItem())
                .pattern("DCD")
                .pattern("CLC")
                .pattern("DAD")
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .define('A', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("resonance_chamber"));

        shaped(RecipeCategory.MISC, NTItems.DORMANT_CRYSTAL_SEED.get())
                .pattern("RSR")
                .pattern("SFS")
                .pattern("RSR")
                .define('R', NTItems.RESONANT_SHARD)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .unlockedBy("has_item", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL))
                .save(pRecipeOutput, key("dormant_crystal_seed"));

        ResonanceCraftingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.PRISMARINE_CRYSTAL_SEED.get(), 1))
                .ingredient(NTItems.DORMANT_CRYSTAL_SEED.get())
                .purity(3.0f)
                .save(pRecipeOutput, key("prismarine_crystal_seed"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.CRYSTAL_CRADLE.asItem())
                .pattern("GFG")
                .pattern("DCD")
                .pattern("DDD")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .unlockedBy("has_item", has(NTItems.DEEP_STEEL_PLATING))
                .save(pRecipeOutput, key("crystal_cradle"));

        ResonanceCraftingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.RESONANT_SHARD.get(), 1))
                .ingredient(NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .purity(3.0f)
                .save(pRecipeOutput, key("resonant_shard"));

        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 2))
                .ingredient(iwcFromTag(Tags.Items.GEMS_PRISMARINE, 1))
                .purity(2.0f)
                .duration(160)
                .save(pRecipeOutput, key("prismarine_crystal_shard_from_crystals"));

        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUARINE_STEEL_INGOT.get(), 2))
                .ingredient(NTItems.AQUARINE_STEEL_COMPOUND.get())
                .purity(2.0f)
                .duration(80)
                .save(pRecipeOutput, key("aquarine_steel_ingot_dense"));
    }

    private void waveJetRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.TRANSPORTATION, NTItems.WAVE_JET.get())
                .pattern(" AC")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('B', NTItems.PRISMATIC_BATTERY)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.PRISMATIC_BATTERY))
                .save(pRecipeOutput, key("wave_jet"));
    }

    private void atlanteanRifleRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.COMBAT, NTItems.ATLANTEAN_RIFLE.get())
                .pattern("  F")
                .pattern(" DL")
                .pattern("BDC")
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .define('B', NTItems.PRISMATIC_BATTERY)
                .define('C', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL))
                .save(pRecipeOutput, key("atlantean_rifle"));
    }

    private void neptunesTridentRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.COMBAT, NTItems.NEPTUNES_TRIDENT.get())
                .pattern("RFR")
                .pattern("DHD")
                .pattern(" D ")
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('R', NTItems.RESONANT_SHARD)
                .define('H', Items.HEART_OF_THE_SEA)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .unlockedBy("has_item", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL))
                .save(pRecipeOutput, key("neptunes_trident"));
    }

    private void dockRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.DECORATIONS, NTBlocks.SUBMARINE_DOCK.asItem())
                .pattern("AAA")
                .pattern("PLP")
                .pattern("APA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("submarine_dock"));
    }

    private void pressureForgeRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.DECORATIONS, NTBlocks.PRESSURE_FORGE.asItem())
                .pattern("DAD")
                .pattern("RCR")
                .pattern("DAD")
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('R', NTItems.RESONANT_SHARD)
                .define('C', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("pressure_forge"));

        PressureForgingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get(), 1))
                .ingredient(NTItems.RESONANT_SHARD.get())
                .minDepth(-20)
                .purity(2.0f)
                .duration(200)
                .save(pRecipeOutput, key("flawless_prismarine_crystal"));

        PressureForgingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.DEEP_STEEL_PLATING.get(), 1))
                .ingredient(NTItems.AQUARINE_STEEL_INGOT.get())
                .minDepth(-40)
                .purity(2.5f)
                .duration(300)
                .save(pRecipeOutput, key("deep_steel_plating"));
    }

    private void gatewayRecipes(@NotNull RecipeOutput pRecipeOutput) {
        ItemStackTemplate packedGateway = new ItemStackTemplate(NTBlocks.GATEWAY.asItem(), 1,
                DataComponentPatch.builder().set(NTDataComponents.GATEWAY_PACKED.get(), PackedGateway.CRAFTED).build());
        shaped(RecipeCategory.DECORATIONS, packedGateway)
                .pattern("FDF")
                .pattern("RNR")
                .pattern("FDF")
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('R', NTItems.RESONANT_SHARD)
                .define('N', Tags.Items.NETHER_STARS)
                .unlockedBy("has_item", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL))
                .save(pRecipeOutput, key("gateway"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.GATEWAY_RING.asItem(), 8)
                .pattern("PIP")
                .pattern("PSP")
                .pattern("PIP")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('I', NTItems.AQUARINE_STEEL_INGOT)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .unlockedBy("has_item", has(NTBlocks.GATEWAY))
                .save(pRecipeOutput, key("gateway_ring"));
    }

    private void beamOpticsRecipes(@NotNull RecipeOutput pRecipeOutput) {

        shaped(RecipeCategory.DECORATIONS, NTBlocks.FOCUSING_LENS.asItem())
                .pattern("ASA")
                .pattern("SLS")
                .pattern("ASA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("focusing_lens"));
    }

    private void aquarineSteelRecipes(@NotNull RecipeOutput pRecipeOutput) {
        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUARINE_STEEL_INGOT.get(), 1))
                .ingredient(NTItems.AQUARINE_STEEL_COMPOUND.get())
                .purity(0)
                .duration(100)
                .save(pRecipeOutput, key("aquarine_steel_ingot"));

        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, NTItems.AQUARINE_STEEL_INGOT.get(), RecipeCategory.BUILDING_BLOCKS, NTBlocks.AQUARINE_STEEL_BLOCK.get());

        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, NTItems.ATLANTIC_GOLD_NUGGET, RecipeCategory.MISC, NTItems.ATLANTIC_GOLD_INGOT,
                Nautec.MODID + ":atlantic_gold_ingot_from_nuggets", null, Nautec.MODID + ":atlantic_gold_nuggets_from_ingot", null);
    }

    private void laserCraftingRecipes(@NotNull RecipeOutput pRecipeOutput) {
        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(IngredientWithCount.fromItemLike(NTItems.AQUARINE_STEEL_COMPOUND.get()))
                .result(new ItemStackTemplate(NTItems.AQUARINE_STEEL_INGOT.get(), 1))
                .power(10)
                .purity(0)
                .duration(20)
                .save(pRecipeOutput, key("laser_crafting/aquarine_steel_ingot"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(IngredientWithCount.fromItemLike(NTItems.AQUARINE_STEEL_COMPOUND.get()))
                .result(new ItemStackTemplate(NTItems.AQUARINE_STEEL_INGOT.get(), 2))
                .power(20)
                .purity(2.0f)
                .duration(20)
                .save(pRecipeOutput, key("laser_crafting/aquarine_steel_ingot_dense"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(IngredientWithCount.fromItemLike(NTItems.AQUARINE_COPPER_COMPOUND.get()))
                .result(new ItemStackTemplate(NTItems.AQUARINE_COPPER_INGOT.get(), 4))
                .power(20)
                .purity(2.1f)
                .duration(20)
                .save(pRecipeOutput, key("laser_crafting/aquarine_copper_ingot"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(IngredientWithCount.fromItemLike(NTItems.BURNT_COIL.get()))
                .result(new ItemStackTemplate(NTItems.LASER_CHANNELING_COIL.get(), 1))
                .power(20)
                .purity(1.5f)
                .duration(100)
                .save(pRecipeOutput, key("laser_crafting/laser_channeling_coil"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(iwcFromTag(Tags.Items.GEMS_PRISMARINE, 1))
                .result(new ItemStackTemplate(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 2))
                .power(20)
                .purity(2.0f)
                .duration(40)
                .save(pRecipeOutput, key("laser_crafting/prismarine_crystal_shard"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(IngredientWithCount.fromItemLike(NTBlocks.CAST_IRON_BLOCK.get()))
                .result(new ItemStackTemplate(NTItems.GEAR.get(), 4))
                .power(40)
                .purity(2.5f)
                .duration(80)
                .save(pRecipeOutput, key("laser_crafting/gear"));
    }

    private void augmentationStationRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTBlocks.AUGMENTATION_STATION.asItem(), 1)
                .pattern("ACA")
                .pattern("PEP")
                .pattern("AAA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('E', NTItems.ELDRITCH_HEART)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("augmentation_station"));

        shaped(RecipeCategory.MISC, NTBlocks.AUGMENTATION_STATION_EXTENSION.asItem(), 2)
                .pattern("ASA")
                .pattern("APA")
                .pattern("ACA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("augmentation_station_extension"));
    }

    private void drainRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTBlocks.DRAIN.asItem(), 1)
                .pattern("CVC")
                .pattern("AGA")
                .pattern("CCC")
                .define('C', NTItems.CAST_IRON_INGOT)
                .define('V', NTItems.VALVE)
                .define('G', NTItems.GEAR)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .unlockedBy("has_item", has(NTItems.VALVE))
                .save(pRecipeOutput, key("drain"));

        shaped(RecipeCategory.MISC, NTBlocks.DRAIN_WALL.asItem(), 2)
                .pattern("CCC")
                .pattern("R R")
                .pattern("CCC")
                .define('C', NTItems.CAST_IRON_INGOT)
                .define('R', NTItems.CAST_IRON_ROD)
                .unlockedBy("has_item", has(NTItems.CAST_IRON_INGOT))
                .save(pRecipeOutput, key("drain_wall"));
    }

    private void laserDeviceRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTBlocks.PRISMARINE_RELAY.asItem(), 4)
                .pattern("AAA")
                .pattern("   ")
                .pattern("AAA")
                .define('A', NTBlocks.POLISHED_PRISMARINE)
                .unlockedBy("has_item", has(NTBlocks.POLISHED_PRISMARINE))
                .save(pRecipeOutput, key("prismarine_relay"));

        shaped(RecipeCategory.MISC, NTBlocks.LASER_JUNCTION.asItem(), 2)
                .pattern("ARA")
                .pattern("RDR")
                .pattern("ARA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('R', NTBlocks.PRISMARINE_RELAY)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("has_item", has(NTBlocks.PRISMARINE_RELAY))
                .save(pRecipeOutput, key("laser_junction"));

        shaped(RecipeCategory.MISC, NTBlocks.AQUATIC_CATALYST.asItem(), 1)
                .pattern("PCP")
                .pattern("P P")
                .pattern("PCP")
                .define('C', Tags.Items.GEMS_PRISMARINE)
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .unlockedBy("has_item", has(NTBlocks.POLISHED_PRISMARINE))
                .save(pRecipeOutput, key("aquatic_catalyst"));
    }

    private void machineRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.DECORATIONS, NTBlocks.MIXER.asItem())
                .pattern("DGD")
                .pattern("PWP")
                .pattern("PAP")
                .define('G', NTItems.GEAR)
                .define('D', NTBlocks.POLISHED_PRISMARINE)
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('W', NTItems.WHISK)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("mixer"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.LASER_CRAFTING_MATRIX.asItem())
                .pattern("ALA")
                .pattern("DCD")
                .pattern("AOA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('L', NTItems.PRISMARINE_LENS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('O', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("laser_crafting_matrix"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.FISHING_STATION.asItem())
                .pattern("DAD")
                .pattern("RGR")
                .pattern("DAD")
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .define('R', NTItems.CAST_IRON_ROD)
                .define('G', NTItems.GEAR)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("fishing_station"));

        shaped(RecipeCategory.MISC, NTItems.SPAWNER_CONFINEMENT_MATRIX.get())
                .pattern("ARA")
                .pattern("CEC")
                .pattern("ARA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('R', NTItems.RESONANT_SHARD)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('E', Tags.Items.ENDER_PEARLS)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("spawner_confinement_matrix"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.CHARGER.asItem())
                .pattern("PAP")
                .pattern("DCD")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('D', Blocks.DARK_PRISMARINE)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("charger"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.INCUBATOR.asItem())
                .pattern("PGP")
                .pattern("CAC")
                .pattern("PLP")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('C', Tags.Items.GEMS_PRISMARINE)
                .define('A', NTItems.AQUATIC_CHIP)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("incubator"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.MUTATOR.asItem())
                .pattern("DCD")
                .pattern("PBP")
                .pattern("DCD")
                .define('P', NTItems.PETRI_DISH)
                .define('B', NTFluids.EAS.getBucket())
                .define('C', NTBlocks.BACTERIAL_CONTAINMENT_SHIELD)
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .unlockedBy("has_item", has(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD))
                .save(pRecipeOutput, key("mutator"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.COLONY_REPLICATOR.asItem())
                .pattern("DMD")
                .pattern("PAP")
                .pattern("DFD")
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('M', NTBlocks.MUTATOR)
                .define('P', NTItems.PETRI_DISH)
                .define('A', NTBlocks.BACTERIAL_ANALYZER)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .unlockedBy("has_item", has(NTBlocks.MUTATOR))
                .save(pRecipeOutput, key("colony_replicator"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.OXYGEN_DIFFUSER.asItem())
                .pattern("CGC")
                .pattern("BVB")
                .pattern("CKC")
                .define('C', NTItems.CAST_IRON_INGOT)
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('B', NTItems.AIR_BOTTLE)
                .define('V', NTItems.VALVE)
                .define('K', Tags.Items.STORAGE_BLOCKS_DRIED_KELP)
                .unlockedBy("has_item", has(NTItems.AIR_BOTTLE))
                .save(pRecipeOutput, key("oxygen_diffuser"));

        shaped(RecipeCategory.TOOLS, NTItems.BUBBLE_CAPSULE.get(), 2)
                .pattern(" K ")
                .pattern("SBS")
                .pattern(" K ")
                .define('K', Items.DRIED_KELP)
                .define('S', Items.PRISMARINE_SHARD)
                .define('B', NTItems.AIR_BOTTLE)
                .unlockedBy("has_item", has(NTItems.AIR_BOTTLE))
                .save(pRecipeOutput, key("bubble_capsule"));

        shaped(RecipeCategory.REDSTONE, NTBlocks.PRESSURE_HATCH.asItem(), 2)
                .pattern("SV")
                .pattern("GS")
                .pattern("SS")
                .define('S', NTItems.AQUARINE_STEEL_INGOT)
                .define('V', NTItems.VALVE)
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("pressure_hatch"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.BUBBLE_ANCHOR.asItem())
                .pattern("PGP")
                .pattern("GKG")
                .pattern("PCP")
                .define('P', Items.PRISMARINE_SHARD)
                .define('G', Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('K', Tags.Items.STORAGE_BLOCKS_DRIED_KELP)
                .define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_item", has(Items.PRISMARINE_SHARD))
                .save(pRecipeOutput, key("bubble_anchor"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.DISTRIBUTOR.asItem())
                .pattern("ACA")
                .pattern("HEH")
                .pattern("ACA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('H', Items.HOPPER)
                .define('E', Tags.Items.ENDER_PEARLS)
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("nautechnical_distributor"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.ADVANCED_BACTERIAL_ANALYZER.asItem())
                .pattern("ACA")
                .pattern("LBL")
                .pattern("ADA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .define('B', NTBlocks.BACTERIAL_ANALYZER)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .unlockedBy("has_item", has(NTBlocks.BACTERIAL_ANALYZER))
                .save(pRecipeOutput, key("advanced_bacterial_analyzer"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.GRAFTING_STATION.asItem())
                .pattern("DGD")
                .pattern("PSP")
                .pattern("DLD")
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .define('G', NTItems.GRAFTING_TOOL)
                .define('P', NTItems.PETRI_DISH)
                .define('S', NTBlocks.BACTERIAL_CONTAINMENT_SHIELD)
                .define('L', NTItems.PRISMARINE_LENS)
                .unlockedBy("has_item", has(NTItems.GRAFTING_TOOL))
                .save(pRecipeOutput, key("grafting_station"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.BACTERIAL_FUEL_CELL.asItem())
                .pattern("DCD")
                .pattern("PSP")
                .pattern("DLD")
                .define('D', NTBlocks.DARK_PRISMARINE_PILLAR)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('P', NTItems.PETRI_DISH)
                .define('S', NTBlocks.BACTERIAL_CONTAINMENT_SHIELD)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD))
                .save(pRecipeOutput, key("bacterial_fuel_cell"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.BACTERIAL_ANALYZER.asItem())
                .pattern("PLP")
                .pattern("A A")
                .pattern("A A")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('L', NTItems.PRISMARINE_LENS)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_LENS))
                .save(pRecipeOutput, key("bacterial_analyzer"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.BIO_REACTOR.asItem())
                .pattern("CCC")
                .pattern("PAP")
                .pattern("PLP")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('A', NTItems.AQUATIC_CHIP)
                .define('C', Tags.Items.GEMS_PRISMARINE)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("bio_reactor"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.ENERGY_CONVERTER.asItem())
                .pattern("PRP")
                .pattern("ACA")
                .pattern("PRP")
                .define('P', NTBlocks.POLISHED_PRISMARINE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("energy_converter"));

        shaped(RecipeCategory.MISC, NTItems.ENERGY_CONVERSION_UPGRADE.get())
                .pattern(" R ")
                .pattern("SCS")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('S', NTItems.AQUARINE_STEEL_INGOT)
                .define('C', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTBlocks.ENERGY_CONVERTER))
                .save(pRecipeOutput, key("energy_conversion_upgrade"));

        shaped(RecipeCategory.MISC, NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE.get())
                .pattern("GPG")
                .pattern("PUP")
                .pattern("GPG")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('P', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('U', NTItems.ENERGY_CONVERSION_UPGRADE)
                .unlockedBy("has_item", has(NTItems.ENERGY_CONVERSION_UPGRADE))
                .save(pRecipeOutput, key("advanced_energy_conversion_upgrade"));

        shaped(RecipeCategory.MISC, NTItems.ULTIMATE_ENERGY_CONVERSION_UPGRADE.get())
                .pattern("DRD")
                .pattern("RUR")
                .pattern("DRD")
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('R', NTItems.RESONANT_SHARD)
                .define('U', NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE)
                .unlockedBy("has_item", has(NTItems.ADVANCED_ENERGY_CONVERSION_UPGRADE))
                .save(pRecipeOutput, key("ultimate_energy_conversion_upgrade"));
    }

    private void ancientItemsRecipes(@NotNull RecipeOutput pRecipeOutput) {
        ItemEtchingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.VALVE.get()))
                .ingredient(NTItems.ANCIENT_VALVE.get())
                .duration(200)
                .save(pRecipeOutput, key("valve"));

        shapeless(RecipeCategory.MISC, NTItems.AQUATIC_CHIP.get())
                .requires(NTItems.DAMAGED_AQUATIC_CHIP)
                .requires(Items.PRISMARINE_SHARD, 3)
                .unlockedBy("has_item", has(NTItems.DAMAGED_AQUATIC_CHIP.get()))
                .save(pRecipeOutput, key("aquatic_chip"));

        ItemEtchingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.GEAR.get()))
                .ingredient(NTItems.RUSTY_GEAR.get())
                .duration(160)
                .save(pRecipeOutput, key("gear"));

        shapeless(RecipeCategory.MISC, NTItems.WHISK.get(), 1)
                .requires(NTItems.BROKEN_WHISK.get())
                .requires(NTItems.CAST_IRON_NUGGET.get(), 4)
                .unlockedBy("has_item", has(NTItems.BROKEN_WHISK.get()))
                .save(pRecipeOutput, key("whisk"));

        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.LASER_CHANNELING_COIL.get()))
                .ingredient(NTItems.BURNT_COIL.get())
                .purity(1.5f)
                .duration(200)
                .save(pRecipeOutput, key("laser_channeling_coil"));
    }

    private void aquarineSteelToolsRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.AQUARINE_PICKAXE.get())
                .pattern("AGA")
                .pattern(" C ")
                .pattern(" R ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('R', NTItems.CAST_IRON_ROD.get())
                .define('G', NTItems.GEAR.get())
                .define('C', NTItems.LASER_CHANNELING_COIL.get())
                .save(pRecipeOutput, key("aquarine_pickaxe"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_SHOVEL.get())
                .pattern(" A ")
                .pattern(" G ")
                .pattern(" R ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('G', NTItems.GEAR.get())
                .define('R', NTItems.CAST_IRON_ROD.get())
                .save(pRecipeOutput, key("aquarine_shovel"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_AXE.get())
                .pattern("AG ")
                .pattern("AR ")
                .pattern(" R ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('G', NTItems.GEAR.get())
                .define('R', NTItems.CAST_IRON_ROD.get())
                .save(pRecipeOutput, key("aquarine_axe"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_HOE.get())
                .pattern("AA ")
                .pattern(" C ")
                .pattern(" R ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('R', NTItems.CAST_IRON_ROD.get())
                .define('C', NTItems.LASER_CHANNELING_COIL.get())
                .save(pRecipeOutput, key("aquarine_hoe"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_SWORD.get())
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" C ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.LASER_CHANNELING_COIL.get())
                .save(pRecipeOutput, key("aquarine_sword"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_WRENCH.get())
                .pattern("A A")
                .pattern(" A ")
                .pattern(" A ")
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.get()))
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .save(pRecipeOutput, key("aquarine_wrench"));
    }

    private void utilityRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.PRISMATIC_BATTERY.get(), 1)
                .pattern("SRS")
                .pattern("ACA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .define('C', NTItems.LASER_CHANNELING_COIL.get())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("prismatic_battery"));


        shaped(RecipeCategory.MISC, NTItems.SUBMARINE.get(), 1)
                .pattern("ACA")
                .pattern("GBG")
                .pattern("ATA")
                .define('A', NTItems.ATLANTIC_GOLD_INGOT.get())
                .define('C', NTItems.ELDRITCH_HEART.get())
                .define('G', Tags.Items.GLASS_BLOCKS_TINTED)
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .define('T', NTItems.BUOYANCY_TANK.get())
                .unlockedBy("has_item", has(NTItems.ELDRITCH_HEART))
                .save(pRecipeOutput, key("submarine"));

        submarineModuleRecipes(pRecipeOutput);

        shaped(RecipeCategory.MISC, NTItems.GRAFTING_TOOL.get(), 1)
                .pattern(" R")
                .pattern("I ")
                .define('R', NTItems.CAST_IRON_ROD.get())
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_item", has(NTItems.CAST_IRON_ROD))
                .save(pRecipeOutput, key("grafting_tool"));
    }

    private void submarineModuleRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.SOLAR_MODULE.get(), 1)
                .pattern("LLL")
                .pattern("GDG")
                .pattern("ACA")
                .define('L', NTItems.PRISMARINE_LENS.get())
                .define('G', NTItems.ATLANTIC_GOLD_INGOT.get())
                .define('D', Items.DAYLIGHT_DETECTOR)
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("solar_module"));

        shaped(RecipeCategory.MISC, NTItems.BOOSTER_MODULE.get(), 1)
                .pattern("AKA")
                .pattern("KBK")
                .pattern("ACA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('K', NTItems.LASER_CHANNELING_COIL.get())
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("booster_module"));

        shaped(RecipeCategory.MISC, NTItems.STEALTH_MODULE.get(), 1)
                .pattern("IMI")
                .pattern("MCM")
                .pattern("ABA")
                .define('I', Items.INK_SAC)
                .define('M', NTItems.LUMINOUS_MEMBRANE.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("stealth_module"));

        shaped(RecipeCategory.MISC, NTItems.ARMOR_MODULE.get(), 1)
                .pattern("DND")
                .pattern("NPN")
                .pattern("ACA")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('N', Tags.Items.INGOTS_NETHERITE)
                .define('P', NTItems.CHITIN_PLATE.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("armor_module"));

        shaped(RecipeCategory.MISC, NTItems.SONAR_MODULE.get(), 1)
                .pattern("ELE")
                .pattern("GCG")
                .pattern("ABA")
                .define('E', Items.ECHO_SHARD)
                .define('L', NTItems.PRISMARINE_LENS.get())
                .define('G', NTItems.ATLANTIC_GOLD_INGOT.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("sonar_module"));

        shaped(RecipeCategory.MISC, NTItems.SHIELD_MODULE.get(), 1)
                .pattern("SHS")
                .pattern("HCH")
                .pattern("ABA")
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .define('H', NTItems.ELDRITCH_HEART.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .unlockedBy("has_item", has(NTItems.ELDRITCH_HEART))
                .save(pRecipeOutput, key("shield_module"));

        shaped(RecipeCategory.MISC, NTItems.IMPULSE_LASER_MODULE.get(), 1)
                .pattern("LYL")
                .pattern("KCK")
                .pattern("ABA")
                .define('L', NTItems.PRISMARINE_LENS.get())
                .define('Y', NTItems.GUARDIAN_EYE.get())
                .define('K', NTItems.LASER_CHANNELING_COIL.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('B', NTItems.PRISMATIC_BATTERY.get())
                .unlockedBy("has_item", has(NTItems.GUARDIAN_EYE))
                .save(pRecipeOutput, key("impulse_laser_module"));

        shaped(RecipeCategory.MISC, NTItems.TELEPORT_MODULE.get(), 1)
                .pattern("PXP")
                .pattern("XHX")
                .pattern("ACA")
                .define('P', Tags.Items.ENDER_PEARLS)
                .define('X', Items.CHORUS_FRUIT)
                .define('H', NTItems.ELDRITCH_HEART.get())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.AQUATIC_CHIP.get())
                .unlockedBy("has_item", has(NTItems.ELDRITCH_HEART))
                .save(pRecipeOutput, key("teleport_module"));

        shaped(RecipeCategory.MISC, NTItems.FLIGHT_MODULE.get(), 1)
                .pattern("MFM")
                .pattern("DPD")
                .pattern("CRC")
                .define('M', Items.PHANTOM_MEMBRANE)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get())
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('P', NTItems.DEEP_STEEL_PLATING.get())
                .define('C', Items.CHORUS_FRUIT)
                .define('R', NTItems.RESONANT_SHARD.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("flight_module"));

        shaped(RecipeCategory.MISC, NTItems.CARGO_MODULE.get(), 1)
                .pattern("ABA")
                .pattern("CPC")
                .pattern("AVA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('B', Tags.Items.BARRELS_WOODEN)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .define('P', NTItems.DEEP_STEEL_PLATING.get())
                .define('V', NTItems.VALVE.get())
                .unlockedBy("has_item", has(NTItems.SUBMARINE))
                .save(pRecipeOutput, key("cargo_module"));
    }

    private void miscItemsRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shapeless(RecipeCategory.MISC, NTItems.BROWN_POLYMER.get(), 2)
                .requires(Items.DRIED_KELP)
                .requires(Items.BROWN_DYE)
                .unlockedBy("has_item", has(Items.DRIED_KELP))
                .save(pRecipeOutput, key("brown_polymer"));

        shapeless(RecipeCategory.MISC, NTItems.AQUARINE_STEEL_COMPOUND.get(), 2)
                .requires(Tags.Items.RAW_MATERIALS_IRON)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .unlockedBy("has_item", has(Tags.Items.GEMS_PRISMARINE))
                .save(pRecipeOutput, key("aquarine_steel_compound"));

        shapeless(RecipeCategory.MISC, NTItems.CAST_IRON_COMPOUND.get(), 2)
                .requires(Tags.Items.RAW_MATERIALS_IRON)
                .requires(ItemTags.COALS)
                .requires(ItemTags.COALS)
                .unlockedBy("has_raw_rion", has(Tags.Items.RAW_MATERIALS_IRON))
                .save(pRecipeOutput, key("cast_iron_compound"));

        shaped(RecipeCategory.MISC, NTItems.PRISMARINE_LENS.get())
                .pattern(" A ")
                .pattern("AGA")
                .pattern(" A ")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("prismarine_lens"));

        shaped(RecipeCategory.MISC, NTItems.PETRI_DISH.get())
                .pattern("G G")
                .pattern("GGG")
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_item", has(Tags.Items.GLASS_PANES_COLORLESS))
                .save(pRecipeOutput, key("petri_dish"));
    }

    private void castIronRecipes(@NotNull RecipeOutput pRecipeOutput) {

        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, NTItems.CAST_IRON_NUGGET, RecipeCategory.MISC, NTItems.CAST_IRON_INGOT,
                Nautec.MODID + ":cast_iron_ingot_from_nuggets", null, Nautec.MODID + ":nuggets_from_cast_iron_ingot", null);
        nineBlockStorageRecipes(pRecipeOutput, RecipeCategory.MISC, NTItems.CAST_IRON_INGOT, RecipeCategory.BUILDING_BLOCKS, NTBlocks.CAST_IRON_BLOCK,
                Nautec.MODID + ":cast_iron_block_from_ingots", null, Nautec.MODID + ":ingots_from_cast_iron_block", null);

        shaped(RecipeCategory.MISC, NTItems.CAST_IRON_ROD.get(), 4)
                .pattern("C")
                .pattern("C")
                .define('C', NTItems.CAST_IRON_INGOT.asItem())
                .unlockedBy("has_item", has(Items.DEEPSLATE))
                .save(pRecipeOutput, key("cast_iron_rod"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.OIL_BARREL.asItem())
                .pattern("CRC")
                .pattern("C C")
                .pattern("CRC")
                .define('C', NTItems.CAST_IRON_INGOT)
                .define('R', NTItems.CAST_IRON_ROD)
                .unlockedBy("has_item", has(NTItems.CAST_IRON_INGOT))
                .save(pRecipeOutput, key("oil_barrel"));
    }

    private void buildingBlockRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.asItem(), 2)
                .pattern("APA")
                .pattern("PCP")
                .pattern("APA")
                .define('P', NTBlocks.POLISHED_PRISMARINE.asItem())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('C', Tags.Items.GEMS_PRISMARINE)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT.asItem()))
                .save(pRecipeOutput, key("bacteria_containment_shield_from_prismarine_crystals"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.asItem(), 4)
                .pattern("APA")
                .pattern("PCP")
                .pattern("APA")
                .define('P', NTBlocks.POLISHED_PRISMARINE.asItem())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.asItem())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD.asItem()))
                .save(pRecipeOutput, key("bacteria_containment_shield_from_prismarine_crystal_shard"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.CHISELED_DARK_PRISMARINE.asItem(), 4)
                .pattern("DD")
                .pattern("DD")
                .define('D', Blocks.DARK_PRISMARINE.asItem())
                .unlockedBy("has_item", has(Blocks.DARK_PRISMARINE))
                .save(pRecipeOutput, key("chiseled_dark_prismarine"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.POLISHED_PRISMARINE.asItem(), 4)
                .pattern("DD")
                .pattern("DD")
                .define('D', Blocks.PRISMARINE.asItem())
                .unlockedBy("has_item", has(Blocks.DARK_PRISMARINE))
                .save(pRecipeOutput, key("polished_prismarine"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.DARK_PRISMARINE_PILLAR.asItem(), 2)
                .pattern("D")
                .pattern("D")
                .define('D', Blocks.DARK_PRISMARINE.asItem())
                .unlockedBy("has_item", has(Blocks.DARK_PRISMARINE))
                .save(pRecipeOutput, key("dark_prismarine_pillar"));
    }

    private void brownPolymerRecipes(RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.BROWN_POLYMER_BLOCK)
                .pattern("BB")
                .pattern("BB")
                .define('B', NTItems.BROWN_POLYMER.asItem())
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput);

        shapeless(RecipeCategory.BUILDING_BLOCKS, NTItems.BROWN_POLYMER, 4)
                .requires(NTBlocks.BROWN_POLYMER_BLOCK)
                .unlockedBy("has_item", has(NTBlocks.BROWN_POLYMER_BLOCK))
                .save(pRecipeOutput, key("brown_polymer_from_block"));

        shapeless(RecipeCategory.MISC, Items.BOOK, 2)
                .requires(NTItems.BROWN_POLYMER)
                .requires(Items.PAPER, 3)
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput, key("book_from_brown_polymer"));

        shaped(RecipeCategory.MISC, Blocks.BROWN_BANNER, 2)
                .pattern("BBB")
                .pattern("BBB")
                .pattern(" S ")
                .define('B', NTItems.BROWN_POLYMER.get())
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER.get()))
                .save(pRecipeOutput, key("banner_from_brown_polymer"));

        shaped(RecipeCategory.MISC, Items.ITEM_FRAME, 2)
                .pattern("SSS")
                .pattern("SBS")
                .pattern("SSS")
                .define('B', NTItems.BROWN_POLYMER.get())
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER.get()))
                .save(pRecipeOutput, key("item_frame_from_brown_polymer"));

        shaped(RecipeCategory.MISC, Blocks.BROWN_BED)
                .pattern("BBB")
                .pattern("PPP")
                .define('B', NTItems.BROWN_POLYMER.get())
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER.get()))
                .save(pRecipeOutput, key("bed_from_brown_polymer"));
    }

    private void aquarineSteelArmorRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.AQUARINE_HELMET.get())
                .pattern("ICI")
                .pattern("I I")
                .define('I', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD.get()))
                .save(pRecipeOutput, key("aquarine_helmet"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_CHESTPLATE.get())
                .pattern("I I")
                .pattern("ICI")
                .pattern("IVI")
                .define('I', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .define('V', NTItems.VALVE.get())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD.get()))
                .save(pRecipeOutput, key("aquarine_chestplate"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_LEGGINGS.get())
                .pattern("IVI")
                .pattern("C C")
                .pattern("I I")
                .define('I', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .define('V', NTItems.VALVE.get())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD.get()))
                .save(pRecipeOutput, key("aquarine_leggings"));

        shaped(RecipeCategory.MISC, NTItems.AQUARINE_BOOTS.get())
                .pattern("C C")
                .pattern("I I")
                .define('I', NTItems.AQUARINE_STEEL_INGOT.get())
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.get())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD.get()))
                .save(pRecipeOutput, key("aquarine_boots"));
    }

    private void divingArmorRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.DIVING_HELMET.get())
                .pattern("CCC")
                .pattern("CGC")
                .define('C', Tags.Items.INGOTS_COPPER)
                .define('G', Tags.Items.GLASS_PANES_COLORLESS)
                .unlockedBy("has_item", has(Tags.Items.INGOTS_COPPER))
                .save(pRecipeOutput, key("diving_helmet"));

        shaped(RecipeCategory.MISC, NTItems.DIVING_CHESTPLATE.get())
                .pattern("C C")
                .pattern("BCB")
                .pattern("BBB")
                .define('B', NTItems.BROWN_POLYMER.get())
                .define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput, key("diving_chestplate"));

        shaped(RecipeCategory.MISC, NTItems.DIVING_LEGGINGS.get())
                .pattern("BBB")
                .pattern("B B")
                .pattern("B B")
                .define('B', NTItems.BROWN_POLYMER.get())
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput, key("diving_leggings"));

        shaped(RecipeCategory.MISC, NTItems.DIVING_BOOTS.get())
                .pattern("B B")
                .pattern("B B")
                .define('B', NTItems.BROWN_POLYMER.get())
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput, key("diving_boots"));

        ItemStackTemplate divingChestplate = new ItemStackTemplate(NTItems.DIVING_CHESTPLATE.get(), 1,
                DataComponentPatch.builder().set(NTDataComponents.OXYGEN.get(), 600).build());

        shaped(RecipeCategory.MISC, divingChestplate)
                .pattern("GGG")
                .pattern("GDG")
                .pattern("GGG")
                .unlockedBy("has_item", has(NTItems.DIVING_CHESTPLATE.get()))
                .define('G', NTItems.AIR_BOTTLE.get())
                .define('D', NTItems.DIVING_CHESTPLATE.get())
                .save(pRecipeOutput, key("diving_chestplate_oxygen"));

        ItemEtchingRecipeBuilder.newRecipe(new ItemStackTemplate(NTBlocks.CRATE.asItem()))
                .ingredient(NTBlocks.RUSTY_CRATE.asItem())
                .duration(200)
                .save(pRecipeOutput, key("crate"));

        shapeless(RecipeCategory.MISC, NTFluids.ETCHING_ACID.getBucket())
                .requires(Items.POISONOUS_POTATO)
                .requires(Tags.Items.GUNPOWDERS)
                .requires(Items.BONE_MEAL)
                .requires(Items.SNOW_BLOCK)
                .requires(Items.PUFFERFISH)
                .requires(Tags.Items.BUCKETS_EMPTY)
                .unlockedBy("has_item", has(Items.POISONOUS_POTATO))
                .save(pRecipeOutput, key("etching_acid_crafting"));
    }

    private void chemistryRecipes(@NotNull RecipeOutput pRecipeOutput) {
        MixingRecipeBuilder.newRecipe()
                .ingredients(iwcFromItemLike(Items.DRIED_KELP, 4),
                        iwcFromTag(Tags.Items.SLIME_BALLS, 2),
                        iwcFromTag(Tags.Items.GEMS_PRISMARINE, 1),
                        iwcFromItemLike(Items.SEAGRASS, 5))
                .duration(200)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(new FluidStackTemplate(NTFluids.EAS.getStillFluid(), 1000))
                .save(pRecipeOutput, key("eas"));

        MixingRecipeBuilder.newRecipe()
                .ingredients(iwcFromItemLike(Items.PUFFERFISH, 1),
                        iwcFromTag(Tags.Items.GUNPOWDERS, 1),
                        iwcFromItemLike(Items.BONE_MEAL, 1))
                .duration(150)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(new FluidStackTemplate(NTFluids.ETCHING_ACID.getStillFluid(), 1000))
                .save(pRecipeOutput, key("etching_acid_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUARINE_STEEL_COMPOUND.get(), 5))
                .ingredients(iwcFromTag(Tags.Items.RAW_MATERIALS_IRON, 2),
                        iwcFromTag(Tags.Items.GEMS_PRISMARINE, 1))
                .duration(100)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(pRecipeOutput, key("aquarine_steel_compound_mixing"));

        MixingRecipeBuilder.newRecipe()
                .ingredients(new IngredientWithCount(tag(NTTags.Items.DUSTS_SALT), 1))
                .duration(100)
                .fluidIngredient(new FluidStackTemplate(Fluids.WATER, 1000))
                .fluidResult(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .save(pRecipeOutput, key("saltwater_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.KELP_SLURRY.get(), 2))
                .ingredients(iwcFromItemLike(Items.KELP, 4))
                .duration(100)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(pRecipeOutput, key("kelp_slurry_mixing"));

        MixingRecipeBuilder.newRecipe()
                .ingredients(iwcFromItemLike(NTItems.ALGAL_LIPID.get(), 1))
                .duration(100)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 250))
                .fluidResult(new FluidStackTemplate(NTFluids.OIL.getStillFluid(), 1000))
                .save(pRecipeOutput, key("oil_from_algal_lipid_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.PRESSURE_SYNTHESIZER.get(), 1))
                .ingredients(iwcFromItemLike(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get(), 1))
                .duration(400)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 8000))
                .fluidResult(null)
                .save(pRecipeOutput, key("pressure_synthesizer_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.ATLANTEAN_PRESSURE_SYNTHESIZER.get(), 1))
                .ingredients(iwcFromItemLike(NTItems.PRESSURE_SYNTHESIZER.get(), 1),
                        iwcFromItemLike(Items.HEART_OF_THE_SEA, 1),
                        iwcFromTag(Tags.Items.NETHER_STARS, 1),
                        iwcFromTag(Tags.Items.ENDER_PEARLS, 4))
                .duration(600)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 8000))
                .fluidResult(null)
                .save(pRecipeOutput, key("atlantean_pressure_synthesizer_mixing"));
    }

    private void fusionPlantRecipes(@NotNull RecipeOutput pRecipeOutput) {

        shaped(RecipeCategory.MISC, NTBlocks.RESONANCE_NODE.asItem(), 2)
                .pattern(" S ")
                .pattern("ARA")
                .pattern(" C ")
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('R', NTItems.RESONANT_SHARD)
                .define('C', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.PRISM_SATELLITE))
                .save(pRecipeOutput, key("resonance_node"));

        shaped(RecipeCategory.MISC, NTBlocks.CONDUIT_BEACON.asItem())
                .pattern("SAS")
                .pattern("ACA")
                .pattern("SAS")
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('C', Items.CONDUIT)
                .unlockedBy("has_item", has(Items.CONDUIT))
                .save(pRecipeOutput, key("conduit_beacon"));

        shaped(RecipeCategory.MISC, NTBlocks.AQUARINE_DISH_STORAGE.asItem())
                .pattern("ADA")
                .pattern("DCD")
                .pattern("ADA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('D', NTItems.PETRI_DISH)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .unlockedBy("has_item", has(NTItems.PETRI_DISH))
                .save(pRecipeOutput, key("aquarine_dish_storage"));

        shaped(RecipeCategory.MISC, NTBlocks.DEEP_STEEL_DISH_STORAGE.asItem())
                .pattern("PCP")
                .pattern("PSP")
                .pattern("PCP")
                .define('P', NTItems.DEEP_STEEL_PLATING)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('S', NTBlocks.AQUARINE_DISH_STORAGE)
                .unlockedBy("has_item", has(NTBlocks.AQUARINE_DISH_STORAGE))
                .save(pRecipeOutput, key("deep_steel_dish_storage"));

        shaped(RecipeCategory.MISC, NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.asItem())
                .pattern("GFG")
                .pattern("GSG")
                .pattern("GFG")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('S', NTBlocks.DEEP_STEEL_DISH_STORAGE)
                .unlockedBy("has_item", has(NTBlocks.DEEP_STEEL_DISH_STORAGE))
                .save(pRecipeOutput, key("atlantic_gold_dish_storage"));

        shaped(RecipeCategory.MISC, NTItems.GRAFTING_ANCHOR.get())
                .pattern(" R ")
                .pattern("DED")
                .pattern(" T ")
                .define('R', NTItems.RESONANT_SHARD)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('E', Items.END_CRYSTAL)
                .define('T', NTItems.GRAFTING_TOOL)
                .unlockedBy("has_item", has(NTBlocks.GRAFTING_STATION))
                .save(pRecipeOutput, key("grafting_anchor"));

        shaped(RecipeCategory.MISC, NTItems.ADVANCED_GRAFTING_ANCHOR.get())
                .pattern(" S ")
                .pattern("SAS")
                .pattern(" S ")
                .define('S', NTItems.REACTOR_SPEED_UPGRADE)
                .define('A', NTItems.GRAFTING_ANCHOR)
                .unlockedBy("has_item", has(NTItems.GRAFTING_ANCHOR))
                .save(pRecipeOutput, key("advanced_grafting_anchor"));

        shaped(RecipeCategory.MISC, NTBlocks.BIOME_TANK.asItem())
                .pattern("DGD")
                .pattern("G G")
                .pattern("GOG")
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('G', Tags.Items.GLASS_PANES)
                .define('O', Tags.Items.OBSIDIANS)
                .unlockedBy("has_item", has(NTItems.DEEP_STEEL_PLATING))
                .save(pRecipeOutput, key("biome_tank"));

        shaped(RecipeCategory.MISC, NTBlocks.PRISMATIC_EMITTER.asItem())
                .pattern(" S ")
                .pattern("CLC")
                .pattern("ARA")
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('L', NTItems.PRISMARINE_LENS)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("prismatic_emitter"));

        shaped(RecipeCategory.TOOLS, NTItems.RESONANCE_CHARM.get())
                .pattern(" S ")
                .pattern("ERE")
                .pattern(" G ")
                .define('S', Tags.Items.STRINGS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('R', NTItems.RESONANT_SHARD)
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("resonance_charm"));

        shaped(RecipeCategory.MISC, NTItems.PRISM_SATELLITE.get())
                .pattern(" A ")
                .pattern("CFC")
                .pattern("GLG")
                .define('A', Items.LIGHTNING_ROD)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('L', NTItems.PRISMARINE_LENS)
                .unlockedBy("has_item", has(NTBlocks.UPLINK_ARRAY))
                .save(pRecipeOutput, key("prism_satellite"));

        shaped(RecipeCategory.MISC, NTBlocks.UPLINK_ARRAY.asItem())
                .pattern("GLG")
                .pattern("CRC")
                .pattern("AJA")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('L', NTItems.PRISMARINE_LENS)
                .define('C', NTItems.AQUATIC_CHIP)
                .define('R', NTItems.RESONANT_SHARD)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('J', NTBlocks.LASER_JUNCTION)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("uplink_array"));

        shapeless(RecipeCategory.TOOLS, NTItems.CONFIGURATION_CARD.get(), 1)
                .requires(Items.PAPER)
                .requires(NTItems.AQUATIC_CHIP.get())
                .requires(Tags.Items.GEMS_PRISMARINE)
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP.get()))
                .save(pRecipeOutput, key("configuration_card"));

        shaped(RecipeCategory.TOOLS, NTItems.TUNING_FORK.get())
                .pattern("A A")
                .pattern(" A ")
                .pattern(" S ")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("tuning_fork"));

        shaped(RecipeCategory.MISC, NTBlocks.TIDAL_ROTOR.asItem())
                .pattern("RIR")
                .pattern("ICI")
                .pattern("RXR")
                .define('R', NTItems.CAST_IRON_ROD)
                .define('I', NTItems.CAST_IRON_INGOT)
                .define('C', Tags.Items.STORAGE_BLOCKS_COPPER)
                .define('X', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_item", has(NTItems.CAST_IRON_INGOT))
                .save(pRecipeOutput, key("tidal_rotor"));

        shaped(RecipeCategory.MISC, NTBlocks.THERMAL_VENT_TAP.asItem())
                .pattern("AVA")
                .pattern("MCM")
                .pattern("AHA")
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('V', NTItems.VALVE)
                .define('M', Items.MAGMA_BLOCK)
                .define('C', NTBlocks.CAST_IRON_BLOCK)
                .define('H', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("thermal_vent_tap"));

        shaped(RecipeCategory.MISC, NTBlocks.COMBUSTION_DYNAMO.asItem())
                .pattern("GTG")
                .pattern("PCP")
                .pattern("AHA")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('T', NTBlocks.OIL_BARREL)
                .define('P', Items.PISTON)
                .define('C', NTBlocks.CAST_IRON_BLOCK)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('H', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.AQUARINE_STEEL_INGOT))
                .save(pRecipeOutput, key("combustion_dynamo"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.FUSION_CASING.asItem(), 8)
                .pattern("DAD")
                .pattern("AGA")
                .pattern("DAD")
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .unlockedBy("has_item", has(NTItems.DEEP_STEEL_PLATING))
                .save(pRecipeOutput, key("fusion_casing"));

        shaped(RecipeCategory.BUILDING_BLOCKS, NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.asItem(), 6)
                .pattern("GAG")
                .pattern("GSG")
                .pattern("GAG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("aquamarine_structural_glass"));

        shaped(RecipeCategory.MISC, NTBlocks.CONTAINMENT_COIL.asItem())
                .pattern("ELE")
                .pattern("GCG")
                .pattern("ELE")
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('L', NTItems.LASER_CHANNELING_COIL)
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .define('C', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTBlocks.FUSION_CASING))
                .save(pRecipeOutput, key("containment_coil"));

        shaped(RecipeCategory.MISC, NTBlocks.LASER_INJECTOR.asItem())
                .pattern("PRP")
                .pattern("ELE")
                .pattern("PFP")
                .define('P', NTItems.DEEP_STEEL_PLATING)
                .define('R', NTItems.RESONANT_SHARD)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('L', NTItems.PRISMARINE_LENS)
                .define('F', NTBlocks.FOCUSING_LENS)
                .unlockedBy("has_item", has(NTBlocks.FUSION_CASING))
                .save(pRecipeOutput, key("laser_injector"));

        shaped(RecipeCategory.MISC, NTBlocks.FUSION_COLLECTOR.asItem())
                .pattern("PLP")
                .pattern("ECE")
                .pattern("PGP")
                .define('P', NTItems.DEEP_STEEL_PLATING)
                .define('L', NTItems.PRISMARINE_LENS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('C', NTItems.LASER_CHANNELING_COIL)
                .define('G', NTItems.ATLANTIC_GOLD_INGOT)
                .unlockedBy("has_item", has(NTBlocks.FUSION_CASING))
                .save(pRecipeOutput, key("fusion_collector"));

        shaped(RecipeCategory.MISC, NTBlocks.FUSION_PORT.asItem())
                .pattern(" A ")
                .pattern("VCV")
                .pattern(" A ")
                .define('A', NTItems.AQUATIC_CHIP)
                .define('V', NTItems.VALVE)
                .define('C', NTBlocks.FUSION_CASING)
                .unlockedBy("has_item", has(NTBlocks.FUSION_CASING))
                .save(pRecipeOutput, key("fusion_port"));

        shaped(RecipeCategory.MISC, NTBlocks.FUSION_CONTROLLER.asItem())
                .pattern("FNF")
                .pattern("ECE")
                .pattern("PAP")
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('N', Tags.Items.NETHER_STARS)
                .define('E', Tags.Items.ENDER_PEARLS)
                .define('C', NTBlocks.FUSION_CASING)
                .define('P', NTItems.DEEP_STEEL_PLATING)
                .define('A', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTBlocks.FUSION_CASING))
                .save(pRecipeOutput, key("fusion_controller"));
    }

    private void guaranteedPartRecipes(@NotNull RecipeOutput pRecipeOutput) {
        ItemTransformationRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.GEAR.get(), 4))
                .ingredient(NTBlocks.CAST_IRON_BLOCK.get())
                .purity(2.5f)
                .duration(160)
                .save(pRecipeOutput, key("gear_from_cast_iron_block"));

        shaped(RecipeCategory.MISC, NTItems.VALVE.get())
                .pattern(" R ")
                .pattern("CGC")
                .pattern(" S ")
                .define('R', NTItems.CAST_IRON_ROD)
                .define('C', NTItems.CAST_IRON_INGOT)
                .define('G', NTItems.GEAR)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .unlockedBy("has_item", has(NTItems.GEAR))
                .save(pRecipeOutput, key("valve_from_cast_iron"));

        shaped(RecipeCategory.MISC, NTItems.WHISK.get())
                .pattern("R R")
                .pattern("RSR")
                .pattern(" A ")
                .define('R', NTItems.CAST_IRON_ROD)
                .define('S', NTItems.PRISMARINE_CRYSTAL_SHARD)
                .define('A', NTItems.AQUARINE_STEEL_INGOT)
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("whisk_from_cast_iron"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.BURNT_COIL.get(), 1))
                .ingredients(iwcFromTag(Tags.Items.INGOTS_COPPER, 4),
                        iwcFromTag(Tags.Items.DUSTS_REDSTONE, 2),
                        iwcFromItemLike(NTItems.AQUARINE_STEEL_INGOT.get(), 1),
                        iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 1))
                .duration(200)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(pRecipeOutput, key("burnt_coil_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.AQUATIC_CHIP.get(), 2))
                .ingredients(iwcFromTag(Tags.Items.INGOTS_GOLD, 2),
                        iwcFromTag(Tags.Items.DUSTS_REDSTONE, 4),
                        iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 2),
                        iwcFromItemLike(NTItems.LASER_CHANNELING_COIL.get(), 1))
                .duration(200)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(pRecipeOutput, key("aquatic_chip_mixing"));

        PressureForgingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.ATLANTIC_GOLD_INGOT.get(), 2))
                .ingredient(tag(Tags.Items.STORAGE_BLOCKS_GOLD))
                .minDepth(-20)
                .purity(2.0f)
                .duration(300)
                .save(pRecipeOutput, key("atlantic_gold_ingot_forging"));
    }

    private void augmentationRecipes(@NotNull RecipeOutput pRecipeOutput) {
        AugmentationRecipeBuilder.newRecipe(NTAugments.DOLPHIN_FIN.get())
                .augmentItem(NTItems.DOLPHIN_FIN.get(), "Greatly improved swimming speed")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.DOLPHIN_FIN.get()))
                .save(pRecipeOutput, key("dolphin_fin"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.DROWNED_LUNG.get())
                .augmentItem(NTItems.DROWNED_LUNGS.get(), "Unlimited underwater breathing")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.DROWNED_LUNGS.get()))
                .save(pRecipeOutput, key("drowned_lung"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.GUARDIAN_EYE.get())
                .augmentItem(NTItems.GUARDIAN_EYE.get(), "Shoots lasers at enemies you are looking at")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.GUARDIAN_EYE.get()))
                .save(pRecipeOutput, key("guardian_eye"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.ELDRITCH_HEART.get())
                .augmentItem(NTItems.ELDRITCH_HEART.get(), "Increased health regeneration when underwater")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.ELDRITCH_HEART.get()))
                .save(pRecipeOutput, key("eldritch_heart"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.LEAP_AUGMENT.get())
                .augmentItem(NTItems.HYDRAULIC_LEG.get(), "Launches you in the direction you are looking")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.HYDRAULIC_LEG.get()))
                .save(pRecipeOutput, key("leap"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.STEP_UP_AUGMENT.get())
                .augmentItem(NTItems.SERVO_KNEE.get(), "Step up full blocks without jumping")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.SERVO_KNEE.get()))
                .save(pRecipeOutput, key("step_up"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.PREVENT_FALL_DAMAGE_AUGMENT.get())
                .augmentItem(NTItems.SHOCK_ABSORBER.get(), "Negates all fall damage")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.SHOCK_ABSORBER.get()))
                .save(pRecipeOutput, key("prevent_fall_damage"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.WALKING_SPEED_AUGMENT.get())
                .augmentItem(NTItems.TENDON_WEAVE.get(), "Greatly increased walking speed")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.TENDON_WEAVE.get()))
                .save(pRecipeOutput, key("walking_speed"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.MAGNET_AUGMENT.get())
                .augmentItem(NTItems.MAGNETIC_COIL_ARM.get(), "Pulls nearby items towards you, hold crouch to suspend it")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.MAGNETIC_COIL_ARM.get()))
                .save(pRecipeOutput, key("magnet"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.ENDER_MAGNET_AUGMENT.get())
                .augmentItem(NTItems.ENDER_COIL_ARM.get(), "Teleports nearby items straight into your inventory, hold crouch to suspend it")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.ENDER_COIL_ARM.get()))
                .save(pRecipeOutput, key("ender_magnet"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.UNDERWATER_MINING_SPEED_AUGMENT.get())
                .augmentItem(NTItems.HYDRO_DRILL_ARM.get(), "Mine underwater as fast as you would on land")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.HYDRO_DRILL_ARM.get()))
                .save(pRecipeOutput, key("underwater_mining_speed"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.THROWN_BOUNCING_TRIDENT_AUGMENT.get())
                .augmentItem(NTItems.TRIDENT_LAUNCHER_ARM.get(), "Throws a trident that ricochets between targets")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.TRIDENT_LAUNCHER_ARM.get()))
                .save(pRecipeOutput, key("throw_bouncing_trident"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.SPREADING_TRIDENT_AUGMENT.get())
                .augmentItem(NTItems.VOLLEY_TRIDENT_ARM.get(), "Throws a spreading volley of tridents")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.VOLLEY_TRIDENT_ARM.get()))
                .save(pRecipeOutput, key("spreading_trident"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.THROW_POTION_AUGMENT.get())
                .augmentItem(NTItems.SYRINGE_ROBOT_ARM.get(), "Throws a randomly brewed splash potion")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.SYRINGE_ROBOT_ARM.get()))
                .save(pRecipeOutput, key("throw_random_potion"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.CREATIVE_FLIGHT_AUGMENT.get())
                .augmentItem(NTItems.BUOYANCY_TANK.get(), "Lets you fly freely, in water and out of it")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.BUOYANCY_TANK.get()))
                .save(pRecipeOutput, key("creative_flight"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.BONUS_HEART_AUGMENT.get())
                .augmentItem(NTItems.AUXILIARY_VENTRICLE.get(), "Doubles your maximum health")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.AUXILIARY_VENTRICLE.get()))
                .save(pRecipeOutput, key("bonus_hearts"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.ABYSSAL_EYES.get())
                .augmentItem(NTItems.ABYSSAL_ORGAN.get(), "Grants night vision in the deep")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.ABYSSAL_ORGAN.get()))
                .save(pRecipeOutput, key("abyssal_eyes"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.PHOTOPHORE_SKIN.get())
                .augmentItem(NTItems.LUMINOUS_MEMBRANE.get(), "Lights up nearby creatures while you are in water")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.LUMINOUS_MEMBRANE.get()))
                .save(pRecipeOutput, key("photophore_skin"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.VENT_CARAPACE.get())
                .augmentItem(NTItems.CHITIN_PLATE.get(), "Armour plating that shrugs off knockback and burns out fast")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.CHITIN_PLATE.get()),
                        IngredientWithCount.fromItemLike(NTItems.CHITIN_PLATE.get()),
                        IngredientWithCount.fromItemLike(NTItems.CHITIN_PLATE.get()),
                        IngredientWithCount.fromItemLike(NTItems.CHITIN_PLATE.get()))
                .save(pRecipeOutput, key("vent_carapace"));

        AugmentationRecipeBuilder.newRecipe(NTAugments.RESONANCE.get())
                .augmentItem(NTItems.RESONANCE_CHARM.get(), "Charges your items from a resonance network without a charm slot")
                .ingredients(IngredientWithCount.fromItemLike(NTItems.RESONANCE_CHARM.get()))
                .save(pRecipeOutput, key("resonance_augment"));
    }

    private void nautecFishingRodRecipe(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.TOOLS, NTItems.NAUTEC_FISHING_ROD.get())
                .pattern("  A")
                .pattern(" AL")
                .pattern("A C")
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('L', Tags.Items.STRINGS)
                .define('C', NTItems.PRISMARINE_CRYSTAL_SHARD.asItem())
                .unlockedBy("has_item", has(NTItems.PRISMARINE_CRYSTAL_SHARD))
                .save(pRecipeOutput, key("nautec_fishing_rod"));
    }

    private void augmentPartRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.HYDRAULIC_LEG.get())
                .pattern(" R ")
                .pattern("RPR")
                .pattern("ACA")
                .define('R', NTItems.CAST_IRON_ROD.asItem())
                .define('P', Items.PISTON)
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("hydraulic_leg"));

        shaped(RecipeCategory.MISC, NTItems.SERVO_KNEE.get())
                .pattern(" G ")
                .pattern("RAR")
                .pattern(" G ")
                .define('G', NTItems.GEAR.asItem())
                .define('R', NTItems.CAST_IRON_ROD.asItem())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .unlockedBy("has_item", has(NTItems.GEAR))
                .save(pRecipeOutput, key("servo_knee"));

        shaped(RecipeCategory.MISC, NTItems.SHOCK_ABSORBER.get())
                .pattern(" S ")
                .pattern("BIB")
                .pattern(" R ")
                .define('S', Tags.Items.SLIME_BALLS)
                .define('B', NTItems.BROWN_POLYMER.asItem())
                .define('I', NTItems.CAST_IRON_INGOT.asItem())
                .define('R', NTItems.CAST_IRON_ROD.asItem())
                .unlockedBy("has_item", has(NTItems.BROWN_POLYMER))
                .save(pRecipeOutput, key("shock_absorber"));

        shaped(RecipeCategory.MISC, NTItems.TENDON_WEAVE.get())
                .pattern("TBT")
                .pattern("BCB")
                .pattern("TBT")
                .define('T', Tags.Items.STRINGS)
                .define('B', NTItems.BROWN_POLYMER.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("tendon_weave"));

        shaped(RecipeCategory.MISC, NTItems.MAGNETIC_COIL_ARM.get())
                .pattern("III")
                .pattern("LCL")
                .pattern(" A ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('L', NTItems.LASER_CHANNELING_COIL.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .unlockedBy("has_item", has(NTItems.LASER_CHANNELING_COIL))
                .save(pRecipeOutput, key("magnetic_coil_arm"));

        shaped(RecipeCategory.MISC, NTItems.ENDER_COIL_ARM.get())
                .pattern(" E ")
                .pattern("PMP")
                .pattern(" E ")
                .define('E', Items.ENDER_EYE)
                .define('P', Tags.Items.ENDER_PEARLS)
                .define('M', NTItems.MAGNETIC_COIL_ARM.asItem())
                .unlockedBy("has_item", has(NTItems.MAGNETIC_COIL_ARM))
                .save(pRecipeOutput, key("ender_coil_arm"));

        shaped(RecipeCategory.MISC, NTItems.HYDRO_DRILL_ARM.get())
                .pattern("SAS")
                .pattern("ACA")
                .pattern(" G ")
                .define('S', Items.PRISMARINE_SHARD)
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .define('G', NTItems.GEAR.asItem())
                .unlockedBy("has_item", has(NTItems.AQUATIC_CHIP))
                .save(pRecipeOutput, key("hydro_drill_arm"));

        shaped(RecipeCategory.MISC, NTItems.TRIDENT_LAUNCHER_ARM.get())
                .pattern(" T ")
                .pattern("ALA")
                .pattern(" C ")
                .define('T', Items.TRIDENT)
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('L', NTItems.LASER_CHANNELING_COIL.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(Items.TRIDENT))
                .save(pRecipeOutput, key("trident_launcher_arm"));

        shaped(RecipeCategory.MISC, NTItems.VOLLEY_TRIDENT_ARM.get())
                .pattern(" T ")
                .pattern("NLN")
                .pattern(" C ")
                .define('T', Items.TRIDENT)
                .define('N', Items.NAUTILUS_SHELL)
                .define('L', NTItems.TRIDENT_LAUNCHER_ARM.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(NTItems.TRIDENT_LAUNCHER_ARM))
                .save(pRecipeOutput, key("volley_trident_arm"));

        shaped(RecipeCategory.MISC, NTItems.SYRINGE_ROBOT_ARM.get())
                .pattern(" V ")
                .pattern("AEA")
                .pattern(" C ")
                .define('V', NTItems.GLASS_VIAL.asItem())
                .define('E', NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.asItem())
                .define('A', NTItems.AQUARINE_STEEL_INGOT.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL))
                .save(pRecipeOutput, key("syringe_robot_arm"));

        shapeless(RecipeCategory.MISC, NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.get(), 3)
                .requires(NTFluids.EAS.getBucket())
                .requires(NTItems.GLASS_VIAL.get(), 3)
                .unlockedBy("has_item", has(NTFluids.EAS.getBucket()))
                .save(pRecipeOutput, key("eas_vial"));

        shaped(RecipeCategory.MISC, NTItems.BUOYANCY_TANK.get())
                .pattern("PBP")
                .pattern("BHB")
                .pattern("PBP")
                .define('P', Items.PHANTOM_MEMBRANE)
                .define('B', NTItems.BROWN_POLYMER.asItem())
                .define('H', Items.HEART_OF_THE_SEA)
                .unlockedBy("has_item", has(Items.HEART_OF_THE_SEA))
                .save(pRecipeOutput, key("buoyancy_tank"));

        shaped(RecipeCategory.MISC, NTItems.AUXILIARY_VENTRICLE.get())
                .pattern(" G ")
                .pattern("GEG")
                .pattern(" C ")
                .define('G', NTItems.ATLANTIC_GOLD_INGOT.asItem())
                .define('E', NTItems.ELDRITCH_HEART.asItem())
                .define('C', NTItems.AQUATIC_CHIP.asItem())
                .unlockedBy("has_item", has(NTItems.ELDRITCH_HEART))
                .save(pRecipeOutput, key("auxiliary_ventricle"));
    }

    private void mutationRecipes(RecipeOutput output) {
        new MutationRecipeBuilder(NTBacterias.THERMOPHILES, NTBacterias.LITHOPHILES, Ingredient.of(Items.STONE), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LITHOPHILES, NTBacterias.CARBOPHAGES, Ingredient.of(Items.COAL), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LITHOPHILES, NTBacterias.SILICOPHILES, Ingredient.of(Items.SAND), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LITHOPHILES, NTBacterias.CALCIOPHILES, Ingredient.of(Items.BONE_MEAL), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.CARBOPHAGES, NTBacterias.METALLOPHILES, Ingredient.of(Items.COPPER_INGOT), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METALLOPHILES, NTBacterias.ACIDOPHILES, Ingredient.of(Items.REDSTONE), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.ACIDOPHILES, NTBacterias.SULFUROPHILES, Ingredient.of(Items.GUNPOWDER), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METALLOPHILES, NTBacterias.AZURITOPHILES, Ingredient.of(Items.LAPIS_LAZULI), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METALLOPHILES, NTBacterias.FERROPHILES, Ingredient.of(Items.IRON_INGOT), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.FERROPHILES, NTBacterias.AURROPHILES, Ingredient.of(Items.GOLD_INGOT), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.AURROPHILES, NTBacterias.ADAMANTOPHILES, Ingredient.of(Items.DIAMOND), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.ADAMANTOPHILES, NTBacterias.SMARAGDOPHILES, Ingredient.of(Items.EMERALD), 5f)
                .save(output);

        new MutationRecipeBuilder(NTBacterias.METHANOGENS, NTBacterias.CARNIVOROUS_BACTERIA, Ingredient.of(Items.ROTTEN_FLESH), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METHANOGENS, NTBacterias.RED_MYCOTROPHIC_BACTERIA, Ingredient.of(Items.RED_MUSHROOM_BLOCK), 20f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METHANOGENS, NTBacterias.BROWN_MYCOTROPHIC_BACTERIA, Ingredient.of(Items.BROWN_MUSHROOM_BLOCK), 20f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.BROWN_MYCOTROPHIC_BACTERIA, NTBacterias.WARPED_MICROBES, Ingredient.of(Items.WARPED_FUNGUS), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RED_MYCOTROPHIC_BACTERIA, NTBacterias.WARPED_MICROBES, Ingredient.of(Items.WARPED_FUNGUS), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.BROWN_MYCOTROPHIC_BACTERIA, NTBacterias.CRIMSON_MICROBES, Ingredient.of(Items.CRIMSON_FUNGUS), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RED_MYCOTROPHIC_BACTERIA, NTBacterias.CRIMSON_MICROBES, Ingredient.of(Items.CRIMSON_FUNGUS), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.CRIMSON_LIGNOCYTES, Ingredient.of(Items.CRIMSON_STEM), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.WARPED_LIGNOCYTES, Ingredient.of(Items.WARPED_STEM), 3f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.METHANOGENS, NTBacterias.LIGNOCYTES, Ingredient.of(Items.OAK_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.DARK_LIGNOCYTES, Ingredient.of(Items.DARK_OAK_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.ACACIOPHYLES, Ingredient.of(Items.ACACIA_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.JUNGLOPHILES, Ingredient.of(Items.JUNGLE_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.BOREOPHILES, Ingredient.of(Items.SPRUCE_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.BETULOPHILES, Ingredient.of(Items.BIRCH_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.RHIZOPHORA_LIGNOCYTES, Ingredient.of(Items.MANGROVE_LOG), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.LIGNOCYTES, NTBacterias.PRUNUS_LIGNOCYTES, Ingredient.of(Items.CHERRY_LOG), 5f)
                .save(output);

        new MutationRecipeBuilder(NTBacterias.CYANOBACTERIA, NTBacterias.PHOTOTROPHS, Ingredient.of(Items.SUGAR_CANE), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.PHOTOTROPHS, NTBacterias.CACTOPHYLES, Ingredient.of(Items.CACTUS), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.HALOBACTERIA, NTBacterias.HALOTROPHS, Ingredient.of(Items.KELP), 25f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.HALOBACTERIA, NTBacterias.ALGAEFORMERS, Ingredient.of(Items.SEAGRASS), 25f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.ALGAEFORMERS, NTBacterias.CRYOBIONTS, Ingredient.of(Items.BLUE_ICE), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.HALOTROPHS, NTBacterias.PHOTOTROPHS, Ingredient.of(Items.SUGAR_CANE), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.HALOTROPHS, NTBacterias.LIPOPHILES, Ingredient.of(NTItems.KELP_SLURRY.get()), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.PHOTOTROPHS, NTBacterias.RHIZOBACTERIA, Ingredient.of(Items.WHEAT), 5f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.BETA_PHYLOBACTERIA, Ingredient.of(Items.BEETROOT), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.CAROTOPHYLES, Ingredient.of(Items.CARROT), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.SOLANOPHILES, Ingredient.of(Items.POTATO), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.CUCURBITOPHILES, Ingredient.of(Items.PUMPKIN), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.CUCURBITOPHILES, NTBacterias.MELOPHAGES, Ingredient.of(Items.MELON_SLICE), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.BAMBOOPHAGES, Ingredient.of(Items.BAMBOO), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.JUNGLOPHILES, NTBacterias.COCOAPHILES, Ingredient.of(Items.COCOA_BEANS), 10f)
                .save(output);
        new MutationRecipeBuilder(NTBacterias.RHIZOBACTERIA, NTBacterias.BRYOPHYTOPHILES, Ingredient.of(Items.MOSS_BLOCK), 5f)
                .save(output);
    }

    private void incubationRecipes(RecipeOutput output) {
        incubation(output, new IncubationRecipeBuilder(NTBacterias.LIGNOCYTES, Ingredient.of(Items.OAK_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.DARK_LIGNOCYTES, Ingredient.of(Items.DARK_OAK_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.ACACIOPHYLES, Ingredient.of(Items.ACACIA_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.JUNGLOPHILES, Ingredient.of(Items.JUNGLE_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BOREOPHILES, Ingredient.of(Items.SPRUCE_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BETULOPHILES, Ingredient.of(Items.BIRCH_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CRIMSON_LIGNOCYTES, Ingredient.of(Items.CRIMSON_STEM), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.WARPED_LIGNOCYTES, Ingredient.of(Items.WARPED_STEM), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.RHIZOPHORA_LIGNOCYTES, Ingredient.of(Items.MANGROVE_LOG), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.PRUNUS_LIGNOCYTES, Ingredient.of(Items.CHERRY_LOG), IntRange.of(10, 30), 0.07f));

        incubation(output, new IncubationRecipeBuilder(NTBacterias.SILICOPHILES, Ingredient.of(Items.SAND), IntRange.of(8, 25), 0.05f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.LITHOPHILES, Ingredient.of(Items.STONE), IntRange.of(8, 25), 0.05f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.METALLOPHILES, tag(Tags.Items.ORES_COPPER), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.FERROPHILES, tag(Tags.Items.ORES_IRON), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.AURROPHILES, tag(Tags.Items.ORES_GOLD), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.ACIDOPHILES, tag(Tags.Items.ORES_REDSTONE), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.ADAMANTOPHILES, tag(Tags.Items.ORES_DIAMOND), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.SMARAGDOPHILES, tag(Tags.Items.ORES_EMERALD), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.AZURITOPHILES, tag(Tags.Items.ORES_LAPIS), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CARBOPHAGES, tag(Tags.Items.ORES_COAL), IntRange.of(8, 25), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CALCIOPHILES, Ingredient.of(Items.BONE_BLOCK), IntRange.of(8, 25), 0.1f));

        incubation(output, new IncubationRecipeBuilder(NTBacterias.PHOTOTROPHS, Ingredient.of(Items.SUGAR_CANE), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.RED_MYCOTROPHIC_BACTERIA, Ingredient.of(Items.RED_MUSHROOM_BLOCK), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BROWN_MYCOTROPHIC_BACTERIA, Ingredient.of(Items.BROWN_MUSHROOM_BLOCK), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.HALOTROPHS, Ingredient.of(Items.SAND), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.LIPOPHILES, Ingredient.of(NTItems.KELP_SLURRY.get()), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BRYOPHYTOPHILES, Ingredient.of(Items.MOSS_BLOCK), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.ALGAEFORMERS, Ingredient.of(Items.KELP), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.RHIZOBACTERIA, Ingredient.of(Items.WHEAT), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.SOLANOPHILES, Ingredient.of(Items.POTATO), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BAMBOOPHAGES, Ingredient.of(Items.BAMBOO), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CACTOPHYLES, Ingredient.of(Items.CACTUS), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CAROTOPHYLES, Ingredient.of(Items.CARROT), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CUCURBITOPHILES, Ingredient.of(Items.PUMPKIN), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.BETA_PHYLOBACTERIA, Ingredient.of(Items.BEETROOT), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.COCOAPHILES, Ingredient.of(Items.COCOA_BEANS), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.MELOPHAGES, Ingredient.of(Items.MELON_SLICE), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CRIMSON_MICROBES, Ingredient.of(Items.CRIMSON_NYLIUM), IntRange.of(10, 30), 0.07f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.WARPED_MICROBES, Ingredient.of(Items.WARPED_NYLIUM), IntRange.of(10, 30), 0.07f));

        incubation(output, new IncubationRecipeBuilder(NTBacterias.SULFUROPHILES, Ingredient.of(Items.GUNPOWDER), IntRange.of(5, 15), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CRYOBIONTS, Ingredient.of(Items.PACKED_ICE), IntRange.of(5, 15), 0.1f));
        incubation(output, new IncubationRecipeBuilder(NTBacterias.CARNIVOROUS_BACTERIA, Ingredient.of(Items.ROTTEN_FLESH), IntRange.of(5, 15), 0.1f));
    }

    private void incubation(RecipeOutput output, IncubationRecipeBuilder incubation) {
        incubation.save(output);
        new ColonyFeedingRecipeBuilder(incubation.bacteria(), incubation.nutrient()).save(output);
    }

    private void bioReactorRecipes(@NotNull RecipeOutput pRecipeOutput) {
        shaped(RecipeCategory.MISC, NTItems.REACTOR_SPEED_UPGRADE.get())
                .pattern(" R ")
                .pattern("DAD")
                .pattern(" R ")
                .define('R', NTItems.RESONANT_SHARD)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('A', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.RESONANT_SHARD))
                .save(pRecipeOutput, key("reactor_speed_upgrade"));

        shaped(RecipeCategory.MISC, NTItems.REACTOR_YIELD_UPGRADE.get())
                .pattern(" F ")
                .pattern("DAD")
                .pattern(" F ")
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('A', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL))
                .save(pRecipeOutput, key("reactor_yield_upgrade"));

        shaped(RecipeCategory.MISC, NTItems.REACTOR_EFFICIENCY_UPGRADE.get())
                .pattern(" D ")
                .pattern("RAF")
                .pattern(" D ")
                .define('R', NTItems.RESONANT_SHARD)
                .define('F', NTItems.FLAWLESS_PRISMARINE_CRYSTAL)
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('A', NTItems.AQUATIC_CHIP)
                .unlockedBy("has_item", has(NTItems.DEEP_STEEL_PLATING))
                .save(pRecipeOutput, key("reactor_efficiency_upgrade"));

        shapeless(RecipeCategory.MISC, NTItems.REACTOR_FUSION_UPGRADE.get())
                .requires(NTItems.REACTOR_SPEED_UPGRADE)
                .requires(NTItems.REACTOR_YIELD_UPGRADE)
                .requires(NTItems.REACTOR_EFFICIENCY_UPGRADE)
                .unlockedBy("has_item", has(NTItems.REACTOR_SPEED_UPGRADE))
                .save(pRecipeOutput, key("reactor_fusion_upgrade"));

        shaped(RecipeCategory.DECORATIONS, NTBlocks.INDUSTRIAL_BIO_REACTOR.asItem())
                .pattern("DRD")
                .pattern("RBR")
                .pattern("DRD")
                .define('D', NTItems.DEEP_STEEL_PLATING)
                .define('R', NTItems.RESONANT_SHARD)
                .define('B', NTBlocks.BIO_REACTOR)
                .unlockedBy("has_item", has(NTBlocks.BIO_REACTOR))
                .save(pRecipeOutput, key("industrial_bio_reactor"));
    }

    private void skyblockRecipes(@NotNull RecipeOutput pRecipeOutput) {
        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTBlocks.PRISMARINE_SAND.asItem(), 2))
                .ingredients(iwcFromTag(Tags.Items.SANDS_COLORLESS, 4),
                        iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 1))
                .duration(200)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(skyblock(pRecipeOutput, SkyblockOption.PRISMARINE_SAND_MIXING), key("skyblock/prismarine_sand_mixing"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTBlocks.BUDDING_PRISMARINE.asItem(), 1))
                .ingredients(iwcFromItemLike(Items.PRISMARINE_BRICKS, 1),
                        iwcFromTag(Tags.Items.GEMS_PRISMARINE, 4),
                        iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 2))
                .duration(600)
                .fluidIngredient(new FluidStackTemplate(NTFluids.SALT_WATER.getStillFluid(), 1000))
                .fluidResult(null)
                .save(skyblock(pRecipeOutput, SkyblockOption.BUDDING_PRISMARINE_MIXING), key("skyblock/budding_prismarine_mixing"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 4))
                .result(new ItemStackTemplate(NTItems.RESONANT_SHARD.get(), 1))
                .power(20)
                .purity(2.5f)
                .duration(400)
                .save(skyblock(pRecipeOutput, SkyblockOption.FUEL_CELL_RESONANCE), key("skyblock/resonant_shard_laser_crafting"));

        LaserCraftingRecipeBuilder.newRecipe()
                .ingredient(iwcFromItemLike(NTItems.DORMANT_CRYSTAL_SEED.get(), 1))
                .result(new ItemStackTemplate(NTItems.PRISMARINE_CRYSTAL_SEED.get(), 1))
                .power(20)
                .purity(2.5f)
                .duration(2400)
                .save(skyblock(pRecipeOutput, SkyblockOption.FUEL_CELL_RESONANCE), key("skyblock/prismarine_crystal_seed_laser_crafting"));

        MixingRecipeBuilder.newRecipe(new ItemStackTemplate(NTItems.ELDRITCH_HEART.get(), 1))
                .ingredients(iwcFromItemLike(Items.HEART_OF_THE_SEA, 1),
                        iwcFromItemLike(NTItems.RESONANT_SHARD.get(), 2),
                        iwcFromItemLike(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), 4))
                .duration(400)
                .fluidIngredient(new FluidStackTemplate(NTFluids.EAS.getStillFluid(), 1000))
                .fluidResult(null)
                .save(skyblock(pRecipeOutput, SkyblockOption.ELDRITCH_HEART_MIXING), key("skyblock/eldritch_heart_mixing"));
    }

    private static RecipeOutput skyblock(RecipeOutput output, SkyblockOption option) {
        return output.withConditions(new SkyblockOptionCondition(option));
    }

    private static @NotNull IngredientWithCount iwcFromItemLike(Item item, int count) {
        return IngredientWithCount.fromItemLike(item, count);
    }

    private @NotNull IngredientWithCount iwcFromTag(TagKey<Item> item, int count) {
        return new IngredientWithCount(tag(item), count);
    }

    private static ResourceLocation key(String path) {
        return Nautec.rl(path);
    }

    private static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return ShapedRecipeBuilder.shaped(category, result);
    }

    private static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return ShapedRecipeBuilder.shaped(category, result, count);
    }

    private static ShapedRecipeBuilder shaped(RecipeCategory category, ItemStackTemplate result) {
        return ShapedRecipeBuilder.shaped(category, result.create());
    }

    private static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return ShapelessRecipeBuilder.shapeless(category, result);
    }

    private static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return ShapelessRecipeBuilder.shapeless(category, result, count);
    }

    private static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemStackTemplate result) {
        return ShapelessRecipeBuilder.shapeless(category, result.create());
    }

    private static Ingredient tag(TagKey<Item> tag) {
        return Ingredient.of(tag);
    }
}
