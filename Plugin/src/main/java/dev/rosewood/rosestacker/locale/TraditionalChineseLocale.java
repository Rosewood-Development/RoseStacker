package dev.rosewood.rosestacker.locale;

import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.rosegarden.locale.Locale;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TraditionalChineseLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "zh_Hant";
    }

    @Override
    public String getTranslatorName() {
        return "billyovo";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<>() {{
            this.put("#0", "插件訊息前缀");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseStacker&7] ");

            this.put("#1", "堆疊實體顯示名稱");
            this.put("entity-stack-display", "&c%amount%x &7%name%");
            this.put("entity-stack-display-custom-name", "%name% &7[&c%amount%x&7]");
            this.put("entity-stack-display-spawn-egg", "&c%amount%x &7%name% 生怪蛋");
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
            this.put("spawner-hologram-display-empty", List.of("&c%amount%x &7%name%"));
            this.put("spawner-hologram-display-empty-single", List.of("&7%name%"));

            this.put("#2", "基礎指令訊息");
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&e使用 &b/rs help &e以獲取更多資訊.");
            this.put("command-help-list-description", "&8 - &d/%cmd% %subcmd% %args% &7- %desc%");
            this.put("command-help-list-description-no-args", "&8 - &d/%cmd% %subcmd% &7- %desc%");

            this.put("#3", "協助指令");
            this.put("command-help-description", "顯示指令協助選單... 對, 就是這個選單");
            this.put("command-help-title", "&e可用指令:");

            this.put("#4", "重新載入指令");
            this.put("command-reload-description", "重新載入插件");
            this.put("command-reload-reloaded", "&e插件資料, 設定及語言文件已經被重新載入了。");

            this.put("#5", "給予指令");
            this.put("command-give-description", "給予已預先堆疊的物品");
            this.put("command-give-usage", "&cUsage: &e/rs give <block|spawner|entity> <玩家名稱> <類別> [堆疊數量] [堆數]");
            this.put("command-give-given", "&e已給予 &b%player% &e[%display%&e]。");
            this.put("command-give-given-multiple", "&e已給予 &b%player% &e%amount%x [%display%&e]。");
            this.put("command-give-unstackable", "&c你指定的類別是無法被堆疊的。");
            this.put("command-give-too-large", "&c你指定的數量超過了該類別可堆疊的上限。");

            this.put("#6", "清除堆疊指令");
            this.put("command-clearall-description", "清除一種類別的所有堆疊");
            this.put("command-clearall-killed-entities", "&e已清除 &b%amount% &e個堆疊的實體。");
            this.put("command-clearall-killed-items", "&e已清除 &b%amount% &e份堆疊的物品。");
            this.put("command-clearall-killed-all", "&e己清除 &b%entityAmount% &e個堆疊的實體及 &b%itemAmount% &e份堆疊的物品。");

            this.put("#7", "統計指令");
            this.put("command-stats-description", "顯示此插件的統計數據");
            this.put("command-stats-header", "&a插件現時的統計數據:");
            this.put("command-stats-threads", "&b%amount% &e個運行中的線程。");
            this.put("command-stats-stacked-entities", "&b%stackAmount% &e個已加載的堆疊實體, 共 &b%total% &e個實體。");
            this.put("command-stats-stacked-items", "&b%stackAmount% &e份個已加載的堆疊物品, 共 &b%total% &e份物品。");
            this.put("command-stats-stacked-blocks", "&b%stackAmount% &e個已加載的堆疊方塊, 共 &b%total% &e個方塊。");
            this.put("command-stats-stacked-spawners", "&b%stackAmount% &e個已加載的生怪磚, 共 &b%total% &e個生怪磚。");
            this.put("command-stats-active-tasks", "&b%amount%&e活動任務。");

            this.put("#9", "翻譯指令");
            this.put("command-translate-description", "翻譯堆疊個體的名稱。");
            this.put("command-translate-loading", "&e正在下載及使用翻譯文件，這過程可能需要一些時間。");
            this.put("command-translate-failure", "&c無法翻譯堆疊個體的名稱。一個錯誤在過程中發生了，請稍後再試。");
            this.put("command-translate-invalid-locale", "&c無法翻譯堆疊個體的名稱。你所指定的語言並非正確語言。");
            this.put("command-translate-spawner-format", "&e生怪磚的名稱無法被準確翻譯。 要解決此問題，你可以使用 &b/rs translate en_us &3{}" +
                    "生怪磚 &e來令生怪磚顯示成 \"牛生怪磚\". 使用 &b{} &e來代替生物的名稱。");
            this.put("command-translate-spawner-format-invalid", "&c你所提供的生怪磚格式並不正確。 格式必須包含 &b{} &c作為生物名稱的代替文字。");
            this.put("command-translate-success", "&a已成功翻譯堆疊顯示名稱。");

            this.put("#10", "堆疊工具指令");
            this.put("command-stacktool-description", "給予玩家堆疊工具。");
            this.put("command-stacktool-given", "&e你已經獲得了堆疊工具。");
            this.put("command-stacktool-given-other", "&b%player% &a已經獲得了堆疊工具。");
            this.put("command-stacktool-no-console", "&c你不能給予控制台堆疊工具。");
            this.put("command-stacktool-no-permission", "&c你沒有使用堆疊工具的權限。");
            this.put("command-stacktool-invalid-entity", "&c該實體並不是堆疊的一部分，他是自訂實體嗎?");
            this.put("command-stacktool-marked-unstackable", "&b%type% &e類別 已經被標示為 &c無法堆疊&e。");
            this.put("command-stacktool-marked-stackable", "&b%type% &e類別 已經被標示為 &a可堆疊&e。");
            this.put("command-stacktool-marked-all-unstackable", "&e整個 &b%type% &e的堆疊 已經被標示為 &c無法堆疊&e。");
            this.put("command-stacktool-select-1", "&b%type% &e已經被選擇為 實體 #1。選擇另一個實體以測試他們可否被堆疊。");
            this.put("command-stacktool-unselect-1", "&b%type% &e已經被取消選擇。");
            this.put("command-stacktool-select-2", "&b%type% &e已經被選擇為 實體 #2。");
            this.put("command-stacktool-can-stack", "&a實體 #1可以與實體 #2堆疊。");
            this.put("command-stacktool-can-not-stack", "&c實體 #1無法與實體 #2堆疊。原因: &b%reason%");
            this.put("command-stacktool-info", "&e堆疊資訊:");
            this.put("command-stacktool-info-uuid", "&eUUID: &b%uuid%");
            this.put("command-stacktool-info-entity-id", "&e實體 ID: &b%id%");
            this.put("command-stacktool-info-custom-name", "&e自訂顯示名稱: &r%name%");
            this.put("command-stacktool-info-location", "&e位置: X: &b%x% &eY: &b%y% &eZ: &b%z% &e世界: &b%world%");
            this.put("command-stacktool-info-chunk", "&e區塊: &b%x%&e, &b%z%");
            this.put("command-stacktool-info-true", "&a是");
            this.put("command-stacktool-info-false", "&c否");
            this.put("command-stacktool-info-entity-type", "&e實體類別: &b%type%");
            this.put("command-stacktool-info-entity-stackable", "&e可堆疊: %value%");
            this.put("command-stacktool-info-entity-has-ai", "&e有AI: %value%");
            this.put("command-stacktool-info-entity-from-spawner", "&e從生怪磚: %value%");
            this.put("command-stacktool-info-item-type", "&e物品類別: &b%type%");
            this.put("command-stacktool-info-block-type", "&e方塊類別: &b%type%");
            this.put("command-stacktool-info-spawner-type", "&e生怪磚類別: &b%type%");
            this.put("command-stacktool-info-stack-size", "&e堆疊數量: &b%amount%");

            this.put("#11", "方塊堆疊介面");
            this.put("gui-stacked-block-title", "正在編輯 %name% 的堆疊");
            this.put("gui-stacked-block-page-back", List.of("&e上一頁 (" + GuiUtil.PREVIOUS_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-page-forward", List.of("&e下一頁 (" + GuiUtil.NEXT_PAGE_NUMBER_PLACEHOLDER + "/" + GuiUtil.MAX_PAGE_NUMBER_PLACEHOLDER + ")"));
            this.put("gui-stacked-block-destroy", List.of("&c解除堆疊狀態", "&e解除堆疊狀態並掉落物品。"));
            this.put("gui-stacked-block-destroy-title", "確定要解除堆疊狀態?");
            this.put("gui-stacked-block-destroy-confirm", List.of("&a確定", "&e是，請解除堆疊狀態"));
            this.put("gui-stacked-block-destroy-cancel", List.of("&c取消", "&e否，返回上一頁"));

            this.put("#12", "生怪磚堆疊介面");
            this.put("gui-stacked-spawner-title", "正在查看 %name%");
            this.put("gui-stacked-spawner-stats", "&6生怪磚資訊");
            this.put("gui-stacked-spawner-min-spawn-delay", "&e最小生成延遲: &b%delay%");
            this.put("gui-stacked-spawner-max-spawn-delay", "&e最大生成延遲: &b%delay%");
            this.put("gui-stacked-spawner-disabled-mob-ai", "&e已關閉生物AI: &b%disabled%");
            this.put("gui-stacked-spawner-entity-search-range", "&e生物追蹤距離: &b%range%");
            this.put("gui-stacked-spawner-player-activation-range", "&e生怪磚觸發範圍: &b%range%");
            this.put("gui-stacked-spawner-spawn-range", "&e生成範圍: &b%range%");
            this.put("gui-stacked-spawner-min-spawn-amount", "&e最少生成數量: &b%amount%");
            this.put("gui-stacked-spawner-max-spawn-amount", "&e最大生成數量: &b%amount%");
            this.put("gui-stacked-spawner-spawn-amount", "&e生成數量: &b%amount%");
            this.put("gui-stacked-spawner-spawn-conditions", "&6生成條件");
            this.put("gui-stacked-spawner-time-until-next-spawn", "&e距離下次生成還有: &b%time% ticks");
            this.put("gui-stacked-spawner-total-spawns", "&e生物已生成數量: &b%amount%");
            this.put("gui-stacked-spawner-valid-spawn-conditions", "&6有效的生成條件");
            this.put("gui-stacked-spawner-invalid-spawn-conditions", "&6無效的生成條件");
            this.put("gui-stacked-spawner-entities-can-spawn", "&a生物可以生成");
            this.put("gui-stacked-spawner-conditions-preventing-spawns", "&e生物無法生成的原因:");

            this.put("#13", "生成條件訊息");
            this.put("spawner-condition-invalid", "&7 - &c%message%");
            this.put("spawner-condition-info", "&e%condition%");
            this.put("spawner-condition-single", "&e%condition%: &b%value%");
            this.put("spawner-condition-list", "&e%condition%:");
            this.put("spawner-condition-list-item", "&7 - &b%message%");
            this.put("spawner-condition-above-sea-level-info", "海平面之上");
            this.put("spawner-condition-above-sea-level-invalid", "海平面上沒有可生成的範圍");
            this.put("spawner-condition-above-y-axis-info", "Y軸以上");
            this.put("spawner-condition-above-y-axis-invalid", "指定的Y軸以上沒有可生成範圍");
            this.put("spawner-condition-air-info", "露天");
            this.put("spawner-condition-air-invalid", "沒有足夠大的露天範圍");
            this.put("spawner-condition-below-sea-level-info", "海平面之下");
            this.put("spawner-condition-below-sea-level-invalid", "海平面下沒有可生成的範圍");
            this.put("spawner-condition-below-y-axis-info", "Y軸以下");
            this.put("spawner-condition-below-y-axis-invalid", "指定的Y軸以下沒有可生成範圍");
            this.put("spawner-condition-biome-info", "生態系");
            this.put("spawner-condition-biome-invalid", "無效的生態系");
            this.put("spawner-condition-block-info", "生成方塊");
            this.put("spawner-condition-block-invalid", "沒有有效的生成方塊");
            this.put("spawner-condition-block-exception-info", "例外生成方塊");
            this.put("spawner-condition-block-exception-invalid", "已從有效名單剔除的例外生成方塊");
            this.put("spawner-condition-darkness-info", "低亮度");
            this.put("spawner-condition-darkness-invalid", "該範圍太光了");
            this.put("spawner-condition-total-darkness-info", "完全黑暗");
            this.put("spawner-condition-total-darkness-invalid", "區域必須沒有光");
            this.put("spawner-condition-fluid-info", "要求流體");
            this.put("spawner-condition-fluid-invalid", "附近沒有流體");
            this.put("spawner-condition-lightness-info", "高亮度");
            this.put("spawner-condition-lightness-invalid", "該範圍太暗了");
            this.put("spawner-condition-max-nearby-entities-info", "附近實體上限");
            this.put("spawner-condition-max-nearby-entities-invalid", "附近太多實體了");
            this.put("spawner-condition-no-skylight-access-info", "沒有自然光");
            this.put("spawner-condition-no-skylight-access-invalid", "沒有無自然光照的方塊");
            this.put("spawner-condition-on-ground-info", "在地上");
            this.put("spawner-condition-on-ground-invalid", "附近沒有固體地面");
            this.put("spawner-condition-skylight-access-info", "自然光條件");
            this.put("spawner-condition-skylight-access-invalid", "沒有生成方塊能接觸自然光");
            this.put("spawner-condition-none-invalid", "已到達嘗試生成次數上限");
            this.put("spawner-condition-not-player-placed-invalid", "它必須由玩家放置");

            this.put("#14", "已給予的物品描述");
            this.put("#15", "Note: This will appear in the lore of the items given from the '/rs give' command");
            this.put("stack-item-lore-spawner", new ArrayList<>());
            this.put("stack-item-lore-block", new ArrayList<>());
            this.put("stack-item-lore-entity", new ArrayList<>());

            this.put("#16", "Generic Command Messages");
            this.put("no-permission", "&c你沒有權限這樣做!");
            this.put("only-player", "&c此命令僅供玩家使用。");
            this.put("unknown-command", "&c未知的命令。使用 &b/%cmd% &c幫助獲取更多信息。");
            this.put("unknown-command-error", "&c出現未知錯誤。詳細信息已打印到控制台。請聯繫服務器管理員。");
            this.put("invalid-subcommand", "&c無效的子命令。");
            this.put("invalid-argument", "&c無效的論點: %message%.");
            this.put("invalid-argument-null", "&c無效參數：%name% 為空。");
            this.put("missing-arguments", "&c缺少參數，需要 &b%amount%&c。");
            this.put("missing-arguments-extra", "&c缺少參數，需要 &b%amount%+&c。");

            this.put("#17", "Argument Handler Error Messages");
            this.put("argument-handler-enum", "%enum% 類型 [%input%] 不存在");
            this.put("argument-handler-enum-list", "%enum% 類型 [%input%] 不存在。 有效類型：%types%");
            this.put("argument-handler-string", "字符串不能為空");
            this.put("argument-handler-integer", "整數 [%input%] 必須是介於 -2^31 和 2^31-1 之間的整數");
            this.put("argument-handler-player", "在線找不到用戶名為 [%input%] 的玩家");
            this.put("argument-handler-stackplugin", "找不到名稱為 [%input%] 的插件");
            this.put("argument-handler-material", "找不到名稱為 [%input%] 的材料");
            this.put("argument-handler-stackamount", "堆棧大小 [%input%] 無效，必須是大於 0 的數字");
            this.put("argument-handler-stacktype", "堆棧類型 [%input%] 無效");
            this.put("argument-handler-translationlocale", "翻譯語言環境 [%input%] 無效");

            this.put("#19", "其他訊息");
            this.put("spawner-silk-touch-protect", "&c警告! &e你需要使用擁有絲綢之觸附魔的十字鎬或擁有拾起生怪磚的權限。 否則你不能這樣做。");
            this.put("spawner-advanced-place-no-permission", "&c警告! &e你沒有放置該類別的生怪磚的權限。");
            this.put("spawner-advanced-break-no-permission", "&c警告! &e你沒有拾起該類別的生怪磚的權限。");
            this.put("spawner-advanced-break-silktouch-no-permission", "&c警告! &e你需要使用擁有絲綢之觸附魔的十字鎬來拾起該類別的生怪磚。");
            this.put("spawner-convert-not-enough", "&c警告! 無法使用生成蛋轉換生怪磚，你並沒有持有足夠的形怪蛋。");
            this.put("number-separator", ",");
            this.put("silktouch-chance-placeholder", "%chance%%");
        }};
    }
}
