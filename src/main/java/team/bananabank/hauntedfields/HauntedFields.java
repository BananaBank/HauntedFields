package team.bananabank.hauntedfields;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.bananabank.hauntedfields.registry.HEntityTypes;

@Mod(HauntedFields.ID)
public class HauntedFields {
    public static final String ID = "hauntedfields";

    public HauntedFields() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        var fgBus = MinecraftForge.EVENT_BUS;

        HEntityTypes.REGISTRY.register(modBus);
    }
}
