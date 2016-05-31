package com.learning.controller;

import com.learning.Application;
import com.learning.colorthief.ColorThief;
import com.learning.colorthief.MMCQ;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class MainController {
    @RequestMapping("/status")
    @ResponseBody
    public String status(HttpServletResponse response) {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        return "alive";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String provideUploadInfo(Model model) {
        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/")
    public String handleFileUpload(@RequestParam(name="ignoreWhite", required = false) boolean ignoreWhite,
                                   @RequestParam("quality") int quality,
                                   @RequestParam("colorCount") int colorCount,
                                   @RequestParam("file") MultipartFile file,
                                   Model model) {
        if (!file.isEmpty()) {
            try {
                InputStream in = new ByteArrayInputStream(file.getBytes());
                BufferedImage img = ImageIO.read(in);
                String dominantColorHTML = getDominantColorAsHTML(img, ignoreWhite, colorCount, quality);
                model.addAttribute("dominantColor", dominantColorHTML);
                in.close();
            }
            catch (Exception e) {
                System.out.println("Error");
            }
        }
        else {
            System.out.println("Error");
        }
        return "dominant";
    }

    private String getDominantColorAsHTML(BufferedImage img, boolean ignoreWhite, int colorCount, int quality) {
        String html = "<style>div.color{width:4em;height:4em;float:left;margin:0 1em 1em 0;}"
                + "th{text-align:left}"
                + "td{vertical-align:top;padding-right:1em}</style>";
        html += "<h2>Dominant Color</h2>";
        // The dominant color is taken from a 5-map
        MMCQ.CMap result = ColorThief.getColorMap(img, colorCount, quality, ignoreWhite);
        MMCQ.VBox dominantColor = result.vboxes.get(0);
        html += getVBoxAsString(dominantColor);
        return html;
    }

    /**
     * get HTML code for a VBox.
     *
     * @param vbox
     *            the vbox
     */
    private static String getVBoxAsString(MMCQ.VBox vbox)
    {
        int[] rgb = vbox.avg(false);

        // Create color String representations
        String rgbString = createRGBString(rgb);
        String rgbHexString = createRGBHexString(rgb);

        StringBuilder line = new StringBuilder();

        line.append("<div>");

        // Print color box
        line
                .append("<div class=\"color\" style=\"background:")
                .append(rgbString)
                .append(";\"></div>");

        // Print table with color code and VBox information
        line
                .append("<table><tr><th>Color code:</th>"
                        + "<th>Volume &times pixel count:</th>"
                        + "<th>VBox:</th></tr>");

        // Color code
        line
                .append("<tr><td>")
                .append(rgbString)
                .append(" / ")
                .append(rgbHexString)
                .append("</td>");

        // Volume / pixel count
        int volume = vbox.volume(false);
        int count = vbox.count(false);
        line
                .append("<td>")
                .append(String.format("%,d", volume))
                .append(" &times; ")
                .append(String.format("%,d", count))
                .append(" = ")
                .append(String.format("%,d", volume * count))
                .append("</td>");

        // VBox
        line
                .append("<td>")
                .append(vbox.toString())
                .append("</td></tr></table>");

        // Stop floating
        line.append("<div style=\"clear:both\"></div>");

        line.append("</div>");

        return line.toString();
    }

    /**
     * Creates a string representation of an RGB array.
     *
     * @param rgb
     *            the RGB array
     *
     * @return the string representation
     */
    private static String createRGBString(int[] rgb)
    {
        return "rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")";
    }

    /**
     * Creates an HTML hex color code for the given RGB array (e.g.
     * <code>#ff0000</code> for red).
     *
     * @param rgb
     *            the RGB array
     *
     * @return the HTML hex color code
     */
    private static String createRGBHexString(int[] rgb)
    {
        String rgbHex = Integer
                .toHexString(rgb[0] << 16 | rgb[1] << 8 | rgb[2]);

        // Left-pad with 0s
        int length = rgbHex.length();
        if (length < 6)
        {
            rgbHex = "00000".substring(0, 6 - length) + rgbHex;
        }

        return "#" + rgbHex;
    }
}
