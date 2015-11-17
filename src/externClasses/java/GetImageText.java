package externClasses.java;

/*
  This program is free software; you can redistribute it and/or modify it
  under the terms of the GNU Library General Public License as published
  by the Free Software Foundation; either version 2, or (at your option)
  any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Library General Public License for more details.

  You should have received a copy of the GNU Library General Public
  License along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;


/**
 * Get text from images
 *
 * @author <a href="http://www.abstractnonsense.com">Dr. William Bland</a>
 * @version 1.0
 */
public class GetImageText {
    private BufferedImage image;

    /**
     * Default constructor
     *
     * @param img The image containing text
     */
    public GetImageText(BufferedImage img) {
        image = img;
        merge_densityFactor = 0.5;
        merge_mass = 15;
        merge_dist1 = 4;
        merge_distfac = 1;
        merge_dist2 = 20;
    }

    /**
     * Constructor for testing purposes
     */
    public GetImageText(BufferedImage img, double m_densityFactor,
                        int m_mass, int m_dist1, double m_distfac,
                        int m_dist2) {
        image = img;
        merge_densityFactor = m_densityFactor;
        merge_mass = m_mass;
        merge_dist1 = m_dist1;
        merge_distfac = m_distfac;
        merge_dist2 = m_dist2;
    }

    /**
     * Only for debugging - prints out the current parameters
     */
    public void print() {
        System.out.println("m_densityFactor = " + merge_densityFactor);
        System.out.println("m_mass = " + merge_mass);
        System.out.println("m_dist1 = " + merge_dist1);
        System.out.println("m_distfac = " + merge_distfac);
        System.out.println("m_dist2 = " + merge_dist2);
    }

    int red(int rgb) {
        return (rgb & 0xff0000) >> 16;
    }

    int green(int rgb) {
        return (rgb & 0x00ff00) >> 8;
    }

    int blue(int rgb) {
        return rgb & 0xff;
    }

    int rgb(int red, int green, int blue) {
        return blue + (green << 8) + (red << 16);
    }

    /**
     * Discard boxes that do not appear to contain text
     */
    LinkedList discardNonText(LinkedList boxes, int[][] contrast) {
        int i = 0;
        while (i < boxes.size()) {
            int numberOfStems = 0;
            TextRegion thisBox = (TextRegion) boxes.get(i);
            // Count the stems in this box
            if (thisBox.y1 != thisBox.y2) {
                for (int a = thisBox.x1 + 1; a < thisBox.x2 - 1; a++) {
                    int thisStemHeight = 0;
                    for (int b = thisBox.y1 + 1; b < thisBox.y2 - 1; b++)
                        if ((contrast[a][b] != 0
                                || contrast[a - 1][b] != 0
                                || contrast[a + 1][b] != 0)
                                && (contrast[a][b - 1] != 0
                                || contrast[a - 1][b - 1] != 0
                                || contrast[a + 1][b - 1] != 0)
                                && (contrast[a][b + 1] != 0
                                || contrast[a - 1][b + 1] != 0
                                || contrast[a + 1][b + 1] != 0))
                            thisStemHeight++;
                    //a stem must cover at least 70% of a vertical line
                    if ((100 * thisStemHeight) / thisBox.height() > 70)
                        numberOfStems++;
                }
            }
            if (thisBox.area() < 50
                    || thisBox.aspect() > .2
                    || thisBox.height() < 5
                    || thisBox.width() < 20
                    // expect at least one stem for every <height> of <width>
                    || numberOfStems < thisBox.width() / thisBox.height())
                boxes.remove(i--);
            i++;
        }
        return (boxes);
    }

    /**
     * Shrink each box as much as possible
     */
    LinkedList shrink(LinkedList boxes, int[][] contrast) {
        int i = 0;
        while (i < boxes.size()) {
            TextRegion thisBox = (TextRegion) boxes.get(i);
            if (thisBox.x1 != thisBox.x2
                    && thisBox.y1 != thisBox.y2) {
                int total = 0;
                for (int a = thisBox.x1; a < thisBox.x2; a++)
                    for (int b = thisBox.y1; b < thisBox.y2; b++)
                        total += contrast[a][b];
                double averagex = total / thisBox.height();
                double averagey = total / thisBox.width();
                int newx1 = thisBox.x1;
                int newx2 = thisBox.x2;
                int newy1 = thisBox.y1;
                int newy2 = thisBox.y2;
                boolean moved = true;
                while (newx1 < newx2 && moved) {
                    moved = false;
                    int t1 = 0, t2 = 0;
                    for (int b = thisBox.y1; b < thisBox.y2; b++) {
                        t1 += contrast[newx1][b];
                        t2 += contrast[newx2][b];
                    }
                    if (t1 < averagey) {
                        newx1++;
                        moved = true;
                    }
                    if (t2 < averagey) {
                        newx2--;
                        moved = true;
                    }
                }
                moved = true;
                while (newy1 < newy2 && moved) {
                    moved = false;
                    int t1 = 0, t2 = 0;
                    for (int a = thisBox.x1; a < thisBox.x2; a++) {
                        t1 += contrast[a][newy1];
                        t2 += contrast[a][newy2];
                    }
                    if (t1 < averagex) {
                        newy1++;
                        moved = true;
                    }
                    if (t2 < averagex) {
                        newy2--;
                        moved = true;
                    }
                }
                thisBox.x1 = newx1;
                thisBox.x2 = newx2;
                thisBox.y1 = newy1;
                thisBox.y2 = newy2;
            }
            i++;
        }
        return (boxes);
    }

    public double merge_densityFactor;
    public int merge_mass;
    public int merge_dist1;
    public double merge_distfac;
    public int merge_dist2;

    LinkedList merge(LinkedList boxes) {
        boolean change = true;
        while (change == true) {
            change = false;
            int i = 0;
            while (i < boxes.size()) {
                int j = 0;
                while (i < boxes.size() && j < boxes.size()) {
                    if (i != j) {
                        TextRegion thisBox = (TextRegion) boxes.get(i);
                        TextRegion thatBox = (TextRegion) boxes.get(j);
                        change = merge(thisBox, thatBox);
                        if (change) {
                            boxes.set(i, thisBox);
                            boxes.remove(j);
                            j--;
                        }
                    }
                    j++;
                }
                i++;
            }
        }
        return (boxes);
    }

    boolean merge(TextRegion thisBox, TextRegion thatBox) {
        int mergex1 = Math.min(thisBox.x1, thatBox.x1);
        int mergex2 = Math.max(thisBox.x2, thatBox.x2);
        int mergey1 = Math.min(thisBox.y1, thatBox.y1);
        int mergey2 = Math.max(thisBox.y2, thatBox.y2);
        double mergemass = thisBox.mass + thatBox.mass;
        double mergedensity = mergemass
                / ((mergex2 - mergex1) * (mergey2 - mergey1));
        double mergeaspect
                = ((double) mergey2 - mergey1) / ((double) mergex2 - mergex1);

        double reasonsToMerge = 0;
        if (mergedensity > merge_densityFactor * thisBox.density())
            reasonsToMerge++;
        if (mergedensity > merge_densityFactor * thatBox.density())
            reasonsToMerge++;
        if (mergeaspect < thisBox.aspect())
            reasonsToMerge++;
        if (mergeaspect < thatBox.aspect())
            reasonsToMerge++;
        if (thisBox.mass > merge_mass && thatBox.mass > merge_mass)
            reasonsToMerge++;
        int maxboxwidth = Math.max(thisBox.width(), thatBox.width());
        if (Math.abs(thisBox.y1 - thatBox.y1) < merge_dist1
                && Math.abs(thisBox.y2 - thatBox.y1) < merge_dist1
                && (Math.abs(thisBox.x1 - thatBox.x2) < merge_distfac * maxboxwidth
                || Math.abs(thisBox.x2 - thatBox.x1)
                < merge_distfac * maxboxwidth))
            reasonsToMerge++;
        if ((Math.abs(thisBox.y1 - thatBox.y1) < merge_dist2
                || Math.abs(thisBox.y2 - thatBox.y2) < merge_dist2)
                && (Math.abs(thisBox.x1 - thatBox.x2) < merge_distfac * maxboxwidth
                || Math.abs(thisBox.x2 - thatBox.x1)
                < merge_distfac * maxboxwidth))
            reasonsToMerge++;
        if (reasonsToMerge > 3) { // 7 reasons max
            thisBox.x1 = mergex1;
            thisBox.x2 = mergex2;
            thisBox.y1 = mergey1;
            thisBox.y2 = mergey2;
            thisBox.mass = mergemass;
            return true;
        }
        return false;
    }

    int[][] getContrast() {
        // Find pixels that stand out from the background
        int[][] contrast = new int[image.getWidth()][image.getHeight()];
        int[][] temp = new int[image.getWidth()][image.getHeight()];
        for (int i = 2; i < image.getWidth() - 2; i++)
            for (int j = 2; j < image.getHeight() - 2; j++) {
                int thisPixel = image.getRGB(i, j);
                int left = image.getRGB(i - 1, j);
                int left2 = image.getRGB(i - 2, j);
                int right = image.getRGB(i + 1, j);
                int right2 = image.getRGB(i + 2, j);
                int up = image.getRGB(i, j - 1);
                int down = image.getRGB(i, j + 1);
                int t1 = 60; // thresholds
                int t2 = 80;
                if (Math.abs(blue(thisPixel) - blue(right)) > t1
                        || Math.abs(blue(thisPixel) - blue(left)) > t1
                        || Math.abs(blue(thisPixel) - blue(down)) > t1
                        || Math.abs(blue(thisPixel) - blue(up)) > t1
                        || Math.abs(blue(thisPixel) - blue(right2)) > t2
                        || Math.abs(blue(thisPixel) - blue(left2)) > t2
                        || Math.abs(green(thisPixel) - green(right)) > t1
                        || Math.abs(green(thisPixel) - green(left)) > t1
                        || Math.abs(green(thisPixel) - green(down)) > t1
                        || Math.abs(green(thisPixel) - green(up)) > t1
                        || Math.abs(green(thisPixel) - green(right2)) > t2
                        || Math.abs(green(thisPixel) - green(left2)) > t2
                        || Math.abs(red(thisPixel) - red(right)) > t1
                        || Math.abs(red(thisPixel) - red(left)) > t1
                        || Math.abs(red(thisPixel) - red(down)) > t1
                        || Math.abs(red(thisPixel) - red(up)) > t1
                        || Math.abs(red(thisPixel) - red(right2)) > t2
                        || Math.abs(red(thisPixel) - red(left2)) > t2)
                    temp[i][j] = 1;
            }
        // Look for areas of contrast that extend vertically and horizontally
        // but not too far, to eliminate long straight lines (e.g. borders)
        for (int j = 2; j < image.getHeight() - 2; j++)
            for (int i = 2; i < image.getWidth() - 2; i++)
                if (temp[i][j] == 1) {
                    int width = 0;
                    int height = 0;
                    for (int k = 0;
                         i + k < image.getWidth() - 2
                                 && i - k > 2
                                 && (temp[i + k][j] == 1 || temp[i - k][j] == 1)
                                 && width++ < 100;
                         k++)
                        ;
                    for (int k = 0;
                         j + k < image.getHeight() - 2
                                 && j - k > 2
                                 && (temp[i][j + k] == 1 || temp[i][j - k] == 1)
                                 && height++ < 100;
                         k++)
                        ;
                    int totalOnLine = 0;
                    for (int k = Math.max(2, i - 40);
                         k < Math.min(image.getWidth() - 2, i + 40);
                         k++)
                        totalOnLine += temp[k][j];
                    if (totalOnLine > 7 && width < 100 && height < 100)
                        contrast[i][j] = 1;
                }
        return contrast;
    }

    /**
     * Looks for areas of text in an image.
     *
     * @return a LinkedList of boxes that are likely to contain text.
     */
    public LinkedList getTextBoxes() {
        LinkedList boxes = new LinkedList();

        int[][] contrast = getContrast();

        try {
            FileOutputStream out = new FileOutputStream("contrast.jpg");
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            BufferedImage contrastjpg
                    = new BufferedImage(image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < image.getWidth(); i++)
                for (int j = 0; j < image.getHeight(); j++)
                    contrastjpg.setRGB(i, j, 0xffffff * contrast[i][j]);
            encoder.encode(contrastjpg);
            out.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        int contrastOnLine[] = new int[image.getHeight()];
        for (int j = 1; j < image.getHeight() - 1; j++) {
            int count = 0;
            contrastOnLine[j] = 0;
            for (int a = 0; a < image.getWidth(); a++) {
                count += contrast[a][j];
                contrastOnLine[j] += contrast[a][j];
            }
        }
        for (int j = 1; j < image.getHeight() - 1; j++)
            contrastOnLine[j] = (contrastOnLine[j - 1]
                    + contrastOnLine[j]
                    + contrastOnLine[j + 1]) / 3;
        for (int j = 1; j < image.getHeight() - 1; j++)
            contrastOnLine[j] = (contrastOnLine[j - 1]
                    + contrastOnLine[j]
                    + contrastOnLine[j + 1]) / 3;
        int averageOnLine = 0;
        for (int j = 1; j < image.getHeight() - 1; j++)
            averageOnLine += contrastOnLine[j];
        averageOnLine /= (image.getHeight() - 2);
        boolean intext = false;
        int boxstart = 0;
        int boxaverage = 0;
        int boxlines = 0;
        for (int j = 1; j < image.getHeight() - 1; j++) {
            if (contrastOnLine[j] > averageOnLine && !intext) {
                intext = true;
                boxstart = j;
                boxaverage = contrastOnLine[j];
                boxlines = 1;
            } else if (contrastOnLine[j] > averageOnLine) {
                boxaverage += contrastOnLine[j];
                boxlines++;
            } else if (contrastOnLine[j] <= averageOnLine && intext) {
                // found vertical limits, now find horizontal.
                intext = false;
                int boxend = j;
                if (boxend - boxstart > 10) {
                    // text must be higher than 10 pixels
                    boxaverage /= boxlines;
                    int contrastOnColumn[]
                            = new int[image.getWidth()];
                    for (int i = 1; i < image.getWidth() - 1; i++)
                        for (int b = boxstart; b < boxend; b++)
                            contrastOnColumn[i] += contrast[i][b];
                    for (int i = 1; i < image.getWidth() - 1; i++)
                        contrastOnColumn[i]
                                = (contrastOnColumn[i - 1]
                                + contrastOnColumn[i]
                                + contrastOnColumn[i + 1]) / 3;
                    for (int i = 1; i < image.getWidth() - 1; i++)
                        contrastOnColumn[i]
                                = (contrastOnColumn[i - 1]
                                + contrastOnColumn[i]
                                + contrastOnColumn[i + 1]) / 3;
                    int averageOnColumn = 0;
                    for (int i = 1; i < image.getWidth() - 1; i++)
                        averageOnColumn += contrastOnColumn[i];
                    averageOnColumn /= (image.getWidth() - 2);
                    boolean intextx = false;
                    int boxstartx = 0;
                    for (int i = 1; i < image.getWidth() - 1; i++) {
                        if (contrastOnColumn[i] > averageOnColumn / 2
                                && !intextx) {
                            intextx = true;
                            boxstartx = i;
                        } else if (contrastOnColumn[i] <= averageOnColumn / 2
                                && intextx) {
                            intextx = false;
                            int boxendx = i;
                            // found horizontal limits,
                            // now (if necessary) shrink
                            // vertical limits
                            int newcount = 0;
                            int tempboxstart = boxstart;
                            int tempboxend = boxend;
                            while (tempboxstart < boxend
                                    && newcount == 0) {
                                for (int a = boxstartx; a < boxendx; a++)
                                    newcount += contrast[a][tempboxstart];
                                if (newcount < 2)
                                    tempboxstart++;
                            }
                            newcount = 0;
                            while (tempboxstart < boxend && newcount == 0) {
                                for (int a = boxstartx; a < boxendx; a++)
                                    newcount += contrast[a][tempboxend];
                                if (newcount < 2)
                                    tempboxend--;
                            }
                            TextRegion thisBox
                                    = new TextRegion(boxstartx,
                                    tempboxstart,
                                    boxendx,
                                    tempboxend,
                                    image.getWidth(),
                                    image.getHeight(),
                                    boxaverage);
                            boxes.add(thisBox);
                        }
                    }
                }
            }
        }

        System.out.println(boxes.size() + " bounding boxes");
        shrink(boxes, contrast);
        boxes = merge(boxes);
        shrink(boxes, contrast);
        System.out.println(boxes.size() + " bounding boxes after merge");
        boxes = discardNonText(boxes, contrast);
        System.out.println(boxes.size() + " bounding boxes after delete");
        return (shrink(boxes, contrast));
    }

    /**
     * Isolate text
     *
     * @return a <code>BufferedImage</code> value
     */
    public BufferedImage isolateText(LinkedList boxes) {
        BufferedImage outputimage
                = new BufferedImage(image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        // make everything monochrome
        for (int a = 0; a < image.getWidth(); a++)
            for (int b = 0; b < image.getHeight(); b++) {
                int colour = image.getRGB(a, b);
                int average = (red(colour) + green(colour) + blue(colour)) / 3;
                outputimage.setRGB(a, b, rgb(average, average, average));
            }
        // fill text boxes with colour
        for (int i = 0; i < boxes.size(); i++) {
            TextRegion thisBox = (TextRegion) boxes.get(i);
            int x1 = Math.max(1, thisBox.x1);
            int x2 = Math.min(image.getWidth() - 2, thisBox.x2);
            int y1 = Math.max(1, thisBox.y1);
            int y2 = Math.min(image.getHeight() - 2, thisBox.y2);
            for (int a = x1; a < x2; a++)
                for (int b = y1; b < y2; b++)
                    outputimage.setRGB(a, b, image.getRGB(a, b));
        }
        // draw red border around each text box
        int RED = 0xff0000;
        for (int i = 0; i < boxes.size(); i++) {
            TextRegion thisBox = (TextRegion) boxes.get(i);
            int x1 = Math.max(1, thisBox.x1);
            int x2 = Math.min(image.getWidth() - 2, thisBox.x2);
            int y1 = Math.max(1, thisBox.y1);
            int y2 = Math.min(image.getHeight() - 2, thisBox.y2);
            for (int a = x1; a < x2; a++) {
                outputimage.setRGB(a, thisBox.y1, RED);
                outputimage.setRGB(a, thisBox.y2, RED);
            }
            for (int a = y1; a < y2; a++) {
                outputimage.setRGB(thisBox.x1, a, RED);
                outputimage.setRGB(thisBox.x2, a, RED);
            }
        }
        return (outputimage);
    }

    public static void main(String[] args) {
        try {
            FileInputStream in = new FileInputStream(args[0]);
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
            BufferedImage image = decoder.decodeAsBufferedImage();
            in.close();

            GetImageText myget = new GetImageText(image);
            LinkedList boxes = myget.getTextBoxes();

            FileOutputStream out = new FileOutputStream(args[1]);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(myget.isolateText(boxes));
            out.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}


/**
 * Two dimensional box
 */
class TextRegion {
    int x1;
    int y1;
    int x2;
    int y2;
    double mass;

    /**
     * Creates a new <code>TextRegion</code> instance.
     *
     * @param xs   an <code>int</code> value
     * @param ys   an <code>int</code> value
     * @param xe   an <code>int</code> value
     * @param ye   an <code>int</code> value
     * @param maxx an <code>int</code> value
     * @param maxy an <code>int</code> value
     */
    TextRegion(int xs, int ys, int xe, int ye, int maxx, int maxy, double m) {
        if (xs < 0)
            x1 = 0;
        else if (xs > maxx)
            x1 = maxx;
        else x1 = xs;
        if (xe < 0)
            x2 = 0;
        else if (xe > maxx)
            x2 = maxx;
        else x2 = xe;
        if (ys < 0)
            y1 = 0;
        else if (ys > maxy)
            y1 = maxy;
        else y1 = ys;
        if (ye < 0)
            y2 = 0;
        else if (ye > maxy)
            y2 = maxy;
        else y2 = ye;
        mass = m;
    }

    int area() {
        return width() * height();
    }

    int height() {
        return y2 - y1;
    }

    int width() {
        return x2 - x1;
    }

    double density() {
        return mass / area();
    }

    double aspect() {
        return (double) height() / (double) width();
    }
}
