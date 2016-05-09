package it.quartara.boser.console.pdfcmgr;

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
public class PDFCManagerServletTest {

	@Mock HttpServletRequest request;
	@Mock HttpServletResponse response;
	@Mock RequestDispatcher rd;
	@Mock AmazonEC2 ec2;
	@Mock Instance instance;
	@Mock InstanceState instanceState;
	/*
	@Test
	public void testDoPostWhenInstanceIsRunning() throws Exception {
		when(instance.getState()).thenReturn(instanceState);
		when(instanceState.getName()).thenReturn("running");
		mockStatic(AWSHelper.class);
		when(AWSHelper.createAmazonEC2Client(anyString())).thenReturn(ec2);
		when(AWSHelper.getInstance(eq(ec2), anyString())).thenReturn(instance);
		
		PDFCManagerServlet servlet = spy(new PDFCManagerServlet());
		servlet.doPost(request, response);
		
		verify(response).sendRedirect("http://boser.quartara.it/conversionHome");
	}
	
	@Test
	public void testDoPostWhenInstanceIsStopped() throws Exception {
		when(request.getRequestDispatcher("/WEB-INF/jsps/pdfcmgr.jsp")).thenReturn(rd);
		when(instance.getState()).thenReturn(instanceState);
		when(instanceState.getName()).thenReturn("stopped").thenReturn("pending").thenReturn("running");
		mockStatic(Thread.class);
		Thread.sleep(anyLong());
		mockStatic(AWSHelper.class);
		when(AWSHelper.createAmazonEC2Client(anyString())).thenReturn(ec2);
		when(AWSHelper.getInstance(eq(ec2), anyString())).thenReturn(instance);
		mockStatic(ConverterManagerHelper.class);
		doNothing().when(ConverterManagerHelper.class, "scheduleStandbyJob", any(DataSource.class), 
																		any(ServletContext.class), 
																		any(Date.class),
																		anyBoolean());
		
		PDFCManagerServlet servlet = spy(new PDFCManagerServlet());
		doReturn(mock(ServletContext.class)).when(servlet).getServletContext();
		
		servlet.doPost(request, response);
	}
	*/
}



