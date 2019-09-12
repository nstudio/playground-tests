package utils;

import functional.tests.core.enums.PlatformType;
import functional.tests.core.image.ImageUtils;
import functional.tests.core.mobile.appium.Client;
import functional.tests.core.mobile.device.Device;
import functional.tests.core.mobile.element.UIRectangle;
import functional.tests.core.utils.FileSystem;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class Sikuli {
    private Device device;
    private String appName;
    private Client client;
    private ImageUtils imageUtils;

    public Sikuli(Device device, String appName, Client client, ImageUtils imageUtils) {
        this.device = device;
        this.appName = appName;
        this.client = client;
        this.imageUtils = imageUtils;
    }

    public UIRectangle findImageOnScreen(String imageName, double similarity) {
        BufferedImage screenBufferImage = this.getScreen();

        Finder finder = this.getFinder(screenBufferImage, imageName, (float) similarity);

        Match searchedImageMatch = finder.next();
        Point point = searchedImageMatch.getCenter().getPoint();

        Rectangle rectangle = this.getRectangle(point, screenBufferImage.getWidth());

        return new UIRectangle(rectangle);
    }

    public UIRectangle[] findImagesOnScreen(String imageName, double similarity) {
        BufferedImage screenBufferImage = this.getScreen();

        Finder finder = this.getFinder(screenBufferImage, imageName, (float) similarity);

        ArrayList<UIRectangle> rectangles = new ArrayList<>();

        while (finder.hasNext()) {
            Match searchedImageMatch = finder.next();
            Point point = searchedImageMatch.getCenter().getPoint();

            Rectangle rectangle = this.getRectangle(point, screenBufferImage.getWidth());

            rectangles.add(new UIRectangle(rectangle));
        }

        UIRectangle[] rectanglesArray = new UIRectangle[rectangles.size()];

        return rectangles.toArray(rectanglesArray);
    }

    public boolean waitForImage(String imageName, double similarity, int timeoutInSeconds) {
        BufferedImage screenBufferImage = this.getScreen();
        timeoutInSeconds *= 1000;

        Finder finder = this.getFinder(screenBufferImage, imageName, (float) similarity);

        Match searchedImageMatch = finder.next();

        if (searchedImageMatch != null && searchedImageMatch.isValid()) {
            return true;
        }

        while (searchedImageMatch == null && timeoutInSeconds > 0) {
            this.client.setWait(1000);
            timeoutInSeconds -= 1000;
            screenBufferImage = this.getScreen();
            finder = this.getFinder(screenBufferImage, imageName, (float) similarity);

            searchedImageMatch = finder.next();
        }

        return timeoutInSeconds > 0 && searchedImageMatch != null && searchedImageMatch.isValid();
    }

    public UIRectangle findTextOnScreen(String text) {
        Settings.InfoLogs = true;
        Settings.OcrTextSearch = true;
        Settings.OcrTextRead = true;

        Image mainImage = new Image(this.getScreen());
        Finder finder = new Finder(mainImage);
        finder.findAllText(text);
        Match searchedImageMatch = finder.next();

        Point point = searchedImageMatch.getCenter().getPoint();
        Rectangle rectangle = this.getRectangle(point, mainImage.getSize().width);

        return new UIRectangle(rectangle);
    }

    private Finder getFinder(BufferedImage screenBufferImage, String imageName, float similarity) {
        BufferedImage searchedBufferImage = this.imageUtils.getImageFromFile(this.imageUtils.getImageFullName(this.getImageFolderPath(this.appName), imageName));
        Image searchedImage = new Image(searchedBufferImage);
        Pattern searchedImagePattern = new Pattern(searchedImage);

        Image mainImage = new Image(screenBufferImage);

        searchedImagePattern.similar(similarity);

        Finder finder = new Finder(mainImage);
        finder.findAll(searchedImagePattern);

        return finder;
    }

    private Rectangle getRectangle(Point point, int screenShotWidth) {
        int densityRatio = this.getDensityRatio(screenShotWidth);

        Rectangle rectangle = new Rectangle(point.x / densityRatio, point.y / densityRatio, 50, 50);

        return rectangle;
    }

    private int getDensityRatio(int screenshotWidth) {
        if (this.client.settings.platform == PlatformType.iOS) {
            return screenshotWidth / this.client.driver.manage().window().getSize().width;
        } else {
            return 1;
        }
    }

    private BufferedImage getScreen() {
        return this.device.getScreenshot();
    }

    private String getImageFolderPath(String appName) {
        String imageFolderPath = this.client.settings.screenshotResDir + File.separator + appName + File.separator + this.client.settings.deviceName;
        FileSystem.ensureFolderExists(imageFolderPath);
        return imageFolderPath;
    }

}
