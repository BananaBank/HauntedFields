package team.bananabank.hauntedfields.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.bananabank.hauntedfields.HauntedFields;

public class HItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, HauntedFields.ID);

    public static final RegistryObject<ForgeSpawnEggItem> SCARECROW_SPAWN_EGG = REGISTRY.register("scarecrow_spawn_egg",
            () -> new ForgeSpawnEggItem(HEntityTypes.SCARECROW, 0x71552d, 0xFF7400,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<ForgeSpawnEggItem> CROW_SPAWN_EGG = REGISTRY.register("crow_spawn_egg",
            () -> new ForgeSpawnEggItem(HEntityTypes.CROW, 0x000000, 0x252525,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

}
