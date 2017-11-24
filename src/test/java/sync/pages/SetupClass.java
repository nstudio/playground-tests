package sync.pages;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import functional.tests.core.mobile.basepage.BasePage;
import functional.tests.core.mobile.element.UIElement;
import functional.tests.core.mobile.settings.MobileSettings;
import org.testng.Assert;
import org.sikuli.script.*;
import functional.tests.core.mobile.appium.Client;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class SetupClass extends BasePage {
public  Screen s = new Screen();;
public String liveSyncConnectionString;
    public SetupClass(Client client, MobileSettings mobileSettings) throws InterruptedException, IOException, FindFailed {
        super();
        if(settings.deviceType == settings.deviceType.Simulator)
        {
            functional.tests.core.mobile.device.ios.IOSDevice ios = new functional.tests.core.mobile.device.ios.IOSDevice(client, mobileSettings);
            ios.installApp("nsplaydev.app",null);
        }
        else {
            functional.tests.core.mobile.device.android.AndroidDevice android = new functional.tests.core.mobile.device.android.AndroidDevice(client, mobileSettings);
            android.installApp("preview-release.apk", "org.nativescript.preview");
        }
        String currentPath = System.getProperty("user.dir");
        ImagePath.add(currentPath+"/src/test/java/sync/pages/images.sikuli");
        this.CloseSafari();
        this.OpenSafari();
    }

    public void CloseSafari() throws InterruptedException, IOException {
        Runtime.getRuntime().exec("killAll Safari");
    }

    public void OpenSafari() throws InterruptedException {
        this.wait(1000);
        App.open("Safari");
        this.wait(10000);
        s.type("f", KeyModifier.CMD+KeyModifier.CTRL);
        this.wait(1000);
    }

    public void NavigateToPage(String URL) throws InterruptedException {
        s.type("l", KeyModifier.CMD);
        this.wait(1000);
        s.type(URL);
        this.wait(1000);
        s.type(Key.ENTER);
        this.wait(10000);

    }

    public void GetDeviceLink() throws InterruptedException, FindFailed, IOException, UnsupportedFlavorException {
        s.dragDrop(new Pattern("devicesLinkMessage.png").similar(0.63f).targetOffset(-101,0),
                new Pattern("devicesLinkMessage.png").similar(0.63f).targetOffset(500,25));
        this.wait(3000);
        s.type("c", KeyModifier.CMD);
        this.liveSyncConnectionString = (String) Toolkit.getDefaultToolkit()
                .getSystemClipboard().getData(DataFlavor.stringFlavor);

    }

    public void startPreviewAppWithLiveSync() throws InterruptedException, FindFailed, IOException {
        List<String> params;

        if(settings.deviceType == settings.deviceType.Simulator)
        {
            this.liveSyncConnectionString = this.liveSyncConnectionString.replaceAll("\\\\", "/");
            params = java.util.Arrays.asList("xcrun", "simctl", "openurl", "booted", liveSyncConnectionString);
        }
        else
        {
            log.info(liveSyncConnectionString);
            params = java.util.Arrays.asList("adb", "shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", "\""+liveSyncConnectionString+"\"", "org.nativescript.preview");
        }

        try {
            ProcessBuilder pb = new
                    ProcessBuilder(params);
            log.info(pb.command().toString());
            final Process p = pb.start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                log.info(line);
            }
        } catch (Exception ex) {
            log.info(ex.toString());
        }

        if(settings.deviceType == settings.deviceType.Simulator) {
            this.wait(2000);
            this.find.byText("Open").click();
            this.wait(3000);
            this.find.byText("Open").click();
        }

        this.waitPreviewAppToLoad(10);
    }

    public void waitPreviewAppToLoad(int numberOfTries) throws InterruptedException {
        while (true)
        {
            UIElement home = this.find.byText(("Home"));
            if(home!=null || numberOfTries<=0)
            {
                break;
            }
            else {
                numberOfTries=numberOfTries-1;
                this.wait(1000);
            }
        }
        UIElement home = this.find.byText("Home");
        Assert.assertNotNull(home, "Preview app not synced!");
        this.log.info("Preview app synced!");
    }

    public void wait(int time) throws InterruptedException {
        synchronized(this.s) {
            this.s.wait(time);
        }
    }
}