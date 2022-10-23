package team.bananabank.hauntedfields.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import team.bananabank.hauntedfields.HauntedFields;
import team.bananabank.hauntedfields.entity.CrowEntity;

public class CrowRenderer extends GeoEntityRenderer<CrowEntity> {
    public CrowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CrowModel());
        this.shadowRadius = 0.3F; // match parrot
    }

    @Override
    public ResourceLocation getTextureLocation(CrowEntity instance) {
        return new ResourceLocation(HauntedFields.ID, "textures/entity/crow/crow.png");
    }

    @Override
    public RenderType getRenderType(CrowEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        stack.scale(1.0f, 1.0f, 1.0f);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
