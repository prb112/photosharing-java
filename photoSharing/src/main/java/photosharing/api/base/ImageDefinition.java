/**
 * © Copyright IBM Corp. 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package photosharing.api.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages the API for fake Image Definitions
 * 
 * @author Paul Bastide <pbastide@us.ibm.com>
 *
 */
public class ImageDefinition implements APIDefinition{

	// Logger
	private final static String className = ImageDefinition.class.getName();
	private Logger logger = Logger.getLogger(className);
	
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
			logger.severe("IOException " + e.toString());
		}
		
		
	}
	
}
