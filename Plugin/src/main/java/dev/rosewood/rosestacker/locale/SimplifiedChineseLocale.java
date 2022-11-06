package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.manager.ConversionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimplifiedChineseLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "zh_CN";
    }

    @Override
    public String getTranslatorName() {
        return "ahdg";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<>() {{
            this.put("#0", "插件信息前缀");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseStacker&7] ");

            this.put("#1", "堆叠整体显示名称");
            this.put("entity-stack-display", "&c%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&c%amount%x&7]");
            this.put("entity-stack-display-spawn-egg", "&c%amount%x &7%name% 刷怪蛋");
            this.put("item-stack-display", "&c%amount%x &7%name%");
            this.put("item-stack-display-single", "&7%name%");
            this.put("block-stack-display", "&c%amount%x &7%name%");
            this.put("spawner-stack-display", "&c%amount%x &7%name%");
            this.put("spawner-stack-display-single", "&7%name%");

            this.put("#1.1", "Hologram Display Tags");
            this.put("#1.2", "Available spawner placeholders: %name%, %amount%, %max_amount%, %time_remaining%, %ticks_remaining%, %total_spawned%");
            this.put("#1.3", "Multiple lines are supported");
            this.put("block-hologram-display", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display-single", List.of("&7%name%"));

            this.put("#2", "基础命令信息");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&e使用 &b/rs help &e来获取更多命令信息。");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#3", "帮助命令");
            this.put("command-help-description", "&8 - &d/rs help &7- 显示命令帮助菜单... 就是您现在看着的这个");
            this.put("command-help-title", "&e可用的命令:");

            this.put("#4", "重载命令");
            this.put("command-reload-description", "重新加载插件");
            this.put("command-reload-reloaded", "&e插件数据，配置文件，语言文件均已被重新加载。");

            this.put("#5", "给予命令");
            this.put("command-give-description", "给予预先堆叠的物品");
            this.put("command-give-usage", "&c用法: &e/rs give <block|spawner|entity> <玩家> <类型> [堆叠量] [份数]");
            this.put("command-give-given", "&e已给予玩家 &b%player% &e[%display%&e]。");
            this.put("command-give-given-multiple", "&e已给予玩家 &b%player% &e%amount%x [%display%&e]。");
            this.put("command-give-unstackable", "&c您指定的类型是无法堆叠的。");
            this.put("command-give-too-large", "&c您指定的堆叠量超过了该类型所允许的最大堆叠量。");

            this.put("#6", "清理堆叠命令");
            this.put("command-clearall-description", "清除一种堆叠类型的所有堆叠个体。");
            this.put("command-clearall-killed-entities", "&e清除了 &b%amount% &e个堆叠的实体。");
            this.put("command-clearall-killed-items", "&e清除了 &b%amount% &e份堆叠的物品。");
            this.put("command-clearall-killed-all", "&e清除了 &b%entityAmount% &e个堆叠的实体和 &b%itemAmount% &e份堆叠的物品。");

            this.put("#7", "统计命令");
            this.put("command-stats-description", "显示插件的统计数据");
            this.put("command-stats-header", "&a当前插件统计数据:");
            this.put("command-stats-threads", "&b%amount% &e个活跃的堆叠线程。");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &e个已加载的堆叠实体, 共计 &b%total% &e个单体实体。");
            this.put("command-stats-stacked-items", "&b%stackAmount% &e份已加载的堆叠物品, 共计 &b%total% &e个单体物品。");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &e份已加载的堆叠方块, 共计 &b%total% &e个单体方块。");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &e个已加载的堆叠刷怪笼, 共计 &b%total% &e个单体刷怪笼。");
            this.put("command-stats-active-tasks", "&b%amount%&e的活动任务。");

            this.put("#8", "转化命令");
            this.put("command-convert-description", "从另一个堆叠类型的插件转化数据");
            this.put("command-convert-converted", "&e已转化插件 &b%plugin% &e的数据至 RoseStacker。拥有数据源的插件已被关闭，请确保在下次重启前将其移出插件文件夹。");
            this.put("command-convert-failed", "&c无法转化插件 &b%plugin% &c的数据, 该插件未处于运行状态。");
            this.put("command-convert-aborted", "&c已中止对插件 &b%plugin% &c进行数据转化的尝试， 您已经从另一个堆叠插件转化过数据了。");

            this.put("#9", "翻译命令");
            this.put("command-translate-description", "翻译堆叠整体的显示名称");
            this.put("command-translate-loading", "&e正在下载并应用翻译数据, 这可能会花点时间。");
            this.put("command-translate-failure", "&c无法翻译堆叠整体的显示名称。在获取语言数据时发生了错误，请稍后再试一次。");
            this.put("command-translate-invalid-locale", "&c无法翻译堆叠整体的显示名称。您所指定的语言文件是无效的。");
            this.put("command-translate-spawner-format", "&e刷怪笼名称无法被精准地翻译。为了修复这个问题，您可以输入 &b/rs translate zh_cn &3{} " +
                    "刷怪笼 &e以确保刷怪笼堆叠的显示名称为 \"牛 刷怪笼\"。请使用 &b{} &e作为生物名字的占位符变量。");
            this.put("command-translate-spawner-format-invalid", "&c您所提供的刷怪笼翻译名称格式是无效的。其必须包含 &b{} &c作为生物名字的占位符变量。");
            this.put("command-translate-success", "&a成功翻译堆叠整体的显示名称。");

            this.put("#10", "堆叠工具命令");
            this.put("command-stacktool-description", "给予玩家一份堆叠工具");
            this.put("command-stacktool-given", "&e您已被给予堆叠工具。");
            this.put("command-stacktool-given-other", "&b%player% &a已被给予堆叠工具。");
            this.put("command-stacktool-no-console", "&c你不能把叠加工具交给控制台。");
            this.put("command-stacktool-no-permission", "&c您没有足够的权限去使用堆叠工具。");
            this.put("command-stacktool-invalid-entity", "&c那个实体不是堆叠的一部分，是自定义的实体吗？");
            this.put("command-stacktool-marked-unstackable", "&e类型 &b%type% &e已被标记为 &c不可堆叠&e。");
            this.put("command-stacktool-marked-stackable", "&e类型 &b%type% &e已被标记 &a可堆叠&e。");
            this.put("command-stacktool-marked-all-unstackable", "&e类型 &b%type% &e的整体堆叠已被标记为 &c不可堆叠&e。");
            this.put("command-stacktool-select-1", "&e类型 &b%type% &e已被选中为实体 #1。选择另一个实体以测试它们是否可被堆叠至整体。");
            this.put("command-stacktool-unselect-1", "&e类型 &b%type% &e不再被选中。");
            this.put("command-stacktool-select-2", "&e类型 &b%type% &e已被选中为实体 #2。");
            this.put("command-stacktool-can-stack", "&a实体 #1 能与实体 #2 进行堆叠。");
            this.put("command-stacktool-can-not-stack", "&c实体 #1 不能与实体 #2 进行堆叠。原因如下: &b%reason%");
            this.put("command-stacktool-info", "&e堆叠信息:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&e实体ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&e自定义名称: &r%name%");
            this.put("command-stacktool-info-location", "&e所处位置: X: &b%x% &eY: &b%y% &eZ: &b%z% &e世界: &b%world%");
            this.put("command-stacktool-info-chunk", "&e所处区块: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&a是");
            this.put("command-stacktool-info-false", "&c否");
            this.put("command-stacktool-info-entity-type", "&e实体类型: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&e能否堆叠: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&e有无AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&e来自刷怪笼: %value%");
            this.put("command-stacktool-info-item-type", "&e物品类型: &b%type%");
            this.put("command-stacktool-info-block-type", "&e方块类型: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&e刷怪笼类型: &b%type%");
            this.put("command-stacktool-info-stack-size", "&e所包含个体数: &b%amount%");

            this.put("#11", "堆叠方块 GUI");
            this.put("gui-stacked-block-title", "编辑堆叠 %name% 中");
            this.put("gui-stacked-block-page-back", List.of("&e上一页 (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&e下一页 (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&c销毁堆叠", "&e拆分堆叠整体为掉落物"));
            this.put("gui-stacked-block-destroy-title", "确定要销毁堆叠整体?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&a确认", "&e是的，帮我拆分它"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&c取消", "&e不，我想回到上一页"));

            this.put("#12", "堆叠刷怪笼 GUI");
            this.put("gui-stacked-spawner-title", "查看 %name% 中");
            this.put("gui-stacked-spawner-stats", "&6刷怪笼信息");
            this.put("gui-stacked-spawner-min-spawn-delay", "&e最小生成延时: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&e最大生成延时: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&e生物AI是否开启: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&e实体搜索范围: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&e玩家活动激活范围: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&e生成范围: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&e最小生成数: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&e最大生成数: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&e当前生成数: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6生成限制条件");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&e距下次生成还有: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&e统计已生成的实体总数: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6有效的生成条件");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6未满足的生成条件");
            this.put("gui-stacked-spawner-entities-can-spawn", "&a实体目前能被生成");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&e阻碍生成的因素:");

            this.put("#13", "生成条件信息");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "位于海平面以上");
            this.put("spawner-condition-above-sea-level-invalid", "没有生成区域范围位于海平面上");
            this.put("spawner-condition-above-y-axis-info", "位于 Y-坐标 以上");
            this.put("spawner-condition-above-y-axis-invalid", "没有生成区域范围在要求的 Y-坐标 上");
            this.put("spawner-condition-air-info", "露天");
            this.put("spawner-condition-air-invalid", "没有足够大的露天空间");
            this.put("spawner-condition-below-sea-level-info", "位于海平面以下");
            this.put("spawner-condition-below-sea-level-invalid", "没有生成区域范围位于海平面以下");
            this.put("spawner-condition-below-y-axis-info", "位于 Y-坐标 以下");
            this.put("spawner-condition-below-y-axis-invalid", "没有生成区域范围在要求的 Y-坐标 下");
            this.put("spawner-condition-biome-info", "生态群系限制");
            this.put("spawner-condition-biome-invalid", "未处于正确的生态群系");
            this.put("spawner-condition-block-info", "生成方块限制");
            this.put("spawner-condition-block-invalid", "范围中没有有效的可供生成生物的方块");
            this.put("spawner-condition-block-exception-info", "生成方块黑名单");
            this.put("spawner-condition-block-exception-invalid", "范围内有被排除的生成方块");
            this.put("spawner-condition-darkness-info", "低光限制");
            this.put("spawner-condition-darkness-invalid", "该区域目前光源充足，太亮了");
            this.put("spawner-condition-total-darkness-info", "完全黑暗");
            this.put("spawner-condition-total-darkness-invalid", "该地区必须没有光线");
            this.put("spawner-condition-fluid-info", "需要流体");
            this.put("spawner-condition-fluid-invalid", "附近没有流体");
            this.put("spawner-condition-lightness-info", "高光要求");
            this.put("spawner-condition-lightness-invalid", "该区域太暗了");
            this.put("spawner-condition-max-nearby-entities-info", "达到最大附近实体限制数");
            this.put("spawner-condition-max-nearby-entities-invalid", "过多实体靠近刷怪笼");
            this.put("spawner-condition-no-skylight-access-info", "自然光照限制");
            this.put("spawner-condition-no-skylight-access-invalid", "没有无自然光照的方块可供生物生成");
            this.put("spawner-condition-on-ground-info", "脚踏实地");
            this.put("spawner-condition-on-ground-invalid", "没有固体地面在附近");
            this.put("spawner-condition-skylight-access-info", "自然光照要求");
            this.put("spawner-condition-skylight-access-invalid", "范围内没有受到自然光照照射的方块可供生物生成");
            this.put("spawner-condition-none-invalid", "超过最大的生成尝试次数");
            this.put("spawner-condition-not-player-placed-invalid", "必须由玩家放置");

            this.put("#14", "给予物品描述(lore)");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&c您没有权限那么做!");
            this.put("only-player", "&c这个命令只能由玩家执行。");
            this.put("unknown-command", "&c未知的命令，使用&b/%cmd%&c帮助以获得更多信息。");
            this.put("unknown-command-error", "&c发生了一个未知的错误。详情已打印到控制台。请联系服务器管理员。");
            this.put("invalid-subcommand", "&c无效的子命令。");
            this.put("invalid-argument", "&c无效参数：%message%。");
            this.put("invalid-argument-null", "&c无效参数：%name%为空。");
            this.put("missing-arguments", "&c缺少参数，需要&b%amount%&c。");
            this.put("missing-arguments-extra", "&c缺少参数，需要&b%amount%+&c。");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% 类型 [%input%] 不存在");
            this.put("argument-handler-enum-list", "%enum%类型[%input%]不存在。有效类型：%types%");
            this.put("argument-handler-string", "字符串不能为空");
            this.put("argument-handler-integer", "整数[%input%]必须是在-2^31和2^31-1之间的整数，包括在内。");
            this.put("argument-handler-player", "没有发现用户名为[%input%]的玩家在线。");
            this.put("argument-handler-stackplugin", "没有找到名称为[%input%]的插件");
            this.put("argument-handler-material", "没有发现名称为[%input%]的材料。");
            this.put("argument-handler-stackamount", "堆栈大小[%input%]无效，必须是一个大于0的数字");
            this.put("argument-handler-stacktype", "堆栈类型[%input%]是无效的");
            this.put("argument-handler-translationlocale", "翻译语言[%input%] 无效");

            this.put("#18", "转化锁定信息");
            this.put("convert-lock-conflictions", "&c服务器上有一些插件与RoseStacker冲突。" +
                    "为了防止冲突造成 数据丢失，RoseStacker禁用了一个或多个堆叠类型。 " +
                    "与此同时，我们在 plugins/" + RoseStacker.getInstance().getName() + "/" + ConversionManager.FILE_NAME + " 创建了文件， 您可以在其中配置禁用的堆叠类型 " +
                    "该文件还允许您确认您已阅读此警告，并允许您禁用此消息。");

            this.put("#19", "杂项信息");
            this.put("spawner-advanced-place-range","&c警告！ 你不能在这里放置一个刷怪笼！");
            this.put("spawner-silk-touch-protect", "&c警告! &e您需要附魔有精准采集的稿子 并且/或 有权限拿起一个刷怪笼。否则您将不能这样做。");
            this.put("spawner-convert-not-enough", "&c警告! &e无法使用生成蛋来转换刷怪笼种类。这可能是由于您手上的刷怪蛋数量并不足以进行转换的操作。");
            this.put("spawner-advanced-place-no-permission", "&c警告! &e您没有恰当的权限来放置这种类型的刷怪笼。");
            this.put("spawner-advanced-break-no-permission", "&c警告! &e您没有恰当的权限来拾取这种类型的刷怪笼。");
            this.put("spawner-advanced-break-silktouch-no-permission", "&c警告! &e您需要用附魔有精准采集的稿子才能拿起那类刷怪笼。");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
