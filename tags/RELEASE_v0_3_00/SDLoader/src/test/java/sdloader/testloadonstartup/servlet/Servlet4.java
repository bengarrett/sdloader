package sdloader.testloadonstartup.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import junit.framework.Assert;

@SuppressWarnings("serial")
public class Servlet4 extends HttpServlet {

	@Override
	public void init() throws ServletException {
		Assert.assertEquals(5, StartUpNum.startupNum++);
	}
}
