package com.breakinblocks.nautec.content.menus;

public record BioReactorLayout(int imageWidth, int imageHeight, int inventoryY,
                               int[][] colonies, int[][] outputs, int[][] nutrients, int[][] upgrades,
                               int[][] vitalityBars, int[][] progressArrows, int[][] progressBars,
                               int[] summaryArrow, int[] dishIn, int[] dishOut, int[] dishEmptyOut) {
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 10;

    public static final BioReactorLayout BIO_REACTOR = new BioReactorLayout(176, 192, 110,
            new int[][]{{52, 21}, {52, 43}, {52, 65}},
            new int[][]{{107, 22}, {107, 44}, {107, 66}},
            new int[][]{{20, 33}, {20, 55}},
            new int[][]{{148, 33}, {148, 55}},
            new int[][]{{47, 22, 3, 16}, {47, 44, 3, 16}, {47, 66, 3, 16}},
            new int[][]{{76, 26}, {76, 48}, {76, 70}},
            null,
            null,
            new int[]{7, 79},
            new int[]{27, 79},
            new int[]{148, 77});

    public static final BioReactorLayout INDUSTRIAL = industrial();

    private static BioReactorLayout industrial() {
        int[][] colonies = new int[9][];
        int[][] outputs = new int[9][];
        int[][] vitality = new int[9][];
        int[][] bars = new int[9][];
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int index = row * 3 + column;
                int x = 12 + column * 24;
                int y = 16 + row * 24;
                colonies[index] = new int[]{x, y};
                vitality[index] = new int[]{x + 19, y + 1, 3, 16};
                bars[index] = new int[]{x + 1, y + 19, 16, 2};
                outputs[index] = new int[]{116 + column * 18, 22 + row * 18};
            }
        }
        int[][] nutrients = new int[3][];
        for (int i = 0; i < 3; i++) {
            nutrients[i] = new int[]{12 + i * 18, 96};
        }
        int[][] upgrades = new int[4][];
        for (int i = 0; i < 4; i++) {
            upgrades[i] = new int[]{98 + i * 18, 96};
        }
        return new BioReactorLayout(176, 208, 126, colonies, outputs, nutrients, upgrades, vitality, null, bars, new int[]{86, 45},
                new int[]{90, 22}, new int[]{90, 62}, new int[]{73, 96});
    }
}
