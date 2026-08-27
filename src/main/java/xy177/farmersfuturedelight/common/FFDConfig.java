package xy177.farmersfuturedelight.common;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import xy177.farmersfuturedelight.common.registry.FFDRawOres;

public final class FFDConfig {
    public enum FeatureMode {
        ENABLED,
        DISABLED,
        AUTO;

        public static FeatureMode parse(String value) {
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ENABLED;
            }
        }
    }

    public enum CloudHeightMode {
        DISABLED,
        CAVES_CLIFFS_ONLY,
        ALL_WORLDS;

        public static CloudHeightMode parse(String value) {
            try {
                return valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return CAVES_CLIFFS_ONLY;
            }
        }
    }

    private static final String CATEGORY_FEATURES = "features";
    private static final String CATEGORY_GAMEPLAY = "gameplay";
    private static final String CATEGORY_WORLDGEN = "worldgen";
    private static final String CATEGORY_GROWTH = "growth";
    private static final String CATEGORY_PARTICLES = "particles";
    private static final String CATEGORY_DEBUG = "debug";
    private static final String CATEGORY_INTERNAL = "internal";
    private static final String CATEGORY_COMPATIBILITY = "compatibility";
    private static final int CURRENT_CONFIG_VERSION = 8;

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
    public static FeatureMode axolotlMode;
    public static FeatureMode goatMode;
    public static FeatureMode phantomMode;
    public static FeatureMode othersideMode;
    public static FeatureMode amethystMode;
    public static FeatureMode deepslateMode;
    public static String[] deepslateCompatToggles;
    public static String[] deepslateCompatGenerationToggles;
    public static FeatureMode rawOreMode;
    public static String[] rawOreMaterialToggles;
    public static FeatureMode copperMode;
    public static FeatureMode dripstoneMode;
    public static FeatureMode ironChainMode;
    public static FeatureMode candleMode;
    public static FeatureMode powderSnowMode;
    public static FeatureMode crimsonMode;
    public static FeatureMode warpedMode;
    public static FeatureMode crimsonWoodMode;
    public static FeatureMode warpedWoodMode;
    public static FeatureMode glowItemFrameMode;
    public static FeatureMode signTextMode;
    public static FeatureMode lightBlockMode;

    public static boolean hostileMobsRequireZeroBlockLight;
    public static CloudHeightMode cloudHeightMode;
    public static boolean smoothBiomeSkyColors;
    public static boolean modernWorldLoadingScreen;
    public static boolean cavesAndCliffsBackgroundMusic;
    public static boolean shovelCreatesDirtPath;
    public static boolean shearsStopPlantGrowth;
    public static boolean oresDropRawMaterials;
    public static int rawOreDropAmount;
    public static boolean denseRawOreDrop;
    public static int furnaceOutputAmount;
    public static boolean rawBlockSmelt;
    public static int maceratorRawOutputAmount;
    public static int ic2BlastFurnaceSteelOutputAmount;
    public static int ic2BlastFurnaceSlagOutputAmount;
    public static int enrichmentOutputAmount;
    public static int purificationOutputAmount;
    public static int chemicalInjectionChamberOutputAmount;
    public static int chemicalDissolutionChamberMultiple;
    public static int pulverizerOutputAmount;
    public static int pulverizerSecondaryOutputAmount;
    public static float magmaCrucibleOutputMultiple;
    public static float fluidMultiple;
    public static int inductionSmelterSandOutputAmount;
    public static int inductionSmelterRichSlagOutputAmount;
    public static int inductionSmelterCinnabarOutputAmount;
    public static int metallurgyCrusherOutputAmount;
    public static int galacticraftCompressedOutputAmount;
    public static int galacticraftPlateOutputAmount;
    public static int galaxySpaceModernStorageModuleOutputAmount;
    public static int rollingMachinePlateOutputAmount;
    public static int smallPlatePressOutputAmount;
    public static int latheRodOutputAmount;
    public static int arcFurnaceTitaniumIridiumOutputAmount;
    public static int arcFurnaceTitaniumAluminideOutputAmount;
    public static int techgunsYellowcakeOutputAmount;
    public static int techgunsTitaniumOreOutputAmount;
    public static int techgunsRawIronOutputAmount;
    public static int techgunsChemicalLabAcidAmount;
    public static int techgunsReactionChamberAcidAmount;
    public static int cropariaRefinedOutputAmount;
    public static int mysticalAgricultureRefinedOutputAmount;
    public static boolean modernCauldronFeatures;
    public static boolean enchantingTableEmitsLight;
    public static boolean pistonBreakParticles;
    public static boolean infestedBlocksHalfBreakTime;
    public static boolean shulkerDuplication;
    public static boolean experienceOrbMerging;
    public static boolean illagersIgnoreBabyVillagers;
    public static boolean logAutoCompatibilityDecisions;
    public static String[] beeAdditionalPollinationTargets;
    public static String[] powderSnowMobTransformations;
    public static String[] powderSnowMobImmunities;
    public static String[] powderSnowAdditionalDamage;
    public static String[] powderSnowWalkableBoots;
    public static String[] powderSnowFreezingArmor;
    public static float goatScreamingChance;
    public static float goatSingleHornChance;
    public static int goatLongJumpCooldownMinTicks;
    public static int goatLongJumpCooldownMaxTicks;
    public static int goatLongJumpHorizontalRange;
    public static int goatLongJumpVerticalRange;
    public static int goatLongJumpPrepareTicks;
    public static float goatLongJumpVelocityMultiplier;
    public static int goatRamCooldownMinTicks;
    public static int goatRamCooldownMaxTicks;
    public static int screamingGoatRamCooldownMinTicks;
    public static int screamingGoatRamCooldownMaxTicks;
    public static int goatRamPrepareTicks;
    public static int goatRamMinDistance;
    public static int goatRamMaxDistance;
    public static float goatRamSpeedMultiplier;
    public static float goatAdultRamKnockback;
    public static float goatBabyRamKnockback;

    public static float dripstoneGrowthChance;
    public static float dripstoneWaterTransferChance;
    public static float dripstoneLavaTransferChance;
    public static float cauldronRainFillChance;
    public static float cauldronSnowFillChance;

    public static int sweetBerryCommonRarity;
    public static int sweetBerryRareRarity;
    public static int sweetBerryPatchAttempts;
    public static int sweetBerryHorizontalOffset;
    public static int sweetBerryVerticalOffset;
    public static int sweetBerryGrowthRoll;
    public static int amethystGrowthRoll;
    public static int amethystGeodeRarity;
    public static int amethystGeodeMinY;
    public static int amethystGeodeMaxY;
    public static int amethystGeodeOuterWallMin;
    public static int amethystGeodeOuterWallMax;
    public static int amethystGeodeDistributionMin;
    public static int amethystGeodeDistributionMax;
    public static int amethystGeodePointOffsetMin;
    public static int amethystGeodePointOffsetMax;
    public static float amethystGeodeCrackChance;
    public static float amethystGeodeBuddingChance;
    public static float amethystGeodeBudPlacementChance;
    public static int deepslateTransitionMinY;
    public static int deepslateTransitionMaxY;
    public static int modernOreVeinSizePercent;
    public static int modernFossilRarity;
    public static int modernVillageSpacing;
    public static String[] cavesAndCliffsAdditionalBiomeCandidates;
    public static int modernCoalUpperCount;
    public static int modernCoalLowerCount;
    public static int modernIronUpperCount;
    public static int modernIronMiddleCount;
    public static int modernIronSmallCount;
    public static int modernGoldCount;
    public static int modernGoldLowerMaxCount;
    public static int modernGoldExtraCount;
    public static int modernRedstoneCount;
    public static int modernRedstoneLowerCount;
    public static int modernDiamondCount;
    public static int modernDiamondBuriedCount;
    public static int modernDiamondLargeRarity;
    public static int modernLapisCount;
    public static int modernLapisBuriedCount;
    public static int modernCopperCount;
    public static int modernCopperLargeCount;
    public static int modernEmeraldCount;
    public static int modernTuffCount;
    public static int modernInfestedCount;
    public static int modernGraniteLowerCount;
    public static int modernGraniteUpperRarity;
    public static int modernDioriteLowerCount;
    public static int modernDioriteUpperRarity;
    public static int modernAndesiteLowerCount;
    public static int modernAndesiteUpperRarity;
    public static int modernDirtCount;
    public static int modernGravelCount;
    public static int glowSquidSpawnWeight;
    public static int glowSquidMinGroupSize;
    public static int glowSquidMaxGroupSize;
    public static int glowSquidDepthBelowSeaLevel;
    public static int phantomSpawnMinIntervalSeconds;
    public static int phantomSpawnMaxIntervalSeconds;
    public static int phantomInsomniaThresholdTicks;
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
    public static int axolotlSpawnCheckIntervalTicks;
    public static int axolotlSpawnAttemptsPerPlayer;
    public static int axolotlMinGroupSize;
    public static int axolotlMaxGroupSize;
    public static int axolotlMobCap;
    public static int goatSpawnWeight;
    public static int goatMinGroupSize;
    public static int goatMaxGroupSize;
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
    public static float lushCaveTallGrassNoiseThreshold;
    public static int lushCaveTallGrassAboveNoiseCount;
    public static int lushCaveTallGrassRarity;
    public static int lushCaveTallGrassPatchAttempts;
    public static int lushCaveTallGrassHorizontalOffset;
    public static int lushCaveTallGrassVerticalOffset;
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
    public static int dripstoneLargeMinAttempts;
    public static int dripstoneLargeMaxAttempts;
    public static int dripstoneClusterMinAttempts;
    public static int dripstoneClusterMaxAttempts;
    public static int dripstonePointedMinAttempts;
    public static int dripstonePointedMaxAttempts;
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

    private static Map<String, Boolean> rawOreMaterialStates = defaultRawOreMaterialStates();

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
        axolotlMode = readMode(config, "axolotlMode", "美西螈与美西螈桶内容开关");
        goatMode = readMode(config, "goatMode", "山羊与山羊角内容开关");
        phantomMode = readMode(config, "phantomMode", "幻翼与幻翼膜内容开关");
        othersideMode = readMode(config, "othersideMode", "《otherside》音乐唱片内容开关");
        amethystMode = readMode(config, "amethystMode", "紫水晶与紫水晶晶洞内容开关");
        deepslateMode = readMode(config, "deepslateMode", "深板岩、凝灰岩与深层矿石内容开关");
        deepslateCompatToggles = config.getStringList(
                "deepslateCompatToggles", CATEGORY_FEATURES,
                new String[]{"mekanism:osmium=true", "mekanism:tin=true",
                        "mekanism:lead=true", "mekanism:uranium=true",
                        "thermalfoundation:tin=true", "thermalfoundation:silver=true",
                        "thermalfoundation:lead=true", "thermalfoundation:nickel=true",
                        "immersiveengineering:aluminium=true", "immersiveengineering:lead=true",
                        "immersiveengineering:silver=true", "immersiveengineering:nickel=true",
                        "immersiveengineering:uranium=true", "techreborn:aluminium=true",
                        "techreborn:iridium=true", "techreborn:lead=true",
                        "techreborn:silver=true", "techreborn:tin=true",
                        "simpleores:tin=true", "simpleores:mythril=true",
                        "simpleores:adamantium=true"},
                "逐个控制来源模组深层矿石兼容。格式为模组ID:材质=true或false；相同材质最终共用一个深层矿石对象；默认开启已确认的兼容对象，修改后需重启游戏。");
        deepslateCompatGenerationToggles = config.getStringList(
                "deepslateCompatGenerationToggles", CATEGORY_WORLDGEN,
                new String[]{"mekanism:osmium=4,8,-64,-1,uniform,0.0", "mekanism:tin=4,6,-64,-1,uniform,0.0",
                        "mekanism:lead=8,9,-88,64,trapezoid,0.25,0",
                        "mekanism:uranium#small=4,4,-64,8,trapezoid,0.0,0",
                        "mekanism:uranium#buried=7,9,-88,-8,trapezoid,0.75,16",
                        "thermalfoundation:tin=6,8,-64,-1,uniform,0.0", "thermalfoundation:silver=4,8,-64,-1,uniform,0.0",
                        "thermalfoundation:lead=6,8,-64,-1,uniform,0.0", "thermalfoundation:nickel=4,8,-64,-1,uniform,0.0",
                        "immersiveengineering:aluminium=6,8,-64,-1,uniform,0.0", "immersiveengineering:lead=6,8,-64,-1,uniform,0.0",
                        "immersiveengineering:silver=5,8,-64,-1,uniform,0.0", "immersiveengineering:nickel=4,6,-64,-1,uniform,0.0",
                        "immersiveengineering:uranium=5,6,-64,-1,uniform,0.0", "techreborn:aluminium=4,6,-64,-1,uniform,0.0",
                        "techreborn:iridium=2,3,-64,-1,uniform,0.0", "techreborn:lead=5,6,-64,-1,uniform,0.0",
                        "techreborn:silver=5,6,-64,-1,uniform,0.0", "techreborn:tin=6,8,-64,-1,uniform,0.0",
                        "simpleores:tin#intrusion=0,7,0,72,uniform,0.0",
                        "simpleores:mythril#deposit=8,4,-63,32,trapezoid,0.0",
                        "simpleores:mythril#intrusion=4,4,-64,35,uniform,0.0",
                        "simpleores:adamantium#deposit=4,4,-63,-16,trapezoid,0.0",
                        "simpleores:adamantium#intrusion=4,4,-64,20,uniform,0.0"},
                "逐个控制来源模组深层矿石生成次数、矿脉大小和分布。格式为模组ID:材质=次数,大小；也可写为次数,大小,最低高度,最高高度,uniform或trapezoid,空气暴露跳过概率；梯形分布还可在末尾填写平顶宽度；相同材质的来源规则会生成到同一个深层矿石对象；修改后需重启游戏。");
        rawOreMode = readMode(config, "rawOreMode", "粗铁、粗金及对应粗矿块内容开关");
        rawOreMaterialToggles = config.getStringList(
                "rawOreMaterialToggles", CATEGORY_FEATURES, defaultRawOreMaterialToggles(),
                "逐种控制粗矿及其粗矿块、掉落替换和联动配方。格式为材质内部名称=true或false；默认全部为true。缺少的材质按true处理，修改后需重启游戏。");
        rawOreMaterialStates = parseRawOreMaterialStates(rawOreMaterialToggles);
        copperMode = readMode(config, "copperMode", "铜矿石、粗铜与铜制内容开关");
        dripstoneMode = readMode(config, "dripstoneMode", "滴水石块与滴水石锥内容开关");
        ironChainMode = readMode(config, "ironChainMode", "铁链内容开关");
        candleMode = readMode(config, "candleMode", "蜡烛与蜡烛蛋糕内容开关");
        powderSnowMode = readMode(config, "powderSnowMode", "细雪与细雪桶内容开关");
        crimsonMode = readMode(config, "crimsonMode", "绯红植物内容开关");
        warpedMode = readMode(config, "warpedMode", "诡异植物内容开关");
        crimsonWoodMode = readMode(config, "crimsonWoodMode", "绯红木材内容开关");
        warpedWoodMode = readMode(config, "warpedWoodMode", "诡异木材内容开关");
        glowItemFrameMode = readMode(config, "glowItemFrameMode", "荧光物品展示框内容开关");
        signTextMode = readMode(config, "signTextMode", "告示牌文字染色与发光内容开关");
        lightBlockMode = readMode(config, "lightBlockMode", "光源方块内容开关");

        hostileMobsRequireZeroBlockLight = config.getBoolean(
                "hostileMobsRequireZeroBlockLight", CATEGORY_GAMEPLAY, true,
                "是否要求自然生成的敌对生物所在位置方块光照必须为 0。对所有世界类型生效，不影响刷怪笼。");
        cloudHeightMode = CloudHeightMode.parse(config.getString("cloudHeightMode",
                CATEGORY_GAMEPLAY, "CAVES_CLIFFS_ONLY",
                "高版本云层高度的作用范围。DISABLED 关闭；CAVES_CLIFFS_ONLY 仅作用于“洞穴与山崖”世界；ALL_WORLDS 作用于所有世界类型。",
                new String[]{"DISABLED", "CAVES_CLIFFS_ONLY", "ALL_WORLDS"}));
        smoothBiomeSkyColors = config.getBoolean("smoothBiomeSkyColors", CATEGORY_GAMEPLAY,
                true, "是否在群系边界平滑混合天空颜色。关闭时直接使用玩家所在位置群系的天空颜色。仅影响客户端显示。");
        modernWorldLoadingScreen = config.getBoolean("modernWorldLoadingScreen",
                CATEGORY_GAMEPLAY, false,
                "是否在创建或进入世界时使用高版本风格的加载画面。关闭时所有世界类型均使用原版 1.12.2 加载画面。仅影响客户端显示。");
        cavesAndCliffsBackgroundMusic = config.getBoolean("cavesAndCliffsBackgroundMusic",
                CATEGORY_GAMEPLAY, true,
                "是否将 1.18 的八首背景音乐加入所有世界类型的生存模式与主菜单音乐池。");
        shovelCreatesDirtPath = config.getBoolean("shovelCreatesDirtPath", CATEGORY_GAMEPLAY,
                true, "是否允许使用锹将泥土、砂土、灰化土和菌丝转化为土径。");
        shearsStopPlantGrowth = config.getBoolean("shearsStopPlantGrowth", CATEGORY_GAMEPLAY,
                true, "是否允许使用剪刀停止海带、洞穴藤蔓、垂泪藤和缠怨藤继续生长。");
        oresDropRawMaterials = config.getBoolean("oresDropRawMaterials", CATEGORY_GAMEPLAY,
                true, "是否让可识别的矿石掉落对应粗矿并支持时运；精准采集仍获得矿石方块，自动冶炼后的产物保持不变。对所有世界类型生效。");
        rawOreDropAmount = config.getInt("rawOreDropAmount", CATEGORY_COMPATIBILITY,
                1, 1, 64, "除铜外的所有受支持矿石被转换为粗矿时的基础掉落数量；Raw Ore 1.5.4 默认值为 1。");
        denseRawOreDrop = config.getBoolean("denseRawOreDrop", CATEGORY_COMPATIBILITY,
                true, "是否按 Dense Metals 的富集矿石倍率增加粗矿掉落；Raw Ore 1.5.4 默认开启。");
        furnaceOutputAmount = config.getInt("furnaceOutputAmount", CATEGORY_COMPATIBILITY,
                1, 1, 64, "熔炉处理粗矿时的锭产物数量；Raw Ore 1.5.4 默认值为 1。");
        rawBlockSmelt = config.getBoolean("rawBlockSmelt", CATEGORY_COMPATIBILITY,
                false, "是否允许粗矿块直接熔炼为对应锭块；Raw Ore 1.5.4 默认关闭。");
        maceratorRawOutputAmount = config.getInt("maceratorRawOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "工业时代粉碎机处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 2。");
        ic2BlastFurnaceSteelOutputAmount = config.getInt(
                "ic2BlastFurnaceSteelOutputAmount", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "工业时代工业高炉处理粗铁时的钢锭产物数量；Raw Ore 1.5.4 默认值为 1。");
        ic2BlastFurnaceSlagOutputAmount = config.getInt(
                "ic2BlastFurnaceSlagOutputAmount", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "工业时代工业高炉处理粗铁时的炉渣副产物数量；Raw Ore 1.5.4 默认值为 1。");
        enrichmentOutputAmount = config.getInt("enrichmentOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "通用机械富集室处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 2。");
        purificationOutputAmount = config.getInt("purificationOutputAmount",
                CATEGORY_COMPATIBILITY, 3, 1, 64,
                "通用机械净化室处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 3。");
        chemicalInjectionChamberOutputAmount = config.getInt(
                "chemicalInjectionChamberOutputAmount", CATEGORY_COMPATIBILITY, 4, 1, 64,
                "通用机械化学注入室处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 4。");
        chemicalDissolutionChamberMultiple = config.getInt(
                "chemicalDissolutionChamberMultiple", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "通用机械化学溶解室粗矿处理倍率；Raw Ore 1.5.4 默认值为 1。");
        pulverizerOutputAmount = config.getInt("pulverizerOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "热力膨胀粉碎机处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 2。");
        pulverizerSecondaryOutputAmount = config.getInt("pulverizerSecondaryOutputAmount",
                CATEGORY_COMPATIBILITY, 1, 1, 64,
                "热力膨胀粉碎机处理粗矿时的副产物数量；Raw Ore 1.5.4 默认值为 1。");
        magmaCrucibleOutputMultiple = config.getFloat("magmaCrucibleOutputMultiple",
                CATEGORY_COMPATIBILITY, 1.0F, 0.0F, 64.0F,
                "热力膨胀岩浆熔炉粗矿流体输出倍率；Raw Ore 1.5.4 默认值为 1。");
        fluidMultiple = config.getFloat("fluidMultiple", CATEGORY_COMPATIBILITY,
                1.0F, 0.0F, 64.0F,
                "匠魂粗矿熔融流体输出倍率；Raw Ore 1.5.4 默认值为 1。");
        inductionSmelterSandOutputAmount = config.getInt("inductionSmelterSandOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "热力膨胀感应炉使用沙处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 2。");
        inductionSmelterRichSlagOutputAmount = config.getInt(
                "inductionSmelterRichSlagOutputAmount", CATEGORY_COMPATIBILITY, 3, 1, 64,
                "热力膨胀感应炉使用富集炉渣处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 3。");
        inductionSmelterCinnabarOutputAmount = config.getInt(
                "inductionSmelterCinnabarOutputAmount", CATEGORY_COMPATIBILITY, 3, 1, 64,
                "热力膨胀感应炉使用朱砂处理粗矿时的主产物数量；Raw Ore 1.5.4 默认值为 3。");
        metallurgyCrusherOutputAmount = config.getInt("metallurgyCrusherOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "冶金4：重铸粉碎机处理粗矿时的产物数量；Raw Ore 1.5.4 默认值为 2。");
        galacticraftCompressedOutputAmount = config.getInt(
                "galacticraftCompressedOutputAmount", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "星系压缩机处理两个锭时的压缩材料产物数量；Raw Ore 1.5.4 默认值为 1。");
        galacticraftPlateOutputAmount = config.getInt(
                "galacticraftPlateOutputAmount", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "星系压缩机处理工业时代锭时的板材产物数量；Raw Ore 1.5.4 默认值为 1。");
        galaxySpaceModernStorageModuleOutputAmount = config.getInt(
                "galaxySpaceModernStorageModuleOutputAmount", CATEGORY_COMPATIBILITY, 1, 1, 64,
                "Galaxy Space 现代储能模块配方的产物数量；Raw Ore 1.5.4 默认值为 1。");
        rollingMachinePlateOutputAmount = config.getInt("rollingMachinePlateOutputAmount",
                CATEGORY_COMPATIBILITY, 1, 1, 64,
                "高级火箭轧制机处理粗矿时的板材产物数量；Raw Ore 1.5.4 默认值为 1。");
        smallPlatePressOutputAmount = config.getInt("smallPlatePressOutputAmount",
                CATEGORY_COMPATIBILITY, 4, 1, 64,
                "高级火箭小型板材压机处理粗矿块时的板材产物数量；Raw Ore 1.5.4 默认值为 4。");
        latheRodOutputAmount = config.getInt("latheRodOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "高级火箭车床处理粗矿时的杆产物数量；Raw Ore 1.5.4 默认值为 2。");
        arcFurnaceTitaniumIridiumOutputAmount = config.getInt(
                "arcFurnaceTitaniumIridiumOutputAmount", CATEGORY_COMPATIBILITY, 2, 1, 64,
                "高级火箭电弧高炉钛铱合金配方的产物数量；Raw Ore 1.5.4 默认值为 2。");
        arcFurnaceTitaniumAluminideOutputAmount = config.getInt(
                "arcFurnaceTitaniumAluminideOutputAmount", CATEGORY_COMPATIBILITY, 3, 1, 64,
                "高级火箭电弧高炉钛铝合金配方的产物数量；Raw Ore 1.5.4 默认值为 3。");
        techgunsYellowcakeOutputAmount = config.getInt("techgunsYellowcakeOutputAmount",
                CATEGORY_COMPATIBILITY, 3, 1, 64,
                "科技枪化学实验室处理粗铀时的黄饼产物数量；Raw Ore 1.5.4 默认值为 3。");
        techgunsTitaniumOreOutputAmount = config.getInt("techgunsTitaniumOreOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "科技枪反应室处理粗钛时的钛矿石产物数量；Raw Ore 1.5.4 默认值为 2。");
        techgunsRawIronOutputAmount = config.getInt("techgunsRawIronOutputAmount",
                CATEGORY_COMPATIBILITY, 1, 1, 64,
                "科技枪反应室处理粗钛时的粗铁副产物数量；Raw Ore 1.5.4 默认值为 1。");
        techgunsChemicalLabAcidAmount = config.getInt("techgunsChemicalLabAcidAmount",
                CATEGORY_COMPATIBILITY,
                250, 1, 16000,
                "科技枪化学实验室粗铀配方消耗的酸液数量，单位为 mB；Raw Ore 1.5.4 默认值为 250。");
        techgunsReactionChamberAcidAmount = config.getInt(
                "techgunsReactionChamberAcidAmount", CATEGORY_COMPATIBILITY,
                100, 1, 16000,
                "科技枪反应室粗钛配方消耗的酸液数量，单位为 mB；Raw Ore 1.5.4 默认值为 100。");
        cropariaRefinedOutputAmount = config.getInt("cropariaRefinedOutputAmount",
                CATEGORY_COMPATIBILITY, 2, 1, 64,
                "矿石作物果实配方的精炼产物数量；Raw Ore 1.5.4 默认值为 2。");
        mysticalAgricultureRefinedOutputAmount = config.getInt(
                "mysticalAgricultureRefinedOutputAmount", CATEGORY_COMPATIBILITY, 4, 1, 64,
                "神秘农业精华配方的精炼产物数量；Raw Ore 1.5.4 默认值为 4。");
        modernCauldronFeatures = config.getBoolean("modernCauldronFeatures", CATEGORY_GAMEPLAY,
                true, "是否启用熔岩炼药锅、细雪炼药锅、滴水石填充与降雪积累等高版本炼药锅行为。对所有世界类型生效。");
        enchantingTableEmitsLight = config.getBoolean("enchantingTableEmitsLight", CATEGORY_GAMEPLAY,
                true, "是否让附魔台发出 7 级方块光照。对所有世界类型生效。");
        pistonBreakParticles = config.getBoolean("pistonBreakParticles", CATEGORY_GAMEPLAY,
                true, "是否让活塞破坏方块时显示对应方块的破坏粒子。对所有世界类型生效。");
        infestedBlocksHalfBreakTime = config.getBoolean("infestedBlocksHalfBreakTime", CATEGORY_GAMEPLAY,
                true, "是否让虫蚀方块拥有对应普通方块一半的挖掘时间。对所有世界类型生效。");
        shulkerDuplication = config.getBoolean("shulkerDuplication", CATEGORY_GAMEPLAY,
                true, "是否允许潜影贝被潜影弹命中后按高版本规则复制。对所有世界类型生效。");
        experienceOrbMerging = config.getBoolean("experienceOrbMerging", CATEGORY_GAMEPLAY,
                true, "是否允许相同经验值的经验球按高版本规则合并，且拾取时不损失经验。对所有世界类型生效。");
        illagersIgnoreBabyVillagers = config.getBoolean("illagersIgnoreBabyVillagers", CATEGORY_GAMEPLAY,
                true, "是否让灾厄村民不再主动攻击或伤害幼年村民。对所有世界类型生效。");
        beeAdditionalPollinationTargets = config.getStringList(
                "beeAdditionalPollinationTargets", CATEGORY_GAMEPLAY, new String[0],
                "蜜蜂可额外用于授粉的方块或方块状态。支持 模组ID:方块ID、模组ID:方块ID@metadata、模组ID:方块ID[属性=值,属性=值] 三种格式。");
        powderSnowMobTransformations = config.getStringList(
                "powderSnowMobTransformations", CATEGORY_GAMEPLAY,
                new String[]{"minecraft:skeleton;minecraft:stray"},
                "细雪中的生物转换规则。每项使用 源生物注册名;目标生物注册名，例如 minecraft:pig;minecraft:creeper。默认保留骷髅转化为流浪者。");
        powderSnowMobImmunities = config.getStringList(
                "powderSnowMobImmunities", CATEGORY_GAMEPLAY,
                new String[]{"minecraft:stray", "minecraft:polar_bear", "minecraft:snowman", "minecraft:wither"},
                "免疫细雪冻结效果的生物注册名列表。旁观者和穿戴皮革盔甲的生物仍按独立规则处理。");
        powderSnowAdditionalDamage = config.getStringList(
                "powderSnowAdditionalDamage", CATEGORY_GAMEPLAY,
                new String[]{"minecraft:blaze;5.0", "minecraft:magma_cube;5.0"},
                "完全冻结后在细雪中受到额外伤害的生物规则。每项使用 生物注册名;伤害值，例如 minecraft:blaze;5.0。");
        powderSnowWalkableBoots = config.getStringList(
                "powderSnowWalkableBoots", CATEGORY_GAMEPLAY, new String[0],
                "穿戴后可在细雪表面行走而不陷入的靴子物品注册名列表，例如 twilightforest:yeti_boots。皮革靴仍始终有效。");
        powderSnowFreezingArmor = config.getStringList(
                "powderSnowFreezingArmor", CATEGORY_GAMEPLAY, new String[0],
                "穿戴后可免疫细雪冻结的盔甲物品注册名列表。任意装备栏中有一件匹配装备即可生效；原版皮革盔甲仍始终有效，例如 twilightforest:yeti_chestplate。");
        logAutoCompatibilityDecisions = config.getBoolean("logAutoCompatibilityDecisions", CATEGORY_DEBUG,
                false, "是否在日志中逐项显示 AUTO 自动避让的内容与世界生成判定。默认关闭；仅建议排查兼容问题时开启。");

        goatScreamingChance = config.getFloat("goatScreamingChance", CATEGORY_GAMEPLAY,
                0.02F, 0.0F, 1.0F, "山羊生成或繁殖时成为尖叫山羊的基础概率，26.3 默认值为 0.02。");
        goatSingleHornChance = config.getFloat("goatSingleHornChance", CATEGORY_GAMEPLAY,
                0.1F, 0.0F, 1.0F, "成年山羊初次生成时随机缺少一只角的概率，26.3 默认值为 0.1。");
        goatLongJumpCooldownMinTicks = config.getInt("goatLongJumpCooldownMinTicks", CATEGORY_GAMEPLAY,
                600, 1, 72000, "山羊两次长跳之间的最短冷却，单位为 tick；26.3 默认值为 600。");
        goatLongJumpCooldownMaxTicks = Math.max(goatLongJumpCooldownMinTicks,
                config.getInt("goatLongJumpCooldownMaxTicks", CATEGORY_GAMEPLAY,
                        1200, 1, 72000, "山羊两次长跳之间的最长冷却，单位为 tick；26.3 默认值为 1200。"));
        goatLongJumpHorizontalRange = config.getInt("goatLongJumpHorizontalRange", CATEGORY_GAMEPLAY,
                5, 1, 16, "山羊寻找长跳落点时的最大水平范围，26.3 默认值为 5 格。");
        goatLongJumpVerticalRange = config.getInt("goatLongJumpVerticalRange", CATEGORY_GAMEPLAY,
                5, 1, 16, "山羊寻找长跳落点时的最大垂直范围，26.3 默认值为 5 格。");
        goatLongJumpPrepareTicks = config.getInt("goatLongJumpPrepareTicks", CATEGORY_GAMEPLAY,
                40, 0, 200, "山羊选定长跳落点后的准备时间，单位为 tick；26.3 默认值为 40。");
        goatLongJumpVelocityMultiplier = config.getFloat("goatLongJumpVelocityMultiplier",
                CATEGORY_GAMEPLAY, 3.5714288F, 0.1F, 10.0F,
                "山羊长跳最大起跳速度相对基础跳跃强度的倍率，26.3 默认值为 3.5714288。");
        goatRamCooldownMinTicks = config.getInt("goatRamCooldownMinTicks", CATEGORY_GAMEPLAY,
                600, 1, 72000, "普通山羊两次冲撞之间的最短冷却，单位为 tick；26.3 默认值为 600。");
        goatRamCooldownMaxTicks = Math.max(goatRamCooldownMinTicks,
                config.getInt("goatRamCooldownMaxTicks", CATEGORY_GAMEPLAY,
                        6000, 1, 72000, "普通山羊两次冲撞之间的最长冷却，单位为 tick；26.3 默认值为 6000。"));
        screamingGoatRamCooldownMinTicks = config.getInt("screamingGoatRamCooldownMinTicks",
                CATEGORY_GAMEPLAY, 100, 1, 72000,
                "尖叫山羊两次冲撞之间的最短冷却，单位为 tick；26.3 默认值为 100。");
        screamingGoatRamCooldownMaxTicks = Math.max(screamingGoatRamCooldownMinTicks,
                config.getInt("screamingGoatRamCooldownMaxTicks", CATEGORY_GAMEPLAY,
                        300, 1, 72000, "尖叫山羊两次冲撞之间的最长冷却，单位为 tick；26.3 默认值为 300。"));
        goatRamPrepareTicks = config.getInt("goatRamPrepareTicks", CATEGORY_GAMEPLAY,
                20, 0, 200, "山羊到达起跑点后低头准备冲撞的时间，单位为 tick；26.3 默认值为 20。");
        goatRamMinDistance = config.getInt("goatRamMinDistance", CATEGORY_GAMEPLAY,
                4, 1, 32, "山羊冲撞起跑点与目标之间的最短距离，26.3 默认值为 4 格。");
        goatRamMaxDistance = Math.max(goatRamMinDistance,
                config.getInt("goatRamMaxDistance", CATEGORY_GAMEPLAY,
                        7, 1, 32, "山羊冲撞起跑点与目标之间的最长距离，26.3 默认值为 7 格。"));
        goatRamSpeedMultiplier = config.getFloat("goatRamSpeedMultiplier", CATEGORY_GAMEPLAY,
                3.0F, 0.1F, 10.0F, "山羊正式冲撞阶段的移动速度倍率，26.3 默认值为 3.0。");
        goatAdultRamKnockback = config.getFloat("goatAdultRamKnockback", CATEGORY_GAMEPLAY,
                2.5F, 0.0F, 16.0F, "成年山羊冲撞的基础击退力，26.3 默认值为 2.5。");
        goatBabyRamKnockback = config.getFloat("goatBabyRamKnockback", CATEGORY_GAMEPLAY,
                1.0F, 0.0F, 16.0F, "幼年山羊冲撞的基础击退力，26.3 默认值为 1.0。");

        dripstoneGrowthChance = config.getFloat("dripstoneGrowthChance", CATEGORY_GROWTH,
                0.011377778F, 0.0F, 1.0F,
                "滴水石锥每次随机刻尝试自然生长的概率；26.3 默认值为 0.011377778。");
        dripstoneWaterTransferChance = config.getFloat("dripstoneWaterTransferChance", CATEGORY_GROWTH,
                0.17578125F, 0.0F, 1.0F,
                "钟乳石每次随机刻尝试把水滴入炼药锅的概率；26.3 默认值为 0.17578125。");
        dripstoneLavaTransferChance = config.getFloat("dripstoneLavaTransferChance", CATEGORY_GROWTH,
                0.05859375F, 0.0F, 1.0F,
                "钟乳石每次随机刻尝试把熔岩滴入炼药锅的概率；26.3 默认值为 0.05859375。");
        cauldronRainFillChance = config.getFloat("cauldronRainFillChance", CATEGORY_WORLDGEN,
                0.05F, 0.0F, 1.0F,
                "空炼药锅每次降水处理积累一级雨水的概率；26.3 默认值为 0.05。");
        cauldronSnowFillChance = config.getFloat("cauldronSnowFillChance", CATEGORY_WORLDGEN,
                0.1F, 0.0F, 1.0F,
                "空炼药锅或细雪炼药锅每次降雪处理积累一级细雪的概率；26.3 默认值为 0.1。");

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

        amethystGrowthRoll = config.getInt("amethystGrowthRoll", CATEGORY_GROWTH,
                5, 1, 1000, "紫水晶母岩每次随机刻尝试生长晶芽的分母，26.3 默认值为 5。");
        amethystGeodeRarity = config.getInt("amethystGeodeRarity", CATEGORY_WORLDGEN,
                24, 1, 100000, "紫水晶晶洞的区块触发分母，26.3 默认值为 24。");
        amethystGeodeMinY = config.getInt("amethystGeodeMinY", CATEGORY_WORLDGEN,
                -58, -64, 319, "紫水晶晶洞中心的最低生成高度，26.3 默认值为 -58。");
        amethystGeodeMaxY = config.getInt("amethystGeodeMaxY", CATEGORY_WORLDGEN,
                30, -64, 319, "紫水晶晶洞中心的最高生成高度，26.3 默认值为 30。");
        amethystGeodeMaxY = Math.max(amethystGeodeMinY, amethystGeodeMaxY);
        amethystGeodeOuterWallMin = config.getInt("amethystGeodeOuterWallMin", CATEGORY_WORLDGEN,
                4, 1, 20, "晶洞分布点到中心的最小外墙距离，26.3 默认值为 4。");
        amethystGeodeOuterWallMax = config.getInt("amethystGeodeOuterWallMax", CATEGORY_WORLDGEN,
                6, 1, 20, "晶洞分布点到中心的最大外墙距离，26.3 默认值为 6。");
        amethystGeodeOuterWallMax = Math.max(amethystGeodeOuterWallMin, amethystGeodeOuterWallMax);
        amethystGeodeDistributionMin = config.getInt("amethystGeodeDistributionMin", CATEGORY_WORLDGEN,
                3, 1, 20, "单个晶洞使用的最少分布点数量，26.3 默认值为 3。");
        amethystGeodeDistributionMax = config.getInt("amethystGeodeDistributionMax", CATEGORY_WORLDGEN,
                4, 1, 20, "单个晶洞使用的最多分布点数量，26.3 默认值为 4。");
        amethystGeodeDistributionMax = Math.max(amethystGeodeDistributionMin,
                amethystGeodeDistributionMax);
        amethystGeodePointOffsetMin = config.getInt("amethystGeodePointOffsetMin", CATEGORY_WORLDGEN,
                1, 0, 10, "晶洞分布点的最小形状偏移，26.3 默认值为 1。");
        amethystGeodePointOffsetMax = config.getInt("amethystGeodePointOffsetMax", CATEGORY_WORLDGEN,
                2, 0, 10, "晶洞分布点的最大形状偏移，26.3 默认值为 2。");
        amethystGeodePointOffsetMax = Math.max(amethystGeodePointOffsetMin, amethystGeodePointOffsetMax);
        amethystGeodeCrackChance = config.getFloat("amethystGeodeCrackChance", CATEGORY_WORLDGEN,
                0.95F, 0.0F, 1.0F, "紫水晶晶洞生成裂缝的概率，26.3 默认值为 0.95。");
        amethystGeodeBuddingChance = config.getFloat("amethystGeodeBuddingChance", CATEGORY_WORLDGEN,
                0.083F, 0.0F, 1.0F, "晶洞内层方块替换为紫水晶母岩的概率，26.3 默认值为 0.083。");
        amethystGeodeBudPlacementChance = config.getFloat("amethystGeodeBudPlacementChance",
                CATEGORY_WORLDGEN, 0.35F, 0.0F, 1.0F,
                "每个紫水晶母岩候选面生成晶芽或晶簇的概率，26.3 默认值为 0.35。");

        deepslateTransitionMinY = config.getInt("deepslateTransitionMinY", CATEGORY_WORLDGEN,
                0, -64, 319, "石头完全替换为深板岩的最高高度；默认在 Y=0 及以下全部使用深板岩。");
        deepslateTransitionMaxY = config.getInt("deepslateTransitionMaxY", CATEGORY_WORLDGEN,
                8, -64, 319, "石头与深板岩渐变过渡的最高高度；默认 Y=0 至 Y=8 逐渐减少深板岩。");
        deepslateTransitionMaxY = Math.max(deepslateTransitionMinY, deepslateTransitionMaxY);
        modernOreVeinSizePercent = config.getInt("modernOreVeinSizePercent", CATEGORY_WORLDGEN,
                100, 1, 1000, "现代矿脉和地下石材团簇尺寸的百分比；100 为 26.3 默认尺寸。");
        modernFossilRarity = config.getInt("modernFossilRarity", CATEGORY_WORLDGEN,
                64, 1, 100000, "ffd_cac 深层化石的区块触发分母；26.3 默认值为 64。");
        modernVillageSpacing = config.getInt("modernVillageSpacing", CATEGORY_WORLDGEN,
                34, 9, 256, "ffd_cac 村庄随机扩散间距（区块）；26.3 默认值为 34，分离距离固定为 8。");
        cavesAndCliffsAdditionalBiomeCandidates = config.getStringList(
                "cavesAndCliffsAdditionalBiomeCandidates", CATEGORY_WORLDGEN, new String[0],
                "额外加入 ffd_cac 主世界候选池的群系注册名。仅用于未登记到 Forge 主世界群系表的群系，格式为模组ID:群系ID。");
        modernCoalUpperCount = oreCount(config, "modernCoalUpperCount", 30, "高层煤矿");
        modernCoalLowerCount = oreCount(config, "modernCoalLowerCount", 20, "低层煤矿");
        modernIronUpperCount = oreCount(config, "modernIronUpperCount", 90, "高层铁矿");
        modernIronMiddleCount = oreCount(config, "modernIronMiddleCount", 10, "中层铁矿");
        modernIronSmallCount = oreCount(config, "modernIronSmallCount", 10, "小型铁矿");
        modernGoldCount = oreCount(config, "modernGoldCount", 4, "金矿");
        modernGoldLowerMaxCount = oreCount(config, "modernGoldLowerMaxCount", 1, "底层额外金矿最大次数");
        modernGoldExtraCount = oreCount(config, "modernGoldExtraCount", 50, "恶地额外金矿");
        modernRedstoneCount = oreCount(config, "modernRedstoneCount", 4, "红石矿");
        modernRedstoneLowerCount = oreCount(config, "modernRedstoneLowerCount", 8, "底层额外红石矿");
        modernDiamondCount = oreCount(config, "modernDiamondCount", 7, "小型钻石矿");
        modernDiamondBuriedCount = oreCount(config, "modernDiamondBuriedCount", 4, "埋藏钻石矿");
        modernDiamondLargeRarity = config.getInt("modernDiamondLargeRarity", CATEGORY_WORLDGEN,
                9, 1, 100000, "大型钻石矿脉的区块触发分母；26.3 默认值为 9。");
        modernLapisCount = oreCount(config, "modernLapisCount", 2, "青金石矿");
        modernLapisBuriedCount = oreCount(config, "modernLapisBuriedCount", 4, "埋藏青金石矿");
        modernCopperCount = oreCount(config, "modernCopperCount", 16, "铜矿");
        modernCopperLargeCount = oreCount(config, "modernCopperLargeCount", 16, "滴水石洞穴大型铜矿");
        modernEmeraldCount = oreCount(config, "modernEmeraldCount", 100, "山地绿宝石矿");
        modernTuffCount = oreCount(config, "modernTuffCount", 2, "凝灰岩团簇");
        modernInfestedCount = oreCount(config, "modernInfestedCount", 14, "山地虫蚀石材");
        modernGraniteLowerCount = oreCount(config, "modernGraniteLowerCount", 2, "低层花岗岩团簇");
        modernGraniteUpperRarity = config.getInt("modernGraniteUpperRarity", CATEGORY_WORLDGEN,
                6, 1, 100000, "高层花岗岩团簇的区块触发分母；26.3 默认值为 6。");
        modernDioriteLowerCount = oreCount(config, "modernDioriteLowerCount", 2, "低层闪长岩团簇");
        modernDioriteUpperRarity = config.getInt("modernDioriteUpperRarity", CATEGORY_WORLDGEN,
                6, 1, 100000, "高层闪长岩团簇的区块触发分母；26.3 默认值为 6。");
        modernAndesiteLowerCount = oreCount(config, "modernAndesiteLowerCount", 2, "低层安山岩团簇");
        modernAndesiteUpperRarity = config.getInt("modernAndesiteUpperRarity", CATEGORY_WORLDGEN,
                6, 1, 100000, "高层安山岩团簇的区块触发分母；26.3 默认值为 6。");
        modernDirtCount = oreCount(config, "modernDirtCount", 7, "地下泥土团簇");
        modernGravelCount = oreCount(config, "modernGravelCount", 14, "地下沙砾团簇");

        glowSquidSpawnWeight = config.getInt("glowSquidSpawnWeight", CATEGORY_WORLDGEN,
                10, 0, 1000, "荧光鱿鱼生成权重，26.3 默认值为 10。");
        glowSquidMinGroupSize = config.getInt("glowSquidMinGroupSize", CATEGORY_WORLDGEN,
                4, 1, 64, "荧光鱿鱼生成群体的最小数量，26.3 默认值为 4。");
        glowSquidMaxGroupSize = config.getInt("glowSquidMaxGroupSize", CATEGORY_WORLDGEN,
                6, 1, 64, "荧光鱿鱼生成群体的最大数量，26.3 默认值为 6。");
        glowSquidMaxGroupSize = Math.max(glowSquidMinGroupSize, glowSquidMaxGroupSize);
        glowSquidDepthBelowSeaLevel = config.getInt("glowSquidDepthBelowSeaLevel", CATEGORY_WORLDGEN,
                33, 0, 255, "荧光鱿鱼最高生成位置低于海平面的格数，26.3 默认值为 33。");

        phantomSpawnMinIntervalSeconds = config.getInt("phantomSpawnMinIntervalSeconds",
                CATEGORY_WORLDGEN, 60, 1, 3600,
                "幻翼生成器两次检查之间的最短秒数，26.3 默认为 60 秒。");
        phantomSpawnMaxIntervalSeconds = config.getInt("phantomSpawnMaxIntervalSeconds",
                CATEGORY_WORLDGEN, 119, 1, 3600,
                "幻翼生成器两次检查之间的最长秒数，26.3 默认为 119 秒。");
        phantomInsomniaThresholdTicks = config.getInt("phantomInsomniaThresholdTicks",
                CATEGORY_WORLDGEN, 72000, 0, Integer.MAX_VALUE,
                "幻翼开始获得生成概率前所需的未休息时间，单位为 tick；26.3 默认为 72000 tick。");

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

        axolotlSpawnCheckIntervalTicks = config.getInt("axolotlSpawnCheckIntervalTicks",
                CATEGORY_WORLDGEN, 400, 1, 72000,
                "美西螈自然生成器两次检查之间的 tick 数；默认 400，与高版本友好生物生成节奏一致。");
        axolotlSpawnAttemptsPerPlayer = config.getInt("axolotlSpawnAttemptsPerPlayer",
                CATEGORY_WORLDGEN, 16, 0, 128,
                "每名玩家每次美西螈生成检查选择繁茂洞穴位置的尝试次数。默认 16。");
        axolotlMinGroupSize = config.getInt("axolotlMinGroupSize", CATEGORY_WORLDGEN,
                4, 1, 64, "美西螈自然生成群体的最小数量，26.3 默认值为 4。");
        axolotlMaxGroupSize = Math.max(axolotlMinGroupSize, config.getInt(
                "axolotlMaxGroupSize", CATEGORY_WORLDGEN, 6, 1, 64,
                "美西螈自然生成群体的最大数量，26.3 默认值为 6。"));
        axolotlMobCap = config.getInt("axolotlMobCap", CATEGORY_WORLDGEN,
                5, 0, 256,
                "每 289 个有效区块允许自然存在的美西螈上限，26.3 默认值为 5；不占用 1.12 水生生物上限。");

        goatSpawnWeight = config.getInt("goatSpawnWeight", CATEGORY_WORLDGEN,
                5, 0, 1000, "山羊在现代山地群系中的生成权重，26.3 默认值为 5。");
        goatMinGroupSize = config.getInt("goatMinGroupSize", CATEGORY_WORLDGEN,
                1, 1, 64, "山羊自然生成群体的最小数量，26.3 默认值为 1。");
        goatMaxGroupSize = Math.max(goatMinGroupSize, config.getInt(
                "goatMaxGroupSize", CATEGORY_WORLDGEN, 3, 1, 64,
                "山羊自然生成群体的最大数量，26.3 默认值为 3。"));

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
        lushCaveTallGrassNoiseThreshold = config.getFloat("lushCaveTallGrassNoiseThreshold",
                CATEGORY_WORLDGEN, -0.8F, -1.0F, 1.0F,
                "繁茂洞穴地表高草斑块的群系信息噪声阈值，26.3 默认值为 -0.8；低于阈值时不生成。");
        lushCaveTallGrassAboveNoiseCount = config.getInt("lushCaveTallGrassAboveNoiseCount",
                CATEGORY_WORLDGEN, 7, 0, 256,
                "噪声达到阈值时，每个源区块的高草斑块候选次数，26.3 默认值为 7。");
        lushCaveTallGrassRarity = config.getInt("lushCaveTallGrassRarity", CATEGORY_WORLDGEN,
                32, 1, 10000, "每个高草斑块候选通过稀有度筛选的分母，26.3 默认值为 32。");
        lushCaveTallGrassPatchAttempts = config.getInt("lushCaveTallGrassPatchAttempts",
                CATEGORY_WORLDGEN, 96, 0, 4000,
                "每个繁茂洞穴地表高草斑块的放置尝试次数，26.3 默认值为 96。");
        lushCaveTallGrassHorizontalOffset = config.getInt("lushCaveTallGrassHorizontalOffset",
                CATEGORY_WORLDGEN, 7, 0, 64,
                "繁茂洞穴地表高草斑块的三角分布水平偏移半径，26.3 默认值为 7。");
        lushCaveTallGrassVerticalOffset = config.getInt("lushCaveTallGrassVerticalOffset",
                CATEGORY_WORLDGEN, 3, 0, 32,
                "繁茂洞穴地表高草斑块的三角分布垂直偏移半径，26.3 默认值为 3。");
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
        dripstoneLargeMinAttempts = config.getInt("dripstoneLargeMinAttempts", CATEGORY_WORLDGEN,
                10, 0, 4000, "每个滴水石洞穴区块生成大型滴水石的最少尝试次数，26.3 默认范围为 10-48。");
        dripstoneLargeMaxAttempts = Math.max(dripstoneLargeMinAttempts,
                config.getInt("dripstoneLargeMaxAttempts", CATEGORY_WORLDGEN,
                        48, 0, 4000, "每个滴水石洞穴区块生成大型滴水石的最多尝试次数，26.3 默认范围为 10-48。"));
        dripstoneClusterMinAttempts = config.getInt("dripstoneClusterMinAttempts", CATEGORY_WORLDGEN,
                48, 0, 4000, "每个滴水石洞穴区块生成滴水石簇的最少尝试次数，26.3 默认范围为 48-96。");
        dripstoneClusterMaxAttempts = Math.max(dripstoneClusterMinAttempts,
                config.getInt("dripstoneClusterMaxAttempts", CATEGORY_WORLDGEN,
                        96, 0, 4000, "每个滴水石洞穴区块生成滴水石簇的最多尝试次数，26.3 默认范围为 48-96。"));
        dripstonePointedMinAttempts = config.getInt("dripstonePointedMinAttempts", CATEGORY_WORLDGEN,
                192, 0, 4000, "每个滴水石洞穴区块生成零散滴水石锥的最少尝试次数，26.3 默认范围为 192-256。");
        dripstonePointedMaxAttempts = Math.max(dripstonePointedMinAttempts,
                config.getInt("dripstonePointedMaxAttempts", CATEGORY_WORLDGEN,
                        256, 0, 4000, "每个滴水石洞穴区块生成零散滴水石锥的最多尝试次数，26.3 默认范围为 192-256。"));

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
        xy177.farmersfuturedelight.common.FFDPowderSnowEvents.reloadConfig();
        configVersion.set(CURRENT_CONFIG_VERSION);
        boolean bilingualCommentsChanged = applyBilingualComments(config);

        if (config.hasChanged() || bilingualCommentsChanged) {
            config.save();
        }
    }

    private static void migrateLegacyDefaults(Configuration config, int loadedConfigVersion) {
        if (loadedConfigVersion < 4 && axolotlSpawnAttemptsPerPlayer == 3) {
            axolotlSpawnAttemptsPerPlayer = 16;
            config.get(CATEGORY_WORLDGEN, "axolotlSpawnAttemptsPerPlayer", 16).set(16);
        }
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
        if (CATEGORY_GAMEPLAY.equals(category)) {
            return "通用玩法规则。\nGeneral gameplay rules.";
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
        if (CATEGORY_DEBUG.equals(category)) {
            return "诊断日志设置。\nDiagnostic logging settings.";
        }
        if (CATEGORY_INTERNAL.equals(category)) {
            return "内部设置，请勿手动修改。\nInternal settings; do not edit manually.";
        }
        if (CATEGORY_COMPATIBILITY.equals(category)) {
            return "模组联动配方设置。\nCross-mod recipe compatibility settings.";
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
        if ("rawOreMaterialToggles".equals(key)) {
            return "Per-material raw-ore toggles in material=true or material=false form. Each entry controls that raw material, its storage block, ore-drop replacement, and compatibility recipes. Missing materials default to true. Restart required.";
        }
        if ("deepslateCompatToggles".equals(key)) {
            return "Per-source deep-ore compatibility toggles in modid:material=true or modid:material=false form. Sources for the same material share one deep-ore object. Missing entries default to true for the built-in compatibility list. Restart required.";
        }
        if ("deepslateCompatGenerationToggles".equals(key)) {
            return "Per-source deep-ore generation settings. Use modid:material=count,size or modid:material=count,size,minY,maxY,uniform|trapezoid,discardChance[,plateau]. Sources for the same material place into one shared deep-ore object. Plateau is optional and only affects trapezoid distributions. Missing entries use the built-in defaults. Restart required.";
        }
        if (CATEGORY_FEATURES.equals(category)) {
            return "Feature mode for " + humanizeKey(key.replace("Mode", ""))
                    + ". ENABLED is the default and uses this mod's implementation; DISABLED turns it off; AUTO resolves each compatible content unit independently and fills only missing units locally.";
        }
        if ("sporeBlossomParticlesEnabled".equals(key)) {
            return "Enable falling spore particles from spore blossoms. Disabled by default.";
        }
        if ("logAutoCompatibilityDecisions".equals(key)) {
            return "Log each AUTO compatibility content and world-generation decision. Disabled by default; enable only when diagnosing compatibility issues.";
        }
        if ("lightBlockMode".equals(key)) {
            return "Feature mode for the invisible administrator light block. ENABLED uses this mod's implementation; DISABLED turns it off; AUTO is enabled unless a compatible provider is added.";
        }
        if ("sporeBlossomParticleFrequency".equals(key)) {
            return "Emission frequency denominator per random display tick. 1 emits every display tick; larger values reduce the frequency.";
        }
        if ("sporeBlossomParticleDensity".equals(key)) {
            return "Number of falling spore particles emitted per burst. The default of 15 matches one primary particle plus fourteen ambient particles.";
        }
        if ("phantomSpawnMinIntervalSeconds".equals(key)) {
            return "Minimum delay in seconds between phantom spawn checks. The 26.3 default is 60 seconds.";
        }
        if ("phantomSpawnMaxIntervalSeconds".equals(key)) {
            return "Maximum delay in seconds between phantom spawn checks. The 26.3 default is 119 seconds.";
        }
        if ("phantomInsomniaThresholdTicks".equals(key)) {
            return "Time without rest before phantoms begin to gain a spawn chance, in ticks. The 26.3 default is 72000 ticks.";
        }
        if ("hostileMobsRequireZeroBlockLight".equals(key)) {
            return "Require block light level 0 for natural hostile mob spawning in every world type. Mob spawners are not affected.";
        }
        if ("cloudHeightMode".equals(key)) {
            return "Scope for the modern cloud height. DISABLED keeps the original height; CAVES_CLIFFS_ONLY changes only Caves & Cliffs worlds; ALL_WORLDS changes every world type.";
        }
        if ("smoothBiomeSkyColors".equals(key)) {
            return "Smoothly blend sky colors across biome borders. When disabled, use the sky color of the biome at the player's position. Client-side display only.";
        }
        if ("modernWorldLoadingScreen".equals(key)) {
            return "Use the modern-style loading screen while creating or entering a world. Disabled by default; when disabled, every world type uses the original 1.12.2 loading screen. Client-side display only.";
        }
        if ("cavesAndCliffsBackgroundMusic".equals(key)) {
            return "Add the eight Minecraft 1.18 background tracks to the survival and main-menu music pools in every world type.";
        }
        if ("shovelCreatesDirtPath".equals(key)) {
            return "Allow shovels to turn dirt, coarse dirt, podzol, and mycelium into dirt paths.";
        }
        if ("shearsStopPlantGrowth".equals(key)) {
            return "Allow shears to set kelp, cave vines, weeping vines, and twisting vines to their maximum growth age.";
        }
        if ("oresDropRawMaterials".equals(key)) {
            return "Make recognized ores drop their matching raw materials with Fortune support in every world type; Silk Touch and auto-smelted outputs remain unchanged.";
        }
        if ("rawOreDropAmount".equals(key)) {
            return "Base raw-material drop amount for every supported ore except copper. The Raw Ore 1.5.4 default is 1.";
        }
        if ("denseRawOreDrop".equals(key)) {
            return "Apply the Dense Metals dense-ore multiplier to raw-material drops. Enabled by default, matching Raw Ore 1.5.4.";
        }
        if ("furnaceOutputAmount".equals(key)) {
            return "Ingot output amount when a raw ore is smelted in a furnace. The Raw Ore 1.5.4 default is 1.";
        }
        if ("rawBlockSmelt".equals(key)) {
            return "Allow raw ore blocks to be smelted directly into matching metal blocks. Disabled by default, matching Raw Ore 1.5.4.";
        }
        if ("maceratorRawOutputAmount".equals(key)) {
            return "IndustrialCraft 2 Macerator raw-ore output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("ic2BlastFurnaceSteelOutputAmount".equals(key)) {
            return "IndustrialCraft 2 Blast Furnace steel output for raw iron. The Raw Ore 1.5.4 default is 1.";
        }
        if ("ic2BlastFurnaceSlagOutputAmount".equals(key)) {
            return "IndustrialCraft 2 Blast Furnace slag output for raw iron. The Raw Ore 1.5.4 default is 1.";
        }
        if ("enrichmentOutputAmount".equals(key)) {
            return "Mekanism Enrichment Chamber raw-ore output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("purificationOutputAmount".equals(key)) {
            return "Mekanism Purification Chamber raw-ore output amount. The Raw Ore 1.5.4 default is 3.";
        }
        if ("chemicalInjectionChamberOutputAmount".equals(key)) {
            return "Mekanism Chemical Injection Chamber raw-ore output amount. The Raw Ore 1.5.4 default is 4.";
        }
        if ("chemicalDissolutionChamberMultiple".equals(key)) {
            return "Mekanism Chemical Dissolution Chamber raw-ore processing multiplier. The Raw Ore 1.5.4 default is 1.";
        }
        if ("pulverizerOutputAmount".equals(key)) {
            return "Thermal Expansion Pulverizer raw-ore primary output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("pulverizerSecondaryOutputAmount".equals(key)) {
            return "Thermal Expansion Pulverizer raw-ore secondary output amount. The Raw Ore 1.5.4 default is 1.";
        }
        if ("magmaCrucibleOutputMultiple".equals(key)) {
            return "Thermal Expansion Magma Crucible raw-ore fluid output multiplier. The Raw Ore 1.5.4 default is 1.";
        }
        if ("fluidMultiple".equals(key)) {
            return "Tinkers' Construct raw-ore melting fluid output multiplier. The Raw Ore 1.5.4 default is 1.";
        }
        if ("inductionSmelterSandOutputAmount".equals(key)) {
            return "Thermal Expansion Induction Smelter primary output amount when processing raw ore with sand. The Raw Ore 1.5.4 default is 2.";
        }
        if ("inductionSmelterRichSlagOutputAmount".equals(key)) {
            return "Thermal Expansion Induction Smelter primary output amount when processing raw ore with rich slag. The Raw Ore 1.5.4 default is 3.";
        }
        if ("inductionSmelterCinnabarOutputAmount".equals(key)) {
            return "Thermal Expansion Induction Smelter primary output amount when processing raw ore with cinnabar. The Raw Ore 1.5.4 default is 3.";
        }
        if ("metallurgyCrusherOutputAmount".equals(key)) {
            return "Metallurgy 4: Reforged Crusher raw-ore output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("galacticraftCompressedOutputAmount".equals(key)) {
            return "Galacticraft Compressor compressed-material output amount. The Raw Ore 1.5.4 default is 1.";
        }
        if ("galacticraftPlateOutputAmount".equals(key)) {
            return "Galacticraft Compressor IndustrialCraft 2 plate output amount. The Raw Ore 1.5.4 default is 1.";
        }
        if ("galaxySpaceModernStorageModuleOutputAmount".equals(key)) {
            return "Galaxy Space modern storage module recipe output amount. The Raw Ore 1.5.4 default is 1.";
        }
        if ("rollingMachinePlateOutputAmount".equals(key)) {
            return "Advanced Rocketry Rolling Machine plate output amount. The Raw Ore 1.5.4 default is 1.";
        }
        if ("smallPlatePressOutputAmount".equals(key)) {
            return "Advanced Rocketry Small Plate Press plate output amount. The Raw Ore 1.5.4 default is 4.";
        }
        if ("latheRodOutputAmount".equals(key)) {
            return "Advanced Rocketry Lathe rod output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("arcFurnaceTitaniumIridiumOutputAmount".equals(key)) {
            return "Advanced Rocketry Electric Arc Furnace titanium-iridium alloy output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("arcFurnaceTitaniumAluminideOutputAmount".equals(key)) {
            return "Advanced Rocketry Electric Arc Furnace titanium-aluminide alloy output amount. The Raw Ore 1.5.4 default is 3.";
        }
        if ("techgunsYellowcakeOutputAmount".equals(key)) {
            return "Techguns Chemical Lab yellowcake output amount for the raw uranium recipe. The Raw Ore 1.5.4 default is 3.";
        }
        if ("techgunsTitaniumOreOutputAmount".equals(key)) {
            return "Techguns Reaction Chamber titanium ore output amount for the raw titanium recipe. The Raw Ore 1.5.4 default is 2.";
        }
        if ("techgunsRawIronOutputAmount".equals(key)) {
            return "Techguns Reaction Chamber raw iron byproduct amount for the raw titanium recipe. The Raw Ore 1.5.4 default is 1.";
        }
        if ("techgunsChemicalLabAcidAmount".equals(key)) {
            return "Techguns Chemical Lab acid input for the raw uranium recipe, in mB. The Raw Ore 1.5.4 default is 250.";
        }
        if ("techgunsReactionChamberAcidAmount".equals(key)) {
            return "Techguns Reaction Chamber acid input for the raw titanium recipe, in mB. The Raw Ore 1.5.4 default is 100.";
        }
        if ("cropariaRefinedOutputAmount".equals(key)) {
            return "Croparia fruit recipe refined-material output amount. The Raw Ore 1.5.4 default is 2.";
        }
        if ("mysticalAgricultureRefinedOutputAmount".equals(key)) {
            return "Mystical Agriculture essence recipe refined-material output amount. The Raw Ore 1.5.4 default is 4.";
        }
        if ("modernCauldronFeatures".equals(key)) {
            return "Enable lava and powder snow cauldrons, pointed dripstone filling, and precipitation accumulation in every world type.";
        }
        if ("modernFossilRarity".equals(key)) {
            return "Chunk trigger denominator for lower fossils in ffd_cac. The 26.3 default is 64.";
        }
        if ("modernVillageSpacing".equals(key)) {
            return "Village random-spread spacing in chunks for ffd_cac. The 26.3 default is 34; separation remains 8.";
        }
        if ("cavesAndCliffsAdditionalBiomeCandidates".equals(key)) {
            return "Registry names of additional Overworld biomes allowed in the ffd_cac candidate pool. Use this only for biomes that are not registered with Forge's Overworld biome lists.";
        }
        if ("enchantingTableEmitsLight".equals(key)) {
            return "Make enchanting tables emit modern light level 7 in every world type.";
        }
        if ("pistonBreakParticles".equals(key)) {
            return "Show the destroyed block's break particles when a piston destroys a block in every world type.";
        }
        if ("infestedBlocksHalfBreakTime".equals(key)) {
            return "Give infested blocks half the breaking time of their corresponding ordinary host blocks in every world type.";
        }
        if ("shulkerDuplication".equals(key)) {
            return "Allow shulkers hit by shulker bullets to duplicate using the modern nearby-shulker chance in every world type.";
        }
        if ("experienceOrbMerging".equals(key)) {
            return "Merge nearby experience orbs with the same value without losing experience, and pick up merged units one at a time.";
        }
        if ("illagersIgnoreBabyVillagers".equals(key)) {
            return "Prevent illagers from targeting or damaging baby villagers in every world type.";
        }
        if ("beeAdditionalPollinationTargets".equals(key)) {
            return "Additional blocks or block states that bees can pollinate. Supported formats: modid:block, modid:block@metadata, and modid:block[property=value,property=value].";
        }
        if ("powderSnowMobTransformations".equals(key)) {
            return "Powder-snow entity transformation rules. Use source entity registry name;target entity registry name, for example minecraft:pig;minecraft:creeper. Skeletons become strays by default.";
        }
        if ("powderSnowMobImmunities".equals(key)) {
            return "Entity registry names that are immune to powder-snow freezing. Spectators and entities wearing leather armor remain handled separately.";
        }
        if ("powderSnowAdditionalDamage".equals(key)) {
            return "Entity registry name;damage rules for extra damage after an entity is fully frozen in powder snow. For example minecraft:blaze;5.0.";
        }
        if ("powderSnowWalkableBoots".equals(key)) {
            return "Item registry names for boots that let their wearer walk on powder snow without sinking. Leather boots always work.";
        }
        if ("powderSnowFreezingArmor".equals(key)) {
            return "Item registry names for armor that prevents powder-snow freezing. One matching item in any armor slot is enough; vanilla leather armor always works. For example twilightforest:yeti_chestplate.";
        }
        if ("goatScreamingChance".equals(key)) {
            return "Base chance for a spawned or bred goat to be a screaming goat. The 26.3 default is 0.02.";
        }
        if ("goatSingleHornChance".equals(key)) {
            return "Chance for a newly spawned adult goat to be missing one random horn. The 26.3 default is 0.1.";
        }
        if ("goatLongJumpCooldownMinTicks".equals(key)) {
            return "Minimum delay between goat long jumps, in ticks. The 26.3 default is 600.";
        }
        if ("goatLongJumpCooldownMaxTicks".equals(key)) {
            return "Maximum delay between goat long jumps, in ticks. The 26.3 default is 1200.";
        }
        if ("goatLongJumpHorizontalRange".equals(key)) {
            return "Maximum horizontal search range for goat long-jump landing positions. The 26.3 default is 5 blocks.";
        }
        if ("goatLongJumpVerticalRange".equals(key)) {
            return "Maximum vertical search range for goat long-jump landing positions. The 26.3 default is 5 blocks.";
        }
        if ("goatLongJumpPrepareTicks".equals(key)) {
            return "Preparation time after a goat selects a long-jump landing position, in ticks. The 26.3 default is 40.";
        }
        if ("goatLongJumpVelocityMultiplier".equals(key)) {
            return "Maximum long-jump launch velocity multiplier relative to the base jump strength. The 26.3 default is 3.5714288.";
        }
        if ("goatRamCooldownMinTicks".equals(key)) {
            return "Minimum delay between rams for a regular goat, in ticks. The 26.3 default is 600.";
        }
        if ("goatRamCooldownMaxTicks".equals(key)) {
            return "Maximum delay between rams for a regular goat, in ticks. The 26.3 default is 6000.";
        }
        if ("screamingGoatRamCooldownMinTicks".equals(key)) {
            return "Minimum delay between rams for a screaming goat, in ticks. The 26.3 default is 100.";
        }
        if ("screamingGoatRamCooldownMaxTicks".equals(key)) {
            return "Maximum delay between rams for a screaming goat, in ticks. The 26.3 default is 300.";
        }
        if ("goatRamPrepareTicks".equals(key)) {
            return "Time a goat lowers its head at the starting position before ramming, in ticks. The 26.3 default is 20.";
        }
        if ("goatRamMinDistance".equals(key)) {
            return "Minimum distance between a goat's ram starting position and its target. The 26.3 default is 4 blocks.";
        }
        if ("goatRamMaxDistance".equals(key)) {
            return "Maximum distance between a goat's ram starting position and its target. The 26.3 default is 7 blocks.";
        }
        if ("goatRamSpeedMultiplier".equals(key)) {
            return "Movement-speed multiplier used during the active ram. The 26.3 default is 3.0.";
        }
        if ("goatAdultRamKnockback".equals(key)) {
            return "Base knockback force applied by an adult goat's ram. The 26.3 default is 2.5.";
        }
        if ("goatBabyRamKnockback".equals(key)) {
            return "Base knockback force applied by a baby goat's ram. The 26.3 default is 1.0.";
        }
        if ("goatSpawnWeight".equals(key)) {
            return "Goat spawn weight in modern mountain biomes. The 26.3 default is 5.";
        }
        if ("goatMinGroupSize".equals(key)) {
            return "Minimum number of goats in a natural spawn group. The 26.3 default is 1.";
        }
        if ("goatMaxGroupSize".equals(key)) {
            return "Maximum number of goats in a natural spawn group. The 26.3 default is 3.";
        }
        if ("dripstoneGrowthChance".equals(key)) {
            return "Chance per random tick for pointed dripstone to attempt natural growth. The 26.3 default is 0.011377778.";
        }
        if ("dripstoneWaterTransferChance".equals(key)) {
            return "Chance per random tick for a stalactite to schedule water transfer into a cauldron. The 26.3 default is 0.17578125.";
        }
        if ("dripstoneLavaTransferChance".equals(key)) {
            return "Chance per random tick for a stalactite to schedule lava transfer into a cauldron. The 26.3 default is 0.05859375.";
        }
        if ("cauldronRainFillChance".equals(key)) {
            return "Chance per precipitation update for rain to add one water level to a cauldron. The 26.3 default is 0.05.";
        }
        if ("cauldronSnowFillChance".equals(key)) {
            return "Chance per precipitation update for snow to add one powder snow level to a cauldron. The 26.3 default is 0.1.";
        }
        if ("dripstoneLargeMinAttempts".equals(key)) {
            return "Minimum large-dripstone placement attempts per dripstone-cave chunk. The 26.3 default range is 10 to 48.";
        }
        if ("dripstoneLargeMaxAttempts".equals(key)) {
            return "Maximum large-dripstone placement attempts per dripstone-cave chunk. The 26.3 default range is 10 to 48.";
        }
        if ("dripstoneClusterMinAttempts".equals(key)) {
            return "Minimum dripstone-cluster placement attempts per dripstone-cave chunk. The 26.3 default range is 48 to 96.";
        }
        if ("dripstoneClusterMaxAttempts".equals(key)) {
            return "Maximum dripstone-cluster placement attempts per dripstone-cave chunk. The 26.3 default range is 48 to 96.";
        }
        if ("dripstonePointedMinAttempts".equals(key)) {
            return "Minimum scattered pointed-dripstone placement attempts per dripstone-cave chunk. The 26.3 default range is 192 to 256.";
        }
        if ("dripstonePointedMaxAttempts".equals(key)) {
            return "Maximum scattered pointed-dripstone placement attempts per dripstone-cave chunk. The 26.3 default range is 192 to 256.";
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
        String value = config.getString(key, CATEGORY_FEATURES, "ENABLED",
                comment + "。默认值为 ENABLED，即关闭自动避让并使用本模组实现。可选值：ENABLED、DISABLED、AUTO。AUTO 会逐个识别外部内容单元，并仅由本模组补齐缺失内容。",
                new String[]{"ENABLED", "DISABLED", "AUTO"});
        return FeatureMode.parse(value);
    }

    public static boolean isRawOreMaterialEnabled(String material) {
        if (material == null) {
            return false;
        }
        Boolean enabled = rawOreMaterialStates.get(material.trim().toLowerCase(Locale.ROOT));
        return enabled == null || enabled;
    }

    public static boolean isDeepslateCompatEnabled(String key) {
        if (key == null) {
            return true;
        }
        if (deepslateCompatToggles == null) {
            return true;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (String entry : deepslateCompatToggles) {
            if (entry == null) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator >= entry.length() - 1) {
                continue;
            }
            if (normalized.equals(entry.substring(0, separator).trim().toLowerCase(Locale.ROOT))) {
                return Boolean.parseBoolean(entry.substring(separator + 1).trim());
            }
        }
        return true;
    }

    public static DeepslateGeneration deepslateCompatGeneration(String key, int defaultCount,
                                                                int defaultSize, int defaultMinY,
                                                                int defaultMaxY, boolean defaultTrapezoid,
                                                                float defaultDiscardChance,
                                                                int defaultPlateau) {
        DeepslateGeneration result = new DeepslateGeneration(defaultCount, defaultSize,
                defaultMinY, defaultMaxY, defaultTrapezoid, defaultDiscardChance,
                defaultPlateau);
        if (key == null || deepslateCompatGenerationToggles == null) {
            return result;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (String entry : deepslateCompatGenerationToggles) {
            if (entry == null) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator >= entry.length() - 1
                    || !normalized.equals(entry.substring(0, separator).trim().toLowerCase(Locale.ROOT))) {
                continue;
            }
            String[] values = entry.substring(separator + 1).split(",");
            if (values.length != 2 && values.length != 5
                    && values.length != 6 && values.length != 7) {
                return result;
            }
            try {
                int count = Integer.parseInt(values[0].trim());
                int size = Integer.parseInt(values[1].trim());
                if (count >= 0 && count <= 1000 && size >= 1 && size <= 64) {
                    int minY = defaultMinY;
                    int maxY = defaultMaxY;
                    boolean trapezoid = defaultTrapezoid;
                    float discardChance = defaultDiscardChance;
                    int plateau = defaultPlateau;
                    if (values.length >= 5) {
                        minY = Integer.parseInt(values[2].trim());
                        maxY = Integer.parseInt(values[3].trim());
                        String distribution = values[4].trim().toLowerCase(Locale.ROOT);
                        if ("uniform".equals(distribution)) {
                            trapezoid = false;
                        } else if ("trapezoid".equals(distribution)) {
                            trapezoid = true;
                        } else {
                            return result;
                        }
                    }
                    if (values.length >= 6) {
                        discardChance = Float.parseFloat(values[5].trim());
                    }
                    if (values.length == 7) {
                        plateau = Integer.parseInt(values[6].trim());
                    }
                    if (minY < -320 || maxY > 319 || minY > maxY
                            || Float.isNaN(discardChance) || Float.isInfinite(discardChance)
                            || discardChance < 0.0F || discardChance > 1.0F
                            || plateau < 0 || !trapezoid && plateau != 0) {
                        return result;
                    }
                    return new DeepslateGeneration(count, size, minY, maxY,
                            trapezoid, discardChance, plateau);
                }
            } catch (NumberFormatException ignored) {
                return result;
            }
            return result;
        }
        return result;
    }

    public static final class DeepslateGeneration {
        private final int count;
        private final int size;
        private final int minY;
        private final int maxY;
        private final boolean trapezoid;
        private final float discardChance;
        private final int plateau;

        public DeepslateGeneration(int count, int size, int minY, int maxY,
                                   boolean trapezoid, float discardChance, int plateau) {
            this.count = count;
            this.size = size;
            this.minY = minY;
            this.maxY = maxY;
            this.trapezoid = trapezoid;
            this.discardChance = discardChance;
            this.plateau = plateau;
        }

        public int count() {
            return count;
        }

        public int size() {
            return size;
        }

        public int minY() {
            return minY;
        }

        public int maxY() {
            return maxY;
        }

        public boolean trapezoid() {
            return trapezoid;
        }

        public float discardChance() {
            return discardChance;
        }

        public int plateau() {
            return plateau;
        }
    }

    private static String[] defaultRawOreMaterialToggles() {
        String[] defaults = new String[FFDRawOres.NAMES.length];
        for (int i = 0; i < FFDRawOres.NAMES.length; i++) {
            defaults[i] = FFDRawOres.NAMES[i] + "=true";
        }
        return defaults;
    }

    private static Map<String, Boolean> defaultRawOreMaterialStates() {
        Map<String, Boolean> states = new HashMap<>();
        for (String material : FFDRawOres.NAMES) {
            states.put(material, true);
        }
        return states;
    }

    private static Map<String, Boolean> parseRawOreMaterialStates(String[] entries) {
        Map<String, Boolean> states = defaultRawOreMaterialStates();
        if (entries == null) {
            return states;
        }
        for (String entry : entries) {
            if (entry == null) {
                continue;
            }
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator >= entry.length() - 1) {
                continue;
            }
            String material = entry.substring(0, separator).trim().toLowerCase(Locale.ROOT);
            String value = entry.substring(separator + 1).trim().toLowerCase(Locale.ROOT);
            if (!material.matches("[a-z0-9][a-z0-9_]{0,47}")
                    || (!"true".equals(value) && !"false".equals(value))) {
                continue;
            }
            states.put(material, Boolean.parseBoolean(value));
        }
        return states;
    }

    private static int readSeagrassAttempts(Configuration config, String key, int defaultValue, String biome) {
        return config.getInt(key, CATEGORY_WORLDGEN, defaultValue, 0, 1000,
                biome + "每区块海草生成尝试次数，26.3 默认为 " + defaultValue + "。");
    }

    private static int readTallPercent(Configuration config, String key, int defaultValue, String biome) {
        return config.getInt(key, CATEGORY_WORLDGEN, defaultValue, 0, 100,
                biome + "生成高海草的百分比，26.3 默认为 " + defaultValue + "% 。");
    }

    private static int oreCount(Configuration config, String key, int defaultValue, String feature) {
        return config.getInt(key, CATEGORY_WORLDGEN, defaultValue, 0, 1000,
                feature + "每区块生成尝试次数；26.3 默认值为 " + defaultValue + "。");
    }
}
