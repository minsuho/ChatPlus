package com.ebicep.chatplus.config.fabric

import com.ebicep.chatplus.config.Config
import com.ebicep.chatplus.config.TimestampMode
import com.ebicep.chatplus.config.queueUpdateConfig
import com.ebicep.chatplus.hud.ChatTab
import com.mojang.blaze3d.platform.InputConstants
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.gui.entries.*
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.util.*
import java.util.function.Consumer

object ConfigScreenImpl {

    @JvmStatic
    fun getConfigScreen(previousScreen: Screen? = null): Screen {
        val builder: ConfigBuilder = ConfigBuilder.create()
            .setParentScreen(previousScreen)
            .setTitle(Component.translatable("chatplus.title"))
            .setSavingRunnable(Config::save)
            .transparentBackground()
        val entryBuilder: ConfigEntryBuilder = builder.entryBuilder()
        addGeneralOptions(builder, entryBuilder)
        addChatTabsOption(builder, entryBuilder)
        addKeyBindOptions(builder, entryBuilder)
        return builder.build()
    }

    private fun addGeneralOptions(builder: ConfigBuilder, entryBuilder: ConfigEntryBuilder) {
        val general = builder.getOrCreateCategory(Component.translatable("chatPlus.title"))
        general.addEntry(entryBuilder.booleanToggle("chatPlus.chatSettings.toggle", Config.values.enabled) { Config.values.enabled = it })
        general.addEntry(
            entryBuilder.intSlider(
                "chatPlus.chatSettings.maxMessages",
                Config.values.maxMessages,
                1000,
                10_000_000
            ) { Config.values.maxMessages = it })
        general.addEntry(entryBuilder.percentSlider("chatPlus.chatSettings.chatTextSize", Config.values.scale) { Config.values.scale = it })
        general.addEntry(
            entryBuilder.percentSlider(
                "chatPlus.chatSettings.textOpacity",
                Config.values.textOpacity
            ) { Config.values.textOpacity = it })
        general.addEntry(
            entryBuilder.percentSlider(
                "chatPlus.chatSettings.backgroundOpacity",
                Config.values.backgroundOpacity
            ) { Config.values.backgroundOpacity = it })
        general.addEntry(
            entryBuilder.percentSlider(
                "chatPlus.chatSettings.lineSpacing",
                Config.values.lineSpacing
            ) { Config.values.lineSpacing = it })
        general.addEntry(entryBuilder.startEnumSelector(
            Component.translatable("chatPlus.chatSettings.chatTimestampMode"),
            TimestampMode::class.java,
            Config.values.chatTimestampMode
        )
            .setEnumNameProvider { (it as TimestampMode).translatable }
            .setDefaultValue(Config.values.chatTimestampMode)
            .setTooltip(Component.translatable("chatPlus.chatSettings.chatTimestampMode.tooltip"))
            .setSaveConsumer { Config.values.chatTimestampMode = it }
            .build())
    }

    private fun addChatTabsOption(builder: ConfigBuilder, entryBuilder: ConfigEntryBuilder) {
        val chatTabs = builder.getOrCreateCategory(Component.translatable("chatPlus.chatTabs.title"))
        chatTabs.addEntry(
            getCustomListOption(
                "chatPlus.chatTabs.title",
                Config.values.chatTabs,
                { Config.values.chatTabs = it },
                Config.values.chatTabs.size in 2..9,
                { ChatTab("", "") },
                { value ->
                    listOf(
                        entryBuilder.startStrField(Component.translatable("chatPlus.chatTabs.name"), value.name)
                            .setTooltip(Component.translatable("chatPlus.chatTabs.name.tooltip"))
                            .setDefaultValue("")
                            .setSaveConsumer { value.name = it }
                            .build(),
                        entryBuilder.startStrField(Component.translatable("chatPlus.chatTabs.pattern"), value.pattern)
                            .setTooltip(Component.translatable("chatPlus.chatTabs.pattern.tooltip"))
                            .setDefaultValue("")
                            .setSaveConsumer { value.pattern = it }
                            .build(),
                    )
                }

            )
        )
    }

    private fun addKeyBindOptions(builder: ConfigBuilder, entryBuilder: ConfigEntryBuilder) {
        val keyBinds = builder.getOrCreateCategory(Component.translatable("chatPlus.chatKeyBinds"))
        keyBinds.addEntry(
            entryBuilder.keyCodeOption("key.noScroll", Config.values.keyNoScroll) { Config.values.keyNoScroll = it }
        )
        keyBinds.addEntry(
            entryBuilder.keyCodeOption("key.fineScroll", Config.values.keyFineScroll) { Config.values.keyFineScroll = it }
        )
        keyBinds.addEntry(
            entryBuilder.keyCodeOption("key.largeScroll", Config.values.keyLargeScroll) { Config.values.keyLargeScroll = it }
        )
        keyBinds.addEntry(
            entryBuilder.keyCodeOption("key.moveChat", Config.values.keyMoveChat) { Config.values.keyMoveChat = it }
        )
        keyBinds.addEntry(
            entryBuilder.keyCodeOption("key.copyMessage", Config.values.keyCopyMessage) { Config.values.keyCopyMessage = it }
        )
//        keyBinds.addEntry(
//            entryBuilder.startModifierKeyCodeField(
//                Component.translatable("test"),
//                ModifierKeyCode.of(Config.values.keyCopyMessage, Modifier.of(false, true, false))
//            ).build()
//        )
    }

    private fun ConfigEntryBuilder.booleanToggle(
        translatable: String,
        variable: Boolean,
        saveConsumer: Consumer<Boolean>
    ): BooleanListEntry {
        return startBooleanToggle(Component.translatable(translatable), variable)
            .setDefaultValue(variable)
            .setTooltip(Component.translatable("$translatable.tooltip"))
            .setSaveConsumer {
                saveConsumer.accept(it)
                queueUpdateConfig = true
            }
            .build()
    }

    private fun ConfigEntryBuilder.percentSlider(translatable: String, variable: Float, saveConsumer: Consumer<Float>): IntegerSliderEntry {
        return percentSlider(translatable, variable, 0, 1, saveConsumer)
    }

    private fun ConfigEntryBuilder.percentSlider(translatable: String, variable: Float, min: Int, max: Int, saveConsumer: Consumer<Float>):
            IntegerSliderEntry {
        val intValue = (variable * 100).toInt()
        return startIntSlider(Component.translatable(translatable), intValue, min * 100, max * 100)
            .setDefaultValue(intValue)
            .setTooltip(Component.translatable("$translatable.tooltip"))
            .setTextGetter { Component.literal("$it%") }
            .setSaveConsumer {
                saveConsumer.accept(it / 100f)
                queueUpdateConfig = true
            }
            .build()
    }

    private fun ConfigEntryBuilder.intSlider(
        translatable: String,
        variable: Int,
        min: Int,
        max: Int,
        saveConsumer: Consumer<Int>
    ): IntegerSliderEntry {
        return startIntSlider(Component.translatable(translatable), variable, min, max)
            .setDefaultValue(variable)
            .setTooltip(Component.translatable("$translatable.tooltip"))
            .setSaveConsumer {
                saveConsumer.accept(it)
                queueUpdateConfig = true
            }
            .build()
    }

    private fun <T> getCustomListOption(
        translatable: String,
        list: MutableList<T>,
        saveConsumer: Consumer<MutableList<T>>,
        canDelete: Boolean,
        create: () -> T,
        render: (T) -> List<AbstractConfigListEntry<*>>
    ): NestedListListEntry<T, MultiElementListEntry<T>> {
        return NestedListListEntry(
            Component.translatable(translatable),
            list,
            true,
            { Optional.empty() },
            saveConsumer,
            { mutableListOf() },
            Component.literal("Reset"),
            canDelete,
            false,
            { value, entry ->
                val v = value ?: create()
                MultiElementListEntry(Component.empty(), v, render(v), true)
            }
        )
    }

    private fun ConfigEntryBuilder.keyCodeOption(
        translatable: String,
        variable: InputConstants.Key,
        saveConsumer: Consumer<InputConstants.Key>
    ): KeyCodeEntry {
        return startKeyCodeField(Component.translatable(translatable), variable)
            .setDefaultValue(variable)
            //.setTooltip(Component.translatable("$translatable.tooltip"))
            .setKeySaveConsumer {
                saveConsumer.accept(it)
                queueUpdateConfig = true
            }
            .build()
    }
}