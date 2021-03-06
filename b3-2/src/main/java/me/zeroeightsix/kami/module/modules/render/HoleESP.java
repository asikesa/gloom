package me.zeroeightsix.kami.module.modules.render;

import static me.zeroeightsix.kami.module.modules.combat.CrystalAura.getPlayerPos;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.opengl.GL11;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.combat.CrystalAura;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.GeometryMasks;
import me.zeroeightsix.kami.util.KamiTessellator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;


/**
 * Created 16 November 2019 by hub
 * Updated by S-B99 on 15/12/19
 */
@Module.Info(name = "HoleESP", category = Module.Category.COMBAT, description = "Show safe holes")
public class HoleESP extends Module {

    private final BlockPos[] surroundOffset = {
            new BlockPos(0, -1, 0), // down
            new BlockPos(0, 0, -1), // north
            new BlockPos(1, 0, 0), // east
            new BlockPos(0, 0, 1), // south
            new BlockPos(-1, 0, 0) // west
    };

    private Setting<Double> renderDistance = register(Settings.d("Render Distance", 8.0d));
    private Setting<Boolean> renObby = register(Settings.b("Render Obby", true));
    private Setting<Boolean> renBedr = register(Settings.b("Render Bedrock", true));
    private Setting<RenderMode> renderMode = register(Settings.e("Render Mode", RenderMode.BLOCK));
    private Setting<Integer> a0 = register(Settings.integerBuilder("Transparency").withMinimum(0).withValue(32).withMaximum(255).build());
    private Setting<Integer> r1 = register(Settings.integerBuilder("Red (Obby)").withMinimum(0).withValue(208).withMaximum(255).build()); // 144
    private Setting<Integer> g1 = register(Settings.integerBuilder("Green (Obby)").withMinimum(0).withValue(144).withMaximum(255).build());
    private Setting<Integer> b1 = register(Settings.integerBuilder("Blue (Obby)").withMinimum(0).withValue(255).withMaximum(255).build());
    private Setting<Integer> r2 = register(Settings.integerBuilder("Red (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).build()); // 208
    private Setting<Integer> g2 = register(Settings.integerBuilder("Green (Bedrock)").withMinimum(0).withValue(144).withMaximum(255).build());
    private Setting<Integer> b2 = register(Settings.integerBuilder("Blue (Bedrock)").withMinimum(0).withValue(255).withMaximum(255).build());

    private ConcurrentHashMap<BlockPos, Boolean> safeHoles;

    @Override
    public void onUpdate() {

        if (safeHoles == null) {
            safeHoles = new ConcurrentHashMap<>();
        } else {
            safeHoles.clear();
        }

        int range = (int) Math.ceil(renderDistance.getValue());

        CrystalAura crystalAura = (CrystalAura) ModuleManager.getModuleByName("CrystalAura");
        List<BlockPos> blockPosList = crystalAura.getSphere(getPlayerPos(), range, range, false, true, 0);

        for (BlockPos pos : blockPosList) {

            // block gotta be air
            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            // block 1 above gotta be air
            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            // block 2 above gotta be air
            if (!mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            boolean isSafe = true;
            boolean isBedrock = true;

            for (BlockPos offset : surroundOffset) {
                Block block = mc.world.getBlockState(pos.add(offset)).getBlock();
                if (block != Blocks.BEDROCK) {
                    isBedrock = false;
                }
                if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                    isSafe = false;
                    break;
                }
            }

            if (isSafe) {
                safeHoles.put(pos, isBedrock);
            }

        }

    }

    @Override
    public void onWorldRender(final RenderEvent event) {

        if (mc.player == null || safeHoles == null) {
            return;
        }

        if (safeHoles.isEmpty()) {
            return;
        }

        KamiTessellator.prepare(GL11.GL_QUADS);

        safeHoles.forEach((blockPos, isBedrock) -> {
            if (isBedrock && renBedr.getValue()) {
                drawBox(blockPos, r2.getValue(), g2.getValue(), b2.getValue());
            } else if (renObby.getValue()){
                drawBox(blockPos, r1.getValue(), g1.getValue(), b1.getValue());
            }
        });

        KamiTessellator.release();

    }

    private void drawBox(BlockPos blockPos, int r, int g, int b) {
        Color color = new Color(r, g, b, a0.getValue());
        if (renderMode.getValue().equals(RenderMode.DOWN)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), GeometryMasks.Quad.DOWN);
        } else if (renderMode.getValue().equals(RenderMode.BLOCK)) {
            KamiTessellator.drawBox(blockPos, color.getRGB(), GeometryMasks.Quad.ALL);
        }
    }

    private enum RenderMode {
        DOWN, BLOCK
    }

}
