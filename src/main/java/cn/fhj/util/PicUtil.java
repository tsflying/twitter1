package cn.fhj.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.fhj.twitter.Conversation;
import cn.fhj.twitter.Twitter;

public class PicUtil {
	public static void createJpg(List<Conversation> Conversations,
			String filename, int width) {
		try {
			new ChartGraphics(width).generate(Conversations, filename);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String mergeImage(List<String> fileNames) {
		if (fileNames.isEmpty()) {
			return null;
		}
		if (fileNames.size() == 1) {
			return fileNames.get(0);
		}
		List<BufferedImage> imgs = new ArrayList();
		int width = -1;
		int height = 0;
		int vSpace = 5;
		try {
			for (String fileName : fileNames) {
				BufferedImage img = javax.imageio.ImageIO.read(new File(
						fileName));
				if (width < img.getWidth()) {
					width = img.getWidth();
				}
				imgs.add(img);
			}
			for (BufferedImage img : imgs) {
				height += img.getHeight() * width / img.getWidth() + vSpace;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		int y = 0;
		for (BufferedImage img : imgs) {
			int h = img.getHeight() * width / img.getWidth();
			graphics.drawImage(img, 0, y, width, h, null);
			y += h + vSpace;
		}
		String fileLocation = Twitter.getDataFold() + "pic.jpg";
		ChartGraphics.createImage(image, fileLocation);
		return fileLocation;
	}

	public static int getWidth(List<String> pics) {
		int width = ChartGraphics.DEFAULT_WIDTH;
		try {
			for (String pic : pics) {
				BufferedImage img = javax.imageio.ImageIO.read(new File(pic));
				if (width < img.getWidth()) {
					width = img.getWidth();
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return width;
	}
}
