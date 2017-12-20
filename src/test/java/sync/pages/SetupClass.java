package sync.pages;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import functional.tests.core.enums.PlatformType;
import functional.tests.core.mobile.appium.Capabilities;
import functional.tests.core.mobile.basepage.BasePage;
import functional.tests.core.mobile.element.UIElement;
import functional.tests.core.mobile.device.Device;
import functional.tests.core.mobile.settings.MobileSettings;
import functional.tests.core.image.Sikuli;
import functional.tests.core.image.ImageUtils;
import functional.tests.core.utils.FileSystem;
import io.appium.java_client.TouchAction;
import org.sikuli.script.Image;
import org.testng.Assert;
import org.sikuli.script.*;
import functional.tests.core.mobile.appium.Client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.io.File;
import functional.tests.core.mobile.element.UIRectangle;
import functional.tests.core.utils.OSUtils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
public class SetupClass extends BasePage {
public  Screen s = new Screen();;
public String liveSyncConnectionString;
public String deviceId = "";
public Sikuli sikuli;
public String ImagePathDirectory = "";
public ImageUtils imageUtils;
public Device device;
public int deviceScreenWidth;
public String appName;
public Client client;
public App browserAPP;
public String typeOfProject = OSUtils.getEnvironmentVariable("typeOfProject","js");
public String browser = OSUtils.getEnvironmentVariable("browser","Google Chrome");
public String folderForScreenshots;

    public SetupClass(Client client, MobileSettings mobileSettings, Device device) throws InterruptedException, IOException, FindFailed {
        super();
        this.client = client;
        this.device = device;
        this.imageUtils = new ImageUtils(settings, client, device);
        this.appName = this.app.getName().replaceAll(".app", "");
        this.sikuli = new Sikuli(this.appName, client, this.imageUtils);
        String currentPath = System.getProperty("user.dir");
        ImagePathDirectory = currentPath+"/src/test/java/sync/pages/images.sikuli";
        this.folderForScreenshots = currentPath+"/target/surefire-reports/screenshots/";
        this.client.driver.removeApp("org.nativescript.preview");
        this.wait(2000);
        if(settings.deviceType == settings.deviceType.Simulator)
        {
            functional.tests.core.utils.Archive.extractArchive(new File(currentPath+"/testapp/nsplaydev.tgz"),new File(currentPath+"/testapp/"));
            this.wait(2000);
            functional.tests.core.mobile.device.ios.IOSDevice ios = new functional.tests.core.mobile.device.ios.IOSDevice(client, mobileSettings);
            ios.installApp("nsplaydev.app","org.nativescript.preview");
            this.deviceId=ios.getId();
            context.settings.packageId = "org.nativescript.preview";
            context.settings.testAppFileName = "nsplaydev.app";
            Capabilities newiOSCapabilities = new Capabilities();
            context.client.driver = new IOSDriver(context.server.service.getUrl(), newiOSCapabilities.loadDesiredCapabilities(context.settings));
        }
        else {
            functional.tests.core.mobile.device.android.AndroidDevice android = new functional.tests.core.mobile.device.android.AndroidDevice(client, mobileSettings);
            android.installApp("preview-release.apk", "org.nativescript.preview");
            this.deviceId=android.getId();
            context.settings.packageId = "org.nativescript.preview";
            context.settings.testAppFileName = "preview-release.apk";
            Capabilities newAndroidCapabilities = new Capabilities();
            context.client.driver = new AndroidDriver(context.server.service.getUrl(), newAndroidCapabilities.loadDesiredCapabilities(context.settings));
        }

        ImagePath.add(ImagePathDirectory);
        //this.CloseBrowser();
        this.OpenBrowser();
    }

    public void CloseBrowser() throws InterruptedException, IOException {
        App.close(this.browser);
        this.wait(5000);
    }

    public void OpenBrowser() throws InterruptedException {
        this.wait(1000);
        this.browserAPP = App.open(this.browser);
        this.wait(12000);
        s.type("f", KeyModifier.CMD+KeyModifier.CTRL);
        this.wait(2000);
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
        this.deviceScreenWidth = client.driver.manage().window().getSize().width;
        if(settings.deviceType == settings.deviceType.Simulator)
        {
            this.liveSyncConnectionString = this.liveSyncConnectionString.replaceAll("\\\\", "/");
            params = java.util.Arrays.asList("xcrun", "simctl", "openurl", this.deviceId, liveSyncConnectionString);
        }
        else
        {
            log.info(liveSyncConnectionString);
            params = java.util.Arrays.asList(System.getenv("ANDROID_HOME")+"/platform-tools/adb", "-s" ,this.deviceId, "shell" ,"am" , "start" , "-a", "android.intent.action.VIEW", "-d", "\""+liveSyncConnectionString+"\"", "org.nativescript.preview");
        }

        try {
            ProcessBuilder pb = new
                    ProcessBuilder(params);
            log.info(pb.command().toString());
            final Process p = pb.start();
            log.info("Start logging command...");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                log.info(line);
            }
            log.info("End logging command...");
        } catch (Exception ex) {
            log.info(ex.toString());
        }

        if(settings.deviceType == settings.deviceType.Simulator) {
            log.info("Searching for Home or Open");
            this.wait(5000);
            String foundItem = this.waitText1OrText2ToBeShown(12,"Home", "Open");
            log.info("Found Item "+foundItem);
            if(foundItem == "Open") {
                if (this.settings.platformVersion.toString().contains("10.") || this.settings.platformVersion.toString().contains("9.")) {
                    if(this.settings.platformVersion.toString().contains("10.")) {
                        this.client.driver.switchTo().alert().accept();

                        this.wait(2000);
                        this.waitPreviewAppToLoad(10, "Open");

                        this.client.driver.switchTo().alert().accept();

                        this.wait(2000);
                    }
                    else {
                        this.client.driver.switchTo().alert().dismiss();
                        this.wait(8000);
                        this.client.driver.switchTo().alert().dismiss();
                        this.wait(8000);
                        this.client.driver.switchTo().alert().dismiss();
                        this.wait(8000);
                    }
                }
                else {
                    this.find.byText("Open").click();
                    this.waitPreviewAppToLoad(10, "Open");
                    this.find.byText("Open").click();
                }
            }

        }

        this.waitPreviewAppToLoad(10);
    }

    public void waitPreviewAppToLoad(int numberOfTries) throws InterruptedException {
        this.waitPreviewAppToLoad(numberOfTries, "Home");
    }

    public String waitText1OrText2ToBeShown(int numberOfTries, String text1, String text2) throws InterruptedException {
        String textFound="";
        while (true) {
            if (this.settings.platformVersion.toString().contains("10.") || this.settings.platformVersion.toString().contains("9.")) {
                log.info("Search for image!");
                if (this.sikuli.waitForImage(text1, 0.7d, 2)) {
                    textFound = text1;
                    break;
                }
                if (this.sikuli.waitForImage(text2, 0.7d, 2)) {
                    textFound = text2;
                    break;
                }
                numberOfTries = numberOfTries - 1;
                if (numberOfTries <= 0) {
                    log.info("Image "+ text1 + " and Image "+text2 + " are not found!");
                    break;
                }

            } else {
                log.info("Search for text!");
                UIElement text1element = this.find.byText(text1, false, this.settings.shortTimeout);
                UIElement text2element = this.find.byText(text2, false, this.settings.shortTimeout);
                log.info("start checking!");
                if (text1element != null) {
                    textFound = text1;
                    log.info("Found "+textFound);
                    break;
                }
                if (text2element != null) {
                    textFound = text2;
                    log.info("Found "+textFound);
                    break;
                }
                numberOfTries = numberOfTries - 1;
                if (numberOfTries <= 0) {
                    log.info("Text  " + text1 + " and Text " + text2 + " are not found!");
                    break;
                }
            }
            log.info("Nothing found in turn "+numberOfTries);
        }
        log.info("Exit loop! Text found "+textFound);
        return textFound;
    }

    public void waitTextToBeShown(int numberOfTries, String object) throws InterruptedException {
        while (true)
        {
            if (this.settings.platformVersion.toString().contains("10.") || this.settings.platformVersion.toString().contains("9.")) {
                if (this.sikuli.waitForImage(object, 0.7d, 2)) {
                    break;
                }
                numberOfTries = numberOfTries - 1;
                if (numberOfTries <= 0) {
                    log.info("Image "+ object + " is not found!");
                    break;
                }

            }
            else {
                UIElement home = this.find.byText(object);
                if (home != null || numberOfTries <= 0) {
                    if (numberOfTries <= 0) {
                        log.info("Text " + object + " is not found!");
                    }
                    break;
                }
                numberOfTries = numberOfTries - 1;
            }
            log.info("Nothing found in turn "+numberOfTries);
        }
    }

    public void waitPreviewAppToLoad(int numberOfTries, String object) throws InterruptedException {
        this.waitTextToBeShown(numberOfTries,object);
        if (this.settings.platformVersion.toString().contains("10.") || this.settings.platformVersion.toString().contains("9.")) {
            UIRectangle home = this.findImageOnScreen(object, 0.9d);
            Assert.assertNotNull(home, "Preview app not synced! Item missing "+ object);
            this.log.info("Preview app synced! The item "+object+" is found!");
        }
        else{
            UIElement home = this.find.byText(object);
            Assert.assertNotNull(home, "Preview app not synced! Item missing "+ object);
            this.log.info("Preview app synced! The item "+object+" is found!");
        }


    }

    public UIRectangle findImageOnScreen(String imageName, double similarity) {
        BufferedImage screenBufferImage = this.device.getScreenshot();
        Finder finder = this.getFinder(screenBufferImage, imageName, (float)similarity);
        Match searchedImageMatch = finder.next();
        Point point;
        if(searchedImageMatch != null) {
            point = searchedImageMatch.getCenter().getPoint();
        }
        else
        {
            return null;
        }
        Rectangle rectangle = this.getRectangle(point, screenBufferImage.getWidth());
        return new UIRectangle(rectangle);
    }
    private Finder getFinder(BufferedImage screenBufferImage, String imageName, float similarity) {
        ImageUtils var10001 = this.imageUtils;
        BufferedImage searchedBufferImage = this.imageUtils.getImageFromFile(ImageUtils.getImageFullName(this.getImageFolderPath(this.appName), imageName));
        Image searchedImage = new Image(searchedBufferImage);
        Pattern searchedImagePattern = new Pattern(searchedImage);
        Image mainImage = new Image(screenBufferImage);
        searchedImagePattern.similar(similarity);
        Finder finder = new Finder(mainImage);
        finder.findAll(searchedImagePattern);
        return finder;
    }
    protected String getImageFolderPath(String appName) {
        String imageFolderPath = this.settings.screenshotResDir + File.separator + appName + File.separator + this.settings.deviceName;
        FileSystem.ensureFolderExists(imageFolderPath);
        return imageFolderPath;
    }
    public Rectangle getRectangle(Point point, int screenShotWidth) {
        int densityRatio = this.getDensityRatio(screenShotWidth);
        Rectangle rectangle = new Rectangle(point.x / densityRatio, point.y / densityRatio, 50, 50);
        return rectangle;
    }

    private int getDensityRatio(int screenshotWidth) {
        return this.client.settings.platform == PlatformType.iOS ? screenshotWidth / this.deviceScreenWidth : 1;
    }

    public void wait(int time) {
        synchronized(this.s) {
            try {
                this.s.wait(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void giveFocus() throws InterruptedException {
        this.browserAPP.focus();
        this.wait(2000);
    }

    public String getComputerName()
    {
        String computerName = "";
        try {
            ProcessBuilder pb = new
                    ProcessBuilder("hostname");
            log.info(pb.command().toString());
            final Process p = pb.start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                log.info(line);
                if(line.trim()!="")
                {
                    computerName = line.trim();
                }
            }
        } catch (Exception ex) {
            log.info(ex.toString());
        }
        return  computerName;
    }

    public String getIOSVersion()
    {
        String version = "";
        List<String> params = java.util.Arrays.asList("xcrun", "simctl", "getenv", this.deviceId, "SIMULATOR_RUNTIME_VERSION");

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
                if(line.trim()!="")
                {
                    version = line.trim();
                }
            }
        } catch (Exception ex) {
            log.info(ex.toString());
        }
        return  version;
    }

    public void getScreenShot(String screenshotName){
        try {
            Process p = Runtime.getRuntime().exec("screencapture -C -x "+this.folderForScreenshots+screenshotName+".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeTutorial()
    {
        Region gettingStartedRegion = null;
        Region closeButton = null;
        try {
             gettingStartedRegion = this.s.find("gettingstartedlogo");
        } catch (FindFailed findFailed) {
            findFailed.printStackTrace();
            log.info("Tutorial is not opened!");
        }
        if(gettingStartedRegion!=null) {
            try {
                closeButton = gettingStartedRegion.right().find("closebutton");
            } catch (FindFailed findFailed) {
                findFailed.printStackTrace();
                log.info("Couldn't find close button for tutorial!");
            }
            closeButton.click();
            this.wait(3000);
            try {
                s.click("okbuttonTutorial");
            } catch (FindFailed findFailed) {
                findFailed.printStackTrace();
            }
            this.wait(3000);
            log.info("Tutorial is closed!");
        }
    }
}
