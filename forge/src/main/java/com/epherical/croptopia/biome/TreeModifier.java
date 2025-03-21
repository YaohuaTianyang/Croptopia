package com.epherical.croptopia.biome;

import com.epherical.croptopia.CroptopiaForge;
import com.epherical.croptopia.common.MiscNames;
import com.epherical.croptopia.registry.GeneratorRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;

import static com.epherical.croptopia.CroptopiaForge.createIdentifier;

public record TreeModifier(GenerationStep.Decoration step) implements BiomeModifier {

    public static final RegistryObject<Codec<? extends BiomeModifier>> SERIALIZER =
            RegistryObject.create(createIdentifier("trees"), ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MiscNames.MOD_ID);

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            biome.unwrapKey().ifPresent(biomeResourceKey -> {
                BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
                Collection<String> strings = CroptopiaForge.config.getFeatures().get(biomeResourceKey.location());
                for (String string : strings) {
                    generation.addFeature(step, GeneratorRegistry.getFeatureKey(string));
                }
            });
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static Codec<TreeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.comapFlatMap(TreeModifier::generationStageFromString,
                        GenerationStep.Decoration::toString).fieldOf("generation_stage").forGetter(TreeModifier::step)
        ).apply(builder, TreeModifier::new));
    }

    private static DataResult<GenerationStep.Decoration> generationStageFromString(String name) {
        try {
            return DataResult.success(GenerationStep.Decoration.valueOf(name));
        } catch (Exception e) {
            return DataResult.error("Not a decoration stage: " + name);
        }
    }

    public static void register(DeferredRegister<BiomeModifier> biomeSerializer) {
        BiomeModifier modifier = new TreeModifier(GenerationStep.Decoration.VEGETAL_DECORATION);
        biomeSerializer.register("tree_modifiers", () -> modifier);
    }
}
