package xy177.farmersfuturedelight.client.model;

import net.minecraft.client.model.ModelRenderer;

final class BabyAxolotlAnimationSet {
    static final int ROOT = 0;
    static final int BODY = 1;
    static final int HEAD = 2;
    static final int TOP_GILLS = 3;
    static final int LEFT_GILLS = 4;
    static final int RIGHT_GILLS = 5;
    static final int TAIL = 6;
    static final int RIGHT_FRONT_LEG = 7;
    static final int LEFT_FRONT_LEG = 8;
    static final int RIGHT_HIND_LEG = 9;
    static final int LEFT_HIND_LEG = 10;
    private static final int ROTATION = 0;
    private static final int POSITION = 1;
    private static final int LINEAR = 0;
    private static final int CATMULL_ROM = 1;
    private static final Clip[] CLIPS = {
            new Clip(2.88F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 2.8F, 0.0F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, 160.0F, -10.0F, -37.5F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {0.0F, 0.2F, 0.0F, 0.0F, LINEAR}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {1.72F, 360.0F, 0.0F, -45.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, 160.0F, 10.0F, 37.5F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {0.0F, -0.2F, 0.0F, 0.0F, LINEAR}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 37.5F, CATMULL_ROM}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, 10.0F, 0.0F, CATMULL_ROM, 0.72F, 0.0F, -14.0F, 0.0F, LINEAR, 0.96F, 0.0F, -21.0F, 0.0F, LINEAR, 1.4F, 0.0F, -25.0F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, -16.75F, 0.0F, CATMULL_ROM, 2.84F, 0.0F, 10.0F, 0.0F, CATMULL_ROM}),
                    new Channel(HEAD, ROTATION, new float[] {0.04F, 0.0F, 0.0F, -6.0F, CATMULL_ROM, 1.32F, -6.45F, -1.45F, -6.5F, LINEAR, 1.48F, -6.45F, -1.45F, -6.5F, LINEAR, 2.84F, 0.0F, 0.0F, -6.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 38.0F, 0.0F, CATMULL_ROM, 1.28F, 0.0F, 45.5F, 0.0F, CATMULL_ROM, 1.64F, 0.0F, 45.5F, 0.0F, CATMULL_ROM, 2.8F, 0.0F, 38.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.04F, 0.0F, -50.0F, 0.0F, CATMULL_ROM, 1.32F, 0.0F, -59.0F, 0.0F, CATMULL_ROM, 1.6F, 0.0F, -59.0F, 0.0F, CATMULL_ROM, 2.84F, 0.0F, -50.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, 33.0F, 0.0F, 0.0F, CATMULL_ROM, 1.28F, 47.4F, 0.0F, 0.0F, CATMULL_ROM, 1.64F, 47.4F, 0.0F, 0.0F, CATMULL_ROM, 2.8F, 33.0F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(5.12F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 5.12F, 0.0F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(BODY, POSITION, new float[] {0.0F, 0.0F, 1.5F, 0.0F, CATMULL_ROM, 5.12F, 0.0F, 1.5F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {5.04F, 190.0F, -15.0F, -30.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {0.0F, 0.3F, 0.0F, 0.5F, CATMULL_ROM, 5.12F, 0.3F, 0.0F, 0.5F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 360.0F, 0.0F, -30.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, POSITION, new float[] {0.0F, 0.18F, 0.0F, 0.0F, LINEAR, 5.12F, 0.18F, 0.0F, 0.0F, LINEAR}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {5.04F, 190.0F, 18.0F, 30.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {0.0F, -0.1F, 0.0F, 0.5F, CATMULL_ROM, 5.12F, -0.1F, 0.0F, 0.5F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {5.04F, 180.0F, 0.0F, 30.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, POSITION, new float[] {0.0F, -0.2F, 0.0F, 0.0F, LINEAR, 5.12F, -0.2F, 0.0F, 0.0F, LINEAR}),
                    new Channel(TAIL, ROTATION, new float[] {0.04F, 0.0F, -12.0F, 0.0F, CATMULL_ROM, 2.56F, 0.0F, 12.0F, 0.0F, CATMULL_ROM, 5.12F, 0.0F, -12.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 10.0F, 0.0F, CATMULL_ROM, 3.12F, 0.0F, -16.0F, 0.0F, CATMULL_ROM, 5.12F, 0.0F, 10.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -16.0F, 0.0F, CATMULL_ROM, 2.08F, 0.0F, 12.0F, 0.0F, CATMULL_ROM, 5.12F, 0.0F, -16.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, 10.0F, 0.0F, 0.0F, CATMULL_ROM, 2.56F, -16.0F, 0.0F, 0.0F, CATMULL_ROM, 5.12F, 10.0F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(5.2F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, -8.9F, 0.0F, 0.0F, CATMULL_ROM, 2.6F, -2.6F, 0.0F, 0.0F, CATMULL_ROM, 5.2F, -8.9F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(BODY, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 2.6F, 0.0F, -0.5F, 0.0F, CATMULL_ROM, 5.2F, 0.0F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, -5.0F, -20.0F, -55.0F, CATMULL_ROM, 2.6F, 1.0F, -20.0F, -65.0F, CATMULL_ROM, 5.2F, -5.0F, -20.0F, -55.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {0.0F, 0.15F, -0.3391F, 0.0876F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 60.0F, -60.0F, 15.0F, CATMULL_ROM, 2.6F, 75.0F, -60.0F, 0.0F, CATMULL_ROM, 5.2F, 60.0F, -60.0F, 15.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, POSITION, new float[] {5.2F, 0.0F, -0.4F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, 0.0F, 20.0F, 50.0F, CATMULL_ROM, 2.6F, 0.0F, 20.0F, 60.0F, CATMULL_ROM, 5.2F, 0.0F, 20.0F, 50.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {2.68F, -0.15F, -0.4F, 0.3F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, -135.0F, -50.0F, -330.0F, CATMULL_ROM, 2.6F, -160.0F, -60.0F, -295.0F, CATMULL_ROM, 5.2F, -135.0F, -50.0F, -330.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, POSITION, new float[] {5.2F, 0.0F, -0.5F, 0.0F, CATMULL_ROM}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, 15.0F, 0.0F, CATMULL_ROM, 2.6F, 0.0F, -32.0F, 0.0F, CATMULL_ROM, 5.2F, 0.0F, 15.0F, 0.0F, CATMULL_ROM}),
                    new Channel(HEAD, ROTATION, new float[] {0.0F, 11.7F, 0.0F, 0.0F, CATMULL_ROM, 2.6F, 2.4F, 0.0F, 0.0F, CATMULL_ROM, 5.2F, 11.7F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -25.6F, 0.0F, CATMULL_ROM, 3.12F, 0.0F, 16.0F, 0.0F, CATMULL_ROM, 5.2F, 0.0F, -25.6F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 23.0F, 0.0F, CATMULL_ROM, 2.08F, 0.0F, -26.0F, 0.0F, CATMULL_ROM, 5.2F, 0.0F, 23.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, -19.2F, 0.0F, 0.0F, CATMULL_ROM, 2.6F, 12.3F, 0.0F, 0.0F, CATMULL_ROM, 5.2F, -19.2F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(1.0F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 8.0F, 0.0F, 0.0F, CATMULL_ROM, 0.24F, 12.0F, 0.0F, 0.0F, CATMULL_ROM, 0.68F, -7.5F, 0.0F, 0.0F, CATMULL_ROM, 1.0F, 8.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(BODY, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 0.52F, 0.0F, -1.0F, 0.0F, CATMULL_ROM, 0.96F, 0.0F, -0.05F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, 310.0F, 70.0F, 400.0F, CATMULL_ROM, 0.24F, 330.0F, 70.0F, 420.0F, CATMULL_ROM, 0.52F, 290.0F, 65.0F, 384.0F, CATMULL_ROM, 0.76F, 290.0F, 70.0F, 380.0F, CATMULL_ROM, 0.96F, 310.0F, 70.0F, 400.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 105.0F, -95.0F, 180.0F, CATMULL_ROM, 0.28F, 105.0F, -85.0F, 180.0F, CATMULL_ROM, 0.52F, 105.0F, -80.0F, 180.0F, CATMULL_ROM, 0.76F, 105.0F, -95.0F, 180.0F, CATMULL_ROM, 0.96F, 105.0F, -95.0F, 180.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, -50.0F, -70.0F, -40.0F, CATMULL_ROM, 0.52F, -60.0F, -68.0F, -30.0F, CATMULL_ROM, 0.76F, -55.0F, -70.0F, -30.0F, CATMULL_ROM, 0.96F, -50.0F, -70.0F, -40.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, 70.0F, -80.0F, 15.0F, CATMULL_ROM, 0.28F, 130.0F, -80.0F, -45.0F, CATMULL_ROM, 0.52F, 120.0F, -70.0F, -35.0F, CATMULL_ROM, 0.76F, 80.0F, -70.0F, 5.0F, CATMULL_ROM, 0.96F, 70.0F, -80.0F, 15.0F, CATMULL_ROM}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, 15.0F, 0.0F, CATMULL_ROM, 0.52F, 0.0F, -15.0F, 0.0F, CATMULL_ROM, 0.96F, 0.0F, 13.36F, 0.0F, CATMULL_ROM}),
                    new Channel(HEAD, ROTATION, new float[] {0.0F, -7.5F, 0.0F, 0.0F, CATMULL_ROM, 0.28F, -13.9F, 0.0F, 0.0F, CATMULL_ROM, 0.72F, 14.23F, 0.0F, 0.0F, CATMULL_ROM, 1.0F, -7.5F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -72.0F, 0.0F, CATMULL_ROM, 0.2F, 0.0F, -79.9F, 0.0F, CATMULL_ROM, 0.64F, 0.0F, -38.1F, 0.0F, CATMULL_ROM, 0.96F, 0.0F, -72.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 72.0F, 0.0F, CATMULL_ROM, 0.2F, 0.0F, 86.5F, 0.0F, CATMULL_ROM, 0.64F, 0.0F, 26.7F, 0.0F, CATMULL_ROM, 0.96F, 0.0F, 72.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, -57.0F, 0.0F, 0.0F, CATMULL_ROM, 0.2F, -68.7F, 0.0F, 0.0F, CATMULL_ROM, 0.64F, -24.2F, 0.0F, 0.0F, CATMULL_ROM, 0.96F, -57.0F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(2.72F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR, 2.72F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, 2.5F, 10.0F, -30.0F, CATMULL_ROM, 1.32F, 2.5F, -22.5F, -30.0F, CATMULL_ROM, 2.72F, 2.5F, 10.0F, -30.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {1.92F, 0.5F, 0.0F, 0.8F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 260.0F, -10.0F, 230.0F, CATMULL_ROM, 1.32F, 90.0F, -20.0F, 50.0F, CATMULL_ROM, 2.72F, 260.0F, -10.0F, 230.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, POSITION, new float[] {0.0F, 0.5F, 0.0F, 0.0F, CATMULL_ROM, 2.72F, 0.5F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, -2.5F, 32.5F, 30.0F, CATMULL_ROM, 1.32F, -2.5F, -12.0F, 30.0F, CATMULL_ROM, 2.72F, -2.5F, 32.5F, 30.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {0.0F, -0.5F, 0.0F, 0.7F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, -2.5F, -30.0F, 30.0F, CATMULL_ROM, 1.32F, 2.5F, 12.0F, 30.0F, CATMULL_ROM, 2.72F, -2.5F, -30.0F, 30.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, POSITION, new float[] {0.0F, -0.5F, 0.0F, 0.0F, CATMULL_ROM, 2.72F, -0.5F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, -10.0F, 0.0F, CATMULL_ROM, 1.32F, 0.0F, 10.0F, 0.0F, CATMULL_ROM, 2.72F, 0.0F, -10.0F, 0.0F, CATMULL_ROM}),
                    new Channel(HEAD, ROTATION, new float[] {0.0F, 0.0F, -7.5F, 0.0F, CATMULL_ROM, 1.32F, 0.0F, 6.7F, 0.0F, CATMULL_ROM, 2.72F, 0.0F, -7.5F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 38.1F, 0.0F, CATMULL_ROM, 1.08F, 0.0F, 45.6F, 0.0F, CATMULL_ROM, 1.44F, 0.0F, 45.6F, 0.0F, CATMULL_ROM, 2.72F, 0.0F, 38.1F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -50.0F, 0.0F, CATMULL_ROM, 1.08F, 0.0F, -59.0F, 0.0F, CATMULL_ROM, 1.36F, 0.0F, -59.0F, 0.0F, CATMULL_ROM, 2.72F, 0.0F, -50.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, 33.0F, 0.0F, 0.0F, CATMULL_ROM, 1.08F, 47.4F, 0.0F, 0.0F, CATMULL_ROM, 1.44F, 47.4F, 0.0F, 0.0F, CATMULL_ROM, 2.72F, 33.0F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(2.04F, true, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 0.0F, -5.0F, 6.0F, CATMULL_ROM, 1.04F, 0.0F, 5.0F, -4.0F, CATMULL_ROM, 2.04F, 0.0F, -5.0F, 6.0F, CATMULL_ROM}),
                    new Channel(BODY, POSITION, new float[] {0.0F, 0.0F, 1.5F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, 1.5F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, 0.0F, -15.0F, -30.0F, CATMULL_ROM, 0.72F, 0.0F, 9.0F, -30.0F, CATMULL_ROM, 1.12F, 0.0F, 13.0F, -24.0F, CATMULL_ROM, 1.44F, 0.0F, 9.0F, -30.0F, CATMULL_ROM, 2.04F, 0.0F, -15.0F, -30.0F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 0.96F, 0.4F, 0.0F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 95.0F, -20.0F, 70.0F, CATMULL_ROM, 0.68F, 255.0F, -20.0F, 230.0F, CATMULL_ROM, 1.12F, 280.0F, -25.0F, 255.0F, CATMULL_ROM, 1.52F, 255.0F, -20.0F, 230.0F, CATMULL_ROM, 2.04F, 95.0F, -20.0F, 70.0F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, POSITION, new float[] {0.0F, 0.5F, 0.0F, 0.0F, CATMULL_ROM, 0.96F, 0.5F, 0.0F, 0.0F, CATMULL_ROM, 2.04F, 0.5F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, -5.0F, -20.0F, 30.0F, CATMULL_ROM, 0.72F, 0.0F, 10.0F, 25.0F, CATMULL_ROM, 1.44F, 0.0F, 10.0F, 25.0F, CATMULL_ROM, 2.04F, -5.0F, -20.0F, 30.0F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {0.0F, -0.5F, 0.0F, 0.0F, CATMULL_ROM, 0.96F, 0.0F, 0.0F, 0.0F, CATMULL_ROM, 2.04F, -0.5F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, 5.0F, 15.0F, 25.0F, CATMULL_ROM, 0.72F, -10.0F, -20.0F, 35.0F, CATMULL_ROM, 1.44F, -10.0F, -20.0F, 35.0F, CATMULL_ROM, 2.04F, 5.0F, 15.0F, 25.0F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, POSITION, new float[] {0.0F, -0.7F, 0.0F, 0.0F, CATMULL_ROM, 0.96F, -0.4F, 0.0F, 0.0F, CATMULL_ROM, 2.04F, -0.7F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, 16.5F, 0.0F, CATMULL_ROM, 0.56F, 0.0F, 1.12F, 0.0F, CATMULL_ROM, 1.44F, 0.0F, 17.5F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, 16.5F, 0.0F, CATMULL_ROM}),
                    new Channel(HEAD, ROTATION, new float[] {0.0F, 0.0F, 4.0F, -5.8F, CATMULL_ROM, 1.04F, 0.0F, -4.0F, 5.0F, CATMULL_ROM, 2.04F, 0.0F, 4.0F, -5.8F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -60.0F, 0.0F, CATMULL_ROM, 1.24F, 0.0F, -45.0F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, -60.0F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 38.0F, 0.0F, CATMULL_ROM, 0.64F, 0.0F, 53.0F, 0.0F, CATMULL_ROM, 2.04F, 0.0F, 38.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, -34.0F, 0.0F, 0.0F, CATMULL_ROM, 1.04F, -41.5F, 0.0F, 0.0F, CATMULL_ROM, 2.04F, -34.0F, 0.0F, 0.0F, CATMULL_ROM}),
            }),
            new Clip(0.04F, false, new Channel[] {
                    new Channel(BODY, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 30.0F, CATMULL_ROM}),
                    new Channel(BODY, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(RIGHT_FRONT_LEG, ROTATION, new float[] {0.0F, 202.35F, -30.33F, -40.82F, CATMULL_ROM}),
                    new Channel(RIGHT_FRONT_LEG, POSITION, new float[] {0.0F, 0.2F, 0.0F, 0.0F, LINEAR}),
                    new Channel(RIGHT_HIND_LEG, ROTATION, new float[] {0.0F, 418.0F, -50.0F, 35.8F, CATMULL_ROM}),
                    new Channel(RIGHT_HIND_LEG, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(LEFT_FRONT_LEG, ROTATION, new float[] {0.0F, 177.0F, 22.76F, 15.9F, CATMULL_ROM}),
                    new Channel(LEFT_FRONT_LEG, POSITION, new float[] {0.0F, -0.2F, 0.0F, 0.0F, LINEAR}),
                    new Channel(LEFT_HIND_LEG, ROTATION, new float[] {0.0F, 16.4287F, -37.6467F, 16.9822F, CATMULL_ROM}),
                    new Channel(LEFT_HIND_LEG, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(TAIL, ROTATION, new float[] {0.0F, 0.0F, -18.67F, 0.0F, LINEAR}),
                    new Channel(TAIL, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(HEAD, ROTATION, new float[] {0.0F, -4.21F, -0.95F, -6.33F, LINEAR}),
                    new Channel(HEAD, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(LEFT_GILLS, ROTATION, new float[] {0.0F, 0.0F, 43.21F, 0.0F, CATMULL_ROM}),
                    new Channel(LEFT_GILLS, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(RIGHT_GILLS, ROTATION, new float[] {0.0F, 0.0F, -55.87F, 0.0F, CATMULL_ROM}),
                    new Channel(RIGHT_GILLS, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(TOP_GILLS, ROTATION, new float[] {0.0F, 43.0F, 0.0F, 0.0F, CATMULL_ROM}),
                    new Channel(TOP_GILLS, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(ROOT, ROTATION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
                    new Channel(ROOT, POSITION, new float[] {0.0F, 0.0F, 0.0F, 0.0F, LINEAR}),
            }),
    };

    private BabyAxolotlAnimationSet() {
    }

    static void apply(int animation, float time, float weight, ModelRenderer[] parts) {
        if (animation < 0 || animation >= CLIPS.length || weight <= 0.0F) {
            return;
        }
        Clip clip = CLIPS[animation];
        float elapsed = clip.looping ? time % clip.length : time;
        for (Channel channel : clip.channels) {
            channel.apply(elapsed, weight, parts[channel.part]);
        }
    }

    private static float catmullRom(float alpha, float first, float second,
                                    float third, float fourth) {
        return 0.5F * (2.0F * second + (third - first) * alpha
                + (2.0F * first - 5.0F * second + 4.0F * third - fourth)
                * alpha * alpha
                + (3.0F * second - first - 3.0F * third + fourth)
                * alpha * alpha * alpha);
    }

    private static final class Clip {
        private final float length;
        private final boolean looping;
        private final Channel[] channels;

        private Clip(float length, boolean looping, Channel[] channels) {
            this.length = length;
            this.looping = looping;
            this.channels = channels;
        }
    }

    private static final class Channel {
        private static final int STRIDE = 5;
        private final int part;
        private final int target;
        private final float[] keyframes;

        private Channel(int part, int target, float[] keyframes) {
            this.part = part;
            this.target = target;
            this.keyframes = keyframes;
        }

        private void apply(float time, float weight, ModelRenderer modelPart) {
            int count = keyframes.length / STRIDE;
            int upper = 0;
            while (upper < count && time > value(upper, 0)) {
                upper++;
            }
            int previous = Math.max(0, upper - 1);
            int next = Math.min(count - 1, previous + 1);
            float previousTime = value(previous, 0);
            float nextTime = value(next, 0);
            float alpha = next == previous ? 0.0F
                    : Math.max(0.0F, Math.min(1.0F,
                    (time - previousTime) / (nextTime - previousTime)));
            float x;
            float y;
            float z;
            if ((int) value(next, 4) == CATMULL_ROM) {
                int first = Math.max(0, previous - 1);
                int fourth = Math.min(count - 1, next + 1);
                x = catmullRom(alpha, value(first, 1), value(previous, 1),
                        value(next, 1), value(fourth, 1));
                y = catmullRom(alpha, value(first, 2), value(previous, 2),
                        value(next, 2), value(fourth, 2));
                z = catmullRom(alpha, value(first, 3), value(previous, 3),
                        value(next, 3), value(fourth, 3));
            } else {
                x = value(previous, 1) + (value(next, 1) - value(previous, 1)) * alpha;
                y = value(previous, 2) + (value(next, 2) - value(previous, 2)) * alpha;
                z = value(previous, 3) + (value(next, 3) - value(previous, 3)) * alpha;
            }
            if (target == ROTATION) {
                float radians = (float) Math.PI / 180.0F * weight;
                modelPart.rotateAngleX += x * radians;
                modelPart.rotateAngleY += y * radians;
                modelPart.rotateAngleZ += z * radians;
            } else {
                modelPart.rotationPointX += x * weight;
                modelPart.rotationPointY -= y * weight;
                modelPart.rotationPointZ += z * weight;
            }
        }

        private float value(int frame, int component) {
            return keyframes[frame * STRIDE + component];
        }
    }
}
