package project;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令行入口：java -jar LowYSwampHut.jar [参数]
 *   无参数 → 启动 GUI（Launcher）
 *   有参数 → 执行命令行搜索
 *
 * 注意：SearchCoords 回调产出的是 SearchCoords.Result（x, z, height），
 * 这里用它做排序与输出。
 */
public class CmdLineRunner {

    public static void main(String[] args) {
        // 没有参数 → 启动 GUI
        if (args.length == 0) {
            Launcher.main(args);
            return;
        }

        initLogging();

        // 默认值
        long seed = 0;
        int maxY = -40;
        int minX = -58594, maxX = 58593;
        int minZ = -58594, maxZ = 58593;
        String outputFile = "result.txt";
        String versionName = "26.2";
        boolean checkGen = false;
        int threads = Math.max(1, Math.min(Runtime.getRuntime().availableProcessors() / 2, 8));

        // 解析参数
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--seed", "-s"     -> seed = parseLong(args[++i], "--seed");
                case "--max-y"          -> maxY = Integer.parseInt(args[++i]);
                case "--min-x"          -> minX = Integer.parseInt(args[++i]);
                case "--max-x"          -> maxX = Integer.parseInt(args[++i]);
                case "--min-z"          -> minZ = Integer.parseInt(args[++i]);
                case "--max-z"          -> maxZ = Integer.parseInt(args[++i]);
                case "--version"        -> versionName = args[++i];
                case "--output", "-o"   -> outputFile = args[++i];
                case "--threads"        -> threads = Integer.parseInt(args[++i]);
                case "--check-gen"      -> checkGen = true;
                case "--help", "-h"    -> { printHelp(); return; }
                default -> {
                    System.err.println("未知参数: " + args[i]);
                    printHelp();
                    System.exit(1);
                }
            }
        }

        if (seed == 0) {
            System.err.println("错误: 必须指定 --seed");
            printHelp();
            System.exit(1);
        }

        GameVersion gameVersion = GameVersion.fromDisplayName(versionName);
        if (gameVersion == null) {
            System.err.println("错误: 不支持的版本 " + versionName);
            System.exit(1);
        }

        System.out.printf("开始搜索种子 %d，最大Y=%d，版本=%s，线程=%d%n",
                seed, maxY, versionName, threads);
        long startTime = System.currentTimeMillis();

        SearchCoords searcher = new SearchCoords(gameVersion, WorldPresetMode.DEFAULT);
        // SearchCoords 内部用 Result(x, z, height) 回调
        List<SearchCoords.Result> rawResults = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger lastPrinted = new AtomicInteger(-1);

        searcher.startSearch(
                seed, threads, minX, maxX, minZ, maxZ, maxY,
                progress -> {
                    int pct = (int) (progress.percentage() * 100);
                    int stage = progress.stage();
                    if (pct >= lastPrinted.get() + 1) {
                        System.out.printf("\r阶段 %d 进度: %d%% (候选:%d)", stage, pct, count.get());
                        lastPrinted.set(pct);
                    }
                },
                rawResults::add,
                checkGen
        );

        try {
            searcher.awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("\n搜索被中断");
            System.exit(1);
        }

        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.printf("%n搜索完成，耗时 %.1f 秒%n", elapsed);

        if (rawResults.isEmpty()) {
            System.out.println("未找到符合条件的女巫小屋。");
            return;
        }

        // 按高度（height）从低到高排序
        rawResults.sort(Comparator.comparingDouble(SearchCoords.Result::height));

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.printf("找到 %d 个女巫小屋：%n", rawResults.size());
            for (SearchCoords.Result r : rawResults) {
                writer.println(r.toString()); // 形如 /tp x height z
            }
            SearchCoords.Result lowest = rawResults.get(0);
            writer.printf("%n最低 Y 坐标的小屋: %s%n", lowest.toString());
            System.out.println("结果已保存到: " + outputFile);
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
            System.exit(1);
        }
    }

    private static long parseLong(String s, String paramName) {
        try {
            if (s.startsWith("0x") || s.startsWith("-0x")) {
                return Long.parseLong(s.substring(s.startsWith("-") ? 3 : 2), 16);
            }
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            System.err.println("参数 " + paramName + " 的值无效: " + s);
            System.exit(1);
            return 0;
        }
    }

    private static void initLogging() {
        System.setProperty("log4j2.isThreadContextMapInheritable", "true");
        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("log4j2.formatMsgNoLookups", "true");
        System.setProperty("log4j2.enable.threadlocals", "false");
        System.setProperty("log4j2.enable.direct.encoders", "false");
        System.setProperty("max.bg.threads", "2");
        try {
            org.apache.logging.log4j.LogManager.getContext(false);
        } catch (Exception ignored) {}
    }

    private static void printHelp() {
        System.out.println("LowYSwampHut 命令行搜索工具");
        System.out.println("用法: java -jar LowYSwampHut.jar --seed <种子> [选项]");
        System.out.println();
        System.out.println("选项:");
        System.out.println("  --seed, -s <种子>       必需，要搜索的种子（支持十进制或 0x 十六进制）");
        System.out.println("  --max-y <数值>           最大Y坐标，默认 -40");
        System.out.println("  --min-x <数值>           X范围最小值，默认 -58594");
        System.out.println("  --max-x <数值>           X范围最大值，默认 58593");
        System.out.println("  --min-z <数值>           Z范围最小值，默认 -58594");
        System.out.println("  --max-z <数值>           Z范围最大值，默认 58593");
        System.out.println("  --version <版本>          版本，默认 26.2（如 1.18.1 / 1.21 / 26.2）");
        System.out.println("  --output, -o <文件>      输出文件，默认 result.txt");
        System.out.println("  --threads <数量>          线程数，默认 CPU核心数/2（上限8）");
        System.out.println("  --check-gen              精确检查生成");
        System.out.println("  --help, -h               显示帮助");
    }
}
