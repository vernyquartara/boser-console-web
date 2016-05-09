package it.quartara.boser.console;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;

import it.quartara.boser.console.helper.AWSHelper;
import it.quartara.boser.console.helper.ConverterManagerHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AWSHelper.class, ConverterManagerHelper.class})
@MockPolicy(Slf4jMockPolicy.class)
public class HomeServletTest {

	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock RequestDispatcher rd;
	@Mock AmazonEC2 ec2;
	@Mock Instance instance;
	@Mock InstanceState instanceState;
	/*
	@Test
	public void testDoGetWhenInstanceIsRunning() throws Exception {
		when(request.getRequestDispatcher("/WEB-INF/jsps/pdfcmgr.jsp")).thenReturn(rd);
		when(instance.getState()).thenReturn(instanceState);
		when(instanceState.getName()).thenReturn("running");
		
		HomeServlet servlet = spy(new HomeServlet());
		mockStatic(AWSHelper.class);
		when(AWSHelper.createAmazonEC2Client(anyString())).thenReturn(ec2);
		when(AWSHelper.getInstance(eq(ec2), anyString())).thenReturn(instance);
		
		servlet.doGet(request, response);
		
		verify(request).setAttribute("status", "operativo");
		verify(request).setAttribute("running", true);
		verify(rd).forward(request, response);
	}
	
	@Test
	public void testDoGetWhenInstanceIsStopped() throws Exception {
		when(request.getRequestDispatcher("/WEB-INF/jsps/pdfcmgr.jsp")).thenReturn(rd);
		when(instance.getState()).thenReturn(instanceState);
		when(instanceState.getName()).thenReturn("stopped");
		mockStatic(AWSHelper.class);
		when(AWSHelper.createAmazonEC2Client(anyString())).thenReturn(ec2);
		when(AWSHelper.getInstance(eq(ec2), anyString())).thenReturn(instance);
		
		HomeServlet servlet = spy(new HomeServlet());
		servlet.doGet(request, response);
		
		verify(request).setAttribute("status", "in stand-by");
		verify(request).setAttribute("running", false);
		verify(rd).forward(request, response);
	}
	
	@Test
	public void testDoGetFailure() throws Exception {
		when(request.getRequestDispatcher("/WEB-INF/jsps/pdfcmgrerror.jsp")).thenReturn(rd);
		mockStatic(AWSHelper.class);
		when(AWSHelper.createAmazonEC2Client(anyString())).thenReturn(ec2);
		when(AWSHelper.getInstance(eq(ec2), anyString())).thenThrow(new AmazonServiceException(""));
		
		HomeServlet servlet = spy(new HomeServlet());
		servlet.doGet(request, response);
		
		verify(rd).forward(request, response);
	}
	
	*/
}



