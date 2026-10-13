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
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
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

    private final List<RecipeBinding<?, ?>> recipeBindings = List.of(
            new RecipeBinding<>(PressureForgingRecipe.Type.INSTANCE, PressureForgingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(ResonanceCraftingRecipe.Type.INSTANCE, ResonanceCraftingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(ItemTransformationRecipe.Type.INSTANCE, ItemTransformationRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(AquaticCatalystChannelingRecipe.Type.INSTANCE, AquaticCatalystChannelingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(CombustionAdditiveRecipe.Type.INSTANCE, CombustionAdditiveCategory.RECIPE_TYPE),
            new RecipeBinding<>(ItemEtchingRecipe.Type.INSTANCE, ItemEtchingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(MixingRecipe.Type.INSTANCE, MixingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(LaserCraftingRecipe.Type.INSTANCE, LaserCraftingRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(AugmentationRecipe.Type.INSTANCE, AugmentationRecipeCategory.RECIPE_TYPE),
            new RecipeBinding<>(BacteriaMutationRecipe.TYPE, BacteriaMutationsCategory.RECIPE_TYPE, ProductNutrients.Derived::mutation),
            new RecipeBinding<>(BacteriaIncubationRecipe.TYPE, BacteriaIncubationCategory.RECIPE_TYPE, ProductNutrients.Derived::incubation),
            new RecipeBinding<>(ColonyFeedingRecipe.TYPE, ColonyFeedingCategory.RECIPE_TYPE, ProductNutrients.Derived::feeding));
    private IJeiRuntime runtime;

    public NTJeiPlugin() {
        // Only instantiated by JEI, so clients without JEI never load its API classes.
        NeoForge.EVENT_BUS.addListener(this::recipesReceived);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    private void recipesReceived(RecipesReceivedEvent event) {
        if (runtime != null) {
            for (RecipeBinding<?, ?> binding : recipeBindings) {
                binding.refresh(runtime.getRecipeManager(), event.getRecipeMap());
            }
        }
    }

    private static final class RecipeBinding<I extends RecipeInput, R extends Recipe<I>> {
        private final RecipeType<R> minecraftType;
        private final IRecipeType<R> jeiType;
        private final @Nullable Function<ProductNutrients.Derived, List<R>> derived;
        private List<R> registered = List.of();

        private RecipeBinding(RecipeType<R> minecraftType, IRecipeType<R> jeiType) {
            this(minecraftType, jeiType, null);
        }

        private RecipeBinding(RecipeType<R> minecraftType, IRecipeType<R> jeiType, @Nullable Function<ProductNutrients.Derived, List<R>> derived) {
            this.minecraftType = minecraftType;
            this.jeiType = jeiType;
            this.derived = derived;
        }

        private List<R> recipes(RecipeMap map) {
            List<R> recipes = new ArrayList<>(map.byType(minecraftType).stream().map(RecipeHolder::value).toList());
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (derived != null && connection != null) {
                recipes.addAll(derived.apply(ProductNutrients.derive(map, connection.registryAccess())));
            }
            return recipes;
        }

        private void register(IRecipeRegistration registration, RecipeMap map) {
            registered = recipes(map);
            registration.addRecipes(jeiType, registered);
        }

        private void refresh(IRecipeManager manager, RecipeMap map) {
            // JEI exposes hide/add for runtime changes, rather than removal.
            manager.hideRecipes(jeiType, registered);
            registered = recipes(map);
            manager.addRecipes(jeiType, registered);
            // Record recipes compare by value; unchanged replacements may share a hidden key.
            manager.unhideRecipes(jeiType, registered);
        }
    }


    @Override
    public @NotNull Identifier getPluginUid() {
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
        for (RecipeBinding<?, ?> binding : recipeBindings) {
            binding.register(registration, ClientRecipes.get());
        }
        ClientLevel level = Minecraft.getInstance().level;
        RegistryAccess registryAccess = level.registryAccess();

        List<AugmentationRecipe> augmentationRecipes = recipesFor(AugmentationRecipe.Type.INSTANCE);

        Registry<Bacteria> registry = registryAccess.lookupOrThrow(NTRegistries.BACTERIA_KEY);
        List<BioReactorCategory.BioReactorRecipe> bioReactorRecipes = registry.entrySet().stream()
                .map(entry -> new BioReactorCategory.BioReactorRecipe(entry.getKey(), entry.getValue().resource()))
                .filter(recipe -> !(recipe.bacteria().equals(NTBacterias.EMPTY) || recipe.resource().isEmpty()))
                .toList();

        Map<ResourceKey<Block>, BacteriaObtainValue> dataMap = BuiltInRegistries.BLOCK.getDataMap(NTDataMaps.BACTERIA_OBTAINING);
        List<BacteriaGraftingCategory.GraftingRecipe> graftingRecipes = new ArrayList<>();
        for (Map.Entry<ResourceKey<Block>, BacteriaObtainValue> entry : dataMap.entrySet()) {
            Block block = BuiltInRegistries.BLOCK.getValueOrThrow(entry.getKey());
            Item sample = block.defaultBlockState().getCloneItemStack(level, BlockPos.ZERO, false).getItem();
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

    private static <I extends RecipeInput, R extends Recipe<I>> List<R> recipesFor(RecipeType<R> type) {
        return ClientRecipes.get().byType(type).stream().map(RecipeHolder::value).toList();
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
        registration.addCraftingStation(PressureForgingRecipeCategory.RECIPE_TYPE, NTBlocks.PRESSURE_FORGE.toStack());
        registration.addCraftingStation(RecipeTypes.SMELTING, NTBlocks.LASER_PURIFICATION_SMELTER.toStack());
        registration.addCraftingStation(ResonanceCraftingRecipeCategory.RECIPE_TYPE, NTBlocks.RESONANCE_CHAMBER.toStack());
        registration.addCraftingStation(AquaticCatalystChannelingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.AQUATIC_CATALYST.get()));
        registration.addCraftingStation(ItemEtchingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTFluids.ETCHING_ACID.getBucket()));
        registration.addCraftingStation(MixingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.MIXER.get()));
        registration.addCraftingStation(LaserCraftingRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.LASER_CRAFTING_MATRIX.get()));
        registration.addCraftingStation(EasInfusionCategory.RECIPE_TYPE,
                new ItemStack(NTFluids.EAS.getBucket()));
        registration.addCraftingStation(AugmentationRecipeCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.AUGMENTATION_STATION.get()));
        registration.addCraftingStation(BacteriaMutationsCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.MUTATOR.get()));
        registration.addCraftingStation(BacteriaIncubationCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.INCUBATOR.get()));
        registration.addCraftingStation(BioReactorCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.BIO_REACTOR.get()),
                new ItemStack(NTBlocks.INDUSTRIAL_BIO_REACTOR.get()));
        registration.addCraftingStation(ColonyFeedingCategory.RECIPE_TYPE,
                new ItemStack(NTBlocks.BIO_REACTOR.get()),
                new ItemStack(NTBlocks.INDUSTRIAL_BIO_REACTOR.get()));
        registration.addCraftingStation(CombustionDynamoCategory.RECIPE_TYPE, NTBlocks.COMBUSTION_DYNAMO.toStack());
        registration.addCraftingStation(CombustionAdditiveCategory.RECIPE_TYPE, NTBlocks.COMBUSTION_DYNAMO.toStack());
        registration.addCraftingStation(BacteriaGraftingCategory.RECIPE_TYPE,
                new ItemStack(NTItems.GRAFTING_TOOL.get()),
                new ItemStack(NTItems.PETRI_DISH.get()),
                new ItemStack(NTBlocks.GRAFTING_STATION.get()));
    }

}
