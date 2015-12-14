package photosharing.api.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages the API for fake Image Definitions
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class ImageDefinition implements APIDefinition{

	// Content Type
	private static final String CONTENTTYPE = "image/svg+xml";
	
	/*
	 * Template for the SVG that is returned for mock images
	 */
	private static final String TEMPLATE = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.2\" width=\"CANVASWIDTH\" height=\"CANVASHEIGHT\" viewBox=\"0 0 200 200\">" +
		"<rect x=\"1\" y=\"1\" width=\"RECTWIDTH\" height=\"RECTHEIGHT\" fill=\"FILLCOLOR\" stroke=\"STROKECOLOR\" stroke-width=\"5\" />" + 
		"</svg>";
	
	/*
	 * Template is updated with the values in the mockimage 
	 */
	private static final String CANVASWIDTH = "CANVASWIDTH";
	private static final String CANVASHEIGHT = "CANVASHEIGHT";
	private static final String RECTWIDTH = "RECTWIDTH";
	private static final String RECTHEIGHT = "RECTHEIGHT";
	
	private static final int STROKEWIDTH = 5;
	
	private static final String FILLCOLOR = "FILLCOLOR";
	private static final String STROKECOLOR = "STROKECOLOR";
	
	/**
	 * List of Colors
	 */
	private final String[] COLORS = {
			"BLACK", 
			"MAGENTA",
			"BLUE",
			"DARK_GRAY",
			"CYAN",
			"RED",
			"GREEN",
			"LIGHT_GRAY",
			"ORANGE",
			"PINK",
			"WHITE",
			"YELLOW"
	};
	
	/**
	 * creates an image with random colors 
	 */
	@Override
	public void run(HttpServletRequest request, HttpServletResponse response) {
		String temp = TEMPLATE.replace(CANVASWIDTH, "200");
		temp = temp.replace(CANVASHEIGHT, "200");
		temp = temp.replace(RECTWIDTH, "" + (200 - STROKEWIDTH));
		temp = temp.replace(RECTHEIGHT, "" + (200 - STROKEWIDTH));
		
		Random ran = new Random();
		int x = ran.nextInt(COLORS.length);
		int y = ran.nextInt(COLORS.length);
		
		while(x == y)
			y = ran.nextInt(COLORS.length);
		
		
		temp = temp.replace(STROKECOLOR, ""+COLORS[x]);
		temp = temp.replace(FILLCOLOR, ""+COLORS[y]);
		
		response.setContentType(CONTENTTYPE);
		
		try {
			PrintWriter out = response.getWriter();
			out.write(temp);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
