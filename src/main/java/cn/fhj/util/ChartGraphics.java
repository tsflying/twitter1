package cn.fhj.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import cn.fhj.twitter.Conversation;
//
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

@SuppressWarnings("restriction")
public class ChartGraphics {

	public static void createImage(BufferedImage image, String fileLocation) {
		FileOutputStream fos=null;
		try {
			fos = new FileOutputStream(fileLocation);
			ImageIO.write(image, "jpg", fos);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			IoUtil.close(fos);
		}
	}

	private static final Color foreColor = new Color(225, 225, 225);
	public static final int DEFAULT_WIDTH = 440;

	private static final Color tColor = new Color(235, 115, 80);
	private final Font font;
	private final FontMetrics fm;

	private final int imageWidth;// 图片的宽度
	private final int margin;
	private final int xSpace;
	private final int ySpace;
	private final int fontSize;

//	private final int dy;

	public ChartGraphics(int width) {
		this.margin = ratio(10, width);
		this.imageWidth = ratio(DEFAULT_WIDTH, width);
		this.xSpace = ratio(2, width);
		this.ySpace = ratio(4, width);
		this.fontSize = ratio(20, width);
//		this.dy=fontSize/2-10;
		font = new Font("Arial, 'Microsoft YaHei'", Font.PLAIN, fontSize);
		fm = new JLabel().getFontMetrics(font);
	}

	private int ratio(int d, int width) {
		return width * d / DEFAULT_WIDTH;
	}

	public void generate(List<Conversation> conversations, String filename) {

		int[] heights = drawText(conversations, null);
		int imageHeight = heights[0];
		BufferedImage image = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_RGB);

		Graphics graphics = image.getGraphics();
		graphics.setColor(new Color(60, 60, 60));
		graphics.fillRect(0, 0, imageWidth, imageHeight);

		graphics.setColor(Color.BLACK);
		int y = heights[1];
		graphics.fillRect(margin / 2, y, imageWidth - margin, imageHeight - y
				- margin - fontSize / 2 - ySpace);

		graphics.setColor(foreColor);
		graphics.setFont(font);

		drawText(conversations, graphics);
		graphics.dispose();
		createImage(image, filename);
	}

	private int[] drawText(List<Conversation> conversations, Graphics graphics) {
		int mainY = 0;
		Point point = new Point();
		point.x = margin;
		point.y = margin + (fontSize + ySpace) / 2;
		int i = 0;
		for (Conversation cv : conversations) {
			for (char c : cv.getOwner().toCharArray()) {
				drawChar(graphics, point, c, tColor);
			}
			drawChar(graphics, point, ':', tColor);
			for (char c : cv.getText().toCharArray()) {
				drawChar(graphics, point, c, foreColor);
			}

			if (i == conversations.size() - 2) {
				point.y += 2 * ySpace;
				if (point.x != margin) {
					point.x = margin;
					point.y += fontSize + ySpace;
				}
				mainY = point.y - fontSize;
			} else if (i < conversations.size() - 2) {
				drawChar(graphics, point, '/', foreColor);
				drawChar(graphics, point, '/', foreColor);
			}
			i++;
		}
		return new int[] {
				(point.x > margin ? point.y + fontSize / 2 : point.y)
						+ fontSize + ySpace, mainY };
	}

	public void drawChar(Graphics graphics, Point point, char c, Color color) {
		if (graphics != null) {
			graphics.setColor(color);
			graphics.drawString(String.valueOf(c), point.x, point.y);
		}
		int width = fm.stringWidth(String.valueOf(c));// 字符串宽度
		point.x += width + xSpace;
		if (point.x + margin + fontSize > imageWidth) {
			point.x = margin;
			point.y += fontSize + ySpace;
		}
	}

}
