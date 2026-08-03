package xy177.farmersfuturedelight.common;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class FFDConfig {
    public enum FeatureMode {
        ENABLED,
        DISABLED,
        AUTO;

        public static FeatureMode parse(String value) {
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return AUTO;
            }
        }
    }

    private static final String CATEGORY_FEATURES = "features";
    private static final String CATEGORY_WORLDGEN = "worldgen";
    private static final String CATEGORY_GROWTH = "growth";
    private static final String CATEGORY_PARTICLES = "particles";
    private static final String CATEGORY_INTERNAL = "internal";
    private static final int CURRENT_CONFIG_VERSION = 2;

    public static FeatureMode sweetBerryMode;
    public static FeatureMode honeyMode;
    public static FeatureMode glowSquidMode;
    public static FeatureMode glowBerryMode;
    public static FeatureMode mossMode;
    public static FeatureMode lushCaveMode;
    public static FeatureMode azaleaMode;
    public static FeatureMode dripleafMode;
    public static FeatureMode rootedDirtMode;
    public static FeatureMode hangingRootsMode;
    public static FeatureMode sporeBlossomMode;
    public static boolean sporeBlossomParticlesEnabled;
    public static int sporeBlossomParticleFrequency;
    public static int sporeBlossomParticleDensity;
    public static FeatureMode glowLichenMode;
    public static FeatureMode kelpMode;
    public static FeatureMode seagrassMode;
    public static FeatureMode seaPickleMode;
    public static FeatureMode turtleMode;
    public static FeatureMode crimsonMode;
    public static FeatureMode warpedMode;
    public static FeatureMode crimsonWoodMode;
    public static FeatureMode warpedWoodMode;

    public static int sweetBerryCommonRarity;
    public static int sweetBerryRareRarity;
    public static int sweetBerryPatchAttempts;
    public static int sweetBerryHorizontalOffset;
    public static int sweetBerryVerticalOffset;
    public static int sweetBerryGrowthRoll;
    public static int glowSquidSpawnWeight;
    public static int glowSquidMinGroupSize;
    public static int glowSquidMaxGroupSize;
    public static int glowSquidDepthBelowSeaLevel;
    public static int kelpColdNoiseRatio;
    public static int kelpWarmNoiseRatio;
    public static int kelpNoiseScale;
    public static int kelpWorldgenMaxBodyHeight;
    public static float kelpGrowthChance;
    public static int seagrassWarmAttempts;
    public static int seagrassNormalAttempts;
    public static int seagrassColdAttempts;
    public static int seagrassRiverAttempts;
    public static int seagrassSwampAttempts;
    public static int seagrassDeepWarmAttempts;
    public static int seagrassDeepAttempts;
    public static int seagrassDeepColdAttempts;
    public static int seagrassWarmTallPercent;
    public static int seagrassNormalTallPercent;
    public static int seagrassColdTallPercent;
    public static int seagrassRiverTallPercent;
    public static int seagrassSwampTallPercent;
    public static int seagrassDeepWarmTallPercent;
    public static int seagrassDeepTallPercent;
    public static int seagrassDeepColdTallPercent;
    public static int seaPickleRarity;
    public static int seaPickleWorldgenAttempts;
    public static int seaPickleWorldgenOffsetRadius;
    public static int seaPickleSpreadChanceRoll;
    public static int turtleSpawnWeight;
    public static int turtleMinGroupSize;
    public static int turtleMaxGroupSize;
    public static float turtleEggHatchChance;
    public static float beeNestPlainsChance;
    public static float beeNestFlowerForestChance;
    public static float beeNestForestChance;
    public static int beeNestMinBees;
    public static int beeNestMaxBees;
    public static int lushCaveMinY;
    public static int lushCaveMaxY;
    public static int lushCaveRegionRarity;
    public static int lushCaveCavernsPerRegion;
    public static int lushCaveMinRadius;
    public static int lushCaveMaxRadius;
    public static int lushCaveMinVerticalRadius;
    public static int lushCaveMaxVerticalRadius;
    public static int lushCaveMinRegionRadius;
    public static int lushCaveMaxRegionRadius;
    public static int lushCaveMinBranches;
    public static int lushCaveMaxBranches;
    public static int lushCaveMinBranchLength;
    public static int lushCaveMaxBranchLength;
    public static int lushCaveMinTunnelRadius;
    public static int lushCaveMaxTunnelRadius;
    public static int lushCaveWaterPoolChance;
    public static int lushCaveMossFloorAttempts;
    public static int lushCaveMossCeilingAttempts;
    public static int lushCaveMossPatchMinRadius;
    public static int lushCaveMossPatchMaxRadius;
    public static int lushCaveMossPatchVerticalRange;
    public static int lushCaveMossCeilingMinDepth;
    public static int lushCaveMossCeilingMaxDepth;
    public static float lushCaveMossEdgeColumnChance;
    public static float lushCaveMossFloorVegetationChance;
    public static float lushCaveMossCeilingVineChance;
    public static int lushCaveClayAttempts;
    public static int lushCaveVineAttempts;
    public static int lushCaveSporeBlossomAttempts;
    public static int lushCaveGlowLichenMinAttempts;
    public static int lushCaveGlowLichenMaxAttempts;
    public static int lushCaveGlowLichenSearchRange;
    public static float lushCaveGlowLichenSpreadChance;
    public static int lushCaveClassicVineAttempts;
    public static int lushCaveAzaleaTreeMin;
    public static int lushCaveAzaleaTreeMax;
    public static int lushCaveRootColumnMaxHeight;
    public static int lushCaveRootPlacementAttempts;
    public static int lushCaveRootRadius;
    public static int lushCaveHangingRootPlacementAttempts;
    public static int lushCaveHangingRootRadius;
    public static int lushCaveHangingRootsVerticalSpan;
    public static int lushCaveRequiredVerticalSpaceForTree;
    public static int lushCaveAllowedVerticalWaterForTree;
    public static int lushCaveClayPatchMinRadius;
    public static int lushCaveClayPatchMaxRadius;
    public static int lushCaveClayPatchDepth;
    public static int lushCaveDryClayVerticalRange;
    public static int lushCaveWaterClayVerticalRange;
    public static float lushCaveClayExtraBottomChance;
    public static float lushCaveClayEdgeColumnChance;
    public static int lushCaveDryDripleafChance;
    public static int lushCaveWaterDripleafChance;
    public static int glowLichenSurfaceOffset;
    public static int netherForestBiomeNoiseScale;
    public static float netherForestBiomeThreshold;
    public static int crimsonForestVegetationAttempts;
    public static int crimsonFungiAttempts;
    public static int warpedForestVegetationAttempts;
    public static int warpedFungiAttempts;
    public static int netherSproutsAttempts;
    public static int netherHugeFungusChanceRoll;
    public static int netherVegetationSpreadWidth;
    public static int netherVegetationSpreadHeight;
    public static int weepingVinesAttempts;
    public static int weepingVinesWartPatchAttempts;
    public static int weepingVinesColumnAttempts;
    public static int twistingVinesAttempts;
    public static int twistingVinesSpreadWidth;
    public static int twistingVinesSpreadHeight;
    public static int twistingVinesMaxHeight;

    private FFDConfig() {
    }

    public static void load(File file) {
        Configuration config = new Configuration(file);
        int loadedConfigVersion = config.hasKey(CATEGORY_INTERNAL, "configVersion")
                ? config.get(CATEGORY_INTERNAL, "configVersion", CURRENT_CONFIG_VERSION).getInt()
                : 0;
        Property configVersion = config.get(CATEGORY_INTERNAL, "configVersion", CURRENT_CONFIG_VERSION,
                "内部配置迁移版本，请勿手动修改。")
                .setShowInGui(false);
        sweetBerryMode = readMode(config, "sweetBerryMode", "甜浆果丛内容开关");
        honeyMode = readMode(config, "honeyMode", "蜜蜂与蜂蜜内容开关");
        glowSquidMode = readMode(config, "glowSquidMode", "荧光鱿鱼内容开关");
        glowBerryMode = readMode(config, "glowBerryMode", "发光浆果内容开关");
        mossMode = readMode(config, "mossMode", "苔藓内容开关");
        lushCaveMode = readMode(config, "lushCaveMode", "繁茂洞穴生成开关");
        azaleaMode = readMode(config, "azaleaMode", "杜鹃与杜鹃树内容开关");
        dripleafMode = readMode(config, "dripleafMode", "垂滴叶内容开关");
        rootedDirtMode = readMode(config, "rootedDirtMode", "缠根泥土内容开关");
        hangingRootsMode = readMode(config, "hangingRootsMode", "垂根内容开关");
        sporeBlossomMode = readMode(config, "sporeBlossomMode", "孢子花内容开关");
        sporeBlossomParticlesEnabled = config.getBoolean("sporeBlossomParticlesEnabled", CATEGORY_PARTICLES,
                false, "是否让孢子花产生下落孢子粒子。默认关闭。");
        sporeBlossomParticleFrequency = config.getInt("sporeBlossomParticleFrequency", CATEGORY_PARTICLES,
                1, 1, 10000, "孢子花下落孢子粒子的生成频率分母。1 为每次显示随机刻生成，数值越大越稀疏。");
        sporeBlossomParticleDensity = config.getInt("sporeBlossomParticleDensity", CATEGORY_PARTICLES,
                15, 0, 256, "每次生成的下落孢子粒子数量。默认 15，保持原先 1 个主孢子与 14 个环境孢子。");
        glowLichenMode = readMode(config, "glowLichenMode", "发光地衣内容开关");
        kelpMode = readMode(config, "kelpMode", "海带内容开关");
        seagrassMode = readMode(config, "seagrassMode", "海草内容开关");
        seaPickleMode = readMode(config, "seaPickleMode", "海泡菜内容开关");
        turtleMode = readMode(config, "turtleMode", "海龟内容开关");
        crimsonMode = readMode(config, "crimsonMode", "绯红植物内容开关");
        warpedMode = readMode(config, "warpedMode", "诡异植物内容开关");
        crimsonWoodMode = readMode(config, "crimsonWoodMode", "绯红木材内容开关");
        warpedWoodMode = readMode(config, "warpedWoodMode", "诡异木材内容开关");

        sweetBerryCommonRarity = config.getInt("sweetBerryCommonRarity", CATEGORY_WORLDGEN,
                32, 1, 10000, "普通针叶林与巨型针叶林生成甜浆果丛斑块的区块触发分母，26.3 默认为 32。");
        sweetBerryRareRarity = config.getInt("sweetBerryRareRarity", CATEGORY_WORLDGEN,
                384, 1, 10000, "积雪针叶林生成甜浆果丛斑块的区块触发分母，26.3 默认为 384。");
        sweetBerryPatchAttempts = config.getInt("sweetBerryPatchAttempts", CATEGORY_WORLDGEN,
                96, 0, 1000, "甜浆果丛斑块触发后的放置尝试次数，26.3 默认为 96。");
        sweetBerryHorizontalOffset = config.getInt("sweetBerryHorizontalOffset", CATEGORY_WORLDGEN,
                7, 0, 64, "甜浆果丛斑块水平三角分布的最大偏移，26.3 默认为 7。");
        sweetBerryVerticalOffset = config.getInt("sweetBerryVerticalOffset", CATEGORY_WORLDGEN,
                3, 0, 32, "甜浆果丛斑块垂直三角分布的最大偏移，26.3 默认为 3。");
        sweetBerryGrowthRoll = config.getInt("sweetBerryGrowthRoll", CATEGORY_GROWTH,
                5, 1, 1000, "甜浆果丛每次随机刻尝试生长的分母，默认与高版本的 1/5 尝试概率一致。");

        glowSquidSpawnWeight = config.getInt("glowSquidSpawnWeight", CATEGORY_WORLDGEN,
                10, 0, 1000, "荧光鱿鱼生成权重，26.3 默认值为 10。");
        glowSquidMinGroupSize = config.getInt("glowSquidMinGroupSize", CATEGORY_WORLDGEN,
                4, 1, 64, "荧光鱿鱼生成群体的最小数量，26.3 默认值为 4。");
        glowSquidMaxGroupSize = config.getInt("glowSquidMaxGroupSize", CATEGORY_WORLDGEN,
                6, 1, 64, "荧光鱿鱼生成群体的最大数量，26.3 默认值为 6。");
        glowSquidMaxGroupSize = Math.max(glowSquidMinGroupSize, glowSquidMaxGroupSize);
        glowSquidDepthBelowSeaLevel = config.getInt("glowSquidDepthBelowSeaLevel", CATEGORY_WORLDGEN,
                33, 0, 255, "荧光鱿鱼最高生成位置低于海平面的格数，26.3 默认值为 33。");

        kelpColdNoiseRatio = config.getInt("kelpColdNoiseRatio", CATEGORY_WORLDGEN,
                120, 0, 1000, "海带冷水海洋噪声密度比例，26.3 默认为 120。");
        kelpWarmNoiseRatio = config.getInt("kelpWarmNoiseRatio", CATEGORY_WORLDGEN,
                80, 0, 1000, "海带暖水海洋噪声密度比例，26.3 默认为 80。");
        kelpNoiseScale = config.getInt("kelpNoiseScale", CATEGORY_WORLDGEN,
                80, 1, 10000, "海带噪声采样尺度，26.3 默认为 80。");
        kelpWorldgenMaxBodyHeight = config.getInt("kelpWorldgenMaxBodyHeight", CATEGORY_WORLDGEN,
                9, 0, 64, "海带世界生成时植株中段的最大高度，26.3 默认为 9。");
        kelpGrowthChance = config.getFloat("kelpGrowthChance", CATEGORY_GROWTH,
                0.14F, 0.0F, 1.0F, "海带每次随机刻生长的概率，26.3 默认为 0.14。");

        seagrassWarmAttempts = readSeagrassAttempts(config, "seagrassWarmAttempts", 80, "暖水海洋");
        seagrassNormalAttempts = readSeagrassAttempts(config, "seagrassNormalAttempts", 48, "普通海洋");
        seagrassColdAttempts = readSeagrassAttempts(config, "seagrassColdAttempts", 32, "冷水海洋");
        seagrassRiverAttempts = readSeagrassAttempts(config, "seagrassRiverAttempts", 48, "河流");
        seagrassSwampAttempts = readSeagrassAttempts(config, "seagrassSwampAttempts", 64, "沼泽");
        seagrassDeepWarmAttempts = readSeagrassAttempts(config, "seagrassDeepWarmAttempts", 80, "深暖水海洋");
        seagrassDeepAttempts = readSeagrassAttempts(config, "seagrassDeepAttempts", 48, "深海");
        seagrassDeepColdAttempts = readSeagrassAttempts(config, "seagrassDeepColdAttempts", 40, "深冷水海洋");

        seagrassWarmTallPercent = readTallPercent(config, "seagrassWarmTallPercent", 30, "暖水海洋");
        seagrassNormalTallPercent = readTallPercent(config, "seagrassNormalTallPercent", 30, "普通海洋");
        seagrassColdTallPercent = readTallPercent(config, "seagrassColdTallPercent", 30, "冷水海洋");
        seagrassRiverTallPercent = readTallPercent(config, "seagrassRiverTallPercent", 40, "河流");
        seagrassSwampTallPercent = readTallPercent(config, "seagrassSwampTallPercent", 60, "沼泽");
        seagrassDeepWarmTallPercent = readTallPercent(config, "seagrassDeepWarmTallPercent", 80, "深暖水海洋");
        seagrassDeepTallPercent = readTallPercent(config, "seagrassDeepTallPercent", 80, "深海");
        seagrassDeepColdTallPercent = readTallPercent(config, "seagrassDeepColdTallPercent", 80, "深冷水海洋");

        seaPickleRarity = config.getInt("seaPickleRarity", CATEGORY_WORLDGEN,
                16, 1, 10000, "海泡菜区块生成触发概率的分母，26.3 默认为 16。");
        seaPickleWorldgenAttempts = config.getInt("seaPickleWorldgenAttempts", CATEGORY_WORLDGEN,
                20, 0, 1000, "海泡菜触发生成后进行的放置尝试次数，26.3 默认为 20。");
        seaPickleWorldgenOffsetRadius = config.getInt("seaPickleWorldgenOffsetRadius", CATEGORY_WORLDGEN,
                7, 0, 64, "海泡菜生成点相对区块内基点的三角分布半径，26.3 默认为 7。");
        seaPickleSpreadChanceRoll = config.getInt("seaPickleSpreadChanceRoll", CATEGORY_GROWTH,
                6, 1, 1000, "海泡菜使用骨粉时每个候选位置扩散成功概率的分母，26.3 默认为 6。");

        turtleSpawnWeight = config.getInt("turtleSpawnWeight", CATEGORY_WORLDGEN,
                5, 0, 1000, "海龟在海滩生成的权重，26.3 默认为 5。");
        turtleMinGroupSize = config.getInt("turtleMinGroupSize", CATEGORY_WORLDGEN,
                2, 1, 64, "海龟生成群体最小数量，26.3 默认为 2。");
        turtleMaxGroupSize = config.getInt("turtleMaxGroupSize", CATEGORY_WORLDGEN,
                5, 1, 64, "海龟生成群体最大数量，26.3 默认为 5。");
        turtleEggHatchChance = config.getFloat("turtleEggHatchChance", CATEGORY_GROWTH,
                0.002F, 0.0F, 1.0F, "海龟蛋每次随机刻推进裂纹的概率，26.3 默认为 0.002。 ");

        beeNestPlainsChance = config.getFloat("beeNestPlainsChance", CATEGORY_WORLDGEN,
                0.05F, 0.0F, 1.0F, "平原和向日葵平原中每棵合适树木生成蜂巢的概率，26.3 默认值为 0.05。");
        beeNestFlowerForestChance = config.getFloat("beeNestFlowerForestChance", CATEGORY_WORLDGEN,
                0.02F, 0.0F, 1.0F, "繁花森林中每棵合适树木生成蜂巢的概率，26.3 默认值为 0.02。");
        beeNestForestChance = config.getFloat("beeNestForestChance", CATEGORY_WORLDGEN,
                0.002F, 0.0F, 1.0F, "普通森林中每棵合适树木生成蜂巢的概率，26.3 默认值为 0.002。");
        beeNestMinBees = config.getInt("beeNestMinBees", CATEGORY_WORLDGEN,
                2, 0, 3, "自然生成蜂巢初始容纳蜜蜂的最小数量，26.3 默认值为 2。");
        beeNestMaxBees = Math.max(beeNestMinBees, config.getInt("beeNestMaxBees", CATEGORY_WORLDGEN,
                3, 0, 3, "自然生成蜂巢初始容纳蜜蜂的最大数量，26.3 默认值为 3。"));

        lushCaveMinY = config.getInt("lushCaveMinY", CATEGORY_WORLDGEN,
                8, 0, 255, "繁茂洞穴主生成带的最低 Y 坐标。1.12 无负高度，默认采用 Y=8-55。硫磺泉不在本项目移植范围内。");
        lushCaveMaxY = Math.max(lushCaveMinY, config.getInt("lushCaveMaxY", CATEGORY_WORLDGEN,
                55, 0, 255, "繁茂洞穴主生成带的最高 Y 坐标。1.12 无负高度，默认采用 Y=8-55。"));
        lushCaveRegionRarity = config.getInt("lushCaveRegionRarity", CATEGORY_WORLDGEN,
                24, 1, 10000, "每个 6x6 区块候选区域生成繁茂洞穴网络的触发分母，默认 1/24。");
        lushCaveCavernsPerRegion = config.getInt("lushCaveCavernsPerRegion", CATEGORY_WORLDGEN,
                1, 1, 16, "每个命中的 6x6 区块候选区域生成的独立连通洞穴网络数量。");
        lushCaveMinRadius = config.getInt("lushCaveMinRadius", CATEGORY_WORLDGEN,
                10, 2, 64, "繁茂洞穴洞室的最小水平半径。");
        lushCaveMaxRadius = Math.max(lushCaveMinRadius, config.getInt("lushCaveMaxRadius", CATEGORY_WORLDGEN,
                18, 2, 64, "繁茂洞穴洞室的最大水平半径。"));
        lushCaveMinVerticalRadius = config.getInt("lushCaveMinVerticalRadius", CATEGORY_WORLDGEN,
                8, 2, 32, "繁茂洞穴洞室的最小垂直半径。");
        lushCaveMaxVerticalRadius = Math.max(lushCaveMinVerticalRadius, config.getInt("lushCaveMaxVerticalRadius", CATEGORY_WORLDGEN,
                14, 2, 32, "繁茂洞穴洞室的最大垂直半径。"));
        lushCaveMinRegionRadius = config.getInt("lushCaveMinRegionRadius", CATEGORY_WORLDGEN,
                48, 16, 128, "繁茂洞穴地下群系区域的最小水平半径。区域内已有的原版洞穴也会获得繁茂装饰。");
        lushCaveMaxRegionRadius = Math.max(lushCaveMinRegionRadius,
                config.getInt("lushCaveMaxRegionRadius", CATEGORY_WORLDGEN,
                        72, 16, 128, "繁茂洞穴地下群系区域的最大水平半径。"));
        lushCaveMinBranches = config.getInt("lushCaveMinBranches", CATEGORY_WORLDGEN,
                4, 1, 16, "每个繁茂洞穴区域的最少连通分支数。" );
        lushCaveMaxBranches = Math.max(lushCaveMinBranches,
                config.getInt("lushCaveMaxBranches", CATEGORY_WORLDGEN,
                        7, 1, 16, "每个繁茂洞穴区域的最多连通分支数。"));
        lushCaveMinBranchLength = config.getInt("lushCaveMinBranchLength", CATEGORY_WORLDGEN,
                28, 8, 128, "繁茂洞穴连通分支的最小水平长度。" );
        lushCaveMaxBranchLength = Math.max(lushCaveMinBranchLength,
                config.getInt("lushCaveMaxBranchLength", CATEGORY_WORLDGEN,
                        52, 8, 128, "繁茂洞穴连通分支的最大水平长度。"));
        lushCaveMinTunnelRadius = config.getInt("lushCaveMinTunnelRadius", CATEGORY_WORLDGEN,
                3, 2, 16, "繁茂洞穴分支通道的最小半径。" );
        lushCaveMaxTunnelRadius = Math.max(lushCaveMinTunnelRadius,
                config.getInt("lushCaveMaxTunnelRadius", CATEGORY_WORLDGEN,
                        6, 2, 16, "繁茂洞穴分支通道的最大半径。"));
        lushCaveWaterPoolChance = config.getInt("lushCaveWaterPoolChance", CATEGORY_WORLDGEN,
                50, 0, 100, "繁茂洞穴黏土斑块生成浅水池的百分比。26.3 对应特征以等概率选择有水或无水版本。");
        lushCaveMossFloorAttempts = config.getInt("lushCaveMossFloorAttempts", CATEGORY_WORLDGEN,
                125, 0, 1000, "每个繁茂洞穴生成区域的地面苔藓装饰尝试次数，26.3 默认值为 125。");
        lushCaveMossCeilingAttempts = config.getInt("lushCaveMossCeilingAttempts", CATEGORY_WORLDGEN,
                125, 0, 1000, "每个繁茂洞穴生成区域的顶部苔藓装饰尝试次数，26.3 默认值为 125。");
        lushCaveMossPatchMinRadius = config.getInt("lushCaveMossPatchMinRadius", CATEGORY_WORLDGEN,
                4, 1, 32, "繁茂洞穴苔藓斑块的最小基础半径，26.3 默认范围为 4-7，实际生成时再加 1。");
        lushCaveMossPatchMaxRadius = Math.max(lushCaveMossPatchMinRadius,
                config.getInt("lushCaveMossPatchMaxRadius", CATEGORY_WORLDGEN,
                        7, 1, 32, "繁茂洞穴苔藓斑块的最大基础半径，26.3 默认范围为 4-7，实际生成时再加 1。"));
        lushCaveMossPatchVerticalRange = config.getInt("lushCaveMossPatchVerticalRange", CATEGORY_WORLDGEN,
                5, 1, 32, "苔藓斑块逐列寻找洞穴表面的垂直范围，26.3 默认值为 5。");
        lushCaveMossCeilingMinDepth = config.getInt("lushCaveMossCeilingMinDepth", CATEGORY_WORLDGEN,
                1, 1, 16, "顶部苔藓斑块的最小替换深度，26.3 默认范围为 1-2。");
        lushCaveMossCeilingMaxDepth = Math.max(lushCaveMossCeilingMinDepth,
                config.getInt("lushCaveMossCeilingMaxDepth", CATEGORY_WORLDGEN,
                        2, 1, 16, "顶部苔藓斑块的最大替换深度，26.3 默认范围为 1-2。"));
        lushCaveMossEdgeColumnChance = config.getFloat("lushCaveMossEdgeColumnChance", CATEGORY_WORLDGEN,
                0.3F, 0.0F, 1.0F, "苔藓斑块非角落边缘列的保留概率，26.3 默认值为 0.3。");
        lushCaveMossFloorVegetationChance = config.getFloat("lushCaveMossFloorVegetationChance", CATEGORY_WORLDGEN,
                0.8F, 0.0F, 1.0F, "地面苔藓表面生成植被的概率，26.3 默认值为 0.8。");
        lushCaveMossCeilingVineChance = config.getFloat("lushCaveMossCeilingVineChance", CATEGORY_WORLDGEN,
                0.08F, 0.0F, 1.0F, "顶部苔藓表面生成洞穴藤蔓的概率，26.3 默认值为 0.08。");
        lushCaveClayAttempts = config.getInt("lushCaveClayAttempts", CATEGORY_WORLDGEN,
                62, 0, 1000, "每个繁茂洞穴生成区域的黏土与垂滴叶斑块尝试次数，26.3 默认值为 62。");
        lushCaveVineAttempts = config.getInt("lushCaveVineAttempts", CATEGORY_WORLDGEN,
                188, 0, 1000, "每个繁茂洞穴生成区域的洞穴藤蔓装饰尝试次数，26.3 默认值为 188。");
        lushCaveSporeBlossomAttempts = config.getInt("lushCaveSporeBlossomAttempts", CATEGORY_WORLDGEN,
                25, 0, 1000, "每个繁茂洞穴生成区域的孢子花装饰尝试次数，26.3 默认值为 25。");
        lushCaveGlowLichenMinAttempts = config.getInt("lushCaveGlowLichenMinAttempts", CATEGORY_WORLDGEN,
                104, 0, 4000, "每个繁茂洞穴区块的发光地衣最少尝试次数，26.3 默认范围为 104-157。");
        lushCaveGlowLichenMaxAttempts = Math.max(lushCaveGlowLichenMinAttempts,
                config.getInt("lushCaveGlowLichenMaxAttempts", CATEGORY_WORLDGEN,
                        157, 0, 4000, "每个繁茂洞穴区块的发光地衣最多尝试次数，26.3 默认范围为 104-157。"));
        lushCaveGlowLichenSearchRange = config.getInt("lushCaveGlowLichenSearchRange", CATEGORY_WORLDGEN,
                20, 1, 64, "发光地衣特征寻找可附着位置的最大距离，26.3 默认值为 20。");
        lushCaveGlowLichenSpreadChance = config.getFloat("lushCaveGlowLichenSpreadChance", CATEGORY_WORLDGEN,
                0.5F, 0.0F, 1.0F, "自然生成的发光地衣额外扩散一次的概率，26.3 默认值为 0.5。");
        lushCaveClassicVineAttempts = config.getInt("lushCaveClassicVineAttempts", CATEGORY_WORLDGEN,
                256, 0, 4000, "每个繁茂洞穴区块的普通藤蔓尝试次数，26.3 默认值为 256。");
        lushCaveAzaleaTreeMin = config.getInt("lushCaveAzaleaTreeMin", CATEGORY_WORLDGEN,
                1, 0, 64, "每个繁茂洞穴生成区域的杜鹃树最少尝试次数，26.3 默认范围为 1-2。");
        lushCaveAzaleaTreeMax = Math.max(lushCaveAzaleaTreeMin, config.getInt("lushCaveAzaleaTreeMax", CATEGORY_WORLDGEN,
                2, 0, 64, "每个繁茂洞穴生成区域的杜鹃树最多尝试次数，26.3 默认范围为 1-2。"));
        lushCaveRootColumnMaxHeight = config.getInt("lushCaveRootColumnMaxHeight", CATEGORY_WORLDGEN,
                100, 1, 255, "缠根杜鹃树从洞穴顶部向上寻找树木落点的最大高度，26.3 默认值为 100。" );
        lushCaveRootPlacementAttempts = config.getInt("lushCaveRootPlacementAttempts", CATEGORY_WORLDGEN,
                20, 1, 256, "缠根泥土在根系每一层的放置尝试次数，26.3 默认值为 20。" );
        lushCaveRootRadius = config.getInt("lushCaveRootRadius", CATEGORY_WORLDGEN,
                3, 1, 64, "缠根泥土围绕根系中心的随机分布半径，26.3 默认值为 3。" );
        lushCaveHangingRootPlacementAttempts = config.getInt("lushCaveHangingRootPlacementAttempts", CATEGORY_WORLDGEN,
                20, 1, 256, "每套根系在洞穴侧放置垂根的尝试次数，26.3 默认值为 20。" );
        lushCaveHangingRootRadius = config.getInt("lushCaveHangingRootRadius", CATEGORY_WORLDGEN,
                3, 1, 64, "垂根围绕洞穴侧根系起点的随机分布半径，26.3 默认值为 3。" );
        lushCaveHangingRootsVerticalSpan = config.getInt("lushCaveHangingRootsVerticalSpan", CATEGORY_WORLDGEN,
                2, 1, 16, "垂根围绕洞穴侧根系起点的垂直随机跨度，26.3 默认值为 2。" );
        lushCaveRequiredVerticalSpaceForTree = config.getInt("lushCaveRequiredVerticalSpaceForTree", CATEGORY_WORLDGEN,
                3, 1, 64, "缠根杜鹃树候选落点必须具备的垂直空间，26.3 默认值为 3。" );
        lushCaveAllowedVerticalWaterForTree = config.getInt("lushCaveAllowedVerticalWaterForTree", CATEGORY_WORLDGEN,
                2, 1, 64, "缠根杜鹃树候选落点允许的垂直水层高度，26.3 默认值为 2。" );
        lushCaveClayPatchMinRadius = config.getInt("lushCaveClayPatchMinRadius", CATEGORY_WORLDGEN,
                4, 1, 32, "繁茂洞穴黏土斑块的最小水平半径，26.3 默认范围为 4-7。");
        lushCaveClayPatchMaxRadius = Math.max(lushCaveClayPatchMinRadius, config.getInt("lushCaveClayPatchMaxRadius", CATEGORY_WORLDGEN,
                7, 1, 32, "繁茂洞穴黏土斑块的最大水平半径，26.3 默认范围为 4-7。"));
        lushCaveClayPatchDepth = config.getInt("lushCaveClayPatchDepth", CATEGORY_WORLDGEN,
                3, 1, 16, "繁茂洞穴黏土斑块的向下覆盖深度，26.3 默认值为 3。");
        lushCaveDryClayVerticalRange = config.getInt("lushCaveDryClayVerticalRange", CATEGORY_WORLDGEN,
                2, 1, 32, "无水黏土斑块逐列寻找洞穴地面的垂直范围，26.3 默认值为 2。");
        lushCaveWaterClayVerticalRange = config.getInt("lushCaveWaterClayVerticalRange", CATEGORY_WORLDGEN,
                5, 1, 32, "含水黏土池逐列寻找洞穴地面的垂直范围，26.3 默认值为 5。");
        lushCaveClayExtraBottomChance = config.getFloat("lushCaveClayExtraBottomChance", CATEGORY_WORLDGEN,
                0.8F, 0.0F, 1.0F, "黏土斑块额外向下替换一层的概率，26.3 默认值为 0.8。");
        lushCaveClayEdgeColumnChance = config.getFloat("lushCaveClayEdgeColumnChance", CATEGORY_WORLDGEN,
                0.7F, 0.0F, 1.0F, "黏土斑块非角落边缘列的保留概率，26.3 默认值为 0.7。");
        lushCaveDryDripleafChance = config.getInt("lushCaveDryDripleafChance", CATEGORY_WORLDGEN,
                5, 0, 100, "无水黏土斑块中每个候选位置生成垂滴叶的百分比，26.3 默认值为 5。");
        lushCaveWaterDripleafChance = config.getInt("lushCaveWaterDripleafChance", CATEGORY_WORLDGEN,
                10, 0, 100, "浅水黏土斑块中每个候选位置生成垂滴叶的百分比，26.3 默认值为 10。");
        glowLichenSurfaceOffset = config.getInt("glowLichenSurfaceOffset", CATEGORY_WORLDGEN,
                13, 0, 255, "发光地衣候选位置必须低于当地海床或地表的最小格数，26.3 默认值为 13。");

        netherForestBiomeNoiseScale = config.getInt("netherForestBiomeNoiseScale", CATEGORY_WORLDGEN,
                192, 32, 4096, "绯红森林与诡异森林区域噪声的水平尺度。1.12 无多噪声下界，默认以约 192 格尺度近似 26.3 的连续下界群系分布。");
        netherForestBiomeThreshold = config.getFloat("netherForestBiomeThreshold", CATEGORY_WORLDGEN,
                0.20F, 0.0F, 1.0F, "区域噪声达到此阈值时生成绯红或诡异森林。数值越小，两种森林越常见。");
        crimsonForestVegetationAttempts = config.getInt("crimsonForestVegetationAttempts", CATEGORY_WORLDGEN,
                6, 0, 1000, "每个绯红森林洞穴层的植被特征尝试次数，26.3 默认值为 6。");
        crimsonFungiAttempts = config.getInt("crimsonFungiAttempts", CATEGORY_WORLDGEN,
                8, 0, 1000, "每个绯红森林洞穴层的巨型菌特征尝试次数，26.3 默认值为 8。");
        warpedForestVegetationAttempts = config.getInt("warpedForestVegetationAttempts", CATEGORY_WORLDGEN,
                5, 0, 1000, "每个诡异森林洞穴层的植被特征尝试次数，26.3 默认值为 5。");
        warpedFungiAttempts = config.getInt("warpedFungiAttempts", CATEGORY_WORLDGEN,
                8, 0, 1000, "每个诡异森林洞穴层的巨型菌特征尝试次数，26.3 默认值为 8。");
        netherSproutsAttempts = config.getInt("netherSproutsAttempts", CATEGORY_WORLDGEN,
                4, 0, 1000, "每个诡异森林洞穴层的下界苗特征尝试次数，26.3 默认值为 4。");
        netherHugeFungusChanceRoll = config.getInt("netherHugeFungusChanceRoll", CATEGORY_WORLDGEN,
                1, 1, 1000, "菌类生成尝试执行巨大菌生成的概率分母，26.3 默认每次都尝试，因此为 1。");
        netherVegetationSpreadWidth = config.getInt("netherVegetationSpreadWidth", CATEGORY_WORLDGEN,
                8, 1, 64, "下界森林植被和下界苗特征的水平扩散宽度，内部尝试次数为宽度平方；26.3 默认值为 8。");
        netherVegetationSpreadHeight = config.getInt("netherVegetationSpreadHeight", CATEGORY_WORLDGEN,
                4, 1, 32, "下界森林植被和下界苗特征的垂直扩散高度，26.3 默认值为 4。");
        weepingVinesAttempts = config.getInt("weepingVinesAttempts", CATEGORY_WORLDGEN,
                10, 0, 1000, "每个绯红森林区块的垂泪藤特征尝试次数，26.3 默认值为 10。");
        weepingVinesWartPatchAttempts = config.getInt("weepingVinesWartPatchAttempts", CATEGORY_WORLDGEN,
                200, 0, 4000, "单次垂泪藤特征扩散下界疣块的尝试次数，26.3 默认值为 200。");
        weepingVinesColumnAttempts = config.getInt("weepingVinesColumnAttempts", CATEGORY_WORLDGEN,
                100, 0, 4000, "单次垂泪藤特征生成藤蔓列的尝试次数，26.3 默认值为 100。");
        twistingVinesAttempts = config.getInt("twistingVinesAttempts", CATEGORY_WORLDGEN,
                10, 0, 1000, "每个诡异森林区块的扭曲藤特征尝试次数，26.3 默认值为 10。");
        twistingVinesSpreadWidth = config.getInt("twistingVinesSpreadWidth", CATEGORY_WORLDGEN,
                8, 1, 64, "扭曲藤特征的水平扩散宽度，内部尝试次数为宽度平方；26.3 默认值为 8。");
        twistingVinesSpreadHeight = config.getInt("twistingVinesSpreadHeight", CATEGORY_WORLDGEN,
                4, 1, 32, "扭曲藤特征的垂直扩散高度，26.3 默认值为 4。");
        twistingVinesMaxHeight = config.getInt("twistingVinesMaxHeight", CATEGORY_WORLDGEN,
                8, 1, 64, "扭曲藤特征单列的基础最大高度，26.3 默认值为 8。");

        migrateLegacyDefaults(config, loadedConfigVersion);
        configVersion.set(CURRENT_CONFIG_VERSION);
        boolean bilingualCommentsChanged = applyBilingualComments(config);

        if (config.hasChanged() || bilingualCommentsChanged) {
            config.save();
        }
    }

    private static void migrateLegacyDefaults(Configuration config, int loadedConfigVersion) {
        if (loadedConfigVersion < 2
                && lushCaveMinVerticalRadius == 4 && lushCaveMaxVerticalRadius == 8) {
            lushCaveMinVerticalRadius = 8;
            lushCaveMaxVerticalRadius = 14;
            config.get(CATEGORY_WORLDGEN, "lushCaveMinVerticalRadius", 8).set(8);
            config.get(CATEGORY_WORLDGEN, "lushCaveMaxVerticalRadius", 14).set(14);
        }
        if (loadedConfigVersion < 1 && netherHugeFungusChanceRoll == 8) {
            netherHugeFungusChanceRoll = 1;
            config.get(CATEGORY_WORLDGEN, "netherHugeFungusChanceRoll", 1).set(1);
        }
    }

    private static boolean applyBilingualComments(Configuration config) {
        boolean changed = false;
        for (String categoryName : config.getCategoryNames()) {
            net.minecraftforge.common.config.ConfigCategory category = config.getCategory(categoryName);
            String categoryComment = bilingualCategoryComment(categoryName);
            if (!categoryComment.equals(category.getComment())) {
                category.setComment(categoryComment);
                changed = true;
            }
            for (Map.Entry<String, Property> entry : category.getValues().entrySet()) {
                Property property = entry.getValue();
                String comment = bilingualPropertyComment(property.getComment(), categoryName, entry.getKey());
                if (!comment.equals(property.getComment())) {
                    property.setComment(comment);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static String bilingualCategoryComment(String category) {
        if (CATEGORY_FEATURES.equals(category)) {
            return "内容开关。\nFeature toggles for the backported content.";
        }
        if (CATEGORY_WORLDGEN.equals(category)) {
            return "世界生成设置。\nWorld-generation settings.";
        }
        if (CATEGORY_GROWTH.equals(category)) {
            return "生长设置。\nGrowth settings.";
        }
        if (CATEGORY_PARTICLES.equals(category)) {
            return "粒子视觉设置，仅影响客户端显示。\nParticle visual settings; these affect client-side rendering only.";
        }
        if (CATEGORY_INTERNAL.equals(category)) {
            return "内部设置，请勿手动修改。\nInternal settings; do not edit manually.";
        }
        return "配置分类：" + humanizeKey(category) + "。\nConfiguration category: "
                + humanizeKey(category) + ".";
    }

    private static String bilingualPropertyComment(String comment, String category, String key) {
        String primary = comment == null ? "" : comment;
        int marker = primary.indexOf("\n[English] ");
        if (marker >= 0) {
            primary = primary.substring(0, marker);
        }
        if (primary.isEmpty()) {
            primary = "配置项：" + key + "。";
        }
        return primary + "\n[English] " + englishDescription(category, key);
    }

    private static String englishDescription(String category, String key) {
        if (CATEGORY_FEATURES.equals(category)) {
            return "Feature mode for " + humanizeKey(key.replace("Mode", ""))
                    + ". ENABLED uses this mod's implementation; DISABLED turns it off; AUTO turns it off only when a compatible non-Delight external mod is detected.";
        }
        if ("sporeBlossomParticlesEnabled".equals(key)) {
            return "Enable falling spore particles from spore blossoms. Disabled by default.";
        }
        if ("sporeBlossomParticleFrequency".equals(key)) {
            return "Emission frequency denominator per random display tick. 1 emits every display tick; larger values reduce the frequency.";
        }
        if ("sporeBlossomParticleDensity".equals(key)) {
            return "Number of falling spore particles emitted per burst. The default of 15 matches one primary particle plus fourteen ambient particles.";
        }
        String prefix = CATEGORY_WORLDGEN.equals(category) ? "World-generation setting: "
                : CATEGORY_GROWTH.equals(category) ? "Growth setting: "
                : CATEGORY_PARTICLES.equals(category) ? "Particle setting: "
                : CATEGORY_INTERNAL.equals(category) ? "Internal setting: " : "Configuration setting: ";
        return prefix + humanizeKey(key) + ".";
    }

    private static String humanizeKey(String key) {
        String[] words = key.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1 $2").split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(englishWord(word));
        }
        if (result.length() > 0) {
            result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
        }
        return result.toString();
    }

    private static String englishWord(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        if ("min".equals(lower)) {
            return "minimum";
        }
        if ("max".equals(lower)) {
            return "maximum";
        }
        if ("y".equals(lower)) {
            return "Y";
        }
        if ("id".equals(lower)) {
            return "ID";
        }
        if ("gui".equals(lower)) {
            return "GUI";
        }
        if ("worldgen".equals(lower)) {
            return "world generation";
        }
        return lower;
    }

    private static FeatureMode readMode(Configuration config, String key, String comment) {
        String value = config.getString(key, CATEGORY_FEATURES, "AUTO",
                comment + "。可选值：ENABLED、DISABLED、AUTO。AUTO 会在检测到对应非乐事类外部模组时关闭本地内容。",
                new String[]{"ENABLED", "DISABLED", "AUTO"});
        return FeatureMode.parse(value);
    }

    private static int readSeagrassAttempts(Configuration config, String key, int defaultValue, String biome) {
        return config.getInt(key, CATEGORY_WORLDGEN, defaultValue, 0, 1000,
                biome + "每区块海草生成尝试次数，26.3 默认为 " + defaultValue + "。");
    }

    private static int readTallPercent(Configuration config, String key, int defaultValue, String biome) {
        return config.getInt(key, CATEGORY_WORLDGEN, defaultValue, 0, 100,
                biome + "生成高海草的百分比，26.3 默认为 " + defaultValue + "% 。");
    }
}
