package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.client.screen.DistributorScreen;
import com.breakinblocks.nautec.client.screen.ConduitTapScreen;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.items.IBacteriaItem;
import com.breakinblocks.nautec.client.ClientRecipes;
import com.breakinblocks.nautec.client.screen.ConfinedSpawnerScreen;
import com.breakinblocks.nautec.compat.jei.categories.AquaticCatalystChannelingRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.CombustionAdditiveCategory;
import com.breakinblocks.nautec.compat.jei.categories.CombustionDynamoCategory;
import com.breakinblocks.nautec.compat.jei.categories.AugmentationRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.BacteriaGraftingCategory;
import com.breakinblocks.nautec.compat.jei.categories.BacteriaIncubationCategory;
import com.breakinblocks.nautec.compat.jei.categories.BacteriaMutationsCategory;
import com.breakinblocks.nautec.compat.jei.categories.BioReactorCategory;
import com.breakinblocks.nautec.compat.jei.categories.ColonyFeedingCategory;
import com.breakinblocks.nautec.compat.jei.categories.EasInfusionCategory;
import com.breakinblocks.nautec.compat.jei.categories.ItemEtchingRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.ItemTransformationRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.LaserCraftingRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.MixingRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.PressureForgingRecipeCategory;
import com.breakinblocks.nautec.compat.jei.categories.ResonanceCraftingRecipeCategory;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import com.breakinblocks.nautec.content.menus.MixerMenu;
import com.breakinblocks.nautec.content.menus.LaserCraftingMatrixMenu;
import com.breakinblocks.nautec.content.menus.IncubatorMenu;
import com.breakinblocks.nautec.content.menus.MutatorMenu;
import com.breakinblocks.nautec.content.menus.BioReactorMenu;
import com.breakinblocks.nautec.content.menus.IndustrialBioReactorMenu;
import com.breakinblocks.nautec.content.menus.CombustionDynamoMenu;
import com.breakinblocks.nautec.content.menus.GraftingStationMenu;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@JeiPlugin
public class NTJeiPlugin implements IModPlugin {

    private final List<RecipeBinding<?>> recipeBindings = List.of(
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(PressureForgingRecipe.Type.INSTANCE), PressureForgingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(ResonanceCraftingRecipe.Type.INSTANCE), ResonanceCraftingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(ItemTransformationRecipe.Type.INSTANCE), ItemTransformationRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(AquaticCatalystChannelingRecipe.Type.INSTANCE), AquaticCatalystChannelingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(CombustionAdditiveRecipe.Type.INSTANCE), CombustionAdditiveCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(ItemEtchingRecipe.Type.INSTANCE), ItemEtchingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(MixingRecipe.Type.INSTANCE), MixingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(LaserCraftingRecipe.Type.INSTANCE), LaserCraftingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(AugmentationRecipe.Type.INSTANCE), AugmentationRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(BacteriaMutationRecipe.TYPE), BacteriaMutationsCategory.RECIPE_TYPE, ProductNutrients.Derived::mutation),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(BacteriaIncubationRecipe.TYPE), BacteriaIncubationCategory.RECIPE_TYPE, ProductNutrients.Derived::incubation),
            new RecipeBinding<>(manager -> manager.getAllRecipesFor(ColonyFeedingRecipe.TYPE), ColonyFeedingCategory.RECIPE_TYPE, ProductNutrients.Derived::feeding));

    private static final class RecipeBinding<R extends Recipe<?>> {
        private final Function<RecipeManager, List<RecipeHolder<R>>> source;
        private final RecipeType<R> jeiType;
        private final @Nullable Function<ProductNutrients.Derived, List<R>> derived;

        private RecipeBinding(Function<RecipeManager, List<RecipeHolder<R>>> source, RecipeType<R> jeiType) {
            this(source, jeiType, null);
        }

        private RecipeBinding(Function<RecipeManager, List<RecipeHolder<R>>> source, RecipeType<R> jeiType, @Nullable Function<ProductNutrients.Derived, List<R>> derived) {
            this.source = source;
            this.jeiType = jeiType;
            this.derived = derived;
        }

        private List<R> recipes(RecipeManager manager) {
            List<RecipeHolder<R>> holders = source.apply(manager);
            List<R> recipes = new ArrayList<>(holders.size());
            for (RecipeHolder<R> holder : holders) {
                recipes.add(holder.value());
            }
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (derived != null && connection != null) {
                recipes.addAll(derived.apply(ProductNutrients.derive(manager, connection.registryAccess())));
            }
            return recipes;
        }

        private void register(IRecipeRegistration registration, RecipeManager manager) {
            registration.addRecipes(jeiType, recipes(manager));
        }
    }


    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Nautec.rl("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (ItemLike item : NTItems.bacteriaItems()) {
            if (item.asItem() instanceof IBacteriaItem) {
                registration.registerSubtypeInterpreter(item.asItem(), BacteriaSubtypeInterpreter.INSTANCE);
            }
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PressureForgingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ResonanceCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ItemTransformationRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new AquaticCatalystChannelingRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new ItemEtchingRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new MixingRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new LaserCraftingRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new EasInfusionCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new AugmentationRecipeCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new BacteriaMutationsCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new BacteriaIncubationCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new BioReactorCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new ColonyFeedingCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new BacteriaGraftingCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new CombustionDynamoCategory(
                registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new CombustionAdditiveCategory(
                registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Component seedInfo = Component.translatable("nautec.jei.crystal_seed");
        registration.addIngredientInfo(NTItems.DORMANT_CRYSTAL_SEED.get(), seedInfo);
        registration.addIngredientInfo(NTItems.PRISMARINE_CRYSTAL_SEED.get(), seedInfo);
        registration.addIngredientInfo(NTBlocks.CRYSTAL_CRADLE.get(), Component.translatable("nautec.jei.crystal_cradle",
                NumberFormat.getIntegerInstance(Locale.ROOT).format(NTConfig.crystalGrowthPower)));
        RecipeManager recipeManager = ClientRecipes.get();
        if (recipeManager != null) {
            for (RecipeBinding<?> binding : recipeBindings) {
                binding.register(registration, recipeManager);
            }
        }
        ClientLevel level = Minecraft.getInstance().level;
        RegistryAccess registryAccess = level.registryAccess();

        List<AugmentationRecipe> augmentationRecipes = new ArrayList<>();
        if (recipeManager != null) {
            for (RecipeHolder<AugmentationRecipe> holder : recipeManager.getAllRecipesFor(AugmentationRecipe.Type.INSTANCE)) {
                augmentationRecipes.add(holder.value());
            }
        }

        Registry<Bacteria> registry = registryAccess.registryOrThrow(NTRegistries.BACTERIA_KEY);
        List<BioReactorCategory.BioReactorRecipe> bioReactorRecipes = registry.entrySet().stream()
                .map(entry -> new BioReactorCategory.BioReactorRecipe(entry.getKey(), entry.getValue().resource()))
                .filter(recipe -> !(recipe.bacteria().equals(NTBacterias.EMPTY) || recipe.resource().isEmpty()))
                .toList();

        Map<ResourceKey<Block>, BacteriaObtainValue> dataMap = BuiltInRegistries.BLOCK.getDataMap(NTDataMaps.BACTERIA_OBTAINING);
        List<BacteriaGraftingCategory.GraftingRecipe> graftingRecipes = new ArrayList<>();
        for (Map.Entry<ResourceKey<Block>, BacteriaObtainValue> entry : dataMap.entrySet()) {
            Block block = BuiltInRegistries.BLOCK.getOrThrow(entry.getKey());
            Item sample = block.getCloneItemStack(level, BlockPos.ZERO, block.defaultBlockState()).getItem();
            if (sample == Items.AIR) {
                sample = block.asItem();
            }
            BacteriaGraftingCategory.GraftingRecipe recipe = new BacteriaGraftingCategory.GraftingRecipe(sample, entry.getValue());
            if (sample != Items.AIR && !graftingRecipes.contains(recipe)) {
                graftingRecipes.add(recipe);
            }
        }

        registration.addRecipes(BioReactorCategory.RECIPE_TYPE, bioReactorRecipes);
        registration.addRecipes(EasInfusionCategory.RECIPE_TYPE, EasInfusionCategory.recipes());
        registration.addRecipes(CombustionDynamoCategory.RECIPE_TYPE, CombustionDynamoCategory.recipes());
        registration.addRecipes(BacteriaGraftingCategory.RECIPE_TYPE, graftingRecipes);

        for (AugmentationRecipe recipe : augmentationRecipes) {
            registration.addIngredientInfo(recipe.augmentItem().getDefaultInstance(), VanillaTypes.ITEM_STACK, Component.translatable(recipe.desc()));
        }

        registration.addIngredientInfo(new FluidStack(NTFluids.EAS.getStillFluid(), 1000), NeoForgeTypes.FLUID_STACK,
                Component.translatable("nautec.jei.info.eas"));
        registration.addIngredientInfo(List.of(new ItemStack(NTFluids.EAS.getBucket()), NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.toStack()), VanillaTypes.ITEM_STACK,
                Component.translatable("nautec.jei.info.eas"));

        registration.addIngredientInfo(NTItems.PRISMARINE_CRYSTAL_SHARD.toStack(), VanillaTypes.ITEM_STACK,
                Component.translatable("nautec.jei.info.prismarine_crystal_shard"));

        registration.addIngredientInfo(List.of(NTItems.BROKEN_WHISK.toStack(), NTItems.BURNT_COIL.toStack(), NTItems.ANCIENT_VALVE.toStack(), NTItems.RUSTY_GEAR.toStack()), VanillaTypes.ITEM_STACK, Component.translatable("nautec.jei.info.machine_parts"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerGhostInputs(IGuiHandlerRegistration registration, Class screenClass) {
        registration.addGhostIngredientHandler(screenClass, new GhostInputJeiHandler());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        ConfinedSpawnerGhostHandler handler = new ConfinedSpawnerGhostHandler();
        registration.addGhostIngredientHandler(ConfinedSpawnerScreen.class, handler);
        registration.addGhostIngredientHandler(DistributorScreen.class, new DistributorJeiHandler());
        registration.addGhostIngredientHandler(ConduitTapScreen.class, new ConduitTapJeiHandler());
        registerGhostInputs(registration, NTMachineScreen.class);
        registerGhostInputs(registration, NTAbstractContainerScreen.class);
        registration.addGuiContainerHandler(ConfinedSpawnerScreen.class, handler);
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new SideConfigJeiHandler());
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new RecipeAreaJeiHandler());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        IRecipeTransferHandlerHelper helper = registration.getTransferHelper();
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, MixerMenu.class, NTMenuTypes.MIXER, MixingRecipeCategory.RECIPE_TYPE,
                (menu, recipe) -> counted(recipe.ingredients(), MixerBlockEntity.INPUT_SLOTS)), MixingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, LaserCraftingMatrixMenu.class, NTMenuTypes.LASER_CRAFTING_MATRIX,
                LaserCraftingRecipeCategory.RECIPE_TYPE, (menu, recipe) -> counted(recipe.ingredients(), LaserCraftingMatrixBlockEntity.INPUT_SLOTS)),
                LaserCraftingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, IncubatorMenu.class, NTMenuTypes.INCUBATOR, BacteriaIncubationCategory.RECIPE_TYPE,
                (menu, recipe) -> List.of(new RecipeTransfer.Entry(IncubatorBlockEntity.NUTRIENT_SLOT, recipe.nutrient(), 1))), BacteriaIncubationCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, MutatorMenu.class, NTMenuTypes.MUTATOR, BacteriaMutationsCategory.RECIPE_TYPE,
                (menu, recipe) -> List.of(new RecipeTransfer.Entry(MutatorBlockEntity.CATALYST, recipe.catalyst(), 1))), BacteriaMutationsCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, BioReactorMenu.class, NTMenuTypes.BIO_REACTOR, ColonyFeedingCategory.RECIPE_TYPE,
                (menu, recipe) -> feeding(menu.blockEntity, recipe)), ColonyFeedingCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, IndustrialBioReactorMenu.class, NTMenuTypes.INDUSTRIAL_BIO_REACTOR,
                ColonyFeedingCategory.RECIPE_TYPE, (menu, recipe) -> feeding(menu.blockEntity, recipe)), ColonyFeedingCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, CombustionDynamoMenu.class, NTMenuTypes.COMBUSTION_DYNAMO,
                CombustionAdditiveCategory.RECIPE_TYPE, (menu, recipe) -> List.of(new RecipeTransfer.Entry(CombustionDynamoBlockEntity.ADDITIVE_SLOT, recipe.ingredient(), 1))),
                CombustionAdditiveCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(new MachineTransferHandler<>(helper, GraftingStationMenu.class, NTMenuTypes.GRAFTING_STATION,
                BacteriaGraftingCategory.RECIPE_TYPE, (menu, recipe) -> grafting(recipe)), BacteriaGraftingCategory.RECIPE_TYPE);
    }

    private static List<RecipeTransfer.Entry> counted(List<IngredientWithCount> ingredients, int slots) {
        List<RecipeTransfer.Entry> entries = new ArrayList<>();
        for (int i = 0; i < ingredients.size() && i < slots; i++) {
            IngredientWithCount ingredient = ingredients.get(i);
            if (!ingredient.ingredient().isEmpty()) {
                entries.add(new RecipeTransfer.Entry(i, ingredient.ingredient(), ingredient.count()));
            }
        }
        return entries;
    }

    private static List<RecipeTransfer.Entry> feeding(AbstractBioReactorBlockEntity reactor, ColonyFeedingRecipe recipe) {
        if (reactor.getNutrientSlotCount() == 0) {
            return List.of();
        }
        return List.of(new RecipeTransfer.Entry(reactor.nutrientSlot(0), recipe.ingredient().ingredient(), recipe.ingredient().count()));
    }

    private static List<RecipeTransfer.Entry> grafting(BacteriaGraftingCategory.GraftingRecipe recipe) {
        Item sample = recipe.sample();
        return List.of(new RecipeTransfer.Entry(GraftingStationBlockEntity.DISH_SLOT, Ingredient.of(NTItems.PETRI_DISH.get()), 1),
                new RecipeTransfer.Entry(GraftingStationBlockEntity.SAMPLE_SLOT, Ingredient.of(sample), 1));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(PressureForgingRecipeCategory.RECIPE_TYPE, NTBlocks.PRESSURE_FORGE.toStack());
        registration.addRecipeCatalysts(ResonanceCraftingRecipeCategory.RECIPE_TYPE, NTBlocks.RESONANCE_CHAMBER.toStack());
        registration.addRecipeCatalysts(AquaticCatalystChannelingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.AQUATIC_CATALYST.get()));
        registration.addRecipeCatalysts(ItemEtchingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTFluids.ETCHING_ACID.getBucket()));
        registration.addRecipeCatalysts(MixingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.MIXER.get()));
        registration.addRecipeCatalysts(LaserCraftingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.LASER_CRAFTING_MATRIX.get()));
        registration.addRecipeCatalysts(EasInfusionCategory.RECIPE_TYPE,
                new ItemStack(NTFluids.EAS.getBucket()));
        registration.addRecipeCatalysts(AugmentationRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.AUGMENTATION_STATION.get()));
        registration.addRecipeCatalysts(BacteriaMutationsCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.MUTATOR.get()));
        registration.addRecipeCatalysts(BacteriaIncubationCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.INCUBATOR.get()));
        registration.addRecipeCatalysts(BioReactorCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.BIO_REACTOR.get()),
                new ItemStack(NTBlocks.INDUSTRIAL_BIO_REACTOR.get()));
        registration.addRecipeCatalysts(ColonyFeedingCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.BIO_REACTOR.get()),
                new ItemStack(NTBlocks.INDUSTRIAL_BIO_REACTOR.get()));
        registration.addRecipeCatalysts(CombustionDynamoCategory.RECIPE_TYPE, NTBlocks.COMBUSTION_DYNAMO.toStack());
        registration.addRecipeCatalysts(CombustionAdditiveCategory.RECIPE_TYPE, NTBlocks.COMBUSTION_DYNAMO.toStack());
        registration.addRecipeCatalysts(BacteriaGraftingCategory.RECIPE_TYPE,
                new ItemStack(NTItems.GRAFTING_TOOL.get()),
                new ItemStack(NTItems.PETRI_DISH.get()),
                new ItemStack(NTBlocks.GRAFTING_STATION.get()));
    }

}
