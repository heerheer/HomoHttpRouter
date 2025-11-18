package top.realme.mc.homohttprouter;

import net.neoforged.fml.event.config.ModConfigEvent;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.homohttprouter.event.HttpServiceBuildEvent;
import top.realme.mc.homohttprouter.http.HttpServerManager;
import top.realme.mc.homohttprouter.http.RouterRegistry;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(HomoHttpRouter.MODID)
public class HomoHttpRouter {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "homohttprouter";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // 私有成员，管理 HTTP 服务器
    private static HttpServerManager httpServerManager;
    private static final RouterRegistry ROUTER_REGISTRY = new RouterRegistry();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public HomoHttpRouter(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (HomoHttpRouter) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HomoHttpRouter common setup");

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        event.getServer().execute(() -> {
            int port = Config.PORT.get();
            httpServerManager = new HttpServerManager(port, ROUTER_REGISTRY);
            httpServerManager.start();

            // 发出构建事件，其他 mod 在这时注册自己的路由
            NeoForge.EVENT_BUS.post(new HttpServiceBuildEvent(ROUTER_REGISTRY));
        });
    }

    private void onConfigReload(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != Config.SPEC) return;

        LOGGER.info("Config changed, restarting HTTP server.");

        if (httpServerManager != null) httpServerManager.stop();

        int port = Config.PORT.get();
        httpServerManager = new HttpServerManager(port, ROUTER_REGISTRY);
        httpServerManager.start();
    }
}
