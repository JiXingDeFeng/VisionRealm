package io.github.jixingdefeng.visionrealm.core.item;

import io.github.jixingdefeng.visionrealm.common.util.random.ArrayWeightRandomList;
import io.github.jixingdefeng.visionrealm.core.VisionRealm;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestItem extends Item {
    public TestItem(Properties properties) {
        super(properties);
    }

    private final List<String> stringList = new ArrayList<>();
    private final List<T> list = new ArrayList<>();
    private final RandomSource randomSource = RandomSource.create();
    {
        for (int i = 0; i < 1000; i++) {
            String s = String.valueOf(i);
            stringList.add(s);
            list.add(new T(s, Weight.of(1)));
        }
    }
    private record T(String s, Weight getWeight) implements WeightedEntry {
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        VisionRealm.LOGGER.warn("\n===== Test Item use ======\n{}\n\n", player.getName().getString());
//        if (level instanceof ServerLevel serverLevel) {
//            IncidentHandler.getInstance().executeImmediately(serverLevel);
//        }

        long start = System.nanoTime();
        long time;

        VisionRealm.LOGGER.warn("数量: Array {}, 原版 {}", stringList.size(), list.size());

        ArrayWeightRandomList<String> arrayList = new ArrayWeightRandomList<>(stringList, s -> 1);

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("Array 创建耗时：{}ms", time / 1000_000.0);
        start = System.nanoTime();

        WeightedRandomList<T> randomList = WeightedRandomList.create(list);

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("原版 创建耗时：{}ms", time / 1000_000.0);
        start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            arrayList.getRandom(randomSource);
        }

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("Array 随机获取耗时：{}ms", time / 10000000.0);
        start = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            randomList.getRandom(randomSource);
        }

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("原版 随机获取耗时：{}ms", time / 10000000.0);




        arrayList.unwrap().forEach(item -> {});

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("Array unwrap遍历：{}ms", time / 10000000.0);
        start = System.nanoTime();

        arrayList.unwrapValue().forEach(item -> {});

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("Array unwrapValue遍历：{}ms", time / 10000000.0);
        start = System.nanoTime();

        arrayList.forEach((i, w) -> {});

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("Array forEach遍历：{}ms", time / 10000000.0);
        start = System.nanoTime();


        randomList.unwrap().forEach(item -> {});

        time = System.nanoTime() - start;
        VisionRealm.LOGGER.warn("原版 遍历：{}ms", time / 10000000.0);

        return super.use(level, player, usedHand);
    }
}
