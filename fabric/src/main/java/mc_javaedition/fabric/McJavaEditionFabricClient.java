package mc_javaedition.fabric;

import jinrui.mcdar.client.McdarClient;
import mcd_java.client.McdaClient;
import mcd_java.mcdw.client.McdwClient;
import net.backupcup.mcde.MCDEClient;
import net.fabricmc.api.ClientModInitializer;

public class McJavaEditionFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new McdaClient().onInitializeClient();
        new McdwClient().onInitializeClient();
        new McdarClient().onInitializeClient();
        new MCDEClient().onInitializeClient();
        CorruptedBeaconSoulHudFabric.register();
    }
}
