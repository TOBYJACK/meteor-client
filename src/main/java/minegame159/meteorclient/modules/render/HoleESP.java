package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.RenderEvent;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.ColorSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.utils.Color;
import minegame159.meteorclient.utils.Pool;
import minegame159.meteorclient.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends ToggleModule {
    private Setting<Integer> horizontalRadius = addSetting(new IntSetting.Builder()
            .name("horizontal-radius")
            .description("Horizontal radius in which to search for holes.")
            .group("General")
            .defaultValue(10)
            .min(0)
            .build()
    );

    private Setting<Integer> verticalRadius = addSetting(new IntSetting.Builder()
            .name("vertical-radius")
            .description("Vertical radius in which to search for holes.")
            .group("General")
            .defaultValue(10)
            .min(0)
            .build()
    );

    private Setting<Boolean> renderBox = addSetting(new BoolSetting.Builder()
            .name("render-box")
            .description("Renders box instead of a quad.")
            .group("General")
            .defaultValue(false)
            .build()
    );

    private Setting<Color> allBedrock = addSetting(new ColorSetting.Builder()
            .name("all-bedrock")
            .description("All blocks are bedrock.")
            .group("Colors")
            .defaultValue(new Color(25, 225, 25))
            .build()
    );

    private Setting<Color> someObsidian = addSetting(new ColorSetting.Builder()
            .name("some-obsidian")
            .description("Some blocks are obsidian.")
            .group("Colors")
            .defaultValue(new Color(225, 145, 25))
            .build()
    );

    private Setting<Color> allObsidian = addSetting(new ColorSetting.Builder()
            .name("all-obsidian")
            .description("All blocks are obsidian.")
            .group("Colors")
            .defaultValue(new Color(225, 25, 25))
            .build()
    );

    private Pool<Hole> holePool = new Pool<>(Hole::new);
    private BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private List<Hole> holes = new ArrayList<>();

    public HoleESP() {
        super(Category.Render, "hole-esp", "Displays holes that u can be in so u dont take explosion damage.");
    }

    @EventHandler
    private Listener<TickEvent> onTick = new Listener<>(event -> {
        for (Hole hole : holes) holePool.free(hole);
        holes.clear();

        for (int x = (int) mc.player.x - horizontalRadius.get(); x <= (int) mc.player.x + horizontalRadius.get(); x++) {
            for (int y = (int) mc.player.y - verticalRadius.get(); y <= (int) mc.player.y + verticalRadius.get(); y++) {
                for (int z = (int) mc.player.z - horizontalRadius.get(); z <= (int) mc.player.z + horizontalRadius.get(); z++) {
                    blockPos.set(x, y, z);

                    Block bottom = mc.world.getBlockState(add(0, -1, 0)).getBlock();
                    if (bottom != Blocks.BEDROCK && bottom != Blocks.OBSIDIAN) continue;
                    Block forward = mc.world.getBlockState(add(0, 1, 1)).getBlock();
                    if (forward != Blocks.BEDROCK && forward != Blocks.OBSIDIAN) continue;
                    Block back = mc.world.getBlockState(add(0, 0, -2)).getBlock();
                    if (back != Blocks.BEDROCK && forward != Blocks.OBSIDIAN) continue;
                    Block right = mc.world.getBlockState(add(1, 0, 1)).getBlock();
                    if (right != Blocks.BEDROCK && right != Blocks.OBSIDIAN) continue;
                    Block left = mc.world.getBlockState(add(-2, 0, 0)).getBlock();
                    if (left != Blocks.BEDROCK && left != Blocks.OBSIDIAN) continue;
                    add(1, 0, 0);

                    if (bottom == Blocks.BEDROCK && forward == Blocks.BEDROCK && back == Blocks.BEDROCK && right == Blocks.BEDROCK && left == Blocks.BEDROCK) {
                        holes.add(holePool.get().set(blockPos, allBedrock.get()));
                    } else {
                        int obsidian = 0;

                        if (bottom == Blocks.OBSIDIAN) obsidian++;
                        if (forward == Blocks.OBSIDIAN) obsidian++;
                        if (back == Blocks.OBSIDIAN) obsidian++;
                        if (right == Blocks.OBSIDIAN) obsidian++;
                        if (left == Blocks.OBSIDIAN) obsidian++;

                        if (obsidian == 5) holes.add(holePool.get().set(blockPos, allObsidian.get()));
                        else holes.add(holePool.get().set(blockPos, someObsidian.get()));
                    }
                }
            }
        }
    });

    @EventHandler
    private Listener<RenderEvent> onRender = new Listener<>(event -> {
        for (Hole hole : holes) {
            int x = hole.blockPos.getX();
            int y = hole.blockPos.getY();
            int z = hole.blockPos.getZ();

            if (renderBox.get()) {
                RenderUtils.blockSides(x, y - 1, z, hole.colorSides, null);
                RenderUtils.blockEdges(x, y - 1, z, hole.colorLines, null);
            } else {
                RenderUtils.boxWithLines(x, y, z, hole.colorSides, hole.colorLines);
            }
        }
    });

    private BlockPos.Mutable add(int x, int y, int z) {
        blockPos.setX(blockPos.getX() + x);
        blockPos.setY(blockPos.getY() + y);
        blockPos.setZ(blockPos.getZ() + z);
        return blockPos;
    }

    private static class Hole {
        public BlockPos.Mutable blockPos = new BlockPos.Mutable();
        public Color colorSides = new Color();
        public Color colorLines = new Color();

        public Hole set(BlockPos blockPos, Color color) {
            this.blockPos.set(blockPos);
            colorLines.set(color);
            colorSides.set(color);
            colorSides.a -= 175;
            colorSides.validate();
            return this;
        }
    }
}