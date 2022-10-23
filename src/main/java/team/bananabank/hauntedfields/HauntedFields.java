package team.bananabank.hauntedfields;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib3.GeckoLib;
import team.bananabank.hauntedfields.entity.client.CrowRenderer;
import team.bananabank.hauntedfields.entity.client.ScarecrowRenderer;
import team.bananabank.hauntedfields.registry.HEntityTypes;
import team.bananabank.hauntedfields.registry.HItems;

@Mod(HauntedFields.ID)
public class HauntedFields {
    public static final String ID = "hauntedfields";

    public HauntedFields() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        var fgBus = MinecraftForge.EVENT_BUS;

        HEntityTypes.REGISTRY.register(modBus);
        HItems.REGISTRY.register(modBus);

        modBus.addListener(this::clientSetup);

        GeckoLib.initialize();
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(HEntityTypes.SCARECROW.get(), ScarecrowRenderer::new);
        EntityRenderers.register(HEntityTypes.CROW.get(), CrowRenderer::new);
    }
}
